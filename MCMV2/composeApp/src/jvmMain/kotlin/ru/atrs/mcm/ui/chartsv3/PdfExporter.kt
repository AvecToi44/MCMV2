package ru.atrs.mcm.ui.chartsv3

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import java.awt.BasicStroke
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileOutputStream
import javax.swing.JFileChooser
import ru.atrsx.chartviewer.koala.xygraph.Point

data class PdfExportConfig(
    val datasets: List<ChartData>,
    val visibilityStates: List<List<Boolean>>,
    val seriesColors: List<Color>,
    val overlapHalves: Boolean = false,
    val chartWidth: Int = 800,
    val chartHeight: Int = 600,
    val renderScale: Int = 3,
    val chartFilePaths: List<String> = emptyList()
)

data class PdfExportResult(
    val success: Boolean,
    val filePath: String? = null,
    val errorMessage: String? = null
)

object PdfExporter {
    private const val A4_WIDTH = 842f   // Landscape width
    private const val A4_HEIGHT = 595f  // Landscape height
    private const val MARGIN = 25f
    private const val HEADER_HEIGHT = 20f
    private const val CHANNEL_CHIP_WIDTH = 39f
    private const val CHANNEL_CHIP_HEIGHT = 10f
    private const val CHIP_SPACING = 2f
    private const val ROW_SPACING = 14f

    private val AWtColor = java.awt.Color::class.java

    private fun pdfChannelLabel(dataset: ChartData, channelIdx: Int): String {
        val raw = dataset.channelNames.getOrElse(channelIdx) { "Ch${channelIdx + 1}" }
        return if (raw.length > 6) raw.take(5) + "~" else raw
    }

    private fun isDarkColor(color: java.awt.Color): Boolean {
        val luminance = 0.299 * color.red + 0.587 * color.green + 0.114 * color.blue
        return luminance < 130.0
    }

    private fun drawChannelChipsInline(
        contentStream: PDPageContentStream,
        regularFont: PDFont,
        yCursor: Float,
        dataset: ChartData,
        vis: List<Boolean>,
        config: PdfExportConfig
    ) {
        val chipCount = vis.size
        if (chipCount == 0) return

        val totalWidth = chipCount * CHANNEL_CHIP_WIDTH + (chipCount - 1).coerceAtLeast(0) * CHIP_SPACING
        var xCursor = (A4_WIDTH - MARGIN - totalWidth).coerceAtLeast(MARGIN + 250f)

        for ((channelIdx, isVisible) in vis.withIndex()) {
            val chipWidth = CHANNEL_CHIP_WIDTH
            if (xCursor + chipWidth > A4_WIDTH - MARGIN) break

            val color = config.seriesColors.getOrElse(channelIdx) { androidx.compose.ui.graphics.Color.Black }
            val awtColor = java.awt.Color(
                (color.red * 255).toInt(),
                (color.green * 255).toInt(),
                (color.blue * 255).toInt()
            )
            val fillColor = if (isVisible) awtColor else java.awt.Color(180, 180, 180)
            val borderColor = if (isVisible) java.awt.Color.DARK_GRAY else java.awt.Color(150, 150, 150)

            contentStream.setNonStrokingColor(fillColor)
            contentStream.addRect(xCursor, yCursor - CHANNEL_CHIP_HEIGHT, chipWidth, CHANNEL_CHIP_HEIGHT)
            contentStream.fill()

            contentStream.setStrokingColor(borderColor)
            contentStream.setLineWidth(0.5f)
            contentStream.addRect(xCursor, yCursor - CHANNEL_CHIP_HEIGHT, chipWidth, CHANNEL_CHIP_HEIGHT)
            contentStream.stroke()

            val textColor = when {
                !isVisible -> java.awt.Color.GRAY
                isDarkColor(fillColor) -> java.awt.Color.WHITE
                else -> java.awt.Color.BLACK
            }

            contentStream.setNonStrokingColor(textColor)
            contentStream.beginText()
            contentStream.setFont(regularFont, 5.5f)
            contentStream.newLineAtOffset(xCursor + 2f, yCursor - CHANNEL_CHIP_HEIGHT + 4f)
            contentStream.showText(pdfChannelLabel(dataset, channelIdx))
            contentStream.endText()

            xCursor += chipWidth + CHIP_SPACING
        }
    }

    private fun detectYAxisPrecision(allPoints: List<Point<Float, Float>>): Int {
        var maxDecimals = 0
        for (point in allPoints) {
            val str = point.y.toString()
            if (str.contains(".")) {
                val decimals = str.length - str.indexOf(".") - 1
                maxDecimals = maxOf(maxDecimals, decimals)
            }
        }
        return maxDecimals.coerceIn(1, 6)
    }

    private fun extractTimestampFromPaths(paths: List<String>): String {
        val timestampPattern = Regex("""(\d{2}_\d{2}_\d{4} \d{2}_\d{2}_\d{2})""")

        for (path in paths) {
            val match = timestampPattern.find(path)
            if (match != null) {
                val rawTimestamp = match.groupValues[1]
                return formatTimestampFromFilename(rawTimestamp)
            }
        }
        return "--"
    }

    private fun formatTimestampFromFilename(raw: String): String {
        try {
            val parts = raw.split(" ")
            val timePart = parts[1].replace("_", ":")
            val dateInputFormat = java.text.SimpleDateFormat("dd_MM_yyyy")
            val dateOutputFormat = java.text.SimpleDateFormat("dd.MMMM.yyyy")
            val date = dateInputFormat.parse(parts[0])
            return "$timePart ${dateOutputFormat.format(date!!)}"
        } catch (e: Exception) {
            return "--"
        }
    }

    fun getPdfPathFromChartPaths(paths: List<String>): String? {
        if (paths.isEmpty()) return null

        val timestampPattern = Regex("""(\d{2}_\d{2}_\d{4} \d{2}_\d{2}_\d{2})""")

        val filesWithTimestamps = paths.mapNotNull { path ->
            val match = timestampPattern.find(path)
            if (match != null) {
                val timestamp = parseTimestampFromFilename(match.groupValues[1])
                File(path) to timestamp
            } else {
                null
            }
        }

        if (filesWithTimestamps.isNotEmpty()) {
            val mostRecentByName = filesWithTimestamps.maxByOrNull { it.second }
            if (mostRecentByName != null) {
                return getPdfPath(mostRecentByName.first)
            }
        }

        val allFiles = paths.map { File(it) }
        val mostRecentByModified = allFiles.maxByOrNull { it.lastModified() }

        return mostRecentByModified?.let { getPdfPath(it) }
    }

    private fun parseTimestampFromFilename(raw: String): Long {
        return try {
            val format = java.text.SimpleDateFormat("dd_MM_yyyy HH_mm_ss")
            format.parse(raw)?.time ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    private fun getPdfPath(file: File): String {
        val pdfFileName = file.nameWithoutExtension + ".pdf"
        return File(file.parentFile, pdfFileName).absolutePath
    }

    suspend fun exportToPdf(config: PdfExportConfig): PdfExportResult = withContext(Dispatchers.IO) {
        exportToPdfWithPath(config, null)
    }

    suspend fun exportToPdf(config: PdfExportConfig, outputPath: String?): PdfExportResult = withContext(Dispatchers.IO) {
        exportToPdfWithPath(config, outputPath)
    }

    private suspend fun exportToPdfWithPath(config: PdfExportConfig, outputPath: String?): PdfExportResult = withContext(Dispatchers.IO) {
        try {
            val outputFile = if (outputPath != null) {
                File(outputPath)
            } else {
                showSaveDialog() ?: return@withContext PdfExportResult(
                    success = false,
                    errorMessage = "Export cancelled"
                )
            }

            val document = PDDocument()
            val page = PDPage(PDRectangle(842f, 595f))
            document.addPage(page)

            val chartAreaWidth = (A4_WIDTH - MARGIN * 2).toInt()
            val chartAreaHeight = (A4_HEIGHT - MARGIN * 2 - 80).toInt()
            val renderScale = config.renderScale.coerceIn(1, 6)

            val chartImage = renderChartToImage(config, chartAreaWidth, chartAreaHeight, renderScale)

            PDPageContentStream(document, page).use { contentStream ->
                drawPage(contentStream, document, config, chartImage)
            }

            outputFile.parentFile?.mkdirs()
            FileOutputStream(outputFile).use { fos ->
                document.save(fos)
            }
            document.close()

            PdfExportResult(success = true, filePath = outputFile.absolutePath)
        } catch (e: Exception) {
            PdfExportResult(success = false, errorMessage = e.message ?: "Unknown error")
        }
    }

    private fun showSaveDialog(): File? {
        val chooser = JFileChooser().apply {
            dialogTitle = "Save PDF"
            fileSelectionMode = JFileChooser.FILES_ONLY
            selectedFile = File("chart_export.pdf")
        }

        return if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            var file = chooser.selectedFile
            if (!file.name.lowercase().endsWith(".pdf")) {
                file = File(file.parentFile, "${file.name}.pdf")
            }
            file
        } else null
    }

    private fun renderChartToImage(config: PdfExportConfig, width: Int, height: Int, renderScale: Int): BufferedImage {
        val scaledWidth = width * renderScale
        val scaledHeight = height * renderScale
        val bufferedImage = BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB)
        val g2d = bufferedImage.createGraphics()
        val preparedChart = prepareChartRender(
            datasets = config.datasets,
            visibilityStates = config.visibilityStates,
            overlapHalves = config.overlapHalves
        )
        val allPoints = preparedChart.series.flatMap { it.points }

        g2d.scale(renderScale.toDouble(), renderScale.toDouble())
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

        g2d.color = java.awt.Color.WHITE
        g2d.fillRect(0, 0, width, height)

        val padding = 60
        val chartWidth = width - padding * 2
        val chartHeight = height - padding * 2

        if (allPoints.isEmpty()) {
            g2d.color = java.awt.Color.GRAY
            g2d.font = java.awt.Font("Arial", java.awt.Font.PLAIN, 14)
            g2d.drawString("No data to display", width / 2 - 60, height / 2)
            g2d.dispose()
            return bufferedImage
        }

        val xMin = preparedChart.xRange.start
        val xMax = preparedChart.xRange.endInclusive
        val yMin = preparedChart.yRange.start
        val yMax = preparedChart.yRange.endInclusive

        val xRange = (xMax - xMin).takeIf { it != 0f } ?: 1f
        val yRange = (yMax - yMin).takeIf { it != 0f } ?: 1f

        val precision = detectYAxisPrecision(allPoints)
        val formatString = "%.${precision}f"

        g2d.color = java.awt.Color.LIGHT_GRAY
        g2d.stroke = BasicStroke(1f)

        val gridLines = 5
        for (i in 0..gridLines) {
            val y = padding + (chartHeight * i / gridLines)
            g2d.drawLine(padding, y, width - padding, y)

            val yLabel = yMax - (yRange * i / gridLines)
            g2d.color = java.awt.Color.GRAY
            g2d.font = java.awt.Font("Arial", java.awt.Font.PLAIN, 10)
            g2d.drawString(String.format(formatString, yLabel), 5, y + 4)
            g2d.color = java.awt.Color.LIGHT_GRAY
        }

        for (i in 0..gridLines) {
            val x = padding + (chartWidth * i / gridLines)
            g2d.drawLine(x, padding, x, height - padding)

            val xLabel = xMin + (xRange * i / gridLines)
            g2d.color = java.awt.Color.GRAY
            g2d.font = java.awt.Font("Arial", java.awt.Font.PLAIN, 10)
            g2d.drawString(String.format("%.1f", xLabel), x - 10, height - padding + 20)
        }

        g2d.color = java.awt.Color.BLACK
        g2d.stroke = BasicStroke(2f)
        g2d.drawRect(padding, padding, chartWidth, chartHeight)

        preparedChart.series.forEach { preparedSeries ->
                val baseColor = config.seriesColors.getOrElse(preparedSeries.seriesIndex % config.seriesColors.size) { Color.Black }
                val red = (baseColor.red * 255).toInt().coerceIn(0, 255)
                val green = (baseColor.green * 255).toInt().coerceIn(0, 255)
                val blue = (baseColor.blue * 255).toInt().coerceIn(0, 255)
                val alpha = if (preparedSeries.isSecondHalf) (255 * 0.65f).toInt() else 255
                val strokeColor = java.awt.Color(red, green, blue, alpha)

                g2d.color = strokeColor
                g2d.stroke = when {
                    preparedSeries.isSecondHalf -> BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, floatArrayOf(4f, 6f), 0f)
                    preparedSeries.datasetIndex == 1 -> BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, floatArrayOf(10f, 6f), 0f)
                    preparedSeries.datasetIndex == 2 -> BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, floatArrayOf(3f, 4f), 0f)
                    else -> BasicStroke(2f)
                }

                val path = java.awt.geom.Path2D.Float()
                var isFirst = true

                for (point in preparedSeries.points) {
                    val px = padding + ((point.x - xMin) / xRange * chartWidth).toFloat()
                    val py = padding + chartHeight - ((point.y - yMin) / yRange * chartHeight).toFloat()

                    if (isFirst) {
                        path.moveTo(px, py)
                        isFirst = false
                    } else {
                        path.lineTo(px, py)
                    }
                }

                g2d.draw(path)
        }

        g2d.font = java.awt.Font("Arial", java.awt.Font.BOLD, 12)
        g2d.color = java.awt.Color.BLACK
        g2d.drawString("Y", 15, padding - 10)
        g2d.drawString("X", width - padding + 10, height - padding + 35)

        g2d.dispose()
        return bufferedImage
    }

    private fun loadCyrillicFont(document: PDDocument): PDFont {
        val arialPaths = listOf(
            "C:/Windows/Fonts/arial.ttf",
            "C:/Windows/Fonts/arialbd.ttf",
            "/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf",
            "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"
        )
        
        for (fontPath in arialPaths) {
            try {
                val fontFile = File(fontPath)
                if (fontFile.exists()) {
                    return org.apache.pdfbox.pdmodel.font.PDType0Font.load(document, fontFile)
                }
            } catch (e: Exception) {
                // Continue to next font
            }
        }
        
        return PDType1Font.HELVETICA
    }

    private fun loadCyrillicBoldFont(document: PDDocument): PDFont {
        val arialBoldPaths = listOf(
            "C:/Windows/Fonts/arialbd.ttf",
            "C:/Windows/Fonts/arial.ttf",
            "/usr/share/fonts/truetype/liberation/LiberationSans-Bold.ttf",
            "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"
        )
        
        for (fontPath in arialBoldPaths) {
            try {
                val fontFile = File(fontPath)
                if (fontFile.exists()) {
                    return org.apache.pdfbox.pdmodel.font.PDType0Font.load(document, fontFile)
                }
            } catch (e: Exception) {
                // Continue to next font
            }
        }
        
        return PDType1Font.HELVETICA_BOLD
    }

    private fun drawPage(
        contentStream: PDPageContentStream,
        document: PDDocument,
        config: PdfExportConfig,
        chartImage: BufferedImage
    ) {
        val boldFont: PDFont = loadCyrillicBoldFont(document)
        val regularFont: PDFont = loadCyrillicFont(document)

        val timestamp = extractTimestampFromPaths(config.chartFilePaths)

        val chartAreaWidth = A4_WIDTH - MARGIN * 2
        val chartAreaHeight = A4_HEIGHT - MARGIN * 2 - ROW_SPACING * 4

        var yCursor = A4_HEIGHT - MARGIN

        // Title
        contentStream.beginText()
        contentStream.setFont(boldFont, 12f)
        contentStream.newLineAtOffset(MARGIN, yCursor)
        contentStream.showText("MCMV2 Measurement from $timestamp")
        contentStream.endText()

        yCursor -= ROW_SPACING
        contentStream.setFont(regularFont, 8f)

        val lineStyles = listOf(
            LineStyleInfo(1f, null),  // Solid for File 1
            LineStyleInfo(1f, floatArrayOf(8f, 4f)),  // Dashed for File 2
            LineStyleInfo(1f, floatArrayOf(2f, 3f))   // Dotted for File 3
        )

        for ((datasetIdx, dataset) in config.datasets.withIndex()) {
            if (datasetIdx >= 3) break

            val vis = config.visibilityStates.getOrNull(datasetIdx) ?: continue
            val lineStyle = lineStyles[datasetIdx]
            val datasetColor = config.seriesColors.getOrElse(0) { androidx.compose.ui.graphics.Color.Gray }
            val dashColor = java.awt.Color(
                (datasetColor.red * 255).toInt(),
                (datasetColor.green * 255).toInt(),
                (datasetColor.blue * 255).toInt()
            )

            // Draw file name (bold)
            contentStream.beginText()
            contentStream.setFont(boldFont, 8f)
            contentStream.newLineAtOffset(MARGIN, yCursor)
            val displayName = if (dataset.fileName.length > 35) dataset.fileName.take(32) + "..." else dataset.fileName
            contentStream.showText("File ${datasetIdx + 1}: $displayName")
            contentStream.endText()

            // Short line style indicator under filename
            val indicatorWidth = 50f
            val indicatorY = yCursor - 3f
            contentStream.setStrokingColor(dashColor)
            contentStream.setLineWidth(1.5f)
            if (lineStyle.dashArray != null) {
                contentStream.setLineDashPattern(lineStyle.dashArray, 0f)
            } else {
                contentStream.setLineDashPattern(floatArrayOf(), 0f)
            }
            contentStream.moveTo(MARGIN, indicatorY)
            contentStream.lineTo(MARGIN + indicatorWidth, indicatorY)
            contentStream.stroke()
            contentStream.setLineDashPattern(floatArrayOf(), 0f)

            drawChannelChipsInline(
                contentStream = contentStream,
                regularFont = regularFont,
                yCursor = yCursor,
                dataset = dataset,
                vis = vis,
                config = config
            )

            yCursor -= ROW_SPACING
        }

        val pdImage = PDImageXObject.createFromByteArray(
            document,
            bufferedImageToByteArray(chartImage),
            "chart"
        )

        val chartTop = yCursor - 10f
        contentStream.drawImage(pdImage, MARGIN, MARGIN, chartAreaWidth, chartTop - MARGIN)
    }

    private fun drawPageWithScreenshot(
        contentStream: PDPageContentStream,
        document: PDDocument,
        config: PdfExportConfig,
        screenshot: BufferedImage
    ) {
        val boldFont: PDFont = loadCyrillicBoldFont(document)
        val regularFont: PDFont = loadCyrillicFont(document)

        val timestamp = extractTimestampFromPaths(config.chartFilePaths)

        val chartAreaWidth = A4_WIDTH - MARGIN * 2

        var yCursor = A4_HEIGHT - MARGIN

        // Title
        contentStream.beginText()
        contentStream.setFont(boldFont, 12f)
        contentStream.newLineAtOffset(MARGIN, yCursor)
        contentStream.showText("MCMV2 Measurement from $timestamp")
        contentStream.endText()

        yCursor -= ROW_SPACING
        contentStream.setFont(regularFont, 8f)

        for ((datasetIdx, dataset) in config.datasets.withIndex()) {
            if (datasetIdx >= 3) break

            val vis = config.visibilityStates.getOrNull(datasetIdx) ?: continue
            val datasetColor = config.seriesColors.getOrElse(0) { androidx.compose.ui.graphics.Color.Gray }
            val dashColor = java.awt.Color(
                (datasetColor.red * 255).toInt(),
                (datasetColor.green * 255).toInt(),
                (datasetColor.blue * 255).toInt()
            )

            val lineStyles = listOf(
                LineStyleInfo(1f, null),
                LineStyleInfo(1f, floatArrayOf(8f, 4f)),
                LineStyleInfo(1f, floatArrayOf(2f, 3f))
            )
            val lineStyle = lineStyles.getOrElse(datasetIdx) { lineStyles[0] }

            // Draw file name (bold)
            contentStream.beginText()
            contentStream.setFont(boldFont, 8f)
            contentStream.newLineAtOffset(MARGIN, yCursor)
            val displayName = if (dataset.fileName.length > 35) dataset.fileName.take(32) + "..." else dataset.fileName
            contentStream.showText("File ${datasetIdx + 1}: $displayName")
            contentStream.endText()

            // Short line style indicator under filename
            val indicatorWidth = 50f
            val indicatorY = yCursor - 3f
            contentStream.setStrokingColor(dashColor)
            contentStream.setLineWidth(1.5f)
            if (lineStyle.dashArray != null) {
                contentStream.setLineDashPattern(lineStyle.dashArray, 0f)
            } else {
                contentStream.setLineDashPattern(floatArrayOf(), 0f)
            }
            contentStream.moveTo(MARGIN, indicatorY)
            contentStream.lineTo(MARGIN + indicatorWidth, indicatorY)
            contentStream.stroke()
            contentStream.setLineDashPattern(floatArrayOf(), 0f)

            drawChannelChipsInline(
                contentStream = contentStream,
                regularFont = regularFont,
                yCursor = yCursor,
                dataset = dataset,
                vis = vis,
                config = config
            )

            yCursor -= ROW_SPACING
        }

        // Embed screenshot as chart image
        val screenshotPdImage = PDImageXObject.createFromByteArray(
            document,
            bufferedImageToByteArray(screenshot),
            "chart"
        )

        // Calculate scaling to fit in available space
        val availableHeight = yCursor - MARGIN - 10f
        val availableWidth = chartAreaWidth

        val screenshotAspect = screenshot.width.toFloat() / screenshot.height.toFloat()
        val availableAspect = availableWidth / availableHeight

        val (drawWidth, drawHeight) = if (screenshotAspect > availableAspect) {
            // Screenshot is wider - fit to width
            availableWidth to (availableWidth / screenshotAspect)
        } else {
            // Screenshot is taller - fit to height
            (availableHeight * screenshotAspect) to availableHeight
        }

        val xOffset = MARGIN + (availableWidth - drawWidth) / 2
        val yOffset = MARGIN + (availableHeight - drawHeight) / 2

        contentStream.drawImage(screenshotPdImage, xOffset, yOffset, drawWidth, drawHeight)
    }

    private data class LineStyleInfo(val width: Float, val dashArray: FloatArray?) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as LineStyleInfo
            if (width != other.width) return false
            if (dashArray != null) {
                if (other.dashArray == null) return false
                if (!dashArray.contentEquals(other.dashArray)) return false
            } else if (other.dashArray != null) return false
            return true
        }

        override fun hashCode(): Int {
            var result = width.hashCode()
            result = 31 * result + (dashArray?.contentHashCode() ?: 0)
            return result
        }
    }

    private fun bufferedImageToByteArray(image: BufferedImage): ByteArray {
        val baos = java.io.ByteArrayOutputStream()
        javax.imageio.ImageIO.write(image, "PNG", baos)
        return baos.toByteArray()
    }
}

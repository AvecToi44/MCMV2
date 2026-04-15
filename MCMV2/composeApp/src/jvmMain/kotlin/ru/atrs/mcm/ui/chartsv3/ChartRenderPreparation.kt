package ru.atrs.mcm.ui.chartsv3

import androidx.compose.ui.graphics.PathEffect
import ru.atrsx.chartviewer.koala.xygraph.Point
import ru.atrsx.chartviewer.koala.xygraph.autoScaleXRange
import ru.atrsx.chartviewer.koala.xygraph.autoScaleYRange

private const val DEFAULT_CHART_MAX_POINTS = 6000
private const val DEFAULT_CHART_MIN_POINTS = 200

data class PreparedChartSeries(
    val datasetIndex: Int,
    val seriesIndex: Int,
    val points: List<Point<Float, Float>>,
    val isSecondHalf: Boolean,
    val pathEffect: PathEffect?
)

data class PreparedChartRender(
    val series: List<PreparedChartSeries>,
    val xRange: ClosedFloatingPointRange<Float>,
    val yRange: ClosedFloatingPointRange<Float>
)

fun prepareChartRender(
    datasets: List<ChartData>,
    visibilityStates: List<List<Boolean>>,
    overlapHalves: Boolean,
    maxPoints: Int = DEFAULT_CHART_MAX_POINTS,
    minPoints: Int = DEFAULT_CHART_MIN_POINTS
): PreparedChartRender {
    val preparedSeries = buildList {
        datasets.forEachIndexed { datasetIndex, dataset ->
            dataset.series.forEachIndexed { seriesIndex, series ->
                if (visibilityStates.getOrNull(datasetIndex)?.getOrNull(seriesIndex) != true) return@forEachIndexed

                val chunks = if (!overlapHalves) {
                    listOf(series to false)
                } else {
                    val (first, secondMirrored) = splitAndOverlapBook(series)
                    listOf(first to false, secondMirrored to true)
                }

                chunks.forEach { (chunk, isSecondHalf) ->
                    val points = downsampleSeries(chunk, maxPoints, minPoints)
                    if (points.isNotEmpty()) {
                        add(
                            PreparedChartSeries(
                                datasetIndex = datasetIndex,
                                seriesIndex = seriesIndex,
                                points = points,
                                isSecondHalf = isSecondHalf,
                                pathEffect = dataset.pathEffect
                            )
                        )
                    }
                }
            }
        }
    }

    val allPoints = preparedSeries.flatMap { it.points }
    val xRange = allPoints.takeIf { it.isNotEmpty() }?.autoScaleXRange(useNiceRange = false) ?: (0f..1f)
    val yRange = allPoints.takeIf { it.isNotEmpty() }?.autoScaleYRange(useNiceRange = false) ?: (0f..1f)

    return PreparedChartRender(
        series = preparedSeries,
        xRange = xRange,
        yRange = yRange
    )
}

private fun splitAndOverlapBook(series: List<Point<Float, Float>>): Pair<List<Point<Float, Float>>, List<Point<Float, Float>>> {
    if (series.size < 2) return series to emptyList()

    val xMin = series.first().x
    val xMax = series.last().x
    val xMid = (xMin + xMax) / 2f

    val first = series.filter { it.x <= xMid }
    val second = series.filter { it.x > xMid }

    if (first.isEmpty() || second.isEmpty()) return first to emptyList()

    val secondMirrored = second
        .map { point -> Point(xMin + (xMax - point.x), point.y) }
        .reversed()

    return first to secondMirrored
}

private fun downsampleSeries(
    series: List<Point<Float, Float>>,
    maxPoints: Int,
    minPoints: Int
): List<Point<Float, Float>> {
    if (series.isEmpty()) return emptyList()
    val step = (series.size / minPoints).coerceAtLeast(1)
    return if (series.size > maxPoints) {
        series.filterIndexed { index, _ -> index % step == 0 }
    } else {
        series
    }
}

// SaveExcelExact.kt
import ru.atrsx.mcmcomposer.MainExperimentConfig
import ru.atrsx.mcmcomposer.PressureChannel
import ru.atrsx.mcmcomposer.ScenarioStep
import ru.atrsx.mcmcomposer.SolenoidChannel
import java.io.File
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import javax.swing.filechooser.FileNameExtensionFilter

fun chooseExcelSavePath(
    initialPath: String? = null,
    defaultFileName: String = "experiment.xlsx"
): String? {
    // Native-ish dialog via JFileChooser (stable everywhere)
    val chooser = JFileChooser().apply {
        fileFilter = FileNameExtensionFilter("Excel Workbook (*.xlsx)", "xlsx")
        isAcceptAllFileFilterUsed = true
        selectedFile = when {
            initialPath == null -> File(System.getProperty("user.home"), defaultFileName)
            File(initialPath).isDirectory -> File(initialPath, defaultFileName)
            else -> File(initialPath)
        }
    }
    val result = chooser.showSaveDialog(null)
    if (result != JFileChooser.APPROVE_OPTION) return null
    val path = chooser.selectedFile.absolutePath
    val withExt = if (path.lowercase().endsWith(".xlsx")) path else "$path.xlsx"
    return avoidOverwrite(withExt)
}

private fun avoidOverwrite(path: String): String {
    var f = File(path)
    if (!f.exists()) return f.absolutePath
    val base = f.nameWithoutExtension
    val ext = ".xlsx"
    var n = 1
    while (true) {
        val candidate = File(f.parentFile, "$base ($n)$ext")
        if (!candidate.exists()) return candidate.absolutePath
        n++
    }
}

// Heuristic: if you don’t pass mainFreq, derive from solenoids (most common tenthFrequency / 10)
private fun deriveMainFreq(solenoids: List<SolenoidChannel>): Int {
    val list = solenoids.map { it.tenthFrequency }.filter { it > 0 }
    if (list.isEmpty()) return 0
    val mode = list.groupingBy { it }.eachCount().maxBy { it.value }.key
    return mode / 10
}

/**
 * Opens a Save dialog and writes the Excel in the exact screenshot layout.
 *
 * @param titleText text in A1 (e.g., source filename)
 * @param mainFreq  global Main Frequency; pass null to auto-derive from solenoids
 * @param testVar   optional value shown next to "TestVariable:" (default 0)
 */
fun saveExperimentExcelWithDialogExact(
    config: MainExperimentConfig,
    pressures: List<PressureChannel>,
    solenoids: List<SolenoidChannel>,
    scenarios: List<ScenarioStep>,
    titleText: String? = null,
    mainFreq: Int? = null,
    testVar: Int = 0
) {
    val suggestedName = (config.sheetName.ifBlank { "test" }) + ".xlsx"
    val picked = chooseExcelSavePath(config.standardPath, suggestedName) ?: return

    runCatching {
        exportExperimentToExcelExact(
            outPath   = picked,
            titleText = titleText,
            sheetName = config.sheetName.ifBlank { "test" },
            pressures = pressures,
            solenoids = solenoids,
            scenarios = scenarios,
            mainFreq  = mainFreq ?: deriveMainFreq(solenoids),
            testVar   = testVar
        )
        config.standardPath = picked
        JOptionPane.showMessageDialog(null, "Excel сохранён:\n$picked", "Успех", JOptionPane.INFORMATION_MESSAGE)
    }.onFailure { e ->
        JOptionPane.showMessageDialog(null, "Не удалось сохранить файл:\n${e.message}", "Ошибка", JOptionPane.ERROR_MESSAGE)
    }
}

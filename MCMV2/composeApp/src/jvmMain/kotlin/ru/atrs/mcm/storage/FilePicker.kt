package ru.atrs.mcm.storage

import ru.atrs.mcm.utils.chartFileAfterExperiment
import ru.atrs.mcm.utils.doOpen_Second_ChartWindow
import java.io.File
import javax.swing.JFileChooser

import javax.swing.filechooser.FileNameExtensionFilter


//https://mkyong.com/swing/java-swing-jfilechooser-example/

fun openPicker(targetDir: File, picker: PickTarget = PickTarget.PICK_SCENARIO,isOnlyViewer: Boolean = false): File? {
    val chooser = JFileChooser(targetDir)
    var filter = FileNameExtensionFilter(
        "Config", "xls","xlsx"
    )
    when(picker) {
        PickTarget.PICK_SCENARIO -> {
            filter = FileNameExtensionFilter(
                "Config", "xls","xlsx"
            )
        }
        PickTarget.PICK_STANDARD_CHART -> {
            filter = FileNameExtensionFilter(
                "Chart", "txt","agregatka"
            )
        }
        PickTarget.PICK_CHART_VIEWER -> {
            filter = FileNameExtensionFilter(
                "Chart", "txt","agregatka"
            )
        }
    }

    chooser.fileFilter = filter
    val returnVal = chooser.showOpenDialog(null)
    if (returnVal == JFileChooser.APPROVE_OPTION) {
        println(
            "You chose to open this file: " +
                    chooser.selectedFile.name
        )
//        CoroutineScope(Dispatchers.IO).launch {
//            comparatorForLaunchScenario(chooser.selectedFile)
//            targetParseScenario(chooser.selectedFile)
//        }

        when {
            picker == PickTarget.PICK_CHART_VIEWER -> {
                chartFileAfterExperiment.value = chooser.selectedFile
                if (isOnlyViewer) {
                    doOpen_Second_ChartWindow.value = true
                }


            }
        }
        return chooser.selectedFile
    }
    return null
}

enum class PickTarget { PICK_SCENARIO, PICK_CHART_VIEWER, PICK_STANDARD_CHART }

suspend fun openPickerLambda(targetDir: File, out: (fileOut: File) -> Unit) {

    val chooser = JFileChooser(targetDir)
    var filter = FileNameExtensionFilter(
        "Config", "xls","xlsx"
    )
    chooser.fileFilter = filter
    val returnVal = chooser.showOpenDialog(null)
    if (returnVal == JFileChooser.APPROVE_OPTION) {
        println(
            "You chose to open this file: " +
                    chooser.selectedFile.name
        )
        out.invoke(chooser.selectedFile)
    }
}
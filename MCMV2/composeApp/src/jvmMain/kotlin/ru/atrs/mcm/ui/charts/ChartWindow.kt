package ru.atrs.mcm.ui.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import ru.atrs.mcm.enums.StateExperiments
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jfree.chart.ChartPanel
import org.jfree.chart.JFreeChart
import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.plot.IntervalMarker
import org.jfree.chart.plot.PlotOrientation
import org.jfree.chart.plot.XYPlot
import org.jfree.chart.renderer.xy.XYItemRenderer
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer
import org.jfree.chart.ui.Layer
import org.jfree.data.xy.XYSeries
import org.jfree.data.xy.XYSeriesCollection
import ru.atrs.mcm.parsing_excel.targetParseScenario
import ru.atrs.mcm.parsing_excel.writeToExcel
import ru.atrs.mcm.ui.showMeSnackBar
import ru.atrs.mcm.utils.Dir0Configs_End
import ru.atrs.mcm.utils.Dir2Reports
import ru.atrs.mcm.utils.Dir3Scenarios
import ru.atrs.mcm.utils.Dir7ReportsStandard
import ru.atrs.mcm.utils.Dir_10_ScenarioForChart
import ru.atrs.mcm.utils.STATE_EXPERIMENT
import ru.atrs.mcm.utils.chartFileAfterExperiment
import ru.atrs.mcm.utils.chartFileStandard
import ru.atrs.mcm.utils.doOpen_First_ChartWindow
import ru.atrs.mcm.utils.doOpen_Second_ChartWindow
import ru.atrs.mcm.utils.logAct
import ru.atrs.mcm.utils.logError
import ru.atrs.mcm.utils.logGarbage
import ru.atrs.mcm.utils.scenario
import ru.atrs.mcm.utils.soundUniversal
import ru.atrs.mcm.storage.PickTarget
import ru.atrs.mcm.storage.openPicker
import ru.atrs.mcm.storage.openPickerLambda
import java.awt.BasicStroke
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import javax.swing.BoxLayout
import javax.swing.JPanel

data class SeriesChart( var xySeries: XYSeries, var isVisible: Boolean )

class ChartWindowDeprecated(var withStandard: Boolean = false, val isViewerOnly: Boolean = false) {
    val dataset = XYSeriesCollection()

    private val series1 = XYSeries("Давление 1")
    private val series2 = XYSeries("Давление 2")
    private val series3 = XYSeries("Давление 3")
    private val series4 = XYSeries("Давление 4")
    private val series5 = XYSeries("Давление 5")
    private val series6 = XYSeries("Давление 6")
    private val series7 = XYSeries("Давление 7")
    private val series8 = XYSeries("Давление 8")


    private val series9 =  XYSeries("Давление 1"+(" [Стандарт]"))
    private val series10 = XYSeries("Давление 2"+(" [Стандарт]"))
    private val series11 = XYSeries("Давление 3"+(" [Стандарт]"))
    private val series12 = XYSeries("Давление 4"+(" [Стандарт]"))
    private val series13 = XYSeries("Давление 5"+(" [Стандарт]"))
    private val series14 = XYSeries("Давление 6"+(" [Стандарт]"))
    private val series15 = XYSeries("Давление 7"+(" [Стандарт]"))
    private val series16 = XYSeries("Давление 8"+(" [Стандарт]"))

    //private var showMeChart = mutableStateOf(false)

    var chartPanel: JPanel? = null
    var isLoading = mutableStateOf(false)
    val crtx = CoroutineScope(Dispatchers.Default)

    private var renderer: XYItemRenderer = XYLineAndShapeRenderer(true, false)
    private val xAxis = NumberAxis("time (ms)")
    private val yAxis = NumberAxis("Bar")
    private var plot = XYPlot(dataset, xAxis, yAxis, renderer)

    var chart = JFreeChart(
        "Эталон и текущий эксперимент", JFreeChart.DEFAULT_TITLE_FONT,
        plot,true
    )

    private var sizeStandard   = 0
    private var sizeExperiment = 0

    var stateOfVisibleSeries = arrayListOf<Boolean>()

    val arrClrExperiment = arrayOf(
        java.awt.Color.RED,
        java.awt.Color.ORANGE,
        java.awt.Color.YELLOW,
        java.awt.Color.GREEN,
        java.awt.Color.BLUE,
        java.awt.Color.CYAN,
        java.awt.Color.MAGENTA,
        java.awt.Color.BLACK
    )

    val arrClrStandard = arrayOf(
        java.awt.Color(255, 145, 145),
        java.awt.Color(255, 200, 50),
        java.awt.Color(255, 255, 150),
        java.awt.Color(147, 255, 100),
        java.awt.Color(147, 147, 255),
        java.awt.Color(147, 255, 255),
        java.awt.Color(255, 147, 255),
        java.awt.Color(128, 128, 128)
    )

    var halfNumberOfCharts = 0

    init {
        crtx.launch {

            delay(1000)
            fillUp()
            delay(1000)
            STATE_EXPERIMENT.value = StateExperiments.NONE
        }.invokeOnCompletion {

            crtx.launch {

                addMarkersScenarios()

            }.invokeOnCompletion {

            }

        }


        chartPanel = ChartPanel(chart)
    }

    @Composable
    fun chartWindow() {

        Window(
            title = if (isViewerOnly) "Viewer" else "Compare with Standard",
            state = WindowState(size = DpSize(1000.dp, 800.dp)),
            onCloseRequest = {

                CoroutineScope(Dispatchers.Default).launch {
                    if (isViewerOnly) {
                        doOpen_Second_ChartWindow.value = false
                    } else {
                        doOpen_First_ChartWindow.value = false
                    }
                    delay(100)

                    writeToExcel(0,0, chartFileStandard.value!!.name)

                }
        },
        ) {
            val chrt = remember { STATE_EXPERIMENT }

            if (chrt.value == StateExperiments.NONE) {
                ChartSecond()
            }else {
                Box(Modifier.fillMaxSize().background(Color.Black)) {
                    Text("Loading...",
                        modifier = Modifier.padding(top = (10).dp,start = 20.dp),
                        fontFamily = FontFamily.Default, fontSize = 40.sp,
                        fontWeight = FontWeight.Bold, color = Color.White
                    )
                }
            }
        }
    }


    private suspend fun fillUp() {
        STATE_EXPERIMENT.value = StateExperiments.PREPARE_CHART
        logAct("fillUp chart ${chartFileAfterExperiment.value.name}  ${chartFileStandard.value!!.name}")

        logGarbage(">>>1")

        /**
         * FILL from EXPERIMENT:
         */
        fillExperiment()
//        try {
//
//            val br = BufferedReader(FileReader(chartFileAfterExperiment.value)) // chartFileStandard
//            var line: String?
//            //var countOfLine = 0
//            while (br.readLine().also { line = it } != null) {
//                if (line != "" || line != " ") {
//                    val items = line?.split(";", "|")?.toTypedArray()
//                    //println("withStandard >>>> ${items?.joinToString()}")
//                    if (items != null) {
//                        series9.add(items[0].toInt(), items[1].toInt())
//                        series10.add(items[2].toInt(), items[3].toInt()).takeIf { items.size > 2 }
//                        series11.add(items[4].toInt(), items[5].toInt()).takeIf { items.size > 4 }
//                        series12.add(items[6].toInt(), items[7].toInt()).takeIf { items.size > 6 }
//                        series13.add(items[8].toInt(), items[9].toInt()).takeIf { items.size > 8 }
//                        series14.add(items[10].toInt(), items[11].toInt()).takeIf { items.size > 10 }
//                        series15.add(items[12].toInt(), items[13].toInt()).takeIf { items.size > 12 }
//                        series16.add(items[14].toInt(), items[15].toInt()).takeIf { items.size > 14 }
//                    }
//                }
//                //countOfLine++
//            }
//            br.close()
//        } catch (e: Exception) {
//            logError("error +${e.message}")
//            showMeSnackBar("Error Chart:  ${e.message}", Color.Red)
//        }

        /**
         * FILL from STANDARD:
         */
        if (withStandard) {
            fillStandard()
//            try {
//                val br = BufferedReader(FileReader(chartFileStandard.value)) // chartFileAfterExperiment
//                var line: String?
//                //var countOfLine = 0
//                while (br.readLine().also { line = it } != null) {
//                    if (line != "" || line != " ") {
//                        val items = line?.split(";", "|")?.toTypedArray()
//                        //println("exp >>>> ${items?.joinToString()}")
//                        if (items != null) {
//
//                            series1.add(items[0].toInt(), items[1].toInt())
//
//                            series2.add(items[2].toInt(), items[3].toInt()).takeIf { items.size > 2 }
//                            series3.add(items[4].toInt(), items[5].toInt()).takeIf { items.size > 4 }
//                            series4.add(items[6].toInt(), items[7].toInt()).takeIf { items.size > 6 }
//                            series5.add(items[8].toInt(), items[9].toInt()).takeIf { items.size > 8 }
//                            series6.add(items[10].toInt(), items[11].toInt()).takeIf { items.size > 10 }
//                            series7.add(items[12].toInt(), items[13].toInt()).takeIf { items.size > 12 }
//                            series8.add(items[14].toInt(), items[15].toInt()).takeIf { items.size > 14 }
//                        }
//                    }
//                }
//                br.close()
//            } catch (e: Exception) {
//                logError("error +${e.message}")
//            }
            logGarbage(">>>2")
        }


        // // //

        logGarbage(">>>3")
        sizeExperiment = series1.items.size
            dataset.addSeries(series1)
            dataset.addSeries(series2)
            dataset.addSeries(series3)
            dataset.addSeries(series4)
            dataset.addSeries(series5)
            dataset.addSeries(series6)
            dataset.addSeries(series7)
            dataset.addSeries(series8)

            if (withStandard) {
                sizeStandard = series10.items.size
                //logInfo("chart series16 ${series16.maximumItemCount} ${series16}")
                dataset.addSeries(series9)
                dataset.addSeries(series10)
                dataset.addSeries(series11)
                dataset.addSeries(series12)
                dataset.addSeries(series13)
                dataset.addSeries(series14)
                dataset.addSeries(series15)
                dataset.addSeries(series16)
            }


            xAxis.autoRangeIncludesZero = false

        logGarbage(">>>4")

        repeat(dataset.seriesCount) {
            stateOfVisibleSeries.add(true)
        }

        logGarbage(">>>5")
            repeat(8) {
                //renderer
                renderer.setSeriesPaint(it, arrClrExperiment[it])
                renderer.setSeriesStroke(it, BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND))
                //renderer.setSeriesShape(it+8,ShapeUtilities.createDiamond(0f))
            }

            if (withStandard) {
                repeat(8) {

                    //renderer.setSeriesShape(it+8,ShapeUtilities.createDiamond(6f))
                    renderer.setSeriesPaint(it + 8, arrClrStandard[it])
                    renderer.setSeriesStroke(
                        it + 8,
                        //ShapeUtilities.createDiamond(6f)
                        BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 1f, floatArrayOf(10f), 0.5f)
                    )
                    //renderer.sha
                }
            }


        logGarbage(">>>6")
        plot = XYPlot(dataset, xAxis, yAxis, renderer)
        //plot
        plot.setOrientation(PlotOrientation.VERTICAL)
        logGarbage(">>>7")
        plot.setRenderer(renderer)




    }

    @Composable
    fun ChartSecond() {
        val standardFile = remember { chartFileStandard }
        //val shwmechrt = remember { showMeChart }
        //val loader = remember { isLoading }
        Box {
            Column(
                modifier = Modifier.fillMaxSize()//fillMaxWidth().height(800.dp)
            ) {
                Row(Modifier.fillMaxWidth().height(20.dp).background(Color.White), horizontalArrangement = Arrangement.Start) {
                    Box(Modifier.height(20.dp).weight(0.5f).clickable {
                        openPicker(Dir7ReportsStandard, PickTarget.PICK_STANDARD_CHART).let {
                            if(it != null) {
                                chartFileStandard.value = it
                                CoroutineScope(Dispatchers.Default).launch {
                                    writeToExcel(0,0, chartFileStandard.value!!.name)
                                }

                            }
                        }

                        CoroutineScope(Dispatchers.Default).launch {
                            fillStandard()
                        }

                    }){
                        Text("Эталон(${sizeStandard}): ${standardFile.value!!.name}", modifier = Modifier.padding(0.dp).align(Alignment.Center),
                            fontFamily = FontFamily.Default, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.Black
                        )
                    }
                    repeat((dataset.seriesCount/2)) {
                        val rmClr = remember { mutableStateOf(stateOfVisibleSeries[it+dataset.seriesCount/2]) }
                        Box(
                            Modifier.width(30.dp).fillMaxHeight().padding(horizontal = 3.dp).background(if (rmClr.value) Color.Blue else Color.LightGray).clickable {
                                stateOfVisibleSeries[it+dataset.seriesCount/2] = !stateOfVisibleSeries[it+dataset.seriesCount/2]
                                rmClr.value = stateOfVisibleSeries[it+dataset.seriesCount/2]
                                renderer.setSeriesVisible(it+dataset.seriesCount/2, stateOfVisibleSeries[it+dataset.seriesCount/2], true)

                                //renderer.setSeriesVisible(0,false,true)
                            }
                        ) {
                            Text("${it+1+dataset.seriesCount/2}")
                        }
                    }

                }
                Row(Modifier.fillMaxWidth().height(20.dp).background(Color.White), horizontalArrangement = Arrangement.Start) {
                    Box(Modifier.height(20.dp).weight(0.5f).clickable {
                        openPicker(Dir2Reports, PickTarget.PICK_CHART_VIEWER).let { if(it != null) chartFileAfterExperiment.value = it }

                        CoroutineScope(Dispatchers.Default).launch {
                            fillExperiment()
                        }

                    }){
                        Text("Эксперимент(${sizeExperiment}): ${chartFileAfterExperiment.value.name}", modifier = Modifier.padding(0.dp).align(Alignment.Center),
                            fontFamily = FontFamily.Default, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.Black
                        )
                    }
                    repeat((dataset.seriesCount/2)) {
                        val rmClr = remember { mutableStateOf(stateOfVisibleSeries[it]) }
                        Box(
                            Modifier.width(30.dp).fillMaxHeight().padding(horizontal = 3.dp).background(if (rmClr.value) Color.Blue else Color.LightGray).clickable {
                                stateOfVisibleSeries[it] = !stateOfVisibleSeries[it]
                                rmClr.value = stateOfVisibleSeries[it]
                                renderer.setSeriesVisible(it, stateOfVisibleSeries[it], true)

                                //renderer.setSeriesVisible(0,false,true)
                            }
                        ) {
                            Text("${it+1}")
                        }
                    }
                }
                SwingPanel(
                    background = Color.DarkGray,
                    modifier = Modifier.fillMaxSize(),//.size(width = 1000.dp, height =  600.dp),
                    factory = {
                        JPanel().apply {
                            setLayout(BoxLayout(this, BoxLayout.Y_AXIS))
                            add(chartPanel)

                        }
                    }
                )

            }
//            if (loader.value) {
//                Box(Modifier.fillMaxSize().background(colorTrans60)){
//                    Text("Загрузка...", modifier = Modifier.padding(0.dp).align(Alignment.Center),
//                        fontFamily = FontFamily.Default, fontSize = 40.sp, fontWeight = FontWeight.Medium, color = Color.Blue
//                    )
//                }
//            }

        }

    }

    private suspend fun fillStandard() {
        isLoading.value = true
        logAct("fillStandard !!!")
        try {
            series9.clear()
            series10.clear()
            series11.clear()
            series12.clear()
            series13.clear()
            series14.clear()
            series15.clear()
            series16.clear()

            //val series9 =  XYSeries("Давление 1"+(" [Стандарт]"))
            //val series10 = XYSeries("Давление 2"+(" [Стандарт]"))
            //val series11 = XYSeries("Давление 3"+(" [Стандарт]"))
            //val series12 = XYSeries("Давление 4"+(" [Стандарт]"))
            //val series13 = XYSeries("Давление 5"+(" [Стандарт]"))
            //val series14 = XYSeries("Давление 6"+(" [Стандарт]"))
            //val series15 = XYSeries("Давление 7"+(" [Стандарт]"))
            //val series16 = XYSeries("Давление 8"+(" [Стандарт]"))

            val br = BufferedReader(FileReader(chartFileStandard.value))
            var line: String?
            //var countOfLine = 0
            while (br.readLine().also { line = it } != null) {
                if(line != ""|| line != " ") {
                    if (line?.first() == '&') {
                        val items = line?.split("&")?.toTypedArray()
                        Dir_10_ScenarioForChart = File(Dir3Scenarios,items?.get(0) ?: "")
                    } else {
                        val items = line?.split(";","|")?.toTypedArray()
                        if (items != null ) {
                            series9 .add(items[0].toInt(),items[1].toInt())
                            series10.add(items[2].toInt(),items[3].toInt()).takeIf { items.size > 2 }
                            series11.add(items[4].toInt(),items[5].toInt()).takeIf { items.size > 4 }
                            series12.add(items[6].toInt(),items[7].toInt()).takeIf { items.size > 6 }
                            series13.add(items[8].toInt(),items[9].toInt()).takeIf { items.size > 8 }
                            series14.add(items[10].toInt(),items[11].toInt()).takeIf { items.size > 10 }
                            series15.add(items[12].toInt(),items[13].toInt()).takeIf { items.size > 12 }
                            series16.add(items[14].toInt(),items[15].toInt()).takeIf { items.size > 14 }
                        }
                    }

                }
                //countOfLine++
            }
            br.close()
        } catch (e: Exception) {
            logError("error +${e.message}")
            showMeSnackBar("Error Chart:  ${e.message}",Color.Red)
        }

        //dataset.notify = true
        series9.notify = true
        series10.notify = true
        series11.notify = true
        series12.notify = true
        series13.notify = true
        series14.notify = true
        series15.notify = true
        series16.notify = true

        isLoading.value = false
    }

    private suspend fun fillExperiment() {
        isLoading.value = true
        logAct("fillExperiment !!!")
        try {
            series1.clear()
            series2.clear()
            series3.clear()
            series4.clear()
            series5.clear()
            series6.clear()
            series7.clear()
            series8.clear()

            //val series9 =  XYSeries("Давление 1"+(" [Стандарт]"))
            //val series10 = XYSeries("Давление 2"+(" [Стандарт]"))
            //val series11 = XYSeries("Давление 3"+(" [Стандарт]"))
            //val series12 = XYSeries("Давление 4"+(" [Стандарт]"))
            //val series13 = XYSeries("Давление 5"+(" [Стандарт]"))
            //val series14 = XYSeries("Давление 6"+(" [Стандарт]"))
            //val series15 = XYSeries("Давление 7"+(" [Стандарт]"))
            //val series16 = XYSeries("Давление 8"+(" [Стандарт]"))

            val br = BufferedReader(FileReader(chartFileAfterExperiment.value))
            var line: String?
            //var countOfLine = 0
            while (br.readLine().also { line = it } != null) {
                if(line != ""|| line != " ") {
                    if (line?.first() == '#') {
                        val items = line?.split("#")?.toTypedArray()
                        if (items != null ) {
                            println("# ${items.joinToString()}")
//                            when {
//                                items[0] == "standard" -> chartFileStandard.value = File(Dir7ReportsStandard, items[1])
//                                items[0] == "visibility" ->
//
//                            }


                        }

                    } else {
                        val items = line?.split(";","|")?.toTypedArray()
                        if (items != null ) {
                            series1.add(items[0].toInt(),items[1].toInt())

                            series2.add(items[2].toInt(), items[3].toInt()).takeIf  { items.size > 2  }
                            series3.add(items[4].toInt(), items[5].toInt()).takeIf  { items.size > 4  }
                            series4.add(items[6].toInt(), items[7].toInt()).takeIf  { items.size > 6  }
                            series5.add(items[8].toInt(), items[9].toInt()).takeIf  { items.size > 8  }
                            series6.add(items[10].toInt(),items[11].toInt()).takeIf { items.size > 10 }
                            series7.add(items[12].toInt(),items[13].toInt()).takeIf { items.size > 12 }
                            series8.add(items[14].toInt(),items[15].toInt()).takeIf { items.size > 14 }
                        }
                    }

                }
                //countOfLine++
            }
            br.close()
        } catch (e: Exception) {
            logError("error +${e.message}")
            showMeSnackBar("Error Chart:  ${e.message}",Color.Red)
        }

        series1.notify = true
        series2.notify = true
        series3.notify = true
        series4.notify = true
        series5.notify = true
        series6.notify = true
        series7.notify = true
        series8.notify = true

        halfNumberOfCharts = dataset.seriesCount / 2
        isLoading.value = false
    }

    suspend fun addMarkersScenarios() {
        if (Dir_10_ScenarioForChart.exists()) {
            targetParseScenario(Dir_10_ScenarioForChart)
        }


        if (scenario.isEmpty()) {
            openPickerLambda(Dir3Scenarios) {
                crtx.launch {
                    targetParseScenario(it)

                    var lastNum = 0.0
                    logGarbage("SCENNNAAX ${scenario.size} ")
                    repeat(scenario.size) {
                        logGarbage("SCENNNAA")
                        val target = IntervalMarker(lastNum, lastNum+ scenario[it].time.toDouble(),
                            if (it % 2 == 0)java.awt.Color.LIGHT_GRAY else java.awt.Color.GRAY)
                        lastNum += scenario[it].time.toDouble()
                        target.label = "[${scenario[it].comment}]"
                        //target.paint = java.awt.Color.GREEN
                        //target.labelAnchor = RectangleAnchor.LEFT
                        //target.labelTextAnchor = TextAnchor.CENTER_LEFT
                        //target.setPaint(java.awt.Color.GREEN)
                        //target.setPaint(new Color(10, 222, 2, 128));
                        //target.setPaint(new Color(10, 222, 2, 128));
                        //plot.addRangeMarker(1,target,Layer.FOREGROUND,true)
                        val plot: XYPlot = chart.getXYPlot()
                        plot.addDomainMarker(target, Layer.BACKGROUND)
                        chart.isNotify = true
                    }
                    //showMeChart.value = true


                    chart = JFreeChart(
                        "Эталон и текущий эксперимент", JFreeChart.DEFAULT_TITLE_FONT,
                        plot,true
                    )
                    logGarbage(">>>8 $}")
                    soundUniversal(Dir0Configs_End)
                    halfNumberOfCharts = dataset.seriesCount
                }

            }
            //openNewScenario(isRefreshForChart = true)
        } else {

        }


    }
}

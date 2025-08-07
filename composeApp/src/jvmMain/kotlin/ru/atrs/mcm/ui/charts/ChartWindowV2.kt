package ru.atrs.mcm.ui.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import com.aay.compose.baseComponents.model.GridOrientation
import com.aay.compose.lineChart.LineChart
import com.aay.compose.lineChart.model.LineParameters
import com.aay.compose.lineChart.model.LineType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.atrs.mcm.enums.StateExperiments
import ru.atrs.mcm.parsing_excel.writeToExcel
import ru.atrs.mcm.utils.STATE_EXPERIMENT
import ru.atrs.mcm.utils.chartFileStandard
import ru.atrs.mcm.utils.doOpen_First_ChartWindow
import ru.atrs.mcm.utils.doOpen_Second_ChartWindow

@Composable
fun chartWindowV2() {

    Window(
        title = "Chart V2",
        state = WindowState(size = DpSize(1000.dp, 800.dp)),
        onCloseRequest = {

            CoroutineScope(Dispatchers.Default).launch {


            }
        },
    ) {
        val testLineParameters: List<LineParameters> = listOf(
            LineParameters(
                label = "revenue",
                data = listOf(70.0, 00.0, 50.33, 40.0, 100.500, 50.0),
                lineColor = Color.Gray,
                lineType = LineType.CURVED_LINE,
                lineShadow = true,
            ),
            LineParameters(
                label = "Earnings",
                data = listOf(60.0, 80.6, 40.33, 86.232, 88.0, 90.0),
                lineColor = Color(0xFFFF7F50),
                lineType = LineType.DEFAULT_LINE,
                lineShadow = true
            ),
            LineParameters(
                label = "Earnings",
                data = listOf(1.0, 40.0, 11.33, 55.23, 1.0, 100.0),
                lineColor = Color(0xFF81BE88),
                lineType = LineType.CURVED_LINE,
                lineShadow = false,
            )
        )

        Box(Modifier) {
            LineChart(
                modifier = Modifier.fillMaxSize(),
                linesParameters = testLineParameters,
                isGrid = true,
                gridColor = Color.Blue,
                xAxisData = listOf("2015", "2016", "2017", "2018", "2019", "2020"),
                animateChart = true,
                showGridWithSpacer = true,
                yAxisStyle = TextStyle(
                    fontSize = 14.sp,
                    color = Color.Gray,
                ),
                xAxisStyle = TextStyle(
                    fontSize = 14.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.W400
                ),
                yAxisRange = 14,
                oneLineChart = false,
                gridOrientation = GridOrientation.VERTICAL
            )
        }
    }
}
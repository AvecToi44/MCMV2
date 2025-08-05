package ru.atrs.mcm.view_results
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.max

sealed class LinearChartStyle {
    object Default : LinearChartStyle()
    object Smooth : LinearChartStyle()
}
data class PointF(val x: Float,val y : Float)

@Composable
fun LinearChart(
    modifier: Modifier = Modifier,
    style: LinearChartStyle = LinearChartStyle.Default,
    data: List<Float>, clr : Color
) {
    Column(Modifier.fillMaxHeight().background(Color.DarkGray),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "2.0", color = Color.White, modifier = Modifier.offset())
        Text(text = "0.0", color = Color.White, modifier = Modifier.offset())
        Text(text = "-2.0", color =Color.White, modifier = Modifier.offset())
    }

    Canvas(modifier = modifier) {
        // distance between each x point

        val height = 150f
        var currentX = 0F
        val maxValue = data.maxOrNull() ?: 0f
        val minValue = data.minOrNull() ?: 0f

        var distanceHoriz = 0f//size.width  / (data.size)
        val distanceVert = size.height / (abs(4f)).toFloat()

        val points = mutableListOf<PointF>()
        //Log.w("fff", "fff ${maxValue} || ${distanceHoriz}")

        data.forEachIndexed { index, currentData ->
            if (data.size >= index + 1) {

                val y0 = (currentData) * distanceVert + distanceVert * 2f
                val x0 = currentX + distanceHoriz //if(index != 0) currentX + distanceHoriz else 0f
                points.add(PointF(x0.toFloat(), y0.toFloat()))
                distanceHoriz = size.width / (data.size - 1)
                currentX += if (index != 0) distanceHoriz else 0f

            }
        }
        for (i in 0..data.size - 1) {
            drawCircle(
                Color.Blue,
                12f,
                center = Offset(distanceHoriz * i.toFloat(), distanceVert * 2f)
            )
        }

        drawLine(
            start = Offset(0f, size.height / 2f), end = Offset(size.width, size.height / 2f),
            color = Color(247, 247, 247, 255),
            strokeWidth = 8f
        )
        println( "fff data${data.joinToString()}~ points${points.joinToString()}")

        if (style == LinearChartStyle.Default) {
            for (i in 0 until points.size - 1) {

                drawLine(
                    start = Offset(points[i].x, points[i].y),
                    end = Offset(points[i + 1].x, points[i + 1].y),
                    color = clr,
                    strokeWidth = 8f
                )
            }
        } else {
            val cubicPoints1 = mutableListOf<PointF>()
            val cubicPoints2 = mutableListOf<PointF>()

            for (i in 1 until points.size) {
                cubicPoints1.add(PointF((points[i].x + points[i - 1].x) / 2, points[i - 1].y))
                cubicPoints2.add(PointF((points[i].x + points[i - 1].x) / 2, points[i].y))
            }

            val path = Path()
            path.moveTo(points.first().x, points.first().y)

            for (i in 1 until points.size) {
                drawCircle(Color.DarkGray, 10f, center = Offset(points[i].x, points[i].y))
                path.cubicTo(
                    cubicPoints1[i - 1].x,
                    cubicPoints1[i - 1].y,
                    cubicPoints2[i - 1].x,
                    cubicPoints2[i - 1].y,
                    points[i].x,
                    points[i].y
                )
            }

            drawPath(path, color = clr, style = Stroke(width = 8f))
        }
    }
}
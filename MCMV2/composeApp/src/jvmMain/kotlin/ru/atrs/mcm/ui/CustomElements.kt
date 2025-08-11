package ru.atrs.mcm.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


@Composable
fun Gauge(
    gaugeWidth: Dp = 40.dp,
    size: Dp = 150.dp, // 300 has been
    pressure: Int = 0,
    name : String =  ""
) {

    val path = Path()

    val scope = rememberCoroutineScope()

    val degrees = 1.6f * pressure - 170f

    val animatedPercentage = remember { Animatable(degrees, Float.VectorConverter) }

    LaunchedEffect(key1 = degrees) {
        scope.launch(Dispatchers.Main) {
            animatedPercentage.animateTo(
                targetValue = degrees,
                animationSpec = tween(
                    durationMillis = 500,
                    easing = FastOutLinearInEasing
//                    easing = {
//                        BounceInterpolator().getInterpolation(it)
//                    }
                )
            )
        }
    }

    Box(contentAlignment = Alignment.Center,
        modifier = Modifier.border(BorderStroke(2.dp, Color(57, 57, 57)))

    ) {
        Text("${name}", modifier = Modifier.align(Alignment.TopStart).background(	Color(103,173,237)))
        Text("####", modifier = Modifier.align(Alignment.TopCenter))
        Text("####", modifier = Modifier.align(Alignment.TopEnd))
        Text("min:####", modifier = Modifier.align(Alignment.BottomStart).background(Color.Black).padding(10.dp,20.dp), color = Color.Green)
        Text("max:####", modifier =   Modifier.align(Alignment.BottomEnd).background(Color.Black).padding(10.dp,20.dp), color = Color.Red)
        Canvas(
            modifier = Modifier
                .padding(20.dp)
                .size(size)
        ) {
            //-170->0
            //-10->160
            //sw = d + 170

            drawBackgroundIndicatorsByLevel(
                path,
                size,
                gaugeWidth,
                animatedPercentage.value + 170f
            )

            for (i in 0..4) {
                drawBackgroundIndicators(path, size, i, gaugeWidth)
            }

            drawCircle(
                color = Color.Black,
                radius = 4.dp.toPx()
            )

            rotate(degrees = animatedPercentage.value) {
                drawLine(
                    color = Color.Black,
                    start = center,
                    end = Offset(size.toPx() - gaugeWidth.toPx() - 4.dp.toPx(), size.toPx() / 2),
                    strokeWidth = 2.dp.toPx()
                )
            }

        }
        Text(
            text = (1000*((animatedPercentage.value + 170f) / 1.6f)/100).roundToInt().toString(),
            modifier = Modifier
                //.offset(y = (-50).dp)
                .background(Color.White),
            fontSize = 15.sp
        )

    }
}

private fun DrawScope.drawBackgroundIndicators(
    path: Path,
    size: Dp,
    i: Int,
    gaugeWidth: Dp
) {
    path.reset()
    path.arcTo(
        rect = Rect(
            topLeft = Offset.Zero,
            Offset(
                size.toPx(),
                size.toPx()
            )
        ),
        -170f + 28 * i + i * 5,
        28f,
        forceMoveTo = false
    )
    path.arcTo(
        rect = Rect(
            topLeft = Offset(gaugeWidth.toPx(), gaugeWidth.toPx()),
            Offset(
                size.toPx() - gaugeWidth.toPx(),
                size.toPx() - gaugeWidth.toPx()
            )
        ),
        -142f + 28 * i + i * 5,
        -28f,
        forceMoveTo = false
    )
    path.close()
    drawPath(
        path,
        color = Color.Red,
        style = Stroke(2.dp.toPx())
    )

}

private fun DrawScope.drawBackgroundIndicatorsByLevel(
    path: Path,
    size: Dp,
    gaugeWidth: Dp,
    value: Float
) {

    path.reset()

    path.arcTo(
        rect = Rect(
            topLeft = Offset.Zero,
            Offset(
                size.toPx(),
                size.toPx()
            )
        ),
        -170f,
        value,
        forceMoveTo = false
    )
    path.arcTo(
        rect = Rect(
            topLeft = Offset(gaugeWidth.toPx(), gaugeWidth.toPx()),
            Offset(
                size.toPx() - gaugeWidth.toPx(),
                size.toPx() - gaugeWidth.toPx()
            )
        ),
        -170 + value,
        -value,
        forceMoveTo = false
    )
    path.close()
    drawPath(
        path,
        brush = Brush.sweepGradient(Pair(0.4f, Color.Green), Pair(0.8f, Color.Yellow), Pair(0.99f, Color.Red)),
        style = Fill
    )


}
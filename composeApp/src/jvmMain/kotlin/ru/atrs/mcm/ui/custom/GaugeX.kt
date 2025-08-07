package ru.atrs.mcm.ui.custom

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Typeface
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.skia.Font
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.TextLine
import org.jetbrains.skia.Typeface
import ru.atrs.mcm.ui.styles.colorTrans60
import ru.atrs.mcm.ui.styles.fontDigital
import ru.atrs.mcm.utils.map
import ru.atrs.mcm.utils.mapFloat
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private var mBasePaint: Paint? = null

private fun createBasePaint() {
    mBasePaint = Paint()
    mBasePaint?.color = (Color.Red)
    //mBasePaint.style = (Paint.style)
}


// progress from 0 to 100
// raw progress 0 to 240 degree
@Composable
fun GaugeX(inputSize: DpSize, progress : Float, minType : Float, maxType: Float, type: String = "",displayName: String = "", comment: String = "",withoutSingMap: Boolean = true) {
    //println("well $progress ${minType} $maxType $type ~> ${checkInterval(map(progress, minType, maxType,0,240))}")

    val signValue = if (withoutSingMap) progress else mapFloat(checkSignInterval(progress,0f,4095f) , 0f,4095f, minType, maxType)
    val angle = mapFloat(signValue, minType, maxType,0f,240f)//checkInterval()

    val scope = rememberCoroutineScope()
    val animatedPercentage = remember { Animatable(angle.toFloat(), Float.VectorConverter) }
    val rememberShowComm = remember { mutableStateOf(false) }

    val valueOfDivision = ( maxType.toFloat() - minType.toFloat() ) / 8f

    LaunchedEffect(key1 = angle) {
        scope.launch(Dispatchers.Default) {
            animatedPercentage.animateTo(
                targetValue = angle.toFloat(),//(if (angle >= 225) 225 else angle).toFloat(),
                animationSpec = tween(
                    durationMillis = 200,
                    easing = FastOutLinearInEasing
                )
            )
        }
    }
    Column(modifier = Modifier.size(if (inputSize.width < 200.dp) DpSize(200.dp,200.dp) else inputSize).aspectRatio(1f).clickable {
        rememberShowComm.value = !rememberShowComm.value
    }) {
        Box(modifier = Modifier.fillMaxSize().weight(6f)) {

            Canvas(modifier = Modifier
                .fillMaxSize()
                .padding(0.dp)
                .align(Alignment.TopCenter)
                .border(BorderStroke(4.dp, Color.DarkGray))
            ) {
                val canvasCenter = Offset(x = size.width / 2f, y = size.height / 2f)
                val canvasW = size.width
                val canvasH = size.height
                val shift = 15f

                drawRect(Color.Black, topLeft = Offset(0f,0f), size = Size(canvasW,canvasH))

                //step 3: draw clock minute markers
                val radius = size.width / 2.9f
                val radiusForText = size.width / 2.6f
                val minuteMarkerLength = radius / 12f
                repeat(60) {
                    rotate((it / 60f) * 360) {
                        val start = center - Offset(0f, radius.toFloat())
                        val end =    start + Offset(0f, minuteMarkerLength)
                        drawLine(
                            color = Color.Blue,
                            start = start,
                            end = end,
                            strokeWidth = 2f
                        )
                    }
                }

                //step 4: draw clock hour markers
                val hourMarkerLength = size.width / 15f
                repeat(12) {
                    rotate((it / 12f) * 360) {
                        val start = center - Offset(0f, radius)
                        val end = start + Offset(0f, hourMarkerLength)
                        drawLine(
                            color = Color.White,
                            start = start,
                            end = end,
                            strokeWidth = 3f
                        )
                    }
                }



                drawCircle(
                    color = Color.White,
                    style = Stroke(
                        width = 5f
                    ),
                    radius = radius,
                    center = canvasCenter
                )



                rotate(degrees = animatedPercentage.value) {
                    drawLine(
                        color = Color.Green,
                        start = center,
                        end = Offset(canvasCenter.x / 3f,canvasCenter.y ),
                        strokeWidth = 4.dp.toPx()
                    )
                }

                drawCircle(
                    color = Color.DarkGray,
                    radius = 10f,
                    center = canvasCenter
                )
                drawRect(topLeft = Offset(0f,canvasCenter.y+shift),color = Color.Black,size = Size(canvasCenter.x,canvasCenter.y-shift))

                val trianglePath = Path().apply {
                    // 1)
                    moveTo(canvasCenter.x, canvasCenter.y+shift)

                    // 2)
                    lineTo(canvasCenter.x, canvasH)

                    // 3)
                    lineTo((canvasCenter.x + canvasCenter.x / 2f) - shift, canvasH)
                }
                drawPath(
                    color = Color.Black,
                    path = trianglePath
                )

                drawIntoCanvas { canvas ->
                    //val textPaint = Paint().asFrameworkPaint()


                    repeat(9) {
                        //The degree difference between the each 'minute' line
                        val angleDegreeDifference = (270f / 9f)
                        val angleRadDifference =
                            (((angleDegreeDifference * it) - 180f) * (PI / 180f)).toFloat()

                        var shifterX = 0
                        var shifterY = 0

                        when (it) {
                            0 -> shifterX = -15
                            1 -> shifterX = -20
                            2 -> shifterX = -20
                            3 -> shifterY = (inputSize.height.value*0.025f).toInt()
                        }
                        val positionX = (radius * 1.12f) * cos(angleRadDifference) + center.x + shifterX
                        val positionY = (radius * 1.12f) * sin(angleRadDifference) + center.y + shifterY
                        val text = (it / 5).toString()
                        val txtPaint = Paint().asFrameworkPaint()
                        txtPaint.color = org.jetbrains.skia.Color.GREEN


                        val typeFace = Typeface.Companion.makeEmpty()//Typeface.makeFromName("TimesRoman", FontStyle.BOLD)

                        canvas.nativeCanvas.drawTextLine(
                            TextLine.Companion.make("${if (it == 8) maxType else{ (minType+it*valueOfDivision).roundToInt()}}", Font(typeFace, (inputSize.height.value*0.05f))),
                            positionX,
                            positionY,
                            txtPaint
                        )
                    }
                }


            }

            Text("[${type}]", modifier = Modifier.align(Alignment.TopCenter).padding(top = (inputSize.height.value*0.25f).dp)
                //.offset(calcNumGaug(90f,WIDTH).x.dp,calcNumGaug(90f,WIDTH).y.dp)
                , fontFamily = FontFamily.Default, fontSize = (inputSize.height.value*0.075f).sp, fontWeight = FontWeight.Bold, color = Color.White
            )

            Column(modifier = Modifier.height(inputSize.height*0.3f).align(Alignment.BottomStart).padding(start = 10.dp, bottom = 10.dp, top = 5.dp), verticalArrangement = Arrangement.SpaceBetween) {

                Text("${signValue}",
                    modifier = Modifier.padding(0.dp),
                    fontFamily = fontDigital,
                    fontSize = (inputSize.height.value*0.200f).sp, fontWeight = FontWeight.Bold, color = Color.White
                )

            }
            if(rememberShowComm.value) {
                Box(modifier = Modifier.fillMaxSize().background(colorTrans60)) {
                    Text("raw: ${progress} .. ${comment}", modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(10.dp)
                        //.offset(calcNumGaug(90f,WIDTH).x.dp,calcNumGaug(90f,WIDTH).y.dp)
                        , fontFamily = FontFamily.Default, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White
                    )
                }
            }

        }
        Box(modifier = Modifier.fillMaxSize().weight(1f).background(Color.Gray)) {
//                Text(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(horizontal = 4.dp), text = txt,
//                    fontSize = TextUnit(10f, TextUnitType.Sp), color = Color.Black
//                )
            Text("${displayName}", modifier = Modifier.fillMaxSize(), textAlign = TextAlign.Center
                //.offset(calcNumGaug(90f,WIDTH).x.dp,calcNumGaug(90f,WIDTH).y.dp)
                , fontFamily = FontFamily.Monospace, fontSize = (inputSize.height.value*0.075f).sp, fontWeight = FontWeight.Medium, color = Color.White
            )
        }


    }
    //var WIDTH = size
//    Box(modifier = Modifier) {
//
//
//    }
}


fun checkInterval(inputProgress: Int): Int {
    //println(">>> ${inputProgress}")
    if (inputProgress >= 240) {
        return 240
    } else {
        return inputProgress
    }
}

fun checkSignInterval(inp : Float, min: Float, max: Float): Float {

    if (inp > max) {
        return max
    }
    if (inp < min) {
        return min
    }
    return inp

}

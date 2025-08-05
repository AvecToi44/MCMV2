package ru.atrs.mcm//import VariablesUSB.SMOOTH_SHOW
//import VariablesUSB.lastNum2
//import java.util.*
//
//object VariablesUSB {
//
//    var SMOOTH_SHOW = true
//    var acc = 5
//    var howLong = 0.5
//
//    var curPoint = 0.0
//    var lastNum = 0.0
//
//    var curPoint2= 0.0
//    var lastNum2 = 0.0
//
//    var curPoint3= 0.0
//    var lastNum3 = 0.0
//
//    var curPoint4= 0.0
//    var lastNum4 = 0.0
//
//    var curPoint5= 0.0
//    var lastNum5 = 0.0
//
//    var curPoint6= 0.0
//    var lastNum6 = 0.0
//
//    var curPoint7= 0.0
//    var lastNum7 = 0.0
//
//    var curPoint8= 0.0
//    var lastNum8 = 0.0
//}
//
//private fun firstAnalog(inputP : Double){
//    gm1.setSpeed(Date(),inputP)
//    return
//    if (SMOOTH_SHOW){
//
//
//        if (inputP > lastNum){
//
//            curPoint += acc
//
//        }else if ( inputP < lastNum ) {
//
//            curPoint -= acc
//
//        }else { // if equals
//
//        }
//        lastNum = curPoint
//
//        gm1.setSpeed(Date(),curPoint) //
//    }else{
//        gm1.setSpeed(Date(),inputP) //
//    }
//}
//
//private fun secondAnalog(inputP : Double){
//    if (SMOOTH_SHOW){
//
//
//        if (inputP > lastNum2){
//
//            curPoint2 += acc
//
//        }else if ( inputP < lastNum2 ) {
//
//            curPoint2 -= acc
//
//        }else { // if equals
//
//        }
//        lastNum2 = curPoint2
//
//        gm2.setSpeed(Date(),curPoint2) //
//    }else{
//        gm2.setSpeed(Date(),inputP) //
//    }
//}
//
//private fun thirdAnalog(inputP : Double){
//    if (SMOOTH_SHOW){
//
//
//        if (inputP > lastNum3){
//
//            curPoint3 += acc
//
//        }else if ( inputP < lastNum3 ) {
//
//            curPoint3 -= acc
//
//        }else { // if equals
//
//        }
//        lastNum3 = curPoint3
//
//        gm3.setSpeed(Date(),curPoint3) //
//    }else{
//        gm3.setSpeed(Date(),inputP) //
//    }
//}
//
//private fun fourthAnalog(inputP : Double){
//    if (SMOOTH_SHOW){
//
//
//        if (inputP > lastNum4){
//
//            curPoint4 += acc
//
//        }else if ( inputP < lastNum4 ) {
//
//            curPoint4 -= acc
//
//        }else { // if equals
//
//        }
//        lastNum4 = curPoint4
//
//        gm4.setSpeed(Date(),curPoint4) //
//    }else{
//        gm4.setSpeed(Date(),inputP) //
//    }
//}
//
//private fun fiveAnalog(inputP : Double){
//    if (SMOOTH_SHOW){
//
//
//        if (inputP > lastNum5){
//
//            curPoint5 += acc
//
//        }else if ( inputP < lastNum5 ) {
//
//            curPoint5 -= acc
//
//        }else { // if equals
//
//        }
//        lastNum5 = curPoint5
//
//        gm5.setSpeed(Date(),curPoint5) //
//    }else{
//        gm5.setSpeed(Date(),inputP) //
//    }
//}
//
//private fun sixAnalog(inputP : Double){
//    if (SMOOTH_SHOW){
//
//
//        if (inputP > lastNum6){
//
//            curPoint6 += acc
//
//        }else if ( inputP < lastNum6 ) {
//
//            curPoint6 -= acc
//
//        }else { // if equals
//
//        }
//        lastNum6 = curPoint6
//
//        gm6.setSpeed(Date(),curPoint6) //
//    }else{
//        gm6.setSpeed(Date(),inputP) //
//    }
//}
//
//private fun sevenAnalog(inputP : Double){
//    if (SMOOTH_SHOW){
//
//
//        if (inputP > lastNum7){
//
//            curPoint7 += acc
//
//        }else if ( inputP < lastNum7 ) {
//
//            curPoint7 -= acc
//
//        }else { // if equals
//
//        }
//        lastNum7 = curPoint7
//
//        gm7.setSpeed(Date(),curPoint7) //
//    }else{
//        gm7.setSpeed(Date(),inputP) //
//    }
//}
//
//private fun eightAnalog(inputP : Double){
//    if (SMOOTH_SHOW){
//
//
//        if (inputP > lastNum8){
//
//            curPoint8 += acc
//
//        }else if ( inputP < lastNum8 ) {
//
//            curPoint8 -= acc
//
//        }else { // if equals
//
//        }
//        lastNum8 = curPoint8
//
//        gm8.setSpeed(Date(),curPoint8) //
//    }else{
//        gm8.setSpeed(Date(),inputP) //
//    }
//}
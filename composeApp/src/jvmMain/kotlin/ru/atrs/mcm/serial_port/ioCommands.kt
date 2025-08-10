package ru.atrs.mcm.serial_port

import com.fazecast.jSerialComm.SerialPort
import kotlinx.coroutines.*
import ru.atrs.mcm.ui.main_screen.center.support_elements.ch1
import ru.atrs.mcm.ui.main_screen.center.support_elements.ch2
import ru.atrs.mcm.ui.main_screen.center.support_elements.ch3
import ru.atrs.mcm.ui.main_screen.center.support_elements.ch4
import ru.atrs.mcm.ui.main_screen.center.support_elements.ch5
import ru.atrs.mcm.ui.main_screen.center.support_elements.ch6
import ru.atrs.mcm.ui.main_screen.center.support_elements.ch7
import ru.atrs.mcm.ui.main_screen.center.support_elements.ch8
import ru.atrs.mcm.utils.BAUD_RATE
import ru.atrs.mcm.utils.COM_PORT
import ru.atrs.mcm.utils.arrayOfComPorts
import ru.atrs.mcm.utils.checkIntervalScenarios
import ru.atrs.mcm.utils.getComPorts_Array
import ru.atrs.mcm.utils.indexOfScenario
import ru.atrs.mcm.utils.logAct
import ru.atrs.mcm.utils.logGarbage
import ru.atrs.mcm.utils.pwm1SeekBar
import ru.atrs.mcm.utils.pwm2SeekBar
import ru.atrs.mcm.utils.pwm3SeekBar
import ru.atrs.mcm.utils.pwm4SeekBar
import ru.atrs.mcm.utils.pwm5SeekBar
import ru.atrs.mcm.utils.pwm6SeekBar
import ru.atrs.mcm.utils.pwm7SeekBar
import ru.atrs.mcm.utils.pwm8SeekBar
import ru.atrs.mcm.utils.scenario
import ru.atrs.mcm.utils.solenoids
import ru.atrs.mcm.utils.toHexString
import ru.atrs.mcm.utils.txtOfScenario
import java.math.BigInteger

private var serialPort: SerialPort = SerialPort.getCommPort(COM_PORT)
private val crtx2 = CoroutineName("main")

suspend fun initSerialCommunication() {
    println(">>>serial communication has been started, COM_PORT:${COM_PORT} ${BAUD_RATE}")
    serialPort = SerialPort.getCommPort(COM_PORT)
    serialPort.setComPortParameters(BAUD_RATE,8,1, SerialPort.NO_PARITY)
    serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0)
    serialPort.openPort()
    //serialPort.clearBreak()
    arrayOfComPorts = getComPorts_Array() as Array<SerialPort>

    delay(2000)
    println("Run Callbacks::")
    val listener = PacketListener()
    serialPort.addDataListener(listener)
    //showMeSnackBar("baudRate of Port:${speedOfPort.value.text.toInt()} ", Color.White)
}

suspend fun startReceiveFullData() {
//    repeat(
//        getComPorts_Array().size
//    ) {
//        println(">>>Available Com ports:${getComPorts_Array().get(it).systemPortName} is Open: ${getComPorts_Array().get(it).isOpen}||${getComPorts_Array().get(it).descriptivePortName}")
//    }
    if (!serialPort.isOpen) {
        initSerialCommunication()
    }
    writeToSerialPort(byteArrayOf(0x74.toByte(), 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,0x00, 0x00,0x00, 0x00,0x00))

}

fun stopSerialCommunication() {
    serialPort.removeDataListener()
    serialPort.closePort()

    println(">< STOP SERIAL PORT // is Open:${serialPort.isOpen} ${BAUD_RATE}")
}

suspend fun pauseSerialComm() {
    //writeToSerialPort(byteArrayOf(0x71,0x00, 0x00,0x00, 0x00,0x00, 0x00,0x00, 0x00,0x00, 0x00,0x00, 0x00,0x00),false, delay = 0L)
    //writeToSerialPort(byteArrayOf(0x51,0x00, 0x00,0x00, 0x00,0x00, 0x00,0x00, 0x00,0x00, 0x00,0x00, 0x00,0x00),false, delay = 0L)
    sendZerosToSolenoid()

    //writeToSerialPort(byteArrayOf(0x78, 0x8A.toByte(), 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,0x00, 0x00,0x00, 0x00,0x00),withFlush = false)
    //delay(500)
    //serialPort.flushIOBuffers()
    writeToSerialPort(byteArrayOf(0x54, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,0x00, 0x00,0x00, 0x00,0x00),withFlush = false)

}

suspend fun writeToSerialPort(sendBytes: ByteArray, withFlush: Boolean = false, delay: Long = 0L) {
//    if (sendBytes[0] == 0x74.toByte()) {
//        //startTimer()
//    }
    repeat(1) {

        logAct("Run Send bytes:: ${sendBytes.toHexString()}   size of bytes: ${sendBytes.size}")
        serialPort.writeBytes(sendBytes, sendBytes.size.toLong())
        if (withFlush) {
            serialPort.flushIOBuffers()
        }
        delay(delay)
        //println("goo " + sendBytes.size)
    }

}

// increment and decrement steps of scenario
suspend fun comparatorToSolenoid(newIndex: Int) {
    //var btary = muta()
    //val size = scenario[newIndex].values.size

    val idx = checkIntervalScenarios(newIndex)


//    val btry = byteArrayOf(
//        scenario[idx].values[0].toByte(),
//        scenario[idx].values[1].toByte(),
//        scenario[idx].values[2].toByte(),
//        scenario[idx].values[3].toByte(),
//
//        scenario[idx].values[4].toByte(),
//        scenario[idx].values[5].toByte(),
//        scenario[idx].values[6].toByte(),
//        scenario[idx].values[7].toByte(),
//    )

    logGarbage("comparatorToSolenoid ${idx} ~~~ ]${scenario.size}[")


    pwm1SeekBar.value = (scenario.getOrNull(idx)?.let { it.channels[0].takeIf { it <= solenoids[0].maxPWM } }) ?: solenoids[0].maxPWM
    pwm2SeekBar.value = (scenario.getOrNull(idx)?.let { it.channels[1].takeIf { it <= solenoids[1].maxPWM } }) ?: solenoids[1].maxPWM
    pwm3SeekBar.value = (scenario.getOrNull(idx)?.let { it.channels[2].takeIf { it <= solenoids[2].maxPWM } }) ?: solenoids[2].maxPWM
    pwm4SeekBar.value = (scenario.getOrNull(idx)?.let { it.channels[3].takeIf { it <= solenoids[3].maxPWM } }) ?: solenoids[3].maxPWM
    pwm5SeekBar.value = (scenario.getOrNull(idx)?.let { it.channels[4].takeIf { it <= solenoids[4].maxPWM } }) ?: solenoids[4].maxPWM
    pwm6SeekBar.value = (scenario.getOrNull(idx)?.let { it.channels[5].takeIf { it <= solenoids[5].maxPWM } }) ?: solenoids[5].maxPWM
    pwm7SeekBar.value = (scenario.getOrNull(idx)?.let { it.channels[6].takeIf { it <= solenoids[6].maxPWM } }) ?: solenoids[6].maxPWM
    pwm8SeekBar.value = (scenario.getOrNull(idx)?.let { it.channels[7].takeIf { it <= solenoids[7].maxPWM } }) ?: solenoids[7].maxPWM

//    pwm1SeekBar.value = (scenario.getOrNull(idx){})              .values[0].takeIf { it <= solenoids[0].maxPWM } ?: solenoids[0].maxPWM // [from 0 to 255]
//    pwm2SeekBar.value = (scenario.getOrElse(idx){ scenario[0] }) .values[1].takeIf { it <= solenoids[1].maxPWM } ?: solenoids[1].maxPWM
//    pwm3SeekBar.value = (scenario.getOrElse(idx){ scenario[0] }) .values[2].takeIf { it <= solenoids[2].maxPWM } ?: solenoids[2].maxPWM
//    pwm4SeekBar.value = (scenario.getOrElse(idx){ scenario[0] }) .values[3].takeIf { it <= solenoids[3].maxPWM } ?: solenoids[3].maxPWM
//    pwm5SeekBar.value = (scenario.getOrElse(idx){ scenario[0] }) .values[4].takeIf { it <= solenoids[4].maxPWM } ?: solenoids[4].maxPWM
//    pwm6SeekBar.value = (scenario.getOrElse(idx){ scenario[0] }) .values[5].takeIf { it <= solenoids[5].maxPWM } ?: solenoids[5].maxPWM
//    pwm7SeekBar.value = (scenario.getOrElse(idx){ scenario[0] }) .values[6].takeIf { it <= solenoids[6].maxPWM } ?: solenoids[6].maxPWM
//    pwm8SeekBar.value = (scenario.getOrElse(idx){ scenario[0] }) .values[7].takeIf { it <= solenoids[7].maxPWM } ?: solenoids[7].maxPWM

    //logGarbage("pwm1SeekBar -> ${pwm1SeekBar.value}  ${pwm2SeekBar.value}  ${pwm3SeekBar.value}")

    ch1 = pwm1SeekBar.value.toByte() //(rawPreByte0).toByte() // from 0 to 0xFF
    ch2 = pwm2SeekBar.value.toByte() //(rawPreByte1).toByte()
    ch3 = pwm3SeekBar.value.toByte() //(rawPreByte2).toByte()
    ch4 = pwm4SeekBar.value.toByte() //(rawPreByte3).toByte()
    ch5 = pwm5SeekBar.value.toByte() //(rawPreByte4).toByte()
    ch6 = pwm6SeekBar.value.toByte() //(rawPreByte5).toByte()
    ch7 = pwm7SeekBar.value.toByte() //(rawPreByte6).toByte()
    ch8 = pwm8SeekBar.value.toByte() //(rawPreByte7).toByte()

    writeToSerialPort(byteArrayOf(0x71, ch1, 0x00, ch2, 0x00, ch3, 0x00, ch4, 0x00,0x00, 0x00,0x00, 0x00,0x00),delay = 100L)

    writeToSerialPort(byteArrayOf(0x51, ch5, 0x00, ch6, 0x00, ch7, 0x00, ch8, 0x00,0x00, 0x00,0x00, 0x00,0x00),delay = 0L)

//    pwm1SeekBar.value = map( rawPreByte0,0,255,0,100 )
//    pwm2SeekBar.value = map( rawPreByte1,0,255,0,100 )
//    pwm3SeekBar.value = map( rawPreByte2,0,255,0,100 )
//    pwm4SeekBar.value = map( rawPreByte3,0,255,0,100 )
//    pwm5SeekBar.value = map( rawPreByte4,0,255,0,100 )
//    pwm6SeekBar.value = map( rawPreByte5,0,255,0,100 )
//    pwm7SeekBar.value = map( rawPreByte6,0,255,0,100 )
//    pwm8SeekBar.value = map( rawPreByte7,0,255,0,100 )


//    repeat(size-1) {
//        btry += byteArrayOf(scenario[newIndex].values[it].toByte())
//    }

    //writeToSerialPort(btry)
    //txtOfScenario.value = scenario[idx].text
    txtOfScenario.value = scenario.getOrElse(idx){
        indexOfScenario.value = 0
        scenario[0]
    }.comment

    //commentOfScenario.value = scenario[idx].comment
}

suspend fun sendZerosToSolenoid() {
    ch1 = 0x00.toByte()
    ch2 = 0x00.toByte()
    ch3 = 0x00.toByte()
    ch4 = 0x00.toByte()
    ch5 = 0x00.toByte()
    ch6 = 0x00.toByte()
    ch7 = 0x00.toByte()
    ch8 = 0x00.toByte()

    writeToSerialPort(byteArrayOf(0x71, ch1, 0x00, ch2, 0x00, ch3, 0x00, ch4, 0x00,0x00, 0x00,0x00, 0x00,0x00), delay = 100L)

    writeToSerialPort(byteArrayOf(0x51, ch5, 0x00, ch6, 0x00, ch7, 0x00, ch8, 0x00,0x00, 0x00,0x00, 0x00,0x00),delay = 0L)

}

suspend fun sendScenarioToController() {
    scenario.forEachIndexed { index, s ->
        val time = BigInteger.valueOf(s.time.toLong()).toByteArray()

        val send = byteArrayOf(
            0x73,index.toByte(),0x00,

            s.channels[0].toByte(),
            s.channels[1].toByte(),
            s.channels[2].toByte(),
            s.channels[3].toByte(),

            s.channels[4].toByte(),
            s.channels[5].toByte(),
            s.channels[6].toByte(),
            s.channels[7].toByte(),

            //time.getOrNull(1).takeIf { time.size == 2 } ?: 0x00,
            time.getOrNull(1) ?: 0x00,
            time[0],

            0x00
        )
        writeToSerialPort(send,delay = 10)
    }
}

fun sendFrequency() {
    var arrSend = arrayListOf<Frequence>()
    solenoids.forEachIndexed { index, solenoidHolder ->
        val mkrs = 1_000_000 / solenoidHolder.frequency

        val time = BigInteger.valueOf(mkrs.toLong()).toByteArray()
        arrSend.add(Frequence(units = time[0], dozens = time[1]))
    }


    CoroutineScope(Dispatchers.IO).launch {
        writeToSerialPort(byteArrayOf(
            0x68,
            arrSend[0].units,arrSend[0].dozens,
            arrSend[1].units,arrSend[1].dozens,
            arrSend[2].units,arrSend[2].dozens,
            arrSend[3].units,arrSend[3].dozens,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
        ))
        writeToSerialPort(byteArrayOf(
            0x48,
            arrSend[4].units,arrSend[4].dozens,
            arrSend[5].units,arrSend[5].dozens,
            arrSend[6].units,arrSend[6].dozens,
            arrSend[7].units,arrSend[7].dozens,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
        ))
    }

}

data class Frequence(val units: Byte, val dozens: Byte)
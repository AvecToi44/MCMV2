package ru.atrs.mcm.serial_port

import com.fazecast.jSerialComm.SerialPort
import kotlinx.coroutines.*
import ru.atrs.mcm.enums.StateExperiments
import ru.atrs.mcm.ui.main_screen.center.support_elements.ch1
import ru.atrs.mcm.ui.main_screen.center.support_elements.ch10
import ru.atrs.mcm.ui.main_screen.center.support_elements.ch11
import ru.atrs.mcm.ui.main_screen.center.support_elements.ch12
import ru.atrs.mcm.ui.main_screen.center.support_elements.ch2
import ru.atrs.mcm.ui.main_screen.center.support_elements.ch3
import ru.atrs.mcm.ui.main_screen.center.support_elements.ch4
import ru.atrs.mcm.ui.main_screen.center.support_elements.ch5
import ru.atrs.mcm.ui.main_screen.center.support_elements.ch6
import ru.atrs.mcm.ui.main_screen.center.support_elements.ch7
import ru.atrs.mcm.ui.main_screen.center.support_elements.ch8
import ru.atrs.mcm.ui.main_screen.center.support_elements.ch9
import ru.atrs.mcm.utils.BAUD_RATE
import ru.atrs.mcm.utils.COM_PORT
import ru.atrs.mcm.utils.SOLENOID_MAIN_FREQ
import ru.atrs.mcm.utils.STATE_EXPERIMENT
import ru.atrs.mcm.utils.TWELVE_CHANNELS_MODE
import ru.atrs.mcm.utils.arrayOfComPorts
import ru.atrs.mcm.utils.checkIntervalScenarios
import ru.atrs.mcm.utils.getComPorts_Array
import ru.atrs.mcm.utils.indexOfScenario
import ru.atrs.mcm.utils.logAct
import ru.atrs.mcm.utils.logError
import ru.atrs.mcm.utils.logGarbage
import ru.atrs.mcm.utils.pwm10SeekBar
import ru.atrs.mcm.utils.pwm11SeekBar
import ru.atrs.mcm.utils.pwm12SeekBar
import ru.atrs.mcm.utils.pwm1SeekBar
import ru.atrs.mcm.utils.pwm2SeekBar
import ru.atrs.mcm.utils.pwm3SeekBar
import ru.atrs.mcm.utils.pwm4SeekBar
import ru.atrs.mcm.utils.pwm5SeekBar
import ru.atrs.mcm.utils.pwm6SeekBar
import ru.atrs.mcm.utils.pwm7SeekBar
import ru.atrs.mcm.utils.pwm8SeekBar
import ru.atrs.mcm.utils.pwm9SeekBar
import ru.atrs.mcm.utils.scenario
import ru.atrs.mcm.utils.solenoids
import ru.atrs.mcm.utils.to2ByteArray
import ru.atrs.mcm.utils.toHexString
import ru.atrs.mcm.utils.txtOfScenario
import java.math.BigInteger


/**
 * OLD PROTOCOL
  */
object CommunicationMachineV1: COMProtocol {
    private var serialPort: SerialPort = SerialPort.getCommPort(COM_PORT)
    private val crtx2 = CoroutineName("main")

    override suspend fun initSerialCommunication() {
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
    override fun stopSerialCommunication() {
        serialPort.removeDataListener()
        serialPort.closePort()

        println(">< STOP SERIAL PORT // is Open:${serialPort.isOpen} ${BAUD_RATE}")
    }

    override suspend fun startReceiveFullData() {
        if (!serialPort.isOpen) {
            initSerialCommunication()
        } else {
            logError("!!! Port (${serialPort.systemPortName}) was not opened !!!")
        }

        if (TWELVE_CHANNELS_MODE) {

        } else {
            writeToSerialPort(byteArrayOf(0x74.toByte(), 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,0x00, 0x00,0x00, 0x00,0x00))
        }
    }



    override suspend fun pauseSerialComm() {
        sendZerosToSolenoid()

        if (TWELVE_CHANNELS_MODE) {

        } else {
            writeToSerialPort(byteArrayOf(0x54, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,0x00, 0x00,0x00, 0x00,0x00),withFlush = false)
        }
    }

    override suspend fun writeToSerialPort(sendBytes: ByteArray, withFlush: Boolean, delay: Long) {
        if (!serialPort.isOpen) {
            logError("Trying to writeToSerialPort: ${sendBytes.toHexString()}")
            return
        }
        repeat(1) {

            logAct("Run Send bytes:: ${sendBytes.toHexString()}   size of bytes: ${sendBytes.size}. delay ${delay} withFlush ${withFlush}}")
            serialPort.writeBytes(sendBytes, sendBytes.size.toLong())
            if (withFlush) {
                serialPort.flushIOBuffers()
            }
            delay(delay)
        }

    }

    // increment and decrement steps of scenario
    override suspend fun comparatorToSolenoid(newIndex: Int) {
        if (TWELVE_CHANNELS_MODE) {

        } else {
            val idx = checkIntervalScenarios(newIndex)

            logGarbage("comparatorToSolenoid ${idx} ~~~ ]${scenario.size}[")

            pwm1SeekBar.value = (scenario.getOrNull(idx)?.let { it.channels[0].takeIf { it <= solenoids[0].maxPWM } }) ?: solenoids[0].maxPWM
            pwm2SeekBar.value = (scenario.getOrNull(idx)?.let { it.channels[1].takeIf { it <= solenoids[1].maxPWM } }) ?: solenoids[1].maxPWM
            pwm3SeekBar.value = (scenario.getOrNull(idx)?.let { it.channels[2].takeIf { it <= solenoids[2].maxPWM } }) ?: solenoids[2].maxPWM
            pwm4SeekBar.value = (scenario.getOrNull(idx)?.let { it.channels[3].takeIf { it <= solenoids[3].maxPWM } }) ?: solenoids[3].maxPWM

            pwm5SeekBar.value = (scenario.getOrNull(idx)?.let { it.channels[4].takeIf { it <= solenoids[4].maxPWM } }) ?: solenoids[4].maxPWM
            pwm6SeekBar.value = (scenario.getOrNull(idx)?.let { it.channels[5].takeIf { it <= solenoids[5].maxPWM } }) ?: solenoids[5].maxPWM
            pwm7SeekBar.value = (scenario.getOrNull(idx)?.let { it.channels[6].takeIf { it <= solenoids[6].maxPWM } }) ?: solenoids[6].maxPWM
            pwm8SeekBar.value = (scenario.getOrNull(idx)?.let { it.channels[7].takeIf { it <= solenoids[7].maxPWM } }) ?: solenoids[7].maxPWM

            pwm9SeekBar.value  = (scenario.getOrNull(idx)?.let { it.channels[8].takeIf { it <= solenoids[8].maxPWM } }) ?: solenoids[8].maxPWM
            pwm10SeekBar.value = (scenario.getOrNull(idx)?.let { it.channels[9].takeIf { it <= solenoids[9].maxPWM } }) ?: solenoids[9].maxPWM
            pwm11SeekBar.value = (scenario.getOrNull(idx)?.let { it.channels[10].takeIf { it <= solenoids[10].maxPWM } }) ?: solenoids[10].maxPWM
            pwm12SeekBar.value = (scenario.getOrNull(idx)?.let { it.channels[11].takeIf { it <= solenoids[11].maxPWM } }) ?: solenoids[11].maxPWM

            //logGarbage("pwm1SeekBar -> ${pwm1SeekBar.value}  ${pwm2SeekBar.value}  ${pwm3SeekBar.value}")

            ch1 = pwm1SeekBar.value.toByte() //(rawPreByte0).toByte() // from 0 to 0xFF
            ch2 = pwm2SeekBar.value.toByte() //(rawPreByte1).toByte()
            ch3 = pwm3SeekBar.value.toByte() //(rawPreByte2).toByte()
            ch4 = pwm4SeekBar.value.toByte() //(rawPreByte3).toByte()
            ch5 = pwm5SeekBar.value.toByte() //(rawPreByte4).toByte()
            ch6 = pwm6SeekBar.value.toByte() //(rawPreByte5).toByte()
            ch7 = pwm7SeekBar.value.toByte() //(rawPreByte6).toByte()
            ch8 = pwm8SeekBar.value.toByte() //(rawPreByte7).toByte()

            ch9 =   pwm9SeekBar.value.toByte() //(rawPreByte7).toByte()
            ch10 = pwm10SeekBar.value.toByte() //(rawPreByte7).toByte()
            ch11 = pwm11SeekBar.value.toByte() //(rawPreByte7).toByte()
            ch12 = pwm12SeekBar.value.toByte() //(rawPreByte7).toByte()

            writeToSerialPort(byteArrayOf(0x71, ch1, 0x00, ch2, 0x00, ch3, 0x00, ch4, 0x00,0x00, 0x00,0x00, 0x00,0x00),delay = 100L)

            writeToSerialPort(byteArrayOf(0x51, ch5, 0x00, ch6, 0x00, ch7, 0x00, ch8, 0x00,0x00, 0x00,0x00, 0x00,0x00),delay = 0L)

            txtOfScenario.value = scenario.getOrElse(idx){
                indexOfScenario.value = 0
                scenario[0]
            }.comment
        }
    }

    override suspend fun sendZerosToSolenoid() {
        ch1 = 0x00.toByte()
        ch2 = 0x00.toByte()
        ch3 = 0x00.toByte()
        ch4 = 0x00.toByte()
        ch5 = 0x00.toByte()
        ch6 = 0x00.toByte()
        ch7 = 0x00.toByte()
        ch8 = 0x00.toByte()
        ch9 = 0x00.toByte()
        ch10 = 0x00.toByte()
        ch11 = 0x00.toByte()
        ch12 = 0x00.toByte()

        if (TWELVE_CHANNELS_MODE) {

        } else {
            writeToSerialPort(byteArrayOf(0x71, ch1, 0x00, ch2, 0x00, ch3, 0x00, ch4, 0x00,0x00, 0x00,0x00, 0x00,0x00), delay = 100L)

            writeToSerialPort(byteArrayOf(0x51, ch5, 0x00, ch6, 0x00, ch7, 0x00, ch8, 0x00,0x00, 0x00,0x00, 0x00,0x00),delay = 0L)
        }


    }

    override suspend fun sendScenarioToController() {
        if (TWELVE_CHANNELS_MODE) {

        } else {
            STATE_EXPERIMENT.value = StateExperiments.SENDING_SCENARIO
            scenario.forEachIndexed { index, s ->
                val time = s.time.to2ByteArray()
//                val time = BigInteger.valueOf(s.time.toLong()).toByteArray()
                val indexHex = index.to2ByteArray()

                val send = byteArrayOf(
                    0x73, // in string is 115
                    indexHex[0],// ones
                    indexHex[1], // tens

                    s.channels[0].toByte(),
                    s.channels[1].toByte(),
                    s.channels[2].toByte(),
                    s.channels[3].toByte(),

                    s.channels[4].toByte(),
                    s.channels[5].toByte(),
                    s.channels[6].toByte(),
                    s.channels[7].toByte(), // # 10

                    //time.getOrNull(1).takeIf { time.size == 2 } ?: 0x00,
                    time[0], // ones
                    time.getOrNull(1) ?: 0x00, // tens
                    0x00
                )

                writeToSerialPort(send,delay = 4)
                STATE_EXPERIMENT.value = StateExperiments.SENDING_SCENARIO//.also { it.msg = "${((index+1)*100)/scenario.size}" }

                if (scenario.lastIndex == index) {
                    STATE_EXPERIMENT.value = StateExperiments.NONE
                }
            }
        }
    }

    override suspend fun reInitSolenoids() {
        if (TWELVE_CHANNELS_MODE) {

        } else {
            comparatorToSolenoid(indexOfScenario.value)

            writeToSerialPort(
                byteArrayOf(0x71, ch1, 0x00, ch2, 0x00, ch3, 0x00, ch4, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00),
                delay = 100L
            )

            writeToSerialPort(
                byteArrayOf(0x51, ch5, 0x00, ch6, 0x00, ch7, 0x00, ch8, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00),
                delay = 0L
            )
        }
    }

    override fun sendFrequency() {

//        var arrSend = arrayListOf<Frequence>()
//        solenoids.forEachIndexed { index, solenoidHolder ->
//            val mkrs = 1_000_000 / solenoidHolder.ditherFrequency
//
//            val time = BigInteger.valueOf(mkrs.toLong()).toByteArray()
//            arrSend.add(Frequence(units = time[0], dozens = time[1]))
//        }

//        var array1 = solenoids[0].ditherFrequency.to2ByteArray()
//        var array2 = solenoids[1].ditherFrequency.to2ByteArray()
//        var array3 = solenoids[2].ditherFrequency.to2ByteArray()
//        var array4 = solenoids[3].ditherFrequency.to2ByteArray()
//        var array5 = solenoids[4].ditherFrequency.to2ByteArray()
//        var array6 = solenoids[5].ditherFrequency.to2ByteArray()
//        var array7 = solenoids[6].ditherFrequency.to2ByteArray()
//        var array8 = solenoids[7].ditherFrequency.to2ByteArray()
        if (SOLENOID_MAIN_FREQ == null) {
            logError("NULL Main Frequency! Double check it")
        }

        var mainFreq = SOLENOID_MAIN_FREQ?.to2ByteArray() ?: byteArrayOf(0,0)


        CoroutineScope(Dispatchers.IO).launch {
            writeToSerialPort(byteArrayOf(
                0x68,
                // ones , tens
                mainFreq[0],mainFreq[1],
                0x00,0x00,
                0x00,0x00,
                0x00,0x00,
                0x00,0x00,
                0x00,0x00,
                0x00,
            ))
//            writeToSerialPort(byteArrayOf(
//                0x68,
//                // tens   ; ones
//                array1[0],array1[1],
//                array2[0],array2[1],
//                array3[0],array3[1],
//                array4[0],array4[1],
//                array5[0],array5[1],
//                array6[0],array6[1],
//                array7[0],array7[1],
//                array8[0],array8[1],
//                0x00,
//            ))
//            writeToSerialPort(byteArrayOf(
//                0x48,
//                arrSend[4].units,arrSend[4].dozens,
//                arrSend[5].units,arrSend[5].dozens,
//                arrSend[6].units,arrSend[6].dozens,
//                arrSend[7].units,arrSend[7].dozens,
//                0x00,
//                0x00,
//                0x00,
//                0x00,
//                0x00,
//            ))
        }

    }

    data class Frequence(val units: Byte, val dozens: Byte)


    override suspend fun solenoidControl(isChangedFirstFourthInternal: Boolean) {
        if (TWELVE_CHANNELS_MODE) {

        } else {
            if (isChangedFirstFourthInternal) {
                writeToSerialPort(byteArrayOf(0x71,ch1, 0x00,ch2, 0x00,ch3, 0x00,ch4, 0x00,0x00, 0x00,0x00, 0x00,0x00),false, delay = 100L)
            }else {
                writeToSerialPort(byteArrayOf(0x51,ch5, 0x00,ch6, 0x00,ch7, 0x00,ch8, 0x00,0x00, 0x00,0x00, 0x00,0x00),false, delay = 0L)
            }
            delay(100)
        }
    }
}

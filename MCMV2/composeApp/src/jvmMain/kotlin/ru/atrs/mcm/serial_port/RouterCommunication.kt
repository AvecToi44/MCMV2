package ru.atrs.mcm.serial_port

import com.fazecast.jSerialComm.SerialPort
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.delay
import ru.atrs.mcm.utils.BAUD_RATE
import ru.atrs.mcm.utils.COM_PORT
import ru.atrs.mcm.utils.PROTOCOL_TYPE
import ru.atrs.mcm.utils.ProtocolType
import ru.atrs.mcm.utils.arrayOfComPorts
import ru.atrs.mcm.utils.getComPorts_Array


object RouterCommunication: COMProtocol {
    private var serialPort: SerialPort = SerialPort.getCommPort(COM_PORT)
    private val crtx2 = CoroutineName("main")

    override suspend fun initSerialCommunication() {
        println(">>>serial communication has been started, COM_PORT:${COM_PORT} ${BAUD_RATE}, Protocol: ${PROTOCOL_TYPE.name}")
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


    fun cleanCOMPort() {
        serialPort.flushIOBuffers()
    }
    override suspend fun startReceiveFullData() {
        if (PROTOCOL_TYPE == ProtocolType.OLD_AUG_2025) {
            CommunicationMachineV1.startReceiveFullData()
        } else {

        }
    }


    override suspend fun pauseSerialComm() {
        if (PROTOCOL_TYPE == ProtocolType.OLD_AUG_2025) {
            CommunicationMachineV1.pauseSerialComm()
        } else {

        }
    }

    override suspend fun writeToSerialPort(sendBytes: ByteArray, withFlush: Boolean, delay: Long) {
        println("ROUTER writeToSerialPort")
        if (PROTOCOL_TYPE == ProtocolType.OLD_AUG_2025) {
            CommunicationMachineV1.writeToSerialPort(sendBytes, withFlush, delay)
        } else {

        }
    }

    override suspend fun comparatorToSolenoid(newIndex: Int) {
        if (PROTOCOL_TYPE == ProtocolType.OLD_AUG_2025) {
            CommunicationMachineV1.comparatorToSolenoid(newIndex)
        } else {

        }
    }

    override suspend fun sendZerosToSolenoid() {
        if (PROTOCOL_TYPE == ProtocolType.OLD_AUG_2025) {
            CommunicationMachineV1.sendZerosToSolenoid()
        } else {

        }
    }

    override suspend fun sendScenarioToController() {
        println("ROUTER sendScenarioToController")
        if (PROTOCOL_TYPE == ProtocolType.OLD_AUG_2025) {
            CommunicationMachineV1.sendScenarioToController()
        } else {

        }
    }

    override suspend fun reInitSolenoids() {
        if (PROTOCOL_TYPE == ProtocolType.OLD_AUG_2025) {
            CommunicationMachineV1.reInitSolenoids()
        } else {

        }
    }

    override fun sendFrequency() {
        if (PROTOCOL_TYPE == ProtocolType.OLD_AUG_2025) {
            CommunicationMachineV1.sendFrequency()
        } else {

        }
    }

    override suspend fun solenoidControl(isChangedFirstFourthInternal: Boolean) {
        if (PROTOCOL_TYPE == ProtocolType.OLD_AUG_2025) {
            CommunicationMachineV1.solenoidControl(isChangedFirstFourthInternal)
        } else {

        }
    }




}
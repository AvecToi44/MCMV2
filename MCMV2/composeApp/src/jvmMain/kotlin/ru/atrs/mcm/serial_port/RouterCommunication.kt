package ru.atrs.mcm.serial_port

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.atrs.mcm.utils.PROTOCOL_TYPE
import ru.atrs.mcm.utils.ProtocolType


object RouterCommunication {

    fun getCOMPortInfo(): String {
        return if (PROTOCOL_TYPE == ProtocolType.OLD_AUG_2025) {
            CommMachineV1.getCOMPortInfo()
        } else {
            CommMachineV2.getCOMPortInfo()
        }
    }
    fun stopSerialCommunication() {
        CoroutineScope(Dispatchers.IO).launch {
            println("Router stopSerialCommunication")
            if (PROTOCOL_TYPE == ProtocolType.OLD_AUG_2025) {
                CommMachineV1.resetSerialComm()
                delay(1000)
                CommMachineV1.stopSerialCommunication()
            } else {
                CommMachineV2.resetSerialComm()
                delay(1000)
                CommMachineV2.stopSerialCommunication()
            }
        }
    }


    fun cleanCOMPort() {
        if (PROTOCOL_TYPE == ProtocolType.OLD_AUG_2025) {
            CommMachineV1.cleanCOMPort()
        } else {
            CommMachineV2.cleanCOMPort()
        }

    }
    suspend fun startReceiveFullData() {
        if (PROTOCOL_TYPE == ProtocolType.OLD_AUG_2025) {
            CommMachineV1.startReceiveFullData()
        } else {
            CommMachineV2.startReceiveFullData()
        }
    }


//    suspend fun resetSerialComm() {
//        if (PROTOCOL_TYPE == ProtocolType.OLD_AUG_2025) {
//            CommMachineV1.resetSerialComm()
//        } else {
//            CommMachineV2.resetSerialComm()
//        }
//    }

    suspend fun writeToSerialPort(sendBytes: ByteArray, withFlush: Boolean, delay: Long = 0L) {
        println("ROUTER writeToSerialPort")
        if (PROTOCOL_TYPE == ProtocolType.OLD_AUG_2025) {
            CommMachineV1.writeToSerialPort(sendBytes, withFlush, delay)
        } else {
            CommMachineV2.writeToSerialPort(sendBytes, withFlush, delay)
        }
    }

    suspend fun comparatorToSolenoid(newIndex: Int) {
        if (PROTOCOL_TYPE == ProtocolType.OLD_AUG_2025) {
            CommMachineV1.comparatorToSolenoid(newIndex)
        } else {
            CommMachineV2.comparatorToSolenoid(newIndex)
        }
    }

//    suspend fun sendZerosToSolenoid() {
//        if (PROTOCOL_TYPE == ProtocolType.OLD_AUG_2025) {
//            CommMachineV1.sendZerosToSolenoid()
//        } else {
//            CommMachineV2.sendZerosToSolenoid()
//        }
//    }

    suspend fun sendScenarioToController() {
        println("ROUTER sendScenarioToController")
        if (PROTOCOL_TYPE == ProtocolType.OLD_AUG_2025) {
            CommMachineV1.sendScenarioToController()
        } else {
            CommMachineV2.sendScenarioToController()
        }
    }

    suspend fun reInitSolenoids() {
        if (PROTOCOL_TYPE == ProtocolType.OLD_AUG_2025) {
            CommMachineV1.reInitSolenoids()
        } else {
            CommMachineV2.reInitSolenoids()
        }
    }

    fun sendFrequency() {
        if (PROTOCOL_TYPE == ProtocolType.OLD_AUG_2025) {
            CommMachineV1.sendFrequency()
        } else {
            CommMachineV2.sendFrequency()
        }
    }

    suspend fun solenoidControl(isChangedFirstFourthInternal: Boolean) {
        if (PROTOCOL_TYPE == ProtocolType.OLD_AUG_2025) {
            CommMachineV1.solenoidControl(isChangedFirstFourthInternal)
        } else {
            CommMachineV2.solenoidControl(isChangedFirstFourthInternal)
        }
    }
}

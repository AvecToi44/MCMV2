package ru.atrs.mcm.serial_port

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.atrs.mcm.utils.PROTOCOL_TYPE
import ru.atrs.mcm.utils.ProtocolType
import ru.atrs.mcm.utils.healthCheck
import ru.atrs.mcm.utils.allowManipulationWithUI


object RouterCommunication {

    private fun enforceV2() {
        if (PROTOCOL_TYPE != ProtocolType.NEW) {
            PROTOCOL_TYPE = ProtocolType.NEW
        }
    }

    fun getCOMPortInfo(): String {
        enforceV2()
        return CommMachineV2.getCOMPortInfo()
    }
    fun stopSerialCommunication() {
        enforceV2()
        allowManipulationWithUI.value = false
        CoroutineScope(Dispatchers.IO).launch {
            println("Router stopSerialCommunication")
            CommMachineV2.resetSerialComm()
            delay(1000)
            CommMachineV2.stopSerialCommunication()
            delay(1000)
            healthCheck()
            allowManipulationWithUI.value = true
        }
    }


    fun cleanCOMPort() {
        enforceV2()
        CommMachineV2.cleanCOMPort()

    }
    suspend fun startReceiveFullData() {
        enforceV2()
        CommMachineV2.startReceiveFullData()
    }


//    suspend fun resetSerialComm() {
//        if (PROTOCOL_TYPE == ProtocolType.OLD_AUG_2025) {
//            CommMachineV1.resetSerialComm()
//        } else {
//            CommMachineV2.resetSerialComm()
//        }
//    }

    suspend fun writeToSerialPort(sendBytes: ByteArray, withFlush: Boolean, delay: Long = 0L) {
        enforceV2()
        println("ROUTER writeToSerialPort")
        CommMachineV2.writeToSerialPort(sendBytes, withFlush, delay)
    }

    suspend fun comparatorToSolenoid(newIndex: Int) {
        enforceV2()
        CommMachineV2.comparatorToSolenoid(newIndex)
    }

//    suspend fun sendZerosToSolenoid() {
//        if (PROTOCOL_TYPE == ProtocolType.OLD_AUG_2025) {
//            CommMachineV1.sendZerosToSolenoid()
//        } else {
//            CommMachineV2.sendZerosToSolenoid()
//        }
//    }

    suspend fun sendScenarioToController() {
        enforceV2()
        allowManipulationWithUI.value = false
        println("ROUTER sendScenarioToController")
        CommMachineV2.sendScenarioToController()
        allowManipulationWithUI.value = true
    }

    suspend fun reInitSolenoids() {
        enforceV2()
        CommMachineV2.reInitSolenoids()
    }

    fun sendFrequency() {
        enforceV2()
        CommMachineV2.sendFrequency()
    }

    suspend fun solenoidControl(isChangedFirstFourthInternal: Boolean) {
        enforceV2()
        CommMachineV2.solenoidControl(isChangedFirstFourthInternal)
    }
}

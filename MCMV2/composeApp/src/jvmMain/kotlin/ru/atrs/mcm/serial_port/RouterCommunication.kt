package ru.atrs.mcm.serial_port

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.atrs.mcm.utils.healthCheck
import ru.atrs.mcm.utils.allowManipulationWithUI


object RouterCommunication {

    fun getCOMPortInfo(): String {
        return CommMachineV2.getCOMPortInfo()
    }
    fun stopSerialCommunication() {
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
        CommMachineV2.cleanCOMPort()

    }
    suspend fun startReceiveFullData() {
        CommMachineV2.startReceiveFullData()
    }

    suspend fun writeToSerialPort(sendBytes: ByteArray, withFlush: Boolean, delay: Long = 0L) {
        println("ROUTER writeToSerialPort")
        CommMachineV2.writeToSerialPort(sendBytes, withFlush, delay)
    }

    suspend fun comparatorToSolenoid(newIndex: Int) {
        CommMachineV2.comparatorToSolenoid(newIndex)
    }

    suspend fun sendScenarioToController() {
        allowManipulationWithUI.value = false
        println("ROUTER sendScenarioToController")
        CommMachineV2.sendScenarioToController()
        allowManipulationWithUI.value = true
    }

    suspend fun reInitSolenoids() {
        CommMachineV2.reInitSolenoids()
    }

    fun sendFrequency() {
        CommMachineV2.sendFrequency()
    }

    suspend fun solenoidControl(isChangedFirstFourthInternal: Boolean) {
        CommMachineV2.solenoidControl(isChangedFirstFourthInternal)
    }
}

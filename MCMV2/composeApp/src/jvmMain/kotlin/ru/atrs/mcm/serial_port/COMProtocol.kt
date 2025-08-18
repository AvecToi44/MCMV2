package ru.atrs.mcm.serial_port

interface COMProtocol {

    suspend fun startReceiveFullData()
    suspend fun initSerialCommunication()
    suspend fun resetSerialComm()
    suspend fun writeToSerialPort(sendBytes: ByteArray, withFlush: Boolean = false, delay: Long = 0L)
    suspend fun comparatorToSolenoid(newIndex: Int)
    suspend fun sendZerosToSolenoid()
    suspend fun sendScenarioToController()
    suspend fun reInitSolenoids()
    fun sendFrequency()
    suspend fun solenoidControl(isChangedFirstFourthInternal: Boolean)
    fun stopSerialCommunication()
    fun cleanCOMPort()
    fun getCOMPortInfo(): String
}
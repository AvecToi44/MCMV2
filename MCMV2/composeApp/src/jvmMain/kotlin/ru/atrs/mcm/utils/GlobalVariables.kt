package ru.atrs.mcm.utils

import androidx.compose.runtime.mutableStateOf
import com.fazecast.jSerialComm.SerialPort
import ru.atrs.mcm.enums.ExplorerMode
import ru.atrs.mcm.enums.StateExperiments
import ru.atrs.mcm.enums.StateParseBytes
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import ru.atrs.mcm.parsing_excel.models.PressuresHolder
import ru.atrs.mcm.parsing_excel.models.ScenarioStep
import ru.atrs.mcm.parsing_excel.models.SolenoidHolder
import ru.atrs.mcm.ui.charts.Pointer
import java.io.File
import javax.swing.JFileChooser



var DELAY_FOR_GET_DATA = 0L
var arrayOfComPorts = arrayOf<SerialPort>()

val MAINFOLDER = "mcm"

val Dir0Configs_Analysis = File("${JFileChooser().fileSystemView.defaultDirectory.toString()}\\${MAINFOLDER}\\config","inner_marker.wav")
val Dir0Configs_End = File("${JFileChooser().fileSystemView.defaultDirectory.toString()}\\${MAINFOLDER}\\config","auto_click.wav")
val Dir0Configs_Run = File("${JFileChooser().fileSystemView.defaultDirectory.toString()}\\${MAINFOLDER}\\config","run_vine.wav")
val Dir0Configs_Error = File("${JFileChooser().fileSystemView.defaultDirectory.toString()}\\${MAINFOLDER}\\config","tesla_err.wav")


val Dir1Configs = File("${JFileChooser().fileSystemView.defaultDirectory.toString()}\\${MAINFOLDER}\\config")
val Dir2Reports = File("${JFileChooser().fileSystemView.defaultDirectory.toString()}\\${MAINFOLDER}\\reports")
val Dir3Scenarios = File("${JFileChooser().fileSystemView.defaultDirectory.toString()}\\${MAINFOLDER}\\scenarios")
val Dir4MainConfig_Json = File(Dir1Configs,"\\config.json")
val Dir4MainConfig_Txt = File(Dir1Configs,"\\config.txt")
val Dir4MainConfig_Log by lazy { File(Dir1Configs,"\\log${generateTimestampLastUpdate()}.txt") }
val Dir5Operators = File(Dir1Configs,"\\operator_ids.txt")

val Dir6 = File(Dir2Reports,"\\demo.txt")
val Dir7ReportsStandard = File("${JFileChooser().fileSystemView.defaultDirectory.toString()}\\${MAINFOLDER}\\reports\\standard")
val Dir8 = File(Dir7ReportsStandard,"\\stndrd.txt")
val Dir9Scenario = File(Dir3Scenarios,"scenario_demo.xls")

var Dir_10_ScenarioForChart = File(Dir3Scenarios,"scenario_demo.xls")
var Dir11ForTargetingSaveNewExperiment = Dir2Reports
var NAME_OF_NEW_EXPERIMENT = "No Name"

var COM_PORT = "COM0"
var BAUD_RATE = 500000
var OPERATOR_ID = "no name"
var SOUND_ENABLED = 1
var LAST_SCENARIO = Dir9Scenario
var DELAY_BEFORE_CHART = 2000
var LOG_LEVEL : LogLevel= LogLevel.DEBUG // from 0 NO logs, and 2 is super detailed logs
enum class LogLevel { ERRORS, DEBUG }
var SHOW_FULLSCREEN = false
var SHOW_BOTTOM_PANEL = true
const val APP_VERSION = "1.2.13"

var solenoids = mutableListOf<SolenoidHolder>()
var pressures = mutableListOf<PressuresHolder>()
var scenario  = mutableListOf<ScenarioStep>()

var GLOBAL_STATE = mutableStateOf(StateParseBytes.INIT)
var STATE_EXPERIMENT = mutableStateOf(StateExperiments.NONE)
var EXPLORER_MODE = mutableStateOf(ExplorerMode.AUTO)


var dataChunkGauges   = MutableSharedFlow<DataChunkG>(replay = 0, extraBufferCapacity = 1000, onBufferOverflow = BufferOverflow.SUSPEND)
var dataChunkCurrents = MutableSharedFlow<DataChunkCurrent>(replay = 0, extraBufferCapacity = 1000, onBufferOverflow = BufferOverflow.SUSPEND)

val PRESSURE_MAX_RAW = 4095
val CURRENT_MAX_RAW = 255

var TWELVE_CHANNELS_MODE = false
////////////////////////////////////////////////////
var arr1Measure = arrayListOf<Pointer>()
var arr2Measure = arrayListOf<Pointer>()
var arr3Measure = arrayListOf<Pointer>()
var arr4Measure = arrayListOf<Pointer>()
var arr5Measure = arrayListOf<Pointer>()
var arr6Measure = arrayListOf<Pointer>()
var arr7Measure = arrayListOf<Pointer>()
var arr8Measure = arrayListOf<Pointer>()

var arr9Measure = arrayListOf<Pointer>()
var arr10Measure = arrayListOf<Pointer>()
var arr11Measure = arrayListOf<Pointer>()
var arr12Measure = arrayListOf<Pointer>()
////////////////////////////////////////////////////
var pwm1SeekBar = mutableStateOf<Int>(-1)
var pwm2SeekBar = mutableStateOf<Int>(-1)
var pwm3SeekBar = mutableStateOf<Int>(-1)
var pwm4SeekBar = mutableStateOf<Int>(-1)
var pwm5SeekBar = mutableStateOf<Int>(-1)
var pwm6SeekBar = mutableStateOf<Int>(-1)
var pwm7SeekBar = mutableStateOf<Int>(-1)
var pwm8SeekBar = mutableStateOf<Int>(-1)
var pwm9SeekBar = mutableStateOf<Int>(-1)
var pwm10SeekBar = mutableStateOf<Int>(-1)
var pwm11SeekBar = mutableStateOf<Int>(-1)
var pwm12SeekBar = mutableStateOf<Int>(-1)
//////////////////////////////////////////////////
var limitTime = -1
var indexOfScenario = mutableStateOf(0)
var txtOfScenario = mutableStateOf("")
var commentOfScenario = mutableStateOf("")

// recording:
var test_time = 0
var indexScenario = 0
var num : Int = 0

var isAlreadyReceivedBytesForChart = mutableStateOf(false)
var doOpen_First_ChartWindow = mutableStateOf(false)
var doOpen_Second_ChartWindow = mutableStateOf(false)

// CHART
var chartFileAfterExperiment = mutableStateOf( File(Dir2Reports,"demo2.txt") )
var chartFileStandard = mutableStateOf<File?>( null ) // File(Dir7ReportsStandard,"17_02_2023X12_04_04_chart.txt")
//var chartFileStandard = mutableStateOf<File?>( File("C:\\Users\\Agregatka\\Documents\\mcm\\reports\\тестирование переделанных МИЛ","07_08_2025 16_56_55_2300_chart.txt"))

var isExperimentStarts = mutableStateOf(false)
var incrementTime = 0

data class DataChunkG(
    var isExperiment: Boolean = false,
    var firstGaugeData:   Float,
    var secondGaugeData:  Float,
    var thirdGaugeData:   Float,
    var fourthGaugeData:  Float,
    var fifthGaugeData:   Float,
    var sixthGaugeData:   Float,
    var seventhGaugeData: Float,
    var eighthGaugeData:  Float,

    var ninthGaugeData:    Float? = null,
    var tenthGaugeData:    Float? = null,
    var eleventhGaugeData: Float? = null,
    var twelfthGaugeData:  Float? = null,
)

data class DataChunkCurrent(
    var firstCurrentData: Int,
    var secondCurrentData: Int,
    var thirdCurrentData: Int,
    var fourthCurrentData: Int,
    var fifthCurrentData: Int,
    var sixthCurrentData: Int,
    var seventhCurrentData: Int,
    var eighthCurrentData: Int,

    var ninthCurrentData:    Int? = null,
    var tenthCurrentData:    Int? = null,
    var eleventhCurrentData: Int? = null,
    var twelfthCurrentData:  Int? = null,
)
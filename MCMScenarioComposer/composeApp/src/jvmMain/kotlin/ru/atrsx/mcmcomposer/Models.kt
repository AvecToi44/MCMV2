package ru.atrsx.mcmcomposer

// ---------- Domain model (matches your updated Excel) ----------

data class SheetModel(
    val pressures: PressuresBlockDto = PressuresBlockDto(),
    val solenoids: SolenoidsBlock = SolenoidsBlock(),
    val scenario: ScenarioBlock = ScenarioBlock(),
    val sheetName: String = "0b5_combi"
)

// --- Pressures (orange) ---

data class PressuresBlockDto(
    val channels: MutableList<PressureChannelDto> = MutableList(12) { i ->
        PressureChannelDto(
            displayName = if (i == 0) "Первый показатель" else "Channel Data ${i+1}",
            index = i,
            minValue = 0,
            maxValue = 25,
            tolerance = 10,
            unit = "Bar",
            comment = "~",
            preferredColorHex = "#FF0080%02X".format(i),
            isVisible = true
        )
    }
)

data class PressureChannelDto(
    var displayName: String,
    var index: Int,
    var minValue: Int,
    var maxValue: Int,
    var tolerance: Int,
    var unit: String,
    var comment: String,
    var preferredColorHex: String,
    var isVisible: Boolean,
    var parameters: MutableMap<String, String> = mutableMapOf()
)

// --- Solenoids (blue) ---

data class SolenoidsBlock(
    var mainFrequencyHz: Int? = 1500,      // "Main Freq:" (optional)
    var testVariable: Int? = 0,            // "TestVariable:" (optional)
    val channels: MutableList<SolenoidChannel> = MutableList(12) { i ->
        SolenoidChannel(
            displayName = "привет ${i+1}",
            index = i,
            maxPwm0_255 = 100,
            valueOfDivision = 10,
            tenthAmplitude = 100,
            tenthFrequency = 300,
            minValue = 0,
            maxValue = 1000,
            isVisible = true
        )
    }
)

data class SolenoidChannel(
    var displayName: String,
    var index: Int,
    var maxPwm0_255: Int,
    var valueOfDivision: Int,
    var tenthAmplitude: Int,
    var tenthFrequency: Int,
    var minValue: Int,
    var maxValue: Int,
    var isVisible: Boolean
)

// --- Scenario (green) ---

/** One horizontal scenario row in Excel. */
data class ScenarioStep(
    var stepTimeMs: Int,
    var channelValues: MutableList<Int>,   // size == number of channels
    var analog1: Int? = null,
    var analog2: Int? = null,
    var gradientTimeMs: Int? = null,       // "Gradient Time"
    var text: String? = null
)

data class ScenarioBlock(
    val steps: MutableList<ScenarioStep> = mutableListOf(
        ScenarioStep(1000, MutableList(12){0}, analog1=0, analog2=0, gradientTimeMs=0, text = "")
    )
)

// ---------- UI helper model (your existing row, extended) ----------

data class UiScenarioRow(
    var number: Int,
    var name: String = "",
    var passThrough: Boolean = true,
    var durationMs: String = "1000",
    var messageText: String = "",
    var interpolationFlags: MutableList<Boolean> = MutableList(16) { false },
    var pressureHighlightFlags: MutableList<Boolean> = MutableList(16) { false },
    var analogSetEnabled: Boolean = false,
    var channels: MutableList<Int> = MutableList(12) { 0 },
    var analog1: Int = 0,
    var analog2: Int = 0,
    var gradientTimeMs: Int = 0
)

// Mapping UI row <-> domain
fun UiScenarioRow.toScenarioStep(nChannels: Int) =
    ScenarioStep(
        stepTimeMs = durationMs.toIntOrNull() ?: 0,
        channelValues = channels.take(nChannels).toMutableList(),
        analog1 = analog1,
        analog2 = analog2,
        gradientTimeMs = gradientTimeMs,
        text = messageText
    )

fun ScenarioStep.toUiScenarioRow(number: Int) =
    UiScenarioRow(
        number = number,
        durationMs = stepTimeMs.toString(),
        messageText = text.orEmpty(),
        channels = channelValues.toMutableList(),
        analog1 = analog1 ?: 0,
        analog2 = analog2 ?: 0,
        gradientTimeMs = gradientTimeMs ?: 0
    )

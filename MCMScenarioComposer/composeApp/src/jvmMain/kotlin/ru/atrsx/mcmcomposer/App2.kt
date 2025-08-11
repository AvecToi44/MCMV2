package ru.atrsx.mcmcomposer

import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import ru.atrsx.mcmcomposer.ui.MainScenarioScreen

// ---------- Models ----------
data class ScenarioRow(
    var number: Int = 0,
    var name: String = "",
    var passThrough: Boolean = true,
    var durationMs: String = "0",
    var messageText: String = "",
    var interpolationFlags: MutableList<Boolean> = MutableList(16) { false },
    var pressureHighlightFlags: MutableList<Boolean> = MutableList(16) { false },
    var analogSetEnabled: Boolean = false
)

data class PressureChannel(
    val index: Int,
    var used: Boolean = true,
    var checked: Boolean = true,
    var color: Color,
    var displayName: String = "Channel Data $index",
    var comment: String = "",
    var maxValue: String = "25",
    var tolerance: String = "10"
)

data class PWMChannel(
    val index: Int,
    var used: Boolean = true,
    var color: Color,
    var displayName: String = "",
    var maxPwm: String = "",
    var tolerance: String = "",
    var frequency: String = "",
    var isDC: Boolean = false,
    var expectedTestValue: String = ""
)

// ---------- App ----------
fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Test Console") {
        MaterialTheme {
            Surface(Modifier.fillMaxSize()) {
                AppRoot()
            }
        }
    }
}

@Composable
fun AppRoot() {
    var tab by remember { mutableStateOf(0) }

    val tabs = listOf("Main Scenario", "Pressures", "Currents")
    Column(Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = tab) {
            tabs.forEachIndexed { i, title ->
                Tab(selected = tab == i, onClick = { tab = i }, text = { Text(title) })
            }
        }
        when (tab) {
            0 -> MainScenarioScreen()
            1 -> PressuresScreen()
            2 -> CurrentsScreen()
        }
    }
}

// ---------- Screen 1: Main Scenario ----------
@Composable
fun MainScenarioScreen0() {
    val rows = remember {
        mutableStateListOf(
            ScenarioRow(number = 0, passThrough = true)
        )
    }

    Row(Modifier.fillMaxSize()) {

        // Table + canvas area (just empty gray area under the table)
        Column(Modifier.weight(1f).fillMaxHeight()) {
            ScenarioHeader()
            ScenarioRowEditor(rows[0])
            Box(
                Modifier.fillMaxSize()
                    .border(1.dp, Color(0xFFBFC7D5))
                    .background(Color(0xFFF7F8FA))
            )
        }

        // Right-side actions
        Column(
            Modifier.width(140.dp)
                .fillMaxHeight()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SideButton("Add Step")
            SideButton("Delete")
            Spacer(Modifier.height(8.dp))
            SideButton("Copy")
            SideButton("Cut")
            SideButton("Paste")
            Spacer(Modifier.height(8.dp))
            SideButton("Set LOOP 1")
            SideButton("Set LOOP 2")
            SideButton("Clear LOOPs")
        }
    }
}

@Composable
private fun ScenarioHeader() {
    Row(
        Modifier.fillMaxWidth()
            .background(Color(0xFFE7F3FF))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HeaderBox("Number", 64.dp)
        HeaderBox("Name", 140.dp)
        HeaderBox("Pass Through", 110.dp)
        HeaderBox("Duration (in ms)", 150.dp)
        HeaderBox("Message Text", 220.dp)
        HeaderBox("Interpolation Parameters", 370.dp)
        HeaderBox("Pressure highlight settings", 360.dp)
        HeaderBox("Analog Outputs", 140.dp)
    }
}

@Composable
private fun ScenarioRowEditor(row: ScenarioRow) {
    val scroll = rememberScrollState()
    Row(
        Modifier.fillMaxWidth()
            .horizontalScroll(scroll)
            .height(IntrinsicSize.Min)
            .background(Color(0xFFD9EEF9))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Number
        ReadOnlyCell(row.number.toString(), 64.dp)

        // Name
        var nm by remember { mutableStateOf(row.name) }
        TextField(
            value = nm, onValueChange = { nm = it; row.name = it },
            modifier = Modifier.width(140.dp).height(40.dp),
            singleLine = true
        )

        // Pass Through
        var pass by remember { mutableStateOf(row.passThrough) }
        Row(Modifier.width(110.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(pass, { pass = it; row.passThrough = it })
            Text(" ")
        }

        // Duration
        var dur by remember { mutableStateOf(row.durationMs) }
        TextField(
            value = dur, onValueChange = { dur = it; row.durationMs = it },
            modifier = Modifier.width(150.dp).height(40.dp),
            singleLine = true
        )

        // Message text
        var msg by remember { mutableStateOf(row.messageText) }
        TextField(
            value = msg, onValueChange = { msg = it; row.messageText = it },
            modifier = Modifier.width(220.dp).height(40.dp),
            singleLine = true
        )

        // Interpolation Parameters (1..16)
        Column(Modifier.width(370.dp)) {
            TextRowLabel("1  2  3  4  5  6  7  8  9  10  11  12  13  14  15  16")
            FlagsStrip(row.interpolationFlags)
        }

        // Pressure highlight settings (1..16)
        Column(Modifier.width(360.dp)) {
            TextRowLabel("1  2  3  4  5  6  7  8  9  10  11  12  13  14  15  16")
            FlagsStrip(row.pressureHighlightFlags)
        }

        // Analog outputs
        Column(Modifier.width(140.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            var enabled by remember { mutableStateOf(row.analogSetEnabled) }
            Checkbox(enabled, { enabled = it; row.analogSetEnabled = it })
            Button(
                onClick = { /* SET */ },
                modifier = Modifier.width(80.dp).height(32.dp)
            ) { Text("SET") }
        }
    }
}

@Composable
private fun FlagsStrip(flags: MutableList<Boolean>) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        flags.forEachIndexed { i, v ->
            Checkbox(checked = v, onCheckedChange = { flags[i] = it })
        }
    }
}

@Composable
private fun TextRowLabel(text: String) {
    Text(text, fontSize = 12.sp, modifier = Modifier.padding(bottom = 2.dp))
}

@Composable
private fun HeaderBox(text: String, width: Dp) {
    Box(
        Modifier.width(width),
        contentAlignment = Alignment.CenterStart
    ) { Text(text, fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }
}

@Composable
private fun ReadOnlyCell(text: String, width: Dp) {
    Box(
        Modifier.width(width).height(40.dp)
            .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
            .background(Color.White)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) { Text(text) }
}

@Composable
private fun SideButton(label: String) {
    Button(
        onClick = { /* action */ },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(6.dp)
    ) { Text(label) }
}

// ---------- Screen 2: Pressures ----------
@Composable
fun PressuresScreen() {
    val palette = listOf(
        Color(0xFF00A651), Color(0xFF8B0000), Color(0xFF0B61A4), Color(0xFFFFA500),
        Color(0xFF9ACD32), Color(0xFF808000), Color(0xFFFFD700), Color(0xFF8B4513),
        Color(0xFF8A2BE2), Color(0xFFFF1493), Color(0xFF9400D3), Color(0xFF000000),
        Color(0xFF1E90FF), Color(0xFF008000), Color(0xFF2E8B57), Color(0xFFFF6F61)
    )
    val channels = remember {
        mutableStateListOf<PressureChannel>().also { list ->
            repeat(16) { i ->
                list.add(PressureChannel(index = i + 1, color = palette[i]))
            }
        }
    }

    Row(Modifier.fillMaxSize()) {
        // Left list
        Box(
            Modifier.width(220.dp).fillMaxHeight()
                .padding(12.dp).border(1.dp, Color.LightGray)
        ) {
            val vScroll = rememberScrollState()
            Column(Modifier.fillMaxSize().verticalScroll(vScroll)) {
                Text("PRESSURES TO USE", modifier = Modifier.padding(8.dp), fontWeight = FontWeight.SemiBold)
                channels.forEach { ch ->
                    PressureListItem(ch)
                }
            }
        }

        // Right config
        Column(
            Modifier.weight(1f).fillMaxHeight().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // General settings
            Card {
                Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("GENERAL SETTINGS", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.width(24.dp))
                    Text("TOLERANCE:")
                    var tol by remember { mutableStateOf("10") }
                    Spacer(Modifier.width(8.dp))
                    TextField(tol, { tol = it }, modifier = Modifier.width(80.dp), singleLine = true)
                    Spacer(Modifier.width(8.dp))
                    Text("%")
                }
            }

            // Details bound to the currently selected item (first selected or first item)
            val selected = channels.firstOrNull { it.used } ?: channels.first()
            Card(Modifier.fillMaxWidth().weight(1f)) {
                Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("DETAILS", fontWeight = FontWeight.SemiBold)
                    LabeledField("DISPLAY NAME:", selected.displayName) { selected.displayName = it }
                    LabeledField("COMMENT:", selected.comment) { selected.comment = it }
                    LabeledField("MAX VALUE:", selected.maxValue, width = 120.dp) { selected.maxValue = it }
                    LabeledField("TOLERANCE:", selected.tolerance, width = 120.dp) { selected.tolerance = it }
                }
            }
        }
    }
}

@Composable
private fun PressureListItem(ch: PressureChannel) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Channel Data ${ch.index}", modifier = Modifier.weight(1f))
        Checkbox(checked = ch.used, onCheckedChange = { ch.used = it })
        Checkbox(checked = ch.checked, onCheckedChange = { ch.checked = it })
        Box(
            Modifier.size(22.dp).border(1.dp, Color.DarkGray).background(ch.color)
        )
    }
}

@Composable
private fun LabeledField(label: String, value: String, width: Dp = 260.dp, onValue: (String) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.width(130.dp))
        TextField(value, onValueChange = onValue, singleLine = true, modifier = Modifier.width(width))
    }
}

// ---------- Screen 3: Currents ----------
@Composable
fun CurrentsScreen() {
    val palette = listOf(
        Color(0xFF00A651), Color(0xFF8B0000), Color(0xFF0B61A4), Color(0xFFFFA500),
        Color(0xFF9ACD32), Color(0xFF808000), Color(0xFFFFD700), Color(0xFF8B4513),
        Color(0xFF8A2BE2), Color(0xFFFF1493), Color(0xFF9400D3), Color(0xFF000000),
        Color(0xFF1E90FF), Color(0xFF008000), Color(0xFF2E8B57), Color(0xFFFF6F61)
    )
    val channels = remember {
        mutableStateListOf<PWMChannel>().also { list ->
            repeat(16) { i -> list.add(PWMChannel(index = i + 1, color = palette[i])) }
        }
    }

    Row(Modifier.fillMaxSize()) {
        // Left list
        Box(
            Modifier.width(220.dp).fillMaxHeight()
                .padding(12.dp).border(1.dp, Color.LightGray)
        ) {
            val vScroll = rememberScrollState()
            Column(Modifier.fillMaxSize().verticalScroll(vScroll)) {
                Text("CURRENTS TO USE", modifier = Modifier.padding(8.dp), fontWeight = FontWeight.SemiBold)
                channels.forEach { ch ->
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Channel PWM ${ch.index}", modifier = Modifier.weight(1f))
                        Checkbox(checked = ch.used, onCheckedChange = { ch.used = it })
                        Box(
                            Modifier.size(22.dp).border(1.dp, Color.DarkGray).background(ch.color)
                        )
                    }
                }
            }
        }

        // Right config
        Column(
            Modifier.weight(1f).fillMaxHeight().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card {
                Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("GENERAL SETTINGS", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.width(24.dp))
                    Text("TOLERANCE (%):")
                    var tol by remember { mutableStateOf("10") }
                    Spacer(Modifier.width(8.dp))
                    TextField(tol, { tol = it }, modifier = Modifier.width(80.dp), singleLine = true)

                    Spacer(Modifier.width(24.dp))
                    Text("FREQUENCY (Hz)")
                    var genFreq by remember { mutableStateOf("1000") }
                    Spacer(Modifier.width(8.dp))
                    TextField(genFreq, { genFreq = it }, modifier = Modifier.width(100.dp), singleLine = true)

                    Spacer(Modifier.width(24.dp))
                    var negative by remember { mutableStateOf(false) }
                    Checkbox(negative, { negative = it })
                    Text("IS NEGATIVE")
                }
            }

            val selected = channels.first()
            Card(Modifier.fillMaxWidth().weight(1f)) {
                Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("DETAILS", fontWeight = FontWeight.SemiBold)
                    LabeledField("DISPLAY NAME:", selected.displayName) { selected.displayName = it }
                    LabeledField("MAX PWM:", selected.maxPwm, width = 120.dp) { selected.maxPwm = it }
                    LabeledField("TOLERANCE:", selected.tolerance, width = 120.dp) { selected.tolerance = it }
                    LabeledField("FREQUENCY:", selected.frequency, width = 120.dp) { selected.frequency = it }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("IS DC", modifier = Modifier.width(130.dp))
                        Checkbox(checked = selected.isDC, onCheckedChange = { selected.isDC = it })
                    }

                    LabeledField("EXPECTED TEST VALUE:", selected.expectedTestValue, width = 160.dp) {
                        selected.expectedTestValue = it
                    }
                }
            }
        }
    }
}

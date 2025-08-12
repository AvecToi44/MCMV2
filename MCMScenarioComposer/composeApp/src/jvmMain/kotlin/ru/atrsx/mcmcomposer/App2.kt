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
import ru.atrsx.mcmcomposer.ui.CurrentsScreen
import ru.atrsx.mcmcomposer.ui.MainScenarioScreen
import ru.atrsx.mcmcomposer.ui.PressuresScreen

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





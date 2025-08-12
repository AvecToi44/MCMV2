package ru.atrsx.mcmcomposer.ui

import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.atrsx.mcmcomposer.ScenarioRow

// ---------- Screen 1: Main Scenario (multi-row, grid, per-row labels) ----------
@Composable
fun MainScenarioScreen() {
    // demo data
    val rows = remember {
        mutableStateListOf(
            ScenarioRow(number = 0, passThrough = true),
            ScenarioRow(number = 1, passThrough = true),
            ScenarioRow(number = 2, passThrough = true),
        )
    }
    var selected by remember { mutableStateOf(0) }

    Row(Modifier.fillMaxSize()) {
        // Left: table + canvas area
        Column(Modifier.weight(1f).background(Color.White).fillMaxHeight()) {
            //ScenarioHeader() // header row

            // rows
            val vScroll = rememberScrollState()
            val horizontalScrollState = rememberScrollState()
            HorizontalScrollbar(
                rememberScrollbarAdapter(horizontalScrollState),
                Modifier.align(Alignment.CenterHorizontally)
            )
            LazyColumn(
                Modifier.fillMaxWidth().horizontalScroll(horizontalScrollState)
                    //.verticalScroll(vScroll)
                    .border(1.dp, Color(0xFFB0B0B0))
            ) {
                stickyHeader {
                    // Fixed item at the top
                    Row(
                        Modifier.fillMaxWidth().background(Color.LightGray).height(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(modifier = Modifier, text = "Number", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                }
                itemsIndexed(rows) {  idx, row ->

                    ScenarioRowItem(
                        row = row,
                        selected = selected == idx,
                        onSelect = { selected = idx }
                    )
                }
            }

            // big empty working area
            Box(
                Modifier.fillMaxSize()
                    .border(1.dp, Color(0xFFBFC7D5))
                    .background(Color(0xFFF7F8FA))
            )
        }

        // Right-side actions
        Column(
            Modifier.width(140.dp).fillMaxHeight().padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val next = (rows.maxOfOrNull { it.number } ?: -1) + 1
                    rows.add(ScenarioRow(number = next, passThrough = true))
                    selected = rows.lastIndex
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Add Step") }

            Button(
                onClick = { if (rows.isNotEmpty()) rows.removeAt(selected.coerceIn(0, rows.lastIndex)) },
                modifier = Modifier.fillMaxWidth(),
                enabled = rows.isNotEmpty()
            ) { Text("Delete") }

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
    // fixed widths to align with rows
    val wNumber = 64.dp
    val wName = 140.dp
    val wPass = 110.dp
    val wDur = 150.dp
    val wMsg = 220.dp
    val wInterp = 370.dp
    val wPress = 360.dp
    val wAnalog = 140.dp

    Row(
        Modifier.fillMaxWidth()
            .background(Color(0xFFEDEFF2))
            .border(1.dp, Color(0xFFB0B0B0))
            .padding(vertical = 6.dp)
    ) {
        HeaderBox("Number", wNumber)
        HeaderBox("Name", wName)
        HeaderBox("Pass Through", wPass)
        HeaderBox("Duration (in ms)", wDur)
        HeaderBox("Message Text", wMsg)
        HeaderBox("Interpolation Parameters", wInterp)
        HeaderBox("Pressure highlight settings", wPress)
        HeaderBox("Analog Outputs", wAnalog)
    }
}

@Composable
private fun ScenarioRowItem(
    row: ScenarioRow,
    selected: Boolean,
    onSelect: () -> Unit
) {
    // same widths as header
    val wNumber = 30.dp
    val wName = 140.dp
    val wPass = 60.dp
    val wDur = 100.dp
    val wMsg = 200.dp
    val wInterp = 370.dp
    val wPress = 360.dp
    val wAnalog = 140.dp

    val ModifierCellBorder = Modifier.fillMaxHeight()//.border(1.dp, Color.Black.copy(alpha = 0.7f))
    val rowBg = if (selected) Color(0xFFBFE6FF) else Color.White

    Row(
        Modifier.border(1.dp, Color.Black.copy(alpha = 0.7f)).fillMaxWidth().height(50.dp)
            .background(rowBg)
            .clickable { onSelect() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Number (read-only)
        Column(ModifierCellBorder.width(wNumber).height(40.dp).padding(start = 8.dp), verticalArrangement = Arrangement.Center) {
            Text(row.number.toString())
        }

        // Name
        var nm by remember { mutableStateOf(row.name) }
        TextField(
            nm, { nm = it; row.name = it },
            modifier = ModifierCellBorder.width(wName),
            //label = { Text("Name", fontSize = 9.sp) },
            textStyle = TextStyle(fontSize = 15.sp),
            singleLine = true
        )

        // Pass Through
        var pass by remember { mutableStateOf(row.passThrough) }
        Row(ModifierCellBorder.width(wPass).height(40.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {

            Checkbox(checked = pass, onCheckedChange = { pass = it; row.passThrough = it })
        }

        // Duration
        var dur by remember { mutableStateOf(row.durationMs) }
        TextField(
            dur, { dur = it; row.durationMs = it },
            modifier = ModifierCellBorder.width(wName),
            //label = { Text("Name", fontSize = 9.sp) },
            textStyle = TextStyle(fontSize = 15.sp),
            singleLine = true
        )
        Spacer(modifier = Modifier.width(5.dp))
        // Message
        var msg by remember { mutableStateOf(row.messageText) }
        TextField(
            msg, { msg = it; row.messageText = it },
            modifier = ModifierCellBorder.width(wName),
            //label = { Text("Name", fontSize = 9.sp) },
            textStyle = TextStyle(fontSize = 15.sp),
            singleLine = true
        )


        // Interpolation Parameters (per-row 1..16 + flags)
        Column(ModifierCellBorder.width(wInterp).padding(horizontal = 8.dp, vertical = 4.dp)) {
            Text((1..16).joinToString("  "), fontSize = 11.sp)
            FlagsStrip(row.interpolationFlags)
        }

        // Pressure highlight settings (per-row 1..16 + flags)
        Column(ModifierCellBorder.width(wPress).padding(horizontal = 8.dp, vertical = 4.dp)) {
            Text((1..16).joinToString("  "), fontSize = 11.sp)
            FlagsStrip(row.pressureHighlightFlags)
        }

        // Analog outputs
        Column(
            ModifierCellBorder.width(wAnalog).height(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            var enabled by remember { mutableStateOf(row.analogSetEnabled) }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = enabled, onCheckedChange = { enabled = it; row.analogSetEnabled = it })

                Spacer(Modifier.width(6.dp))
                Button(onClick = { /* SET */ }, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)) {
                    Text("SET")
                }
            }
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
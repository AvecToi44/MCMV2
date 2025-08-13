package ru.atrsx.mcmcomposer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.atrsx.mcmcomposer.PressureChannel

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
        // Left config
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

        // Right list
        Box(
            Modifier.width(320.dp).fillMaxHeight()
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
    }
}

@Composable
private fun PressureListItem(ch: PressureChannel) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Channel Data ${ch.index}", modifier = Modifier.weight(1f))
        Checkbox(checked = ch.isVisible, onCheckedChange = { ch.isVisible = it })
        Box(
            Modifier.size(22.dp).border(1.dp, Color.DarkGray).background(ch.color)
        )
    }
}

@Composable
fun LabeledField(label: String, value: String, width: Dp = 260.dp, onValue: (String) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.width(130.dp))
        TextField(value, onValueChange = onValue, singleLine = true, modifier = Modifier.width(width))
    }
}
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
import androidx.compose.ui.unit.dp
import ru.atrsx.mcmcomposer.PWMChannel

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

        Box(
            Modifier.width(320.dp).fillMaxHeight()
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
    }
}
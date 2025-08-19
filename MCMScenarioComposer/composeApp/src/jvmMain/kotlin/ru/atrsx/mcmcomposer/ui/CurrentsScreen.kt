// SolenoidsScreen.kt — Compose Desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import ru.atrsx.mcmcomposer.MAIN_CONFIG
import ru.atrsx.mcmcomposer.SolenoidChannel
import ru.atrsx.mcmcomposer.solenoids

@Composable
fun SolenoidsScreen(modifier: Modifier = Modifier) {
    // --- Selection ---
    var selectedIndex by remember { mutableStateOf(solenoids.firstOrNull()?.index) }

    // --- Left editor state (mirrors selected item) ---
    var mainFrequency by remember { mutableStateOf(MAIN_CONFIG.value.solenoids.mainFrequencyHz) } // global integer

    var nameText by remember { mutableStateOf("") }
    var pwmText by remember { mutableStateOf("") }
    var divText by remember { mutableStateOf("") }
    var amp10Text by remember { mutableStateOf("") }
    var freq10Text by remember { mutableStateOf("") }
    var minText by remember { mutableStateOf("") }
    var maxText by remember { mutableStateOf("") }

    // Load editor when selection changes
    LaunchedEffect(selectedIndex) {
        val ch = solenoids.firstOrNull { it.index == selectedIndex }
        if (ch != null) {
            nameText   = ch.displayName
            pwmText    = ch.maxPwm0_255.toString()
            divText    = ch.valueOfDivision.toString()
            amp10Text  = ch.DitherAmplitude.toString()
            freq10Text = ch.DitherFrequency.toString()
            minText    = ch.minValue.toString()
            maxText    = ch.maxValue.toString()
        } else {
            nameText = ""; pwmText = ""; divText = ""; amp10Text = ""
            freq10Text = ""; minText = ""; maxText = ""
        }
    }

    fun updateSelected(transform: (SolenoidChannel) -> SolenoidChannel) {
        val idx = solenoids.indexOfFirst { it.index == selectedIndex }
        if (idx != -1) solenoids[idx] = transform(solenoids[idx])
    }

    Row(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val verticalScroll = rememberScrollState()
        // -------- LEFT: Editor Panel --------
        Column(
            modifier = Modifier
                .width(360.dp)
                .fillMaxHeight()
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium)
                .padding(12.dp).verticalScroll(verticalScroll),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Solenoids — Global & Channel Params", style = MaterialTheme.typography.titleMedium)

            // --- Global: Main Frequency (Integer) ---
            OutlinedTextField(
                value = mainFrequency.toString(),
                onValueChange = { txt ->
                    mainFrequency = txt.toIntOrNull() ?: mainFrequency
                    MAIN_CONFIG.value.solenoids.mainFrequencyHz = mainFrequency
//                    val f10 = mainFrequency * 10
//                    for (i in solenoids.indices) {
//                        solenoids[i] = solenoids[i].copy(DitherFrequency = f10)
//                    }
//                    if (selectedIndex != null) freq10Text = f10.toString()
                },
                label = { Text("Main Frequency (Integer)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
//            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//                Button(
//                    onClick = {
//                        val f10 = mainFrequency * 10
//                        for (i in solenoids.indices) {
//                            solenoids[i] = solenoids[i].copy(tenthFrequency = f10)
//                        }
//                        if (selectedIndex != null) freq10Text = f10.toString()
//                    }
//                ) { Text("Apply to all") }
//            }

            Divider()

            val enabled = selectedIndex != null

            OutlinedTextField(
                value = nameText,
                onValueChange = {
                    nameText = it
                    if (enabled) updateSelected { ch -> ch.copy(displayName = it) }
                },
                label = { Text("Display Name") },
                enabled = enabled,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = pwmText,
                onValueChange = { txt ->
                    pwmText = txt
                    if (!enabled) return@OutlinedTextField
                    txt.toIntOrNull()?.let { raw ->
                        val clamped = raw.coerceIn(0, 255)
                        if (clamped.toString() != pwmText) pwmText = clamped.toString()
                        updateSelected { ch -> ch.copy(maxPwm0_255 = clamped) }
                    }
                },
                label = { Text("Max Value [0 - 255]") },
                enabled = enabled,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = divText,
                onValueChange = { txt ->
                    divText = txt
                    if (!enabled) return@OutlinedTextField
                    txt.toIntOrNull()?.let { newDiv ->
                        updateSelected { ch -> ch.copy(valueOfDivision = newDiv) }
                    }
                },
                label = { Text("Value of division") },
                enabled = enabled,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = amp10Text,
                onValueChange = { txt ->
                    amp10Text = txt
                    if (!enabled) return@OutlinedTextField
                    txt.toIntOrNull()?.let { newAmp10 ->
                        updateSelected { ch -> ch.copy(DitherAmplitude = newAmp10) }
                    }
                },
                label = { Text("Dither Amplitude") },
                enabled = enabled,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = freq10Text,
                onValueChange = { txt ->
                    freq10Text = txt
                    if (!enabled) return@OutlinedTextField
                    txt.toIntOrNull()?.let { newFreq10 ->
                        updateSelected { ch -> ch.copy(DitherFrequency = newFreq10) }
                    }
                },
                label = { Text("Dither Frequency") },
                enabled = enabled,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = minText,
                onValueChange = { txt ->
                    minText = txt
                    if (!enabled) return@OutlinedTextField
                    txt.toIntOrNull()?.let { newMin ->
                        updateSelected { ch ->
                            val coercedMax = if (newMin > ch.maxValue) newMin else ch.maxValue
                            if (coercedMax.toString() != maxText) maxText = coercedMax.toString()
                            ch.copy(minValue = newMin, maxValue = coercedMax)
                        }
                    }
                },
                label = { Text("Min Current Value") },
                enabled = enabled,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = maxText,
                onValueChange = { txt ->
                    maxText = txt
                    if (!enabled) return@OutlinedTextField
                    txt.toIntOrNull()?.let { newMax ->
                        updateSelected { ch ->
                            val coercedMin = if (newMax < ch.minValue) newMax else ch.minValue
                            if (coercedMin.toString() != minText) minText = coercedMin.toString()
                            ch.copy(maxValue = newMax, minValue = coercedMin)
                        }
                    }
                },
                label = { Text("Max Current Value") },
                enabled = enabled,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (!enabled) {
                Text(
                    "Выберите соленоид справа, чтобы редактировать параметры.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // -------- RIGHT: Solenoids List (checkbox for visibility, row selection) --------
        Column(Modifier.weight(1f)) {
            Text("Соленоиды", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = solenoids,
                    key = { it.index }
                ) { ch ->
                    val isSelected = (selectedIndex == ch.index)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.surfaceVariant
                                else MaterialTheme.colorScheme.surface
                            )
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant,
                                shape = MaterialTheme.shapes.medium
                            )
                            .padding(12.dp)
                            .alpha(if (ch.isVisible) 1f else 0.55f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // LEFT: visibility checkbox
                        Checkbox(
                            checked = ch.isVisible,
                            onCheckedChange = { checked ->
                                val idx = solenoids.indexOfFirst { it.index == ch.index }
                                if (idx != -1) {
                                    solenoids[idx] = solenoids[idx].copy(isVisible = checked)
                                }
                            }
                        )

                        // MIDDLE: selection + details
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedIndex = ch.index },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(ch.displayName, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "PWM: ${ch.maxPwm0_255}  •  Div: ${ch.valueOfDivision}  •  Amp: ${ch.DitherAmplitude}  •  Freq: ${ch.DitherFrequency}\n" +
                                            "Range: ${ch.minValue}…${ch.maxValue}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

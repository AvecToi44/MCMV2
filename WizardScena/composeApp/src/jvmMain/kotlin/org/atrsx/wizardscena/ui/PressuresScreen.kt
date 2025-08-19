package org.atrsx.wizardscena.ui// PressuresScreen.kt — Compose Desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.alpha
import org.atrsx.wizardscena.PressureChannel
import org.atrsx.wizardscena.pressures
import java.util.Locale
import javax.swing.JColorChooser

// ---------- Preset series colors (by channel index) ----------
private val seriesColors = listOf(
    Color(0xFF2E7D32),  // Dark Green
    Color(0xFF5D4037),  // Brown
    Color(0xFF101010),  // Dark Gray
    Color(0xFF1976D2),  // Blue
    Color(0xFFD32F2F),  // Red
    Color(0xFFFBC02D),  // Yellow
    Color(0xFFDE4504),  // Magenta (as provided)
    Color(0xFF7F7F7F),  // Light Gray
    Color(0xFF9C27B0),  // Purple
    Color(0xFFE91E63),  // Pink
    Color(0xFFBE9FDA),  // Light Purple
    Color(0xFF4CAF50),  // Green
    Color(0xFF8D6E63),  // Light Brown
    Color(0xFFAB47BC),  // Medium Purple
    Color(0xFF66BB6A),  // Light Green
    Color(0xFFF06292)   // Light Pink
)

// ---------- Color helpers ----------
private fun String.toComposeColorOr(default: Color = Color(0xFF008001)): Color {
    val hex = this.removePrefix("#")
    return try {
        when (hex.length) {
            8 -> { // AARRGGBB
                val a = hex.substring(0, 2).toInt(16)
                val r = hex.substring(2, 4).toInt(16)
                val g = hex.substring(4, 6).toInt(16)
                val b = hex.substring(6, 8).toInt(16)
                Color(r, g, b, a)
            }
            6 -> { // RRGGBB
                val r = hex.substring(0, 2).toInt(16)
                val g = hex.substring(2, 4).toInt(16)
                val b = hex.substring(4, 6).toInt(16)
                Color(r, g, b, 0xFF)
            }
            else -> default
        }
    } catch (_: Throwable) {
        default
    }
}

private fun Color.toHexARGB(): String {
    val a = (alpha * 255).toInt().coerceIn(0, 255)
    val r = (red   * 255).toInt().coerceIn(0, 255)
    val g = (green * 255).toInt().coerceIn(0, 255)
    val b = (blue  * 255).toInt().coerceIn(0, 255)
    return String.format(Locale.US, "#%02X%02X%02X%02X", a, r, g, b)
}

private fun Color.toAwt(): java.awt.Color =
    java.awt.Color(
        (red * 255).toInt().coerceIn(0, 255),
        (green * 255).toInt().coerceIn(0, 255),
        (blue * 255).toInt().coerceIn(0, 255),
        (alpha * 255).toInt().coerceIn(0, 255)
    )

private fun java.awt.Color.toCompose(): Color = Color(red, green, blue, alpha)

private fun pickDesktopColor(initialHex: String): String? {
    val initial = initialHex.toComposeColorOr()
    val chosen = JColorChooser.showDialog(
        null, "Выбор цвета канала", initial.toAwt()
    ) ?: return null
    return chosen.toCompose().toHexARGB()
}

// ---------- Single-select state sync ----------
private fun setSelectedExclusive(
    list: MutableList<PressureChannel>,
    selectedIndex: Int?
) {
    for (i in list.indices) {
        val ch = list[i]
        val shouldBeSelected = (selectedIndex != null && ch.index == selectedIndex)
        if (ch.isSelected != shouldBeSelected) {
            list[i] = ch.copy(isSelected = shouldBeSelected)
        }
    }
}

// Apply presets: if a channel still has the default "#FF008001", give it a preset color by index
private fun applyPresetColorsIfDefault() {
    for (i in pressures.indices) {
        val ch = pressures[i]
        val isDefault = ch.preferredColorHex.equals("#FF008001", ignoreCase = true)
        if (isDefault) {
            val preset = seriesColors[i % seriesColors.size].toHexARGB()
            pressures[i] = ch.copy(preferredColorHex = preset)
        }
    }
}

// ---------- UI ----------
@Composable
fun PressuresScreen(modifier: Modifier = Modifier) {
    // Assign presets on first composition and whenever the list size changes (e.g., channels added)
    LaunchedEffect(Unit) { applyPresetColorsIfDefault() }
    LaunchedEffect(pressures.size) { applyPresetColorsIfDefault() }

    var selectedIndex by remember {
        mutableStateOf(pressures.firstOrNull { it.isSelected }?.index)
    }

    // Keep list flags in sync when selectedIndex changes
    LaunchedEffect(selectedIndex) {
        setSelectedExclusive(pressures, selectedIndex)
    }

    // Left panel editor state
    var nameText by remember { mutableStateOf("") }
    var minText by remember { mutableStateOf("") }
    var maxText by remember { mutableStateOf("") }
    var tolText by remember { mutableStateOf("") }
    var unitText by remember { mutableStateOf("") }

    // Load editor fields when selection changes
    LaunchedEffect(selectedIndex) {
        val ch = pressures.firstOrNull { it.index == selectedIndex }
        if (ch != null) {
            nameText = ch.displayName
            minText  = ch.minValue.toString()
            maxText  = ch.maxValue.toString()
            tolText  = ch.tolerance.toString()
            unitText = ch.unit
        } else {
            nameText = ""; minText = ""; maxText = ""; tolText = ""; unitText = ""
        }
    }

    fun updateSelected(transform: (PressureChannel) -> PressureChannel) {
        val idx = pressures.indexOfFirst { it.index == selectedIndex }
        if (idx != -1) pressures[idx] = transform(pressures[idx])
    }

    Row(modifier.fillMaxSize().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {

        // -------- LEFT: Editor Panel --------
        Column(
            modifier = Modifier.width(320.dp)
                .fillMaxHeight()
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Параметры канала", style = MaterialTheme.typography.titleMedium)
            val enabled = selectedIndex != null

            OutlinedTextField(
                value = nameText,
                onValueChange = {
                    nameText = it
                    if (enabled) updateSelected { ch -> ch.copy(displayName = it) }
                },
                label = { Text("DisplayName") },
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
                            if (coercedMax != ch.maxValue) maxText = coercedMax.toString()
                            ch.copy(minValue = newMin, maxValue = coercedMax)
                        }
                    }
                },
                label = { Text("MinValue") },
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
                            if (coercedMin != ch.minValue) minText = coercedMin.toString()
                            ch.copy(maxValue = newMax, minValue = coercedMin)
                        }
                    }
                },
                label = { Text("MaxValue") },
                enabled = enabled,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = tolText,
                onValueChange = { txt ->
                    tolText = txt
                    if (!enabled) return@OutlinedTextField
                    txt.toIntOrNull()?.let { newTol ->
                        updateSelected { ch -> ch.copy(tolerance = newTol) }
                    }
                },
                label = { Text("Tolerance") },
                enabled = enabled,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = unitText,
                onValueChange = {
                    unitText = it
                    if (enabled) updateSelected { ch -> ch.copy(unit = it) }
                },
                label = { Text("Unit") },
                enabled = enabled,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (!enabled) {
                Text(
                    "Выберите канал справа, чтобы редактировать параметры.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // -------- RIGHT: Channels List with checkboxes --------
        Column(Modifier.weight(1f)) {
            Text("Давления", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = pressures, // show all, even if hidden
                    key = { it.index }
                ) { ch ->
                    val isSelected = ch.isSelected
                    val color = ch.preferredColorHex.toComposeColorOr()

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
                                val idx = pressures.indexOfFirst { it.index == ch.index }
                                if (idx != -1) {
                                    pressures[idx] = pressures[idx].copy(isVisible = checked)
                                }
                            }
                        )

                        // MIDDLE: selection area (click row content to select)
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedIndex = ch.index },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(ch.displayName, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "Диапазон: ${ch.minValue}…${ch.maxValue} ${ch.unit}  •  Допуск: ${ch.tolerance}  •  ${ch.comment}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2
                                )
                            }
                        }

                        // RIGHT: small color square (click to custom-pick)
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .border(1.dp, MaterialTheme.colorScheme.outline, shape = MaterialTheme.shapes.extraSmall)
                                .background(color, shape = MaterialTheme.shapes.extraSmall)
                                .clickable {
                                    val pickedHex = pickDesktopColor(ch.preferredColorHex)
                                    if (pickedHex != null) {
                                        val idx = pressures.indexOfFirst { it.index == ch.index }
                                        if (idx != -1) {
                                            pressures[idx] = pressures[idx].copy(preferredColorHex = pickedHex)
                                        }
                                    }
                                }
                        )
                    }
                }
            }
        }
    }
}

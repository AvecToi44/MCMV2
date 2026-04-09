package org.atrsx.wizardscena.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.atrsx.wizardscena.MAIN_CONFIG
import org.atrsx.wizardscena.tr
import org.atrsx.wizardscena.trf

@Composable
fun FrequencyParamsScreen(modifier: Modifier = Modifier) {
    val editorValues = remember { mutableStateListOf<String>() }

    LaunchedEffect(MAIN_CONFIG.value) {
        val params = normalizeFrequencyParams(MAIN_CONFIG.value.solenoids.frequencyParams0x68)
        if (MAIN_CONFIG.value.solenoids.frequencyParams0x68 != params) {
            MAIN_CONFIG.value = MAIN_CONFIG.value.copy(
                solenoids = MAIN_CONFIG.value.solenoids.copy(frequencyParams0x68 = params.toMutableList())
            )
        }

        editorValues.clear()
        repeat(10) { idx ->
            editorValues.add(params[idx].toString())
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(tr("freq_params_title"), style = MaterialTheme.typography.titleLarge)
        Text(
            tr("freq_params_hint"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed((0 until 10).toList()) { index, _ ->
                val value = editorValues.getOrNull(index).orEmpty()

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = value,
                        onValueChange = { raw ->
                            val digitsOnly = raw.filter { it.isDigit() }
                            val parsed = digitsOnly.toIntOrNull()
                            val finalValue = (parsed ?: 0).coerceIn(0, 255)

                            if (digitsOnly.isBlank()) {
                                editorValues[index] = ""
                            } else {
                                editorValues[index] = finalValue.toString()
                            }

                            val normalized = normalizeFrequencyParams(MAIN_CONFIG.value.solenoids.frequencyParams0x68)
                            normalized[index] = finalValue
                            MAIN_CONFIG.value = MAIN_CONFIG.value.copy(
                                solenoids = MAIN_CONFIG.value.solenoids.copy(
                                    frequencyParams0x68 = normalized.toMutableList()
                                )
                            )
                        },
                        singleLine = true,
                        label = { Text(trf("freq_param_label", index + 1)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

private fun normalizeFrequencyParams(source: List<Int>): MutableList<Int> {
    return MutableList(10) { idx -> source.getOrNull(idx)?.coerceIn(0, 255) ?: 0 }
}

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ToggleSpec(
    val label: String,
    val checked: Boolean,
    val onCheckedChange: (Boolean) -> Unit,
    val info: String
)

@Composable
fun TogglesPlate(
    modifier: Modifier = Modifier,
    toggles: List<ToggleSpec>,
    onExportPdf: (() -> Unit)? = null,
    onExportPdfTo1C: (() -> Unit)? = null,
    isExporting: Boolean = false,
    isExportingTo1C: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.width(164.dp)
            .background(Color(0xE6000000), RoundedCornerShape(10.dp))
            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(10.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = 1.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("View options", color = Color.White, fontSize = 9.sp)
            Text(if (expanded) "▼" else "▶", color = Color.White, fontSize = 9.sp)
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                toggles.forEach { spec ->
                    ToggleRowWithInfo(spec)
                }

                if (onExportPdf != null) {
                    Button(
                        onClick = onExportPdf,
                        enabled = !isExporting,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 26.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1976D2),
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFF666666),
                            disabledContentColor = Color(0xFFAAAAAA)
                        ),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        if (isExporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = if (isExporting) "Экспорт..." else "Экспорт PDF",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (onExportPdfTo1C != null) {
                    Button(
                        onClick = onExportPdfTo1C,
                        enabled = false,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 26.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF444444),
                            contentColor = Color(0xFF888888),
                            disabledContainerColor = Color(0xFF444444),
                            disabledContentColor = Color(0xFF888888)
                        ),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        if (isExportingTo1C) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                color = Color(0xFFAAAAAA),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = if (isExportingTo1C) "Отправка..." else "Экспорт PDF в 1С",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ToggleRowWithInfo(spec: ToggleSpec) {
    var showInfo by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Switch(
                modifier = Modifier.scale(0.72f),
                checked = spec.checked,
                onCheckedChange = spec.onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF1976D2)
                )
            )
            Text(spec.label, color = Color.White, fontSize = 9.sp)

            // Info pill — click to expand/collapse description
            Box(
                modifier = Modifier
                    .padding(start = 2.dp)
                    .background(Color(0x33FFFFFF), RoundedCornerShape(8.dp))
                    .clickable { showInfo = !showInfo }
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            ) {
                Text("i", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        AnimatedVisibility(
            visible = showInfo,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xCC1E1E1E), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(8.dp))
                    .padding(6.dp)
            ) {
                Text(
                    text = spec.info,
                    color = Color(0xFFEFEFEF),
                    fontSize = 9.sp,
                    lineHeight = 12.sp
                )
            }
        }
    }
}

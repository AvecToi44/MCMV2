package ru.atrs.mcm.ui.chartsv3

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

// Small data holder to drive each header slot
data class HeaderSlot(
    val label: String,
    val path: String?,
    val result: ResultOrNull,
    val visibility: List<Boolean>,
    val onPick: (String) -> Unit,
    val onClear: () -> Unit,
    val onToggleAll: () -> Unit,
    val onToggleIdx: (Int) -> Unit
)

@Composable
fun HeaderBar(
    modifier: Modifier = Modifier,
    slots: List<HeaderSlot>,
    colors: List<Color>
) {
    // One tight row: [Pick btn] [filename or error] [chips...] [Show/Clear]
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        slots.forEach { slot ->
            CompactFileSlot(slot, colors)
        }
        // Spacer()
    }
}

@Composable
private fun CompactFileSlot(
    slot: HeaderSlot,
    colors: List<Color>
) {
    Row(
        modifier = Modifier
            .border(1.dp, Color(0x22222222), RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Pick
        TinyActionBtn(slot.label) {
            FileDialog(null as Frame?, "Select ${slot.label}", FileDialog.LOAD).apply {
                isVisible = true
                file?.let { filePath ->
                    val dir = directory ?: ""
                    val full = if (dir.endsWith(File.separator)) "$dir$filePath" else "$dir${File.separator}$filePath"
                    slot.onPick(full)
                }
            }
        }

        // File name / status (very compact)
        when (slot.result) {
            is ResultOrNull.Loading -> Text("...", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.widthIn(max = 130.dp))
            is ResultOrNull.Failure -> {
                val name = slot.path?.let { File(it).name } ?: "(none)"
                Text(
                    "$name — ${slot.result.message}",
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color(0xFFB00020),
                    modifier = Modifier.widthIn(max = 210.dp)
                )
            }
            is ResultOrNull.Success -> {
                val cd = slot.result.chartData
                Text(
                    cd.fileName,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 210.dp)
                )
            }
        }

        // Chips (horizontal, compact). Use per-channel color.
        if (slot.result is ResultOrNull.Success) {
            val cd = slot.result.chartData
            val scroll = rememberScrollState()
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(scroll),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                cd.series.forEachIndexed { idx, series ->
                    if (series.isEmpty()) return@forEachIndexed
                    val on = slot.visibility.getOrNull(idx) == true
                    val c = colors[idx % colors.size]
                    CompactChip(
                        label = "Ch${idx + 1}",
                        color = c,
                        active = on
                    ) { slot.onToggleIdx(idx) }
                }
            }
        }

        // Show/Clear tiny buttons
        if (slot.result is ResultOrNull.Success) {
            TinyActionBtn(if (slot.visibility.any { it }) "Hide" else "Show") { slot.onToggleAll() }
            TinyActionBtn("Clear") { slot.onClear() }
        }
    }
}

@Composable
private fun TinyActionBtn(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(Color.Black, RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text, fontSize = 10.sp, color = Color.White)
    }
}

@Composable
private fun CompactChip(
    label: String,
    color: Color,
    active: Boolean,
    onClick: () -> Unit
) {
    val bg = if (active) color.copy(alpha = 0.85f) else color.copy(alpha = 0.25f)
    val border = if (active) Color.Black else Color.White
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(5.dp))
            .clickable { onClick() }
            .border(1.dp, border, RoundedCornerShape(5.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Text(label, fontSize = 10.sp, color = Color.White)
    }
}

@Composable
fun StickyHint() {
    val showHint = remember { mutableStateOf(true) }
    if (showHint.value) {
        Box(
            modifier = Modifier
                .padding(8.dp)
                .background(Color(0xAA000000), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp).clickable {
                    showHint.value = !showHint.value
                }
        ) {
            Text(
                "Zoom: Ctrl + Mouse Wheel\nHorizontal pan: Shift + Mouse Wheel",
                fontSize = 11.sp,
                color = Color.White,
                lineHeight = 14.sp
            )
        }
    }

}

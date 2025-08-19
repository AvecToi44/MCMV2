package org.atrsx.wizardscena.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TextFieldDefaults.OutlinedTextFieldDecorationBox
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.util.UUID
import org.atrsx.wizardscena.ScenarioStep
import org.atrsx.wizardscena.scenarios
import org.atrsx.wizardscena.solenoids

/* ==================== Keyboard grid helpers ==================== */

private const val COL_TEXT      = 0
private const val COL_DURATION  = 1
private const val COL_GRADIENT  = 2
private const val COL_CH_START  = 3
private const val COL_CH_END    = 14 // 12 channels => 3..14
private const val COL_ANALOG1   = 15
private const val COL_ANALOG2   = 16
private const val COL_COUNT     = 17

private data class CellId(val row: Int, val col: Int)

private class FocusGrid {
    private val map = mutableMapOf<CellId, FocusRequester>()
    fun requester(id: CellId): FocusRequester = map.getOrPut(id) { FocusRequester() }
}

/** Compute next cell for arrow/tab/enter navigation. Returns null if no move. */
private fun nextCellForKey(
    key: Key,
    shift: Boolean,
    row: Int,
    col: Int,
    rows: Int
): CellId? {
    var r = row
    var c = col
    when (key) {
        Key.Tab -> if (shift) c-- else c++
        Key.DirectionRight -> c++
        Key.DirectionLeft  -> c--
        Key.DirectionDown, Key.Enter -> r++
        Key.DirectionUp -> r--
        else -> return null
    }
    if (r !in 0 until rows) return null
    if (c !in 0 until COL_COUNT) return null
    return CellId(r, c)
}

/* ==================== Screen ==================== */

@Composable
fun MainScenarioScreen() {
    val items = remember { scenarios }
    val listState = rememberLazyListState()
    val focusGrid = remember { FocusGrid() }
    val scope = rememberCoroutineScope()

    // ---- pending focus target (fixes "FocusRequester not initialized") ----
    var pendingFocus by remember { mutableStateOf<CellId?>(null) }

    // ---- clipboard for copy/paste ----
    var clipboard by remember { mutableStateOf<List<ScenarioStep>>(emptyList()) }
    val anySelected by derivedStateOf { items.any { it.isSelected } }
    val anyUnselected by derivedStateOf { items.any { !it.isSelected } }

    Row(Modifier.fillMaxSize()) {

        // Left: table
        Column(
            Modifier.weight(1f)
                .background(Color.White)
                .fillMaxHeight()
        ) {
            LazyColumn(state = listState) {
                itemsIndexed(
                    items = items,
                    key = { _, it -> it.id } // ScenarioStep must have a stable id
                ) { rowIndex, item ->
                    ScenarioStepRow(
                        rowIndex = rowIndex,
                        totalRows = items.size,
                        item = item,
                        focusGrid = focusGrid,
                        pendingFocus = pendingFocus,
                        setPendingFocus = { pendingFocus = it },
                        listScrollTo = { targetRow ->
                            scope.launch { listState.animateScrollToItem(targetRow) }
                        },
                        onItemChange = { updated -> updateScenarioStep(items, updated) }
                    )
                }
            }
        }

        // Right actions
        Column(
            Modifier.width(180.dp).fillMaxHeight().padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    items.add(
                        ScenarioStep(
                            stepTimeMs = 1000,
                            channelValues = MutableList(12) { 0 },
                            analog1 = 0, analog2 = 0,
                            gradientTimeMs = 0, text = "New Step"
                        )
                    )
                    val newRow = items.lastIndex
                    scope.launch { listState.animateScrollToItem(newRow) }
                    pendingFocus = CellId(newRow, COL_TEXT) // queue focus (cell will request after composed)
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Add Step") }

            Button(
                onClick = { items.removeAll { it.isSelected } },
                modifier = Modifier.fillMaxWidth(),
                enabled = anySelected
            ) { Text("Delete") }



            Spacer(Modifier.height(12.dp))

            // Copy / Paste
            Button(
                onClick = {
                    clipboard = items.filter { it.isSelected }.map { deepCloneForClipboard(it) }
                },
                enabled = anySelected,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Copy selected") }

            val selIndices by derivedStateOf { items.withIndex().filter { it.value.isSelected }.map { it.index } }
            val canPaste by derivedStateOf { clipboard.isNotEmpty() && selIndices.isNotEmpty() }

            Button(
                onClick = {
                    // paste BEFORE first selected
                    val insertAt = selIndices.minOrNull() ?: 0
                    val clones = clipboard.map { deepCloneForPaste(it) }
                    items.addAll(insertAt, clones)
                    clones.forEach { pasted -> markSelected(items, pasted.id, true) }
                    scope.launch { listState.animateScrollToItem(insertAt) }
                    pendingFocus = CellId(insertAt, COL_TEXT)
                },
                enabled = canPaste,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Paste BEFORE") }

            Button(
                onClick = {
                    // paste AFTER last selected
                    val last = selIndices.maxOrNull() ?: (items.size - 1)
                    val clones = clipboard.map { deepCloneForPaste(it) }
                    val insertAt = (last + 1).coerceAtMost(items.size)
                    items.addAll(insertAt, clones)
                    clones.forEach { pasted -> markSelected(items, pasted.id, true) }
                    scope.launch { listState.animateScrollToItem(insertAt) }
                    pendingFocus = CellId(insertAt, COL_TEXT)
                },
                enabled = canPaste,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Paste AFTER") }
            Spacer(Modifier.height(20.dp))
            // Select/Deselect all
            Button(
                onClick = { for (i in items.indices) items[i] = items[i].copy(isSelected = true) },
                enabled = items.isNotEmpty() && anyUnselected,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Select All") }

            Button(
                onClick = { for (i in items.indices) items[i] = items[i].copy(isSelected = false) },
                enabled = anySelected,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Deselect") }
        }
    }
}

/* ==================== Row ==================== */

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ScenarioStepRow(
    rowIndex: Int,
    totalRows: Int,
    item: ScenarioStep,
    focusGrid: FocusGrid,
    pendingFocus: CellId?,
    setPendingFocus: (CellId?) -> Unit,
    listScrollTo: (Int) -> Unit,
    onItemChange: (ScenarioStep) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth()
            .height(50.dp)
            .border(1.dp, Color.Black.copy(alpha = 0.7f))
            .background(if (item.isSelected) Color(0xFFE8F5E9) else Color.White),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val cellBorder = Modifier.fillMaxHeight().border(1.dp, Color.Black.copy(alpha = 0.7f))

        // Row index + selection toggle
        Column(
            cellBorder.width(30.dp).height(40.dp)
                .clickable { onItemChange(item.copy(isSelected = !item.isSelected)) }
                .padding(start = 8.dp),
            verticalArrangement = Arrangement.Center
        ) { Text("$rowIndex") }

        // text/comment (col 0)
        var textTF by remember(item.id) { mutableStateOf(TextFieldValue(item.text ?: "")) }
        GridCell(
            id = CellId(rowIndex, COL_TEXT),
            focusGrid = focusGrid,
            pendingFocus = pendingFocus,
            setPendingFocus = setPendingFocus,
            totalRows = totalRows,
            value = textTF,
            width = 120.dp,
            label = "text",
            listScrollTo = listScrollTo,
            numberOnly = false,
            onValueChange = {
                textTF = it
                onItemChange(item.copy(text = it.text))
            }
        )

        // duration (col 1)
        var timeTF by remember(item.id) { mutableStateOf(TextFieldValue(item.stepTimeMs.toString())) }
        GridCell(
            id = CellId(rowIndex, COL_DURATION),
            focusGrid = focusGrid,
            pendingFocus = pendingFocus,
            setPendingFocus = setPendingFocus,
            totalRows = totalRows,
            value = timeTF,
            width = 90.dp,
            label = "duration",
            listScrollTo = listScrollTo,
            numberOnly = true,
            onValueChange = {
                timeTF = it
                it.text.toIntOrNull()?.let { num -> onItemChange(item.copy(stepTimeMs = num)) }
            }
        )

        // gradient (col 2)
        var gradTF by remember(item.id) { mutableStateOf(TextFieldValue(item.gradientTimeMs.toString())) }
        GridCell(
            id = CellId(rowIndex, COL_GRADIENT),
            focusGrid = focusGrid,
            pendingFocus = pendingFocus,
            setPendingFocus = setPendingFocus,
            totalRows = totalRows,
            value = gradTF,
            width = 110.dp,
            label = "gradient time",
            listScrollTo = listScrollTo,
            numberOnly = true,
            onValueChange = {
                gradTF = it
                it.text.toIntOrNull()?.let { num -> onItemChange(item.copy(gradientTimeMs = num)) }
            }
        )

        Spacer(Modifier.width(6.dp))

        // channels 0..11 (cols 3..14)
        val scrollState = rememberScrollState()
        val density = LocalDensity.current
        val itemWidthPx = with(density) { 80.dp.toPx().toInt() }
        val scope = rememberCoroutineScope()

        Row(Modifier.horizontalScroll(scrollState)) {
            repeat(12) { ch ->
                var chTF by remember(item.id to ch) { mutableStateOf(TextFieldValue(item.channelValues[ch].toString())) }
                val col = COL_CH_START + ch
                GridCell(
                    id = CellId(rowIndex, col),
                    focusGrid = focusGrid,
                    pendingFocus = pendingFocus,
                    setPendingFocus = setPendingFocus,
                    totalRows = totalRows,
                    value = chTF,
                    width = 80.dp,
                    label = solenoids.getOrNull(ch)?.displayName ?: "ch$ch",
                    listScrollTo = listScrollTo,
                    numberOnly = true,
                    range = 0..255, // clamp here
                    onValueChange = {
                        chTF = it
                        it.text.toIntOrNull()?.let { num ->
                            val clamped = num.coerceIn(0, 255)
                            val new = item.channelValues.toMutableList()
                            new[ch] = clamped
                            onItemChange(item.copy(channelValues = new))
                        }
                    },
                    extraModifier = Modifier.onFocusChanged {
                        if (it.isFocused) scope.launch { scrollState.animateScrollTo(ch * itemWidthPx) }
                    }
                )
            }

            Spacer(Modifier.width(6.dp))

            // analog 1 (col 15)
            var a1TF by remember(item.id) { mutableStateOf(TextFieldValue((item.analog1 ?: 0).toString())) }
            GridCell(
                id = CellId(rowIndex, COL_ANALOG1),
                focusGrid = focusGrid,
                pendingFocus = pendingFocus,
                setPendingFocus = setPendingFocus,
                totalRows = totalRows,
                value = a1TF,
                width = 90.dp,
                label = "analog 1",
                listScrollTo = listScrollTo,
                numberOnly = true,
                onValueChange = {
                    a1TF = it
                    it.text.toIntOrNull()?.let { num -> onItemChange(item.copy(analog1 = num)) }
                }
            )

            // analog 2 (col 16)
            var a2TF by remember(item.id) { mutableStateOf(TextFieldValue((item.analog2 ?: 0).toString())) }
            GridCell(
                id = CellId(rowIndex, COL_ANALOG2),
                focusGrid = focusGrid,
                pendingFocus = pendingFocus,
                setPendingFocus = setPendingFocus,
                totalRows = totalRows,
                value = a2TF,
                width = 90.dp,
                label = "analog 2",
                listScrollTo = listScrollTo,
                numberOnly = true,
                onValueChange = {
                    a2TF = it
                    it.text.toIntOrNull()?.let { num -> onItemChange(item.copy(analog2 = num)) }
                }
            )
        }
    }
}

/* ==================== Reusable grid cell ==================== */

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun GridCell(
    id: CellId,
    focusGrid: FocusGrid,
    pendingFocus: CellId?,
    setPendingFocus: (CellId?) -> Unit,
    totalRows: Int,
    value: TextFieldValue,
    width: Dp,
    label: String,
    listScrollTo: (Int) -> Unit,
    numberOnly: Boolean,
    range: IntRange? = null,           // use 0..255 for channels
    onValueChange: (TextFieldValue) -> Unit,
    extraModifier: Modifier = Modifier
) {
    val req = remember { focusGrid.requester(id) }

    // If this cell is the target, request focus *after* it's composed
    LaunchedEffect(pendingFocus, id) {
        if (pendingFocus == id) {
            req.requestFocus()
            setPendingFocus(null)
        }
    }

    BasicTextField(
        value = value,
        onValueChange = { tf ->
            if (!numberOnly) {
                onValueChange(tf)
            } else {
                val raw = tf.text
                if (raw.isBlank()) {
                    onValueChange(tf.copy(text = ""))
                } else {
                    val parsed = raw.toIntOrNull()
                    if (parsed != null) {
                        val clamped = range?.let { parsed.coerceIn(it) } ?: parsed
                        val txt = clamped.toString()
                        val fixed = if (txt != raw) TextFieldValue(txt, TextRange(txt.length)) else tf
                        onValueChange(fixed)
                    }
                }
            }
        },
        modifier = Modifier
            .width(width)
            .border(1.dp, Color.Black)
            .fillMaxHeight()
            .focusRequester(req)
            .onPreviewKeyEvent { e ->
                if (e.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                val next = nextCellForKey(
                    key = e.key,
                    shift = e.isShiftPressed,
                    row = id.row,
                    col = id.col,
                    rows = totalRows
                ) ?: return@onPreviewKeyEvent false
                listScrollTo(next.row)
                setPendingFocus(next) // queue focus; target cell will request it safely
                true
            }
            .onFocusChanged {
                if (it.isFocused) {
                    onValueChange(value.copy(selection = TextRange(0, value.text.length)))
                }
            }
            .then(extraModifier),
        textStyle = TextStyle(fontSize = 16.sp),
        singleLine = true,
        decorationBox = { inner ->
            OutlinedTextFieldDecorationBox(
                value = value.text,
                innerTextField = inner,
                enabled = true,
                singleLine = true,
                interactionSource = remember { MutableInteractionSource() },
                colors = TextFieldDefaults.textFieldColors(
                    focusedLabelColor = Color.Blue,
                    unfocusedLabelColor = Color.Transparent,
                    focusedIndicatorColor = Color.Blue,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                contentPadding = PaddingValues(0.dp),
                visualTransformation = VisualTransformation.None,
                label = {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 6.dp, top = 9.dp),
                        maxLines = 1,
                        color = Color.Gray
                    )
                }
            )
        }
    )
}

/* ==================== helpers ==================== */

private fun deepCloneForClipboard(s: ScenarioStep): ScenarioStep =
    s.copy(
        id = UUID.randomUUID().toString(),
        isSelected = false,
        channelValues = s.channelValues.toMutableList()
    )

private fun deepCloneForPaste(s: ScenarioStep): ScenarioStep =
    s.copy(
        id = UUID.randomUUID().toString(),
        isSelected = true, // highlight pasted block
        channelValues = s.channelValues.toMutableList()
    )

private fun markSelected(list: MutableList<ScenarioStep>, id: String, sel: Boolean) {
    val i = list.indexOfFirst { it.id == id }
    if (i != -1) list[i] = list[i].copy(isSelected = sel)
}

fun updateScenarioStep(items: MutableList<ScenarioStep>, updatedItem: ScenarioStep) {
    val index = scenarios.indexOfFirst { it.id == updatedItem.id }
    if (index != -1) scenarios[index] = updatedItem
}

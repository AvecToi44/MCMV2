package ru.atrsx.mcmcomposer.ui

import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.atrsx.mcmcomposer.ScenarioStep
import ru.atrsx.mcmcomposer.scenarios
import kotlin.ranges.coerceIn

var ROWS1 = mutableStateListOf(
    ScenarioStep(
        stepTimeMs = 1000,
        channelValues = MutableList(12) { 0 },
        analog1 = 0,
        analog2 = 0,
        gradientTimeMs = 0,
        text = ""
    ),
    ScenarioStep(stepTimeMs = 1000,  channelValues = MutableList(12){0}, analog1=0, analog2=0, gradientTimeMs=0, text = ""),
    ScenarioStep(stepTimeMs = 1000,  channelValues = MutableList(12){0}, analog1=0, analog2=0, gradientTimeMs=0, text = ""),
)

// ---------- Screen 1: Main Scenario (multi-row, grid, per-row labels) ----------
@Composable
fun MainScenarioScreen() {
    var selected by remember { mutableStateOf(0) }

//    val rowsx = remember {
//        scenarios
//    }

    val items = remember {
        mutableStateListOf(
            ScenarioStep(stepTimeMs = 1000, channelValues = mutableListOf(0, 1, 2), text = "Step 1"),
            ScenarioStep(stepTimeMs = 2000, channelValues = mutableListOf(1, 2, 3), text = "Step 2"),
            ScenarioStep(stepTimeMs = 3000, channelValues = mutableListOf(2, 3, 4), text = "Step 3")
        )
    }

    var editingItemId by remember { mutableStateOf<String?>(null) }
    var editingText by remember { mutableStateOf("") }

    LaunchedEffect(items) {
        println("rows ${items.joinToString()}")
    }

    Row(Modifier.fillMaxSize()) {
        // Left: table + canvas area
        Column(Modifier.weight(1f).background(Color.White).fillMaxHeight()) {
            LazyColumn {
                items(
                    items = items,
                    key = { it.id }
                ) { item ->
                    val isEditing = (editingItemId == item.id)

                    val onToggle = rememberUpdatedState { _: Offset ->
                        val index = items.indexOfFirst { it.id == item.id }
                        if (index != -1) {
                            val current = items[index]
                            items[index] = current.copy(isSelected = !current.isSelected)
                        }
                    }

                    val onEdit = rememberUpdatedState { _: Offset ->
                        editingItemId = item.id
                        editingText = item.text.orEmpty()
                    }

                    ScenarioStepItem(
                        item = item,
                        isEditing = isEditing,
                        editingText = editingText,
                        onTextChange = { editingText = it },
                        onSave = {
                            editingItemId?.let { id ->
                                val index = items.indexOfFirst { it.id == id }
                                if (index != -1 && editingText.isNotBlank()) {
                                    items[index] = items[index].copy(text = editingText)
                                }
                            }
                            editingItemId = null
                            editingText = ""
                        },
                        onToggle = onToggle.value,
                        onEdit = onEdit.value
                    )
                }
            }
        }

        // Right-side actions
        Column(
            Modifier.width(140.dp).fillMaxHeight().padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    items.add(ScenarioStep(
                        stepTimeMs = 777,
                        channelValues = mutableListOf(1,2,3),
                        analog1 = 0,
                        analog2 = 1,
                        gradientTimeMs = 123,
                        text = "New"
                    ))
                    selected = items.lastIndex
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(modifier = Modifier,text = "Add Step") }

            Button(
                onClick = {
                    if (items.isNotEmpty()) {
                        println(">>> rows:${items.joinToString()}  ${selected} ${items.lastIndex}")
                        items.removeAll { it.isSelected }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = items.any { it.isSelected }
            ) { Text("Delete") }

            Spacer(Modifier.height(8.dp))
            SideButton("Copy")
            SideButton("Paste")
        }
    }
}


@Composable
private fun ScenarioStepItem(
    item: ScenarioStep,
    isEditing: Boolean,
    editingText: String,
    onTextChange: (String) -> Unit,
    onSave: () -> Unit,
    onToggle: (Offset) -> Unit,
    onEdit: (Offset) -> Unit
) {
    TextField(
        value = editingText,
        onValueChange = onTextChange,
        textStyle = TextStyle(fontSize = 16.sp),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
//        trailingIcon = {
//            Button(onClick = onSave) { Text("Save") }
//        }
    )
//    if (isEditing) {
//        TextField(
//            value = editingText,
//            onValueChange = onTextChange,
//            textStyle = TextStyle(fontSize = 16.sp),
//            modifier = Modifier.fillMaxWidth(),
//            singleLine = true,
//            trailingIcon = {
//                Button(onClick = onSave) { Text("Save") }
//            }
//        )
//    } else {
//        Text(
//            text = item.text ?: "Step ${item.stepTimeMs}ms",
//            fontSize = 16.sp,
//            modifier = Modifier
//                .fillMaxWidth()
//                .background(if (item.isSelected) Color.LightGray else Color.Transparent)
//                .pointerInput(Unit) {
//                    detectTapGestures(
//                        onTap = onToggle,
//                        onDoubleTap = onEdit
//                    )
//                }
//        )
//    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ScenarioRowItem(
    row: ScenarioStep,
    selected: Boolean,
    onSelect: () -> Unit,
    index: Int
) {
    // same widths as header
    val wNumber = 30.dp
    val wName = 140.dp

    val ModifierCellBorder = Modifier.fillMaxHeight()//.border(1.dp, Color.Black.copy(alpha = 0.7f))
    val rowBg = if (selected) Color(0xFFBFE6FF) else Color.White

    Row(Modifier.fillMaxWidth().height(50.dp).border(1.dp, Color.Black.copy(alpha = 0.7f))
            .background(rowBg)
            .clickable { onSelect() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Number (read-only)
        Column(ModifierCellBorder.width(wNumber).height(40.dp).padding(start = 8.dp), verticalArrangement = Arrangement.Center) {
            Text("${index}")
        }

        // Name
        var nm by remember { mutableStateOf(row.text) }
        TextField("${nm}", {
                nm = it
                ROWS1[index].text = it
            },
            modifier = ModifierCellBorder.width(wName),
            //label = { Text("Name", fontSize = 9.sp) },
            textStyle = TextStyle(fontSize = 15.sp),
            singleLine = true
        )

        // Pass Through
//        var pass by remember { mutableStateOf(row.passThrough) }
//        Row(ModifierCellBorder.width(wPass).height(40.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
//            Checkbox(checked = pass, onCheckedChange = { pass = it; row.passThrough = it })
//        }
//
//        // Duration
//        var dur by remember { mutableStateOf(row.durationMs) }
//        TextField(
//            dur, { dur = it; row.durationMs = it },
//            modifier = ModifierCellBorder.width(wName),
//            //label = { Text("Name", fontSize = 9.sp) },
//            textStyle = TextStyle(fontSize = 15.sp),
//            singleLine = true
//        )
//        Spacer(modifier = Modifier.width(5.dp))
//        // Message
//        var msg by remember { mutableStateOf(row.messageText) }
//        TextField(
//            msg, { msg = it; row.messageText = it },
//            modifier = ModifierCellBorder.width(wName),
//            //label = { Text("Name", fontSize = 9.sp) },
//            textStyle = TextStyle(fontSize = 15.sp),
//            singleLine = true
//        )

        // Analog outputs
        Column(
            ModifierCellBorder.fillMaxWidth().height(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
//            var enabled by remember { mutableStateOf(row.analogSetEnabled) }
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                Checkbox(checked = enabled, onCheckedChange = { enabled = it; row.analogSetEnabled = it })
//
//                Spacer(Modifier.width(6.dp))
//                Button(onClick = { /* SET */ }, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)) {
//                    Text("SET")
//                }
//
//
//                repeat(12) {
//                    var currencySet by remember { mutableStateOf(TextFieldValue("")) }
//
//                    BasicTextField(
//                        value = currencySet,
//                        onValueChange = { currencySet = it },
//                        modifier = Modifier
//                            .width(50.dp)
//                            .border(1.dp, Color.Black)
//                            .then(ModifierCellBorder),
//                        textStyle = TextStyle(fontSize = 16.sp),
//                        singleLine = false,
//                        decorationBox = { innerTextField ->
//                            OutlinedTextFieldDecorationBox(
//                                value = currencySet.text,
//                                innerTextField = innerTextField,
//                                enabled = true,
//                                singleLine = false,
////                            visualDensity = VisualDensity.compact,
//                                interactionSource = remember { MutableInteractionSource() },
////                            colors = TextFieldDefaults.colors(
////                                focusedContainerColor = Color.Transparent,
////                                unfocusedContainerColor = Color.Transparent,
////                                focusedIndicatorColor = Color.Transparent,
////                                unfocusedIndicatorColor = Color.Transparent
////                            ),
//                                contentPadding = PaddingValues(0.dp), // Remove internal padding
//                                visualTransformation = VisualTransformation.None
//                            )
//                        }
//                    )
//                }
//            }
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
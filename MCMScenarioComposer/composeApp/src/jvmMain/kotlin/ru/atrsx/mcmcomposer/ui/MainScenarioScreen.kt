package ru.atrsx.mcmcomposer.ui

import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.atrsx.mcmcomposer.ScenarioStep
import ru.atrsx.mcmcomposer.ScenarioStepDto
import ru.atrsx.mcmcomposer.scenarios
import kotlin.ranges.coerceIn

var ROWS1 = mutableStateListOf(
    ScenarioStepDto(1000, MutableList(12){0}, analog1=0, analog2=0, gradientTimeMs=0, text = ""),
    ScenarioStepDto(1000, MutableList(12){0}, analog1=0, analog2=0, gradientTimeMs=0, text = ""),
    ScenarioStepDto(1000, MutableList(12){0}, analog1=0, analog2=0, gradientTimeMs=0, text = ""),
)

// ---------- Screen 1: Main Scenario (multi-row, grid, per-row labels) ----------
@Composable
fun MainScenarioScreen() {
    var selected by remember { mutableStateOf(0) }

    val rowsx = remember {
        scenarios
    }
    LaunchedEffect(rowsx) {
        println("rows ${rowsx.joinToString()}")
    }

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
                    .border(1.dp, Color(0xFFFA0C0C))
            ) {
                stickyHeader {
                    Row(
                        Modifier.fillMaxWidth().background(Color.Gray).height(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(modifier = Modifier, text = "Number", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                }
                items(
                    items = rowsx,
                    key = { it.hashCode() }
                ) { item ->
//                    ScenarioRowItem(
//                        row = item,
//                        selected = selected == idx,
//                        onSelect = { selected = idx }
//                    )
                }
            }

//            LazyColumn(
//                Modifier.fillMaxWidth().horizontalScroll(horizontalScrollState)
//                    //.verticalScroll(vScroll)
//                    .border(1.dp, Color(0xFFFA0C0C))
//            ) {
//                stickyHeader {
//                    // Fixed item at the top
//                    Row(
//                        Modifier.fillMaxWidth().background(Color.Gray).height(20.dp),
//                        verticalAlignment = Alignment.CenterVertically,
//                    ) {
//                        Text(modifier = Modifier, text = "Number", fontSize = 8.sp, fontWeight = FontWeight.Bold)
//                    }
//                }
//                items(
//                    items = rowsx,
//                    key = it.hascode()
//                ) { item ->
//                    Text(" ${item}")
//                }
////                items(
////                    items = scenarios, //rowsx//rows.value.steps
////                    key = { it.hashCode() }
////                ) {  idx, row ->
////                    ScenarioRowItem(
////                        index = idx,
////                        row = row,
////                        selected = selected == idx,
////                        onSelect = { selected = idx }
////                    )
////                }
//            }
        }

        // Right-side actions
        Column(
            Modifier.width(140.dp).fillMaxHeight().padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    rowsx.add(ScenarioStep(
                        stepTimeMs = 777,
                        channelValues = mutableListOf(1,2,3),
                        analog1 = 0,
                        analog2 = 1,
                        gradientTimeMs = 123,
                        text = ""
                    ))
                    selected = rowsx.lastIndex
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(modifier = Modifier,text = "Add Step") }

            Button(
                onClick = {
                    if (rowsx.isNotEmpty()) {
                        println(">>> rows:${rowsx.joinToString()}  ${selected} ${rowsx.lastIndex}")
                        rowsx.removeAt(selected.coerceIn(0, rowsx.lastIndex))
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = rowsx.isNotEmpty()
            ) { Text("Delete") }

            Spacer(Modifier.height(8.dp))
            SideButton("Copy")
            SideButton("Paste")
        }
    }
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
    val wPass = 60.dp

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
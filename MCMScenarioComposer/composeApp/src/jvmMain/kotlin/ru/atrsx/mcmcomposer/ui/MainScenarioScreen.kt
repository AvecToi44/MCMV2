package ru.atrsx.mcmcomposer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TextFieldDefaults.OutlinedTextFieldDecorationBox
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.atrsx.mcmcomposer.ScenarioStep
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import ru.atrsx.mcmcomposer.scenarios

// ---------- Screen 1: Main Scenario (multi-row, grid, per-row labels) ----------
@Composable
fun MainScenarioScreen() {
    var selected by remember { mutableStateOf(0) }

//    val rowsx = remember {
//        scenarios
//    }

    val items = remember {
        scenarios
    }

    var editingItemId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(items) {
        println("rows ${items.joinToString()}")
    }

    Row(Modifier.fillMaxSize()) {
        // Left: table
        Column(Modifier.weight(1f).background(Color.White).fillMaxHeight()) {
            LazyColumn {
                items(
                    items = items,
                    key = { it.id }
                ) { item ->
                    ScenarioStepItem(
                        index = items.indexOf(item),
                        item = item,
                        onItemChange = { scenarioStep ->
                            updateScenarioStep(items,scenarioStep)
                        }
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
                        stepTimeMs = 1000,
                        channelValues = MutableList(12) { 0 },
                        analog1 = 0,
                        analog2 = 1,
                        gradientTimeMs = 0,
                        text = "New Step"
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


@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ScenarioStepItem(
    index: Int,
    item: ScenarioStep,
//    editingText: String,
    onItemChange: (ScenarioStep) -> Unit,
) {
    Row(Modifier.fillMaxWidth().height(50.dp).border(1.dp, Color.Black.copy(alpha = 0.7f))
            .background(if (item.isSelected) Color.Green else Color.White)
//            .clickable { onSelect() }
        ,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val wNumber = 30.dp
        val wName = 140.dp

        val ModifierCellBorder = Modifier.fillMaxHeight()//.border(1.dp, Color.Black.copy(alpha = 0.7f))
        //val rowBg = if (selected) Color(0xFFBFE6FF) else Color.White

        Column(ModifierCellBorder.width(wNumber).height(40.dp).clickable {
            onItemChange(item.copy(isSelected = !item.isSelected))
        }.padding(start = 8.dp), verticalArrangement = Arrangement.Center) {
            Text("${index}")
        }
        // Comment or text
        BasicTextField(
            value = item.text ?: "",
            onValueChange = { newText ->
                onItemChange(item.copy(text = newText))
            },
            modifier = Modifier
                .width(150.dp)
                .border(1.dp, Color.Black)
                .then(ModifierCellBorder),
            textStyle = TextStyle(fontSize = 16.sp),
            singleLine = false,
            decorationBox = { innerTextField ->
                OutlinedTextFieldDecorationBox(
                    value = item.text ?: "",
                    innerTextField = innerTextField,
                    enabled = true,
                    singleLine = false,
                    interactionSource = remember { MutableInteractionSource() },
                    colors = TextFieldDefaults.textFieldColors(
                        focusedLabelColor = Color.Blue,
                        unfocusedLabelColor = Color.Transparent,
                        focusedIndicatorColor = Color.Blue,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues(0.dp), // Remove internal padding
                    visualTransformation = VisualTransformation.None
                )
            }
        )
        // Duration
        var stepTime by remember { mutableStateOf(TextFieldValue("${item.stepTimeMs ?: 0}")) }
        BasicTextField(
            value = stepTime,
            onValueChange = { newText ->
                stepTime = newText
                if (newText.text.toIntOrNull() != null && (newText.text.isNotBlank() || newText.text.isNotEmpty())) {
                    stepTime = newText
                    onItemChange(item.copy(stepTimeMs = newText.text.toInt()))
                } else {
                    stepTime = TextFieldValue("0")
                    onItemChange(item.copy(stepTimeMs =0))
                }
            },
            modifier = Modifier
                .width(100.dp)
                .border(1.dp, Color.Black)
                .then(ModifierCellBorder),
            textStyle = TextStyle(fontSize = 16.sp),
            singleLine = false,
            decorationBox = { innerTextField ->
                OutlinedTextFieldDecorationBox(
                    value = "${item.stepTimeMs ?: 0}",
                    innerTextField = innerTextField,
                    enabled = true,
                    singleLine = false,
//                            visualDensity = VisualDensity.compact,
                    interactionSource = remember { MutableInteractionSource() },
                    colors = TextFieldDefaults.textFieldColors(
                        focusedLabelColor = Color.Blue,
                        unfocusedLabelColor = Color.Transparent,
                        focusedIndicatorColor = Color.Blue,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues(0.dp), // Remove internal padding
                    visualTransformation = VisualTransformation.None,
                    label = {
                        Text(text = "duration", fontSize = 14.sp, modifier =  Modifier.align(Alignment.Top).fillMaxWidth().padding(top = 13.dp), textAlign = TextAlign.Center, color = Color.Gray)
                    }

                )
            }
        )
        Spacer(modifier = Modifier.width(5.dp))
        var gradientTime by remember { mutableStateOf(TextFieldValue("${item.gradientTimeMs ?: 0}")) }
        BasicTextField(
            value = gradientTime,
            onValueChange = { newText ->
                gradientTime = newText
                if (newText.text.toIntOrNull() != null && (newText.text.isNotBlank() || newText.text.isNotEmpty())) {
                    gradientTime = newText
                    onItemChange(item.copy(gradientTimeMs = newText.text.toInt()))
                } else {
                    gradientTime = TextFieldValue("0")
                    onItemChange(item.copy(gradientTimeMs = 0))
                }
            },
            modifier = Modifier
                .width(100.dp)
                .border(1.dp, Color.Black)
                .then(ModifierCellBorder),
            textStyle = TextStyle(fontSize = 16.sp),
            singleLine = false,
            decorationBox = { innerTextField ->
                OutlinedTextFieldDecorationBox(
                    value = "${item.stepTimeMs ?: 0}",
                    innerTextField = innerTextField,
                    enabled = true,
                    singleLine = false,
//                            visualDensity = VisualDensity.compact,
                    interactionSource = remember { MutableInteractionSource() },
                    colors = TextFieldDefaults.textFieldColors(
                        focusedLabelColor = Color.Blue,
                        unfocusedLabelColor = Color.Transparent,
                        focusedIndicatorColor = Color.Blue,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues(0.dp), // Remove internal padding
                    visualTransformation = VisualTransformation.None,
                    label = { Text(text = "step time", fontSize = 14.sp, modifier =  Modifier.align(Alignment.Top).padding(start = 9.dp, top = 9.dp), color = Color.Gray) },
                )
            }
        )
        Spacer(modifier = Modifier.width(1.dp))
        repeat(12) { channel ->
            var currencySet by remember { mutableStateOf(TextFieldValue("${item.channelValues[channel]}")) }

            BasicTextField(
                value = currencySet,
                onValueChange = { newtText ->
                    if (newtText.text.toIntOrNull() != null) {
                        item.channelValues[channel] = newtText.text.toInt()
                        currencySet = newtText
                        onItemChange(item.copy(channelValues = item.channelValues))
                    } else {
                        item.channelValues[channel] = 0
                        onItemChange(item.copy(channelValues = item.channelValues))
                    }
                },
                modifier = Modifier.width(50.dp).border(1.dp, Color.Black).then(ModifierCellBorder),
                textStyle = TextStyle(fontSize = 16.sp),
                singleLine = false,
                decorationBox = { innerTextField ->
                    OutlinedTextFieldDecorationBox(
                        value = currencySet.text,
                        innerTextField = innerTextField,
                        enabled = true,
                        singleLine = false,
//                            visualDensity = VisualDensity.compact,
                        interactionSource = remember { MutableInteractionSource() },
                        colors = TextFieldDefaults.textFieldColors(
                            focusedLabelColor = Color.Blue,
                            unfocusedLabelColor = Color.Transparent,
                            focusedIndicatorColor = Color.Blue,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        contentPadding = PaddingValues(0.dp), // Remove internal padding
                        visualTransformation = VisualTransformation.None,
                        label = { Text(text = "ch ${channel}", fontSize = 14.sp, modifier =  Modifier.align(Alignment.Top).padding(start = 9.dp, top = 9.dp), color = Color.Gray) }
                    )
                },
            )
        }
    }
}

fun updateScenarioStep(items: MutableList<ScenarioStep>, updatedItem: ScenarioStep) {
    val index = scenarios.indexOfFirst { it.id == updatedItem.id }
    if (index != -1) {
        scenarios[index] = updatedItem
    }
    println(">>> ${scenarios.joinToString { "${it.text}" }}")
}


@Composable
private fun SideButton(label: String) {
    Button(
        onClick = { /* action */ },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(6.dp)
    ) { Text(label) }
}
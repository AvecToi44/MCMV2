package ru.atrsx.mcmcomposer

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

@Composable
private fun EditableLazyColumn() {
    val items = remember { mutableStateListOf("Item 1", "Item 2", "Item 3") }
    val selectedItems = remember { mutableStateListOf<String>() }
    var editingItem by remember { mutableStateOf<String?>(null) }
    var editingText by remember { mutableStateOf("") }

    Column {
        Button(onClick = { items.add("Item ${items.size + 1}") }) {
            Text("Add Item")
        }
        Button(
            onClick = {
                items.removeAll(selectedItems)
                selectedItems.clear()
            },
            enabled = selectedItems.isNotEmpty()
        ) {
            Text("Remove Selected")
        }

        LazyColumn {
            items(
                items = items,
                key = { item -> item.hashCode() }
            ) { item ->
                val isSelected = item in selectedItems
                val isEditing = item == editingItem

                if (isEditing) {
                    TextField(
                        value = editingText,
                        onValueChange = { editingText = it },
                        textStyle = TextStyle(fontSize = 16.sp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        trailingIcon = {
                            Button(
                                onClick = {
                                    if (editingText.isNotBlank()) {
                                        val index = items.indexOf(item)
                                        items[index] = editingText
                                        if (selectedItems.contains(item)) {
                                            selectedItems.remove(item)
                                            selectedItems.add(editingText)
                                        }
                                    }
                                    editingItem = null
                                    editingText = ""
                                }
                            ) {
                                Text("Save")
                            }
                        }
                    )
                } else {
                    val onToggle = rememberUpdatedState {
                        if (selectedItems.contains(item)) selectedItems.remove(item)
                        else selectedItems.add(item)
                    }
                    val onEdit = rememberUpdatedState {
                        editingItem = item
                        editingText = item
                    }
                    Text(
                        text = item,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isSelected) Color.LightGray else Color.Transparent)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = { onToggle.value() },
                                    onDoubleTap = { onEdit.value() }
                                )
                            }
                    )
                }
            }
        }
    }
}


private data class Item2(var text: String, val selected: Boolean, var comment: String)

@Composable
private fun EditableLazyColumn2() {
    val items = remember { mutableStateListOf(
        Item2(text = "Item 1", selected = false, comment = ""),
        Item2(text = "Item 2", selected = false, comment = ""),
        Item2(text = "Item 3", selected = false, comment = "")
    ) }
    var editingItem by remember { mutableStateOf<Item2?>(null) }
    var editingText by remember { mutableStateOf("") }

    Column {
        Button(onClick = { items.add(Item2("Item ${items.size + 1}", comment = "${(0..1200).random()}", selected = false)) }) {
            Text("Add Item")
        }
        Button(
            onClick = { items.removeAll { it.selected } },
            enabled = items.any { it.selected }
        ) {
            Text("Remove Selected")
        }

        LazyColumn {
            items(
                items = items,
                key = { it.hashCode() }
            ) { item ->
                val isEditing = item == editingItem

                if (isEditing) {
                    TextField(
                        value = editingText,
                        onValueChange = { editingText = it },
                        textStyle = TextStyle(fontSize = 16.sp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        trailingIcon = {
                            Button(
                                onClick = {
                                    if (editingText.isNotBlank()) {
                                        val index = items.indexOf(item)
                                        if (index != -1) {
                                            items[index] = item.copy(text = editingText)
                                        }
                                    }
                                    editingItem = null
                                    editingText = ""
                                }
                            ) {
                                Text("Save")
                            }
                        }
                    )
                } else {
                    val onToggle = rememberUpdatedState {
                        val index = items.indexOf(item)
                        if (index != -1) {
                            items[index] = item.copy(selected = !item.selected)
                        }
                    }
                    val onEdit = rememberUpdatedState {
                        editingItem = item
                        editingText = item.text
                    }
                    Text(
                        text = "${item.text}  ${item.selected}  ${item.comment}",
                        fontSize = 16.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (item.selected) Color.LightGray else Color.Transparent)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = { onToggle.value() },
                                    onDoubleTap = { onEdit.value() }
                                )
                            }
                    )
                }
            }
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Test Console") {
        MaterialTheme {
            Surface(Modifier.fillMaxSize()) {
                EditableLazyColumn2()
            }
        }
    }
}
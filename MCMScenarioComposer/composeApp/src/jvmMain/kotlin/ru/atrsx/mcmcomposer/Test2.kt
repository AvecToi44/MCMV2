import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.util.UUID

data class ScenarioStep2(
    val id: String = UUID.randomUUID().toString(),
    var stepTimeMs: Int,
    var channelValues: MutableList<Int>,
    var analog1: Int? = null,
    var analog2: Int? = null,
    var gradientTimeMs: Int? = null,
    var text: String? = null,
    var isSelected: Boolean = false
)

@Composable
fun ScenarioStepItem(
    item: ScenarioStep2,
    isEditing: Boolean,
    editingText: String,
    onTextChange: (String) -> Unit,
    onSave: () -> Unit,
    onToggle: (Offset) -> Unit,
    onEdit: (Offset) -> Unit
) {
    if (isEditing) {
        TextField(
            value = editingText,
            onValueChange = onTextChange,
            textStyle = TextStyle(fontSize = 16.sp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            trailingIcon = {
                Button(onClick = onSave) { Text("Save") }
            }
        )
    } else {
        Text(
            text = item.text ?: "Step ${item.stepTimeMs}ms",
            fontSize = 16.sp,
            modifier = Modifier
                .fillMaxWidth()
                .background(if (item.isSelected) Color.LightGray else Color.Transparent)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = onToggle,
                        onDoubleTap = onEdit
                    )
                }
        )
    }
}

@Composable
fun EditableScenarioStepList() {
    val items = remember {
        mutableStateListOf(
            ScenarioStep2(stepTimeMs = 1000, channelValues = mutableListOf(0, 1, 2), text = "Step 1"),
            ScenarioStep2(stepTimeMs = 2000, channelValues = mutableListOf(1, 2, 3), text = "Step 2"),
            ScenarioStep2(stepTimeMs = 3000, channelValues = mutableListOf(2, 3, 4), text = "Step 3")
        )
    }

    var editingItemId by remember { mutableStateOf<String?>(null) }
    var editingText by remember { mutableStateOf("") }

    Column {
        Button(onClick = {
            items.add(
                ScenarioStep2(
                    stepTimeMs = 10,
                    channelValues = MutableList(3) { 0 },
                    text = "Step ${items.size + 1}"
                )
            )
        }) { Text("Add Step") }

        Button(
            onClick = { items.removeAll { it.isSelected } },
            enabled = items.any { it.isSelected }
        ) { Text("Remove Selected") }

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
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Test Console") {
        MaterialTheme {
            Surface(Modifier.fillMaxSize()) {
                EditableScenarioStepList()
            }
        }
    }
}

package ru.atrsx.mcmcomposer

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import java.awt.FileDialog
import java.io.File

//@Composable
//fun FilePickerButton(onFileSelected: (String?) -> Unit) {
//    var showDialog by remember { mutableStateOf(false) }
//
//    Button(onClick = { showDialog = true }) {
//        Text("Pick File")
//    }
//
//    if (showDialog) {
//        val dialog = FileDialog(null as java.awt.Frame?, "Select File", FileDialog.LOAD)
//        dialog.isVisible = true
//        val file = dialog.file
//        if (file != null) {
//            onFileSelected(File(dialog.directory, file).absolutePath)
//        } else {
//            onFileSelected(null)
//        }
//        showDialog = false
//    }
//}
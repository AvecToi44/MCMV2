package org.atrsx.wizardscena

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
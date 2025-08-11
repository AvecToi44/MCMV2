package ru.atrs.mcm.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.atrs.mcm.ui.navigation.Screens
import ru.atrs.mcm.ui.screenNav

@Composable
fun Represent() {
    Box(Modifier.fillMaxSize().background(Color.Black).clickable { screenNav.value = Screens.STARTER }) {
        Text("Authors: Eugene Zolotov, Arsen Tagaev \n 2023",
            modifier = Modifier.align(Alignment.Center).padding(4.dp).clickable {
            }, fontSize = 40.sp, fontFamily = FontFamily.Monospace, color = Color.White, textAlign = TextAlign.Center)

    }
}
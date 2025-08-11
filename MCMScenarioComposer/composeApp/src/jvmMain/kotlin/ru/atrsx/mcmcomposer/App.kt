//package ru.atrsx.mcmcomposer
//
//import androidx.compose.animation.AnimatedVisibility
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.safeContentPadding
//import androidx.compose.material3.Button
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextField
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.unit.dp
//import org.jetbrains.compose.resources.painterResource
//import org.jetbrains.compose.ui.tooling.preview.Preview
//
//import mcmscenariocomposer.composeapp.generated.resources.Res
//import mcmscenariocomposer.composeapp.generated.resources.compose_multiplatform
//import ru.atrsx.mcmcomposer.ui.CurrentsScreen
//import ru.atrsx.mcmcomposer.ui.MainScenarioScreen
//import ru.atrsx.mcmcomposer.ui.PressuresScreen
//
//enum class CURRENT_SCREEN { MAIN_SCENARIO, PRESSURES, CURRENTS }
//@Composable
//@Preview
//fun App() {
//    MaterialTheme {
//        var currentScreen by remember { mutableStateOf<CURRENT_SCREEN>(CURRENT_SCREEN.MAIN_SCENARIO) }
//        Column(
//            modifier = Modifier
//                .background(MaterialTheme.colorScheme.primaryContainer)
//                .safeContentPadding()
//                .fillMaxSize(),
//            horizontalAlignment = Alignment.CenterHorizontally,
//        ) {
//            var text by remember { mutableStateOf("") }
//
//            TextField(
//                value = text,
//                onValueChange = { newText -> text = newText },
//                label = { Text("Enter name") }
//            )
//
//            Row(modifier = Modifier.fillMaxWidth().height(90.dp), horizontalArrangement = Arrangement.SpaceBetween) {
//                Box(Modifier.fillMaxSize().weight(1f).clickable {  }) { Text(text = "Main Scenario", modifier = Modifier.align(Alignment.Center)) }
//                Box(Modifier.fillMaxSize().weight(1f).clickable {  }) { Text(text = "Pressures", modifier = Modifier.align(Alignment.Center)) }
//                Box(Modifier.fillMaxSize().weight(1f).clickable {  }) { Text(text = "Currents", modifier = Modifier.align(Alignment.Center)) }
//            }
//
//            Row(Modifier.fillMaxSize().background(Color.Red)) {
//                when (currentScreen) {
//                    CURRENT_SCREEN.MAIN_SCENARIO -> {
//                        MainScenarioScreen()
//                    }
//                    CURRENT_SCREEN.PRESSURES -> {
//                        PressuresScreen()
//                    }
//                    CURRENT_SCREEN.CURRENTS -> {
//                        CurrentsScreen()
//                    }
//                }
//            }
//        }
//    }
//}

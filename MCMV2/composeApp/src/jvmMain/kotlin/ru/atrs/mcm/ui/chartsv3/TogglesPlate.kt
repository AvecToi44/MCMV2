package ru.atrs.mcm.ui.chartsv3

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TogglesPlate(
    modifier: Modifier = Modifier,
    overlapHalves: Boolean,
    onOverlapChanged: (Boolean) -> Unit
) {
    Column(
        modifier = modifier
            .background(Color(0xE6000000), RoundedCornerShape(10.dp)) // semi-transparent black
            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Title (optional, small)
        Text("View options", color = Color.White, fontSize = 11.sp)

        // Overlap halves switch
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Switch(
                checked = overlapHalves,
                onCheckedChange = onOverlapChanged,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF1976D2)
                )
            )
            Text(
                "Overlap halves",
                color = Color.White,
                fontSize = 8.sp
            )
            //first half uses your normal style; second half uses the same color with alpha ≈ 0.65 and a dotted path effect so the overlap is obvious.
//            Text(
//                "First half uses your normal style; second half uses the same color with alpha ≈ 0.65 and a dotted path effect so the overlap is obvious.",
//                color = Color.White,
//                fontSize = 8.sp
//            )
        }

        // -------- place future toggles here ----------
        // Row { Switch(...); Text("Another option", color = Color.White, fontSize = 12.sp) }
    }
}

package com.paymentoptions.pos.ui.composables._components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paymentoptions.pos.R
import com.paymentoptions.pos.ui.theme.noBorder
import com.paymentoptions.pos.ui.theme.purple50

@Composable
fun NoteChip(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 12.sp,
    fontWeight: FontWeight = FontWeight.Light,
    color: Color = purple50,
    icon: Int = R.drawable.hint_bulb,
) {
    AssistChip(
        modifier = modifier,
        onClick = { },
        label = {
            Text(
                text = text,
                fontSize = fontSize,
                fontWeight = fontWeight,
                color = color,
                lineHeight = 16.sp,
                modifier = Modifier.padding(vertical = 6.dp)
            )
        },
        border = noBorder,
        colors = AssistChipDefaults.assistChipColors(containerColor = Color.LightGray.copy(0.2f)),
        leadingIcon = {
            Icon(
                painter = painterResource(icon),
                contentDescription = "Hint",
                tint = Color(0xFFFFC107),
                modifier = Modifier.size(AssistChipDefaults.IconSize)
            )
        })
}
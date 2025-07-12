package com.paymentoptions.pos.ui.composables._components.inputs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paymentoptions.pos.ui.composables.screens.receivemoney.dashedBorder
import com.paymentoptions.pos.ui.theme.AppTheme
import com.paymentoptions.pos.ui.theme.primary100
import com.paymentoptions.pos.ui.theme.purple50
import com.paymentoptions.pos.ui.theme.red500

@Composable
fun DashedBorderInput(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    placeholder: String,
    maxLength: Int = 50,
) {

    Column(modifier = modifier) {
        TextField(
            state = state,
            placeholder = {
                Text(
                    text = placeholder, color = Color.Gray, modifier = Modifier,
                    fontStyle = FontStyle.Italic,
                    fontSize = 12.sp,
                )
            },
            inputTransformation = InputTransformation.maxLength(maxLength),
            colors = TextFieldDefaults.colors().copy(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                unfocusedLabelColor = primary100,
                focusedLabelColor = primary100,
                focusedTextColor = purple50,
                unfocusedTextColor = Color.LightGray,
                errorContainerColor = red500,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .dashedBorder(color = Color.LightGray, shape = RoundedCornerShape(8.dp))
                .bottomStroke(strokeWidth = 0.dp, color = Color.Transparent),
            shape = RoundedCornerShape(8.dp),
            lineLimits = TextFieldLineLimits.SingleLine,

            textStyle = TextStyle(
                fontSize = 16.sp,
                textDecoration = TextDecoration.None,
            ),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        )

        Text(
            text = "Max ${state.text.length}/$maxLength characters",
            modifier = Modifier.fillMaxWidth(),
            style = AppTheme.typography.footnote,
            textAlign = TextAlign.End
        )
    }
}

fun Modifier.bottomStroke(color: Color, strokeWidth: Dp = 2.dp): Modifier = this.then(
    Modifier.drawBehind {
        val strokePx = strokeWidth.toPx()
        // Draw a line at the bottom
        drawLine(
            color = color,
            start = Offset(x = 0f, y = size.height - strokePx / 2),
            end = Offset(x = size.width, y = size.height - strokePx / 2),
            strokeWidth = strokePx
        )
    })
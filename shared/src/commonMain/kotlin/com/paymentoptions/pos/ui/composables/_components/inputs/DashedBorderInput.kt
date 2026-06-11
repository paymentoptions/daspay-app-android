package com.paymentoptions.pos.ui.composables._components.inputs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.material3.Text
import androidx.compose.material3.LocalTextStyle
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
import com.paymentoptions.pos.ui.theme.AppTheme
import com.paymentoptions.pos.ui.theme.primary100
import com.paymentoptions.pos.ui.theme.purple50
import com.paymentoptions.pos.ui.theme.red500
import com.paymentoptions.pos.utils.modifiers.dashedBorder
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment

@Composable
fun DashedBorderInput(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    placeholder: String,
    maxLength: Int = 50,
) {

    Column(modifier = modifier) {
        BasicTextField(
            state = state,
            inputTransformation = InputTransformation.maxLength(maxLength),
            lineLimits = TextFieldLineLimits.SingleLine,
            textStyle = TextStyle(
                fontSize = 16.sp,
                textDecoration = TextDecoration.None,
                color = purple50
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .dashedBorder(color = Color.LightGray, shape = RoundedCornerShape(8.dp))
                .bottomStroke(strokeWidth = 0.dp, color = Color.Transparent),
            decorator = { innerTextField ->
                Box(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (state.text.isEmpty()) {
                        Text(
                            text = placeholder, color = Color.Gray,
                            fontStyle = FontStyle.Italic,
                            fontSize = 12.sp,
                        )
                    }
                    innerTextField()
                }
            }
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

package com.paymentoptions.pos.ui.composables._components.inputs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paymentoptions.pos.ui.theme.primary500
import com.paymentoptions.pos.ui.theme.purple50
import com.paymentoptions.pos.ui.theme.red300

@Composable
fun OutlinedTextInput(
    state: TextFieldState,
    placeholder: String,
    modifier: Modifier = Modifier,
    label: String = "",
    isError: Boolean = false,
    textFieldHeight: Dp = 46.dp,
    onlyDigits : Boolean = false,
    disabled: Boolean = false,
    maxLength: Int = 50,
) {
    var wasFocusedAtLeastOnce by remember { mutableStateOf(false) }

    val error = wasFocusedAtLeastOnce && isError

    Column(modifier = modifier) {

        if (label.isNotEmpty()) {
            Text(
                text = label,
                fontSize = 14.sp,
                color = if (error) red300 else purple50,
                fontWeight = FontWeight.Medium,
            )

            Spacer(modifier = Modifier.height(4.dp))
        }

        Box {
            BasicTextField(
                state = state,
                enabled = !disabled,
                keyboardOptions = if (onlyDigits) KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions.Default,
                lineLimits = TextFieldLineLimits.SingleLine,
                textStyle = LocalTextStyle.current.copy(
                    color = if (error) red300 else if (disabled) purple50.copy(alpha = 0.5f) else primary500,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                ),
                cursorBrush = SolidColor(if (error) red300 else primary500),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(textFieldHeight)
                    .border(
                        width = 1.5.dp,
                        color = if (error) red300 else Color(0xFF90CAF9).copy(alpha = 0.5f),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .background(
                        color = if (error) red300.copy(alpha = 0.1f) else Color.White,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .onFocusChanged { if (it.isFocused) wasFocusedAtLeastOnce = true },
                decorator = { innerTextField ->
                    Box(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (state.text.isEmpty()) {
                            Text(
                                placeholder,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = primary500.copy(alpha = 0.2f)
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
    }
}

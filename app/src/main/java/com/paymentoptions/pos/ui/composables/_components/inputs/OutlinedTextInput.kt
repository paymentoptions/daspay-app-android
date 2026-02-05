package com.paymentoptions.pos.ui.composables._components.inputs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
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
            OutlinedTextField(
                state = state,
                isError = error,
                enabled = !disabled,
                keyboardOptions = if (onlyDigits) KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions.Default,
                placeholder = {
                    Text(
                        placeholder,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = primary500.copy(alpha = 0.2f)
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    focusedTextColor = primary500,
                    focusedIndicatorColor = Color.Transparent,

                    unfocusedContainerColor = Color.White,
                    unfocusedTextColor = purple50,
                    unfocusedIndicatorColor = Color.Transparent,

                    errorContainerColor = red300.copy(alpha = 0.1f),
                    errorTextColor = red300,
                    errorIndicatorColor = Color.Transparent,
                    errorCursorColor = red300.copy(alpha = 0.1f),
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(textFieldHeight)
                    .border(
                        width = 1.5.dp,
                        color = Color(0xFF90CAF9).copy(alpha = 0.5f), // blue outline
                        shape = RoundedCornerShape(6.dp)
                    )
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .onFocusChanged { if (it.isFocused) wasFocusedAtLeastOnce = true }
                , lineLimits = TextFieldLineLimits.SingleLine,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp))
        }
    }
}
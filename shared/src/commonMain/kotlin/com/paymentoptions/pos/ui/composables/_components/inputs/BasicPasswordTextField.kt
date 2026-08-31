package com.paymentoptions.pos.ui.composables._components.inputs

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paymentoptions.pos.ui.theme.innerShadow
import com.paymentoptions.pos.ui.theme.primary500
import com.paymentoptions.pos.ui.theme.purple50
import com.paymentoptions.pos.ui.theme.red300
import com.paymentoptions.pos.utils.modifiers.innerShadow
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
        /***
         * This is for Password entry only(Specially for iOS)
         */
fun BasicPasswordTextInput(
    state: TextFieldState,
    placeholder: String,
    modifier: Modifier = Modifier,
    label: String = "",
    isError: Boolean = false,
    textFieldHeight: Dp = 46.dp,
    disabled: Boolean = false,
) {
    var showText by remember { mutableStateOf(false) }
    var wasFocusedAtLeastOnce by remember { mutableStateOf(false) }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

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
            val commonModifier = Modifier
                .fillMaxWidth()
                .height(textFieldHeight)
                .bringIntoViewRequester(bringIntoViewRequester)
                .onFocusChanged {
                    if (it.isFocused) {
                        wasFocusedAtLeastOnce = true
                        coroutineScope.launch { bringIntoViewRequester.bringIntoView() }
                    }
                }
                .background(
                    color = if (error) red300.copy(alpha = 0.1f) else Color.White,
                    shape = RoundedCornerShape(6.dp),
                )
                .innerShadow(
                    blur = 16.dp,
                    color = innerShadow,
                    cornersRadius = 6.dp,
                    offsetX = 0.5.dp,
                    offsetY = 0.5.dp,
                )

            BasicTextField(
                value = state.text.toString(),
                onValueChange = { newValue ->
                    state.setTextAndPlaceCursorAtEnd(newValue)
                },
                visualTransformation =
                    if (!showText)
                        PasswordVisualTransformation()
                    else
                        VisualTransformation.None,
                textStyle = LocalTextStyle.current.copy(
                    color = if (error) red300 else if (disabled) purple50.copy(alpha = 0.5f) else primary500,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                ),
                cursorBrush = SolidColor(if (error) red300 else primary500),
                modifier = commonModifier,
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        if (state.text.isEmpty()) {
                            Text(
                                text = placeholder,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = primary500.copy(alpha = 0.2f),
                            )
                        }

                        innerTextField()
                    }
                }

            )

            Icon(
                if (showText) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                contentDescription = if (showText) "Hide password" else "Show password",
                tint = Color.Gray,
                modifier = Modifier
                    .align(alignment = Alignment.CenterEnd)
                    .padding(end = 10.dp)
                    .clickable { showText = !showText },
            )
        }
    }
}

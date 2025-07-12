package com.paymentoptions.pos.ui.composables._components.inputs

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paymentoptions.pos.ui.theme.borderThick
import com.paymentoptions.pos.ui.theme.primary100
import com.paymentoptions.pos.ui.theme.primary500
import com.paymentoptions.pos.ui.theme.purple50
import com.paymentoptions.pos.ui.theme.red300

@Composable
fun BasicTextInput(
    state: TextFieldState,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    isSecure: Boolean = false,
    maxLength: Int = 50,
) {
    var showText by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = if (isError) red300 else purple50,
            fontWeight = FontWeight.Medium,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Box {
            TextField(
                state = state,
                isError = isError,
                placeholder = {
                    Text(
                        placeholder,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = primary500.copy(alpha = 0.2f)
                    )
                },
                inputTransformation = InputTransformation.maxLength(maxLength),
                outputTransformation = OutputTransformation {

                    if (isSecure && !showText) replace(
                        0, this.length, '*'.toString().repeat(this.length)
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
                    .height(46.dp)
                    .shadow(
                        elevation = 2.dp, shape = RoundedCornerShape(6.dp),
                        ambientColor = Color.LightGray, spotColor = primary100
                    )
                    .border(
                        border = borderThick, shape = RoundedCornerShape(6.dp)
                    ),
                lineLimits = TextFieldLineLimits.SingleLine,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            )

            if (isSecure) Icon(
                if (showText) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                contentDescription = if (showText) "Hide password" else "Show password",
                tint = Color.Gray,
                modifier = Modifier
                    .align(alignment = Alignment.CenterEnd)
                    .padding(end = 10.dp)
                    .clickable {
                        showText = !showText
                    })
        }
    }
}
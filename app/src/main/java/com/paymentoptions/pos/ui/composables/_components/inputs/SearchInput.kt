package com.paymentoptions.pos.ui.composables._components.inputs

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paymentoptions.pos.ui.theme.borderThin
import com.paymentoptions.pos.ui.theme.primary100
import com.paymentoptions.pos.ui.theme.primary500
import com.paymentoptions.pos.ui.theme.primary900
import com.paymentoptions.pos.ui.theme.purple50
import com.paymentoptions.pos.ui.theme.red300

@Composable
fun SearchInput(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    placeholder: String = "Search",
    isError: Boolean = false,
    maxLength: Int = 50,
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(8.dp),
                ambientColor = Color.LightGray,
                spotColor = primary100
            )
            .border(
                border = borderThin, shape = RoundedCornerShape(8.dp)
            ),
    ) {
        TextField(
            modifier = Modifier.fillMaxSize(),
            state = state,
            isError = isError,
            placeholder = {
                Text(
                    placeholder,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                )
            },
            inputTransformation = InputTransformation.maxLength(maxLength),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                focusedTextColor = primary900,
                focusedIndicatorColor = Color.Transparent,

                unfocusedContainerColor = Color.White,
                unfocusedTextColor = primary500,
                unfocusedIndicatorColor = Color.Transparent,

                errorContainerColor = red300.copy(alpha = 0.1f),
                errorTextColor = red300,
                errorIndicatorColor = Color.Transparent,
                errorCursorColor = red300.copy(alpha = 0.1f),

                focusedPlaceholderColor = purple50,
                unfocusedPlaceholderColor = Color.LightGray
            ),
            lineLimits = TextFieldLineLimits.SingleLine,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        )

        Icon(
            Icons.Default.Search,
            contentDescription = "Search",
            tint = primary500,
            modifier = Modifier
                .align(alignment = Alignment.CenterEnd)
                .padding(end = 13.dp)
                .clickable {
                })
    }
}

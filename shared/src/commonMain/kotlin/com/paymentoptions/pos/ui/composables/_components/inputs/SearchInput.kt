package com.paymentoptions.pos.ui.composables._components.inputs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
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
            )
            .background(Color.White, RoundedCornerShape(8.dp)),
    ) {
        BasicTextField(
            modifier = Modifier.fillMaxSize(),
            state = state,
            inputTransformation = InputTransformation.maxLength(maxLength),
            lineLimits = TextFieldLineLimits.SingleLine,
            textStyle = LocalTextStyle.current.copy(
                color = if (isError) red300 else primary900,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal
            ),
            decorator = { innerTextField ->
                Box(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (state.text.isEmpty()) {
                        Text(
                            placeholder,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.LightGray
                        )
                    }
                    innerTextField()
                }
            }
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

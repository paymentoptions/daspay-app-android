package com.paymentoptions.pos.ui.composables._components.inputs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation


@Composable
fun PasswordInput(
    value: String,
    label: String,
    placeholder: String,
    onChange: (newValue: String) -> Unit,
    modifier: Modifier = Modifier,
    onClickTrailingIcon: () -> Unit = {},
    visible: Boolean = false,
    error: Boolean = false,
    errorText: String = "Error",
) {

    OutlinedTextField(
        value = value,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        singleLine = true,
        modifier = modifier,
        onValueChange = { onChange(it) },
        colors = TextFieldDefaults.colors(
            focusedTextColor = if (error) Color.Red else Color.Blue,
            unfocusedTextColor = if (error) Color.Red else Color.Gray,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            unfocusedLabelColor = if (error) Color.Red else Color.Unspecified,
            focusedLabelColor = if (error) Color.Red else Color.Unspecified,
        ),
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = {
                onClickTrailingIcon()
            }) {
                Icon(
                    imageVector = if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = null
                )
            }
        })
}
package com.paymentoptions.pos.ui.composables._components.inputs

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color


@Composable
fun BasicTextInput(
    value: String,
    label: String,
    placeholder: String,
    onChange: (newValue: String) -> Unit,
    modifier: Modifier = Modifier,
    error: Boolean = false,
    errorText: String = "Error",
) {
    OutlinedTextField(
        value = value,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        singleLine = true,
        modifier = modifier,
        onValueChange = {
            onChange(it)
        },
        colors = TextFieldDefaults.colors(
            focusedTextColor = if (error) Color.Red else Color.Blue,
            unfocusedTextColor = if (error) Color.Red else Color.Gray,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            unfocusedLabelColor = if (error) Color.Red else Color.Unspecified,
            focusedLabelColor = if (error) Color.Red else Color.Unspecified,
        )
    )
}
package com.paymentoptions.pos.ui.composables._components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerModal(
    title: String,
    onDateSelected: (Long?, Long?) -> Unit,
    onDismiss: () -> Unit,
) {
    val dateRangePickerState = rememberDateRangePickerState()

    DatePickerDialog(onDismissRequest = onDismiss, confirmButton = {
        TextButton(
            enabled = dateRangePickerState.selectedStartDateMillis != null && dateRangePickerState.selectedEndDateMillis != null,
            onClick = {
                onDateSelected(
                    dateRangePickerState.selectedStartDateMillis,
                    dateRangePickerState.selectedEndDateMillis
                )
            }) {
            Text("OK")
        }
    }, dismissButton = {
        TextButton(
            onClick = onDismiss
        ) {
            Text("Cancel")
        }
    }) {
        DateRangePicker(state = dateRangePickerState, title = {
            Text(
                text = title,
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth(),
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        })
    }
}
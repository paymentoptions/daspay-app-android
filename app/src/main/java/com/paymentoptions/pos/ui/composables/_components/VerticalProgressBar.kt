package com.paymentoptions.pos.ui.composables._components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paymentoptions.pos.ui.theme.green200
import com.paymentoptions.pos.ui.theme.primary500
import com.paymentoptions.pos.ui.theme.purple50

@Composable
fun VerticalProgressBar(
    currentState: ProgressState,
    submittedTitle: String,
    submittedText: String,
    initiatedTitle: String,
    initiatedText: String,
    processingTitle: String,
    processingText: String,
    completedTitle: String,
    completedText: String,
    modifier: Modifier = Modifier,
    currentStatePathColor: Color = primary500,
    completedStatePathColor: Color = green200,
    pendingStatePathColor: Color = Color.LightGray,
) {

    val requestedTextMeasure = rememberTextMeasurer()

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(start = 5.dp, top = 6.dp)
    ) {

        fun step(
            progressState: ProgressState,
            currentState: ProgressState,
            x: Dp,
            y: Dp,
            title: String,
            text: String,
        ) {

            val progressStepRadius = 4.dp.toPx()
            val pathLineThickness = 1.dp.toPx()
            val isLastStep = progressState === ProgressState.COMPLETED
            val isComplete = progressState < currentState
            val isCurrent = progressState == currentState


            drawCircle(
                color = if (isComplete) completedStatePathColor else if (isCurrent) currentStatePathColor else pendingStatePathColor,
                center = Offset(x.toPx(), y.toPx()),
                radius = progressStepRadius
            )

            if (!isLastStep) drawLine(
                color = if (isComplete) completedStatePathColor else if (isCurrent) currentStatePathColor else pendingStatePathColor,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f),
                start = Offset(x.toPx(), y.toPx()),
                end = Offset(x.toPx(), y.plus(78.dp).toPx()),
                strokeWidth = pathLineThickness
            )

            drawText(
                textMeasurer = requestedTextMeasure,
                text = title,
                topLeft = Offset(x.plus(16.dp).toPx(), y.minus(8.dp).toPx()),
                style = TextStyle(
                    fontSize = 14.sp,
                    color = if (isComplete) completedStatePathColor else currentStatePathColor,
                    fontWeight = FontWeight.Medium
                )
            )

            drawText(
                textMeasurer = requestedTextMeasure,
                text = text,
                topLeft = Offset(x.plus(16.dp).toPx(), y.plus(10.dp).toPx()),
                style = TextStyle(
                    fontSize = 10.sp, color = purple50, fontWeight = FontWeight.Normal
                )
            )
        }

        step(
            progressState = ProgressState.SUBMITTED,
            currentState = currentState,
            x = 0.dp,
            y = 0.dp,
            title = submittedTitle,
            text = submittedText,
        )
        step(
            progressState = ProgressState.INITIATED,
            currentState = currentState,
            x = 0.dp,
            y = 80.dp,
            title = initiatedTitle,
            text = initiatedText,
        )
        step(
            progressState = ProgressState.PROCESSING,
            currentState = currentState,
            x = 0.dp,
            y = 160.dp,
            title = processingTitle,
            text = processingText,
        )
        step(
            progressState = ProgressState.COMPLETED,
            currentState = currentState,
            x = 0.dp,
            y = 240.dp,
            title = completedTitle,
            text = completedText,
        )
    }
}

enum class ProgressState {
    SUBMITTED, INITIATED, PROCESSING, COMPLETED
}
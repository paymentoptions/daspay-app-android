package com.paymentoptions.pos.ui.composables.screens._flow.receiveMoneyFlow

import android.graphics.Bitmap
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.createBitmap
import androidx.navigation.NavController
import com.paymentoptions.pos.ui.composables._components.buttons.FilledButton
import com.paymentoptions.pos.ui.composables._components.buttons.OutlinedButton
import com.paymentoptions.pos.ui.composables._components.screentitle.ScreenTitleWithCloseButton
import com.paymentoptions.pos.ui.composables.layout.sectioned.DEFAULT_BOTTOM_SECTION_PADDING_IN_DP
import com.paymentoptions.pos.ui.theme.AppTheme
import com.paymentoptions.pos.ui.theme.primary500
import com.paymentoptions.pos.ui.theme.primary900
import com.paymentoptions.pos.ui.theme.purple50
import com.paymentoptions.pos.utils.modifiers.dashedBorder
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.math.roundToInt


@Composable
fun TakeDigitalSignatureBottomSectionContent(
    navController: NavController,
    enableScrolling: Boolean = false,
    signaturePath: Path,
    signatureDate: Date,
    updateSignature: (Path, Bitmap?, Date) -> Unit = { _, _, _ -> },
    updateFlowStage: (ReceiveMoneyFlowStage) -> Unit = {},
) {

    var path by remember { mutableStateOf(signaturePath) }
    var saveBitmap by remember { mutableStateOf(false) }
    var isSigned by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(Offset.Unspecified) }
    val density = LocalDensity.current
    val canvasHeight = 300.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        ScreenTitleWithCloseButton(
            navController = navController,
            onClose = { updateFlowStage(ReceiveMoneyFlowStage.TRANSACTION_SUCCESSFUL) })

        Text(
            text = "Signature",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = primary900,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .dashedBorder(color = Color.LightGray, shape = RoundedCornerShape(8.dp))
                .padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                text = "Sign Again",
                onClick = {
                    path = Path()
                    updateSignature(path, null, signatureDate)
                },
                modifier = Modifier
                    .align(alignment = Alignment.End)
                    .height(35.dp)
                    .scale(0.8f)
                    .offset(x = 20.dp)
            )

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White, RoundedCornerShape(4.dp))
                    .clipToBounds()
                    .pointerInput(true) {
                        detectDragGestures(onDragStart = { offset ->
                            path.moveTo(offset.x, offset.y)
                            isSigned = true
                            currentPosition = offset
                        }, onDrag = { change, _ ->
                            path.lineTo(change.position.x, change.position.y)
                            currentPosition = change.position
                            isSigned = true
                        }, onDragEnd = {
                            saveBitmap = true
                        })
                    }) {

//                if (currentPosition != Offset.Unspecified) {
                drawPath(
                    path = path, color = primary500, style = Stroke(
                        width = 4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round
                    )
                )
//                }

                if (saveBitmap) {
                    updateSignature(
                        path, createSignatureBitmap(
                            path, size.width, with(density) { canvasHeight.toPx() }.toInt()
                        ), signatureDate

                    )
                    saveBitmap = false
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        purple50, fontWeight = FontWeight.Medium
                    )
                ) { append("Signing at: ") }

                withStyle(SpanStyle(primary500)) {
                    append(
                        SimpleDateFormat("dd MMMM, YYYY HH:mm:ss").format(
                            signatureDate
                        )
                    )
                }
            }, style = AppTheme.typography.footnote
        )

        Spacer(modifier = Modifier.height(24.dp))

        FilledButton(
            text = "Confirm", onClick = {
                saveBitmap = true
                updateFlowStage(ReceiveMoneyFlowStage.TRANSACTION_SUCCESSFUL)
            }, modifier = Modifier.fillMaxWidth()
        )
    }

}

fun createSignatureBitmap(
    path: Path,
    width: Float,
    height: Int,
): Bitmap {
    val bitmap = createBitmap(width.roundToInt(), height)

    // Create a Canvas to draw on the Bitmap
    val canvas = android.graphics.Canvas(bitmap)
    canvas.drawColor(android.graphics.Color.WHITE)

    // Set up paint for the drawing
    val paint = Paint().apply {
        color = android.graphics.Color.BLACK
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    // Draw the signature path onto the Android Canvas
    canvas.drawPath(path.asAndroidPath(), paint)

    return bitmap
}
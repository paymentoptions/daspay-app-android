package com.paymentoptions.pos.ui.composables.screens._flow.receiveMoneyFlow

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Paint
import android.widget.Toast
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.createBitmap
import androidx.navigation.NavController
import com.google.gson.Gson
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.services.apiService.PaymentDetailsResponse
import com.paymentoptions.pos.services.apiService.endpoints.uploadSignature
import com.paymentoptions.pos.ui.composables._components.MyCircularProgressIndicator
import com.paymentoptions.pos.ui.composables._components.ScreenTitleWithCloseButton
import com.paymentoptions.pos.ui.composables._components.buttons.FilledButton
import com.paymentoptions.pos.ui.composables._components.buttons.OutlinedButton
import com.paymentoptions.pos.ui.composables.layout.sectioned.DEFAULT_BOTTOM_SECTION_PADDING_IN_DP
import com.paymentoptions.pos.ui.theme.AppTheme
import com.paymentoptions.pos.ui.theme.primary500
import com.paymentoptions.pos.ui.theme.primary900
import com.paymentoptions.pos.ui.theme.purple50
import com.paymentoptions.pos.utils.modifiers.dashedBorder
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.math.roundToInt


@Composable
fun TakeDigitalSignatureBottomSectionContent(
    navController: NavController,
    enableScrolling: Boolean = false,
    signaturePath: Path,
    signatureDate: Date,
    paymentDetailsResponse: PaymentDetailsResponse?,
    updateSignature: (Path, Bitmap?, Date) -> Unit = { _, _, _ -> },
    updateFlowStageToSuccess: () -> Unit = {},
) {
    var path by remember { mutableStateOf(signaturePath) }
    var isSigned by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(Offset.Unspecified) }
    LocalDensity.current
//    val canvasHeight = 300.dp
    var canvasWith by remember { mutableStateOf(0f) }
    var canvasHeight by remember { mutableStateOf(0) }
    var startY by remember { mutableStateOf(0f) }
    var isDrawnTopToBottom by remember { mutableStateOf(true) }

    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun resetPath() {
        path = Path()
        updateSignature(path, null, signatureDate)
        isSigned = false
    }

    LaunchedEffect(Unit) {
        resetPath()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(DEFAULT_BOTTOM_SECTION_PADDING_IN_DP),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        ScreenTitleWithCloseButton(
            navController = navController,
            onClose = { updateFlowStageToSuccess() })

        Text(
            text = "Signature",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = primary900,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .dashedBorder(color = Color.Gray, shape = RoundedCornerShape(8.dp))
                .padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                text = "Sign Again",
                onClick = { resetPath() },
                modifier = Modifier
                    .align(alignment = Alignment.End)
                    .height(35.dp)
                    .scale(0.8f)
                    .offset(x = 10.dp)
            )

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .background(Color.Black.copy(alpha = 0.03f), RoundedCornerShape(4.dp))
                    .clipToBounds()
                    .onSizeChanged {
                        canvasWith = it.width.toFloat()
                        canvasHeight = it.height

                    }
                    .pointerInput(true) {
                        detectDragGestures(onDragStart = { offset ->
                            path.moveTo(offset.x, offset.y)
                            currentPosition = offset
                            isSigned = true
                            startY = offset.y //record the starting Y position
                        }, onDrag = { change, _ ->
                            path.lineTo(change.position.x, change.position.y)
                            currentPosition = change.position
                            isSigned = true
                        }, onDragEnd = {
//                            saveBitmap = true
                            isDrawnTopToBottom =
                                currentPosition.y > startY //determine direction when user lifts their finger
                        })
                    }) {

                if (currentPosition != Offset.Unspecified) {
                    drawPath(
                        path = path, color = primary500, style = Stroke(
                            width = 8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round
                        )
                    )
                }

                /*if (saveBitmap) {
                    updateSignature(
                        path, createSignatureBitmap(
                            path, size.width, with(density) { canvasHeight.toPx() }.toInt()
                        ), signatureDate
                    )
                    saveBitmap = false
                }*/
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

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            MyCircularProgressIndicator()
        } else {
            FilledButton(
                text = "Confirm",
                onClick = {
                    val transactionId = resolveTransactionId(paymentDetailsResponse)

                    if (isSigned && transactionId != null) {
                        val signatureBitmap = createSignatureBitmap(
                            path,
                            canvasWith,
                            canvasHeight,
                            isDrawnTopToBottom
                        )
                        updateSignature(path, signatureBitmap, signatureDate)

                        scope.launch {
                            isLoading = true
                            try {
                                val response = uploadSignature(
                                    context = context,
                                    signatureBitmap = signatureBitmap,
                                    transactionId = transactionId
                                )

                                if (response != null && response.success) {
                                    AppLogger.info(
                                        "TakeDigitalSignature",
                                        "Signature upload success for transactionId=$transactionId"
                                    )
                                    Toast.makeText(
                                        context,
                                        "Signature Uploaded Successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    updateFlowStageToSuccess()
                                } else {
                                    AppLogger.warn(
                                        "TakeDigitalSignature",
                                        "Signature upload failed for transactionId=$transactionId, responseSuccess=${response?.success}"
                                    )
                                    Toast.makeText(
                                        context,
                                        "Signature upload failed. Please try again.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            } catch (e: Exception) {
                                AppLogger.error(
                                    "TakeDigitalSignature",
                                    "Signature upload exception for transactionId=$transactionId",
                                    e
                                )
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG)
                                    .show()
                            } finally {
                                isLoading = false
                            }
                        }

                    } else if (transactionId == null) {
                        AppLogger.warn(
                            "TakeDigitalSignature",
                            "Missing transaction id in payment details payload. payloadPresent=${paymentDetailsResponse != null}"
                        )
                        Toast.makeText(
                            context,
                            "Error: Transaction ID not found.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        updateFlowStageToSuccess()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun resolveTransactionId(paymentDetailsResponse: PaymentDetailsResponse?): String? {
    val data = paymentDetailsResponse?.data ?: return null

    return runCatching {
        val transactionId = data.TransactionRefID
        if(transactionId.isNotBlank()) {
            AppLogger.debug(
                "TakeDigitalSignature", "Resolved transaction id from TransactionRefID=$transactionId"
            )
            return@runCatching transactionId
        }

        val dataJson = Gson().toJsonTree(data).asJsonObject

        val directRef = dataJson.get("TransactionRefID")
            ?.takeIf { !it.isJsonNull }
            ?.asString
            ?.takeIf { it.isNotBlank() }

        if (directRef != null) {
            AppLogger.debug("TakeDigitalSignature", "Resolved transaction id from TransactionRefID=$directRef")
            return@runCatching directRef
        }

        AppLogger.warn("TakeDigitalSignature", "TransactionRefID is null/blank, trying TransactionHistory.uuid")

        run {
            val historyArray = dataJson.getAsJsonArray("TransactionHistory")
            var historyUuid: String? = null

            if (historyArray != null) {
                for (entry in historyArray) {
                    val uuid = entry.asJsonObject.get("uuid")
                        ?.takeIf { !it.isJsonNull }
                        ?.asString
                        ?.takeIf { it.isNotBlank() }
                    if (uuid != null) {
                        historyUuid = uuid
                        break
                    }
                }
            }

            if (historyUuid != null) {
                AppLogger.debug("TakeDigitalSignature", "Resolved transaction id from TransactionHistory.uuid=$historyUuid")
            } else {
                AppLogger.warn("TakeDigitalSignature", "TransactionHistory.uuid is also null/blank")
            }

            historyUuid
        }
    }.onFailure {
        AppLogger.error("TakeDigitalSignature", "Failed to resolve transaction id", it)
    }.getOrNull()
}

/*
fun createSignatureBitmap(
    path: Path,
    width: Float,
    height: Int,
    isDrawnTopToBottom: Boolean,
): Bitmap {
    //get the actual bounds of the signature drawing
    val bounds = path.getBounds()

    //create a clean bitmap that is tightly cropped to the signature
    val croppedBitmap = createBitmap(
        bounds.width.roundToInt(),
        bounds.height.roundToInt()
    )
    val canvas = android.graphics.Canvas(croppedBitmap)
    canvas.drawColor(android.graphics.Color.TRANSPARENT)

    //Move the signature path to the top left corner
    val croppedPath = Path().apply { addPath(path) }

    croppedPath.translate(Offset(-bounds.left, -bounds.top))
    //Set up the paint for drawing
    val paint = Paint().apply {
        color = android.graphics.Color.BLACK
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        strokeWidth = 8f
        isAntiAlias = true
    }
    canvas.drawPath(croppedPath.asAndroidPath(), paint)

    //if the signature is taller than it is wide
    if (bounds.height > bounds.width) {
        val matrix = Matrix().apply {
            if (isDrawnTopToBottom) {
                //For toptobottom, rotate counter clockwise
                postRotate(270f)
            } else {
                //For bottomtotop, rotate clockwise
                postRotate(90f)
            }
        }
        return Bitmap.createBitmap(
            croppedBitmap, 0, 0, croppedBitmap.width, croppedBitmap.height, matrix, true
        )
    } else {
        //If signature is already landscape return it as it is
        return croppedBitmap
    }
}
*/

fun createSignatureBitmap(
    path: Path,
    width: Float,
    height: Int,
    isDrawnTopToBottom: Boolean,
): Bitmap {
    val bounds = path.getBounds()

    val croppedBitmap = createBitmap(
        bounds.width.roundToInt(),
        bounds.height.roundToInt()
    )
    val canvas = android.graphics.Canvas(croppedBitmap)
    canvas.drawColor(android.graphics.Color.TRANSPARENT)

    val croppedPath = Path().apply { addPath(path) }
    croppedPath.translate(Offset(-bounds.left, -bounds.top))

    val paint = Paint().apply {
        color = android.graphics.Color.BLACK
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        strokeWidth = 8f
        isAntiAlias = true
    }
    canvas.drawPath(croppedPath.asAndroidPath(), paint)

    // aspect ratio with threshold to determine if signature is vertical
    val aspectRatio = bounds.height / bounds.width

    // If signature is significantly taller than wide threshold of 1.2 or higher
    if (aspectRatio > 1.2f) {
        val signatureCenterX = bounds.left + (bounds.width / 2f)
        val canvasCenterX = width / 2f

        val matrix = Matrix().apply {
            if (signatureCenterX < canvasCenterX) {
                postRotate(270f)
            } else {
                postRotate(90f)
            }
        }

        return Bitmap.createBitmap(
            croppedBitmap, 0, 0, croppedBitmap.width, croppedBitmap.height, matrix, true
        )
    }

    return croppedBitmap
}
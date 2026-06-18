package com.paymentoptions.pos.ui.screens._flow.receiveMoneyFlow

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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.paymentoptions.pos.createSignatureImage
import com.paymentoptions.pos.formatDate
import com.paymentoptions.pos.imageBitmapToByteArray
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.network.PaymentDetailsResponse
import com.paymentoptions.pos.network.PaymentDetailsResponseData
import com.paymentoptions.pos.network.endpoints.uploadSignature
import com.paymentoptions.pos.showToast
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
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive


@Composable
fun TakeDigitalSignatureBottomSectionContent(
    navController: NavController,
    enableScrolling: Boolean = false,
    signaturePath: Path,
    signatureDate: Instant,
    paymentDetailsResponse: PaymentDetailsResponse?,
    updateSignature: (Path, ImageBitmap?, Instant) -> Unit = { _, _, _ -> },
    updateFlowStageToSuccess: () -> Unit = {},
) {
    var path by remember { mutableStateOf(signaturePath) }
    var isSigned by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(Offset.Unspecified) }
    var canvasWith by remember { mutableStateOf(0f) }
    var canvasHeight by remember { mutableStateOf(0) }
    var startY by remember { mutableStateOf(0f) }
    var isDrawnTopToBottom by remember { mutableStateOf(true) }

    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

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
                            startY = offset.y
                        }, onDrag = { change, _ ->
                            path.lineTo(change.position.x, change.position.y)
                            currentPosition = change.position
                            isSigned = true
                        }, onDragEnd = {
                            isDrawnTopToBottom = currentPosition.y > startY
                        })
                    }) {

                if (currentPosition != Offset.Unspecified) {
                    drawPath(
                        path = path, color = primary500, style = Stroke(
                            width = 8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(purple50, fontWeight = FontWeight.Medium)) {
                    append("Signing at: ")
                }
                withStyle(SpanStyle(primary500)) {
                    append(formatDate(signatureDate, "dd MMMM, YYYY HH:mm:ss"))
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
                    val transactionIdValue = resolveTransactionId(paymentDetailsResponse)

                    if (isSigned && transactionIdValue != null) {
                        val signatureBitmap = createSignatureImage(
                            path,
                            canvasWith,
                            canvasHeight,
                            isDrawnTopToBottom
                        )
                        updateSignature(path, signatureBitmap, signatureDate)

                        scope.launch {
                            isLoading = true
                            try {
                                if (signatureBitmap != null) {
                                    val signatureBytes = imageBitmapToByteArray(signatureBitmap)

                                    AppLogger.debug(
                                        "TakeDigitalSignature",
                                        "Uploading signature — bytes=${signatureBytes.size} transactionId=$transactionIdValue"
                                    )

                                    val response = uploadSignature(
                                        signatureBytes = signatureBytes,
                                        transactionId = transactionIdValue
                                    )

                                    if (response != null && response.success) {
                                        AppLogger.debug(
                                            "TakeDigitalSignature",
                                            "Signature uploaded successfully"
                                        )
                                        showToast("Signature Uploaded Successfully")
                                        updateFlowStageToSuccess()
                                    } else {
                                        // ✅ FIX: Surface actual API error message instead of a generic string
                                        val errorMsg = response?.message
                                            ?: "Signature upload failed. Please try again."
                                        AppLogger.error(
                                            "TakeDigitalSignature",
                                            "Upload failed — message=$errorMsg code=${response?.messageCode}"
                                        )
                                        showToast(errorMsg)
                                    }
                                } else {
                                    AppLogger.warn(
                                        "TakeDigitalSignature",
                                        "signatureBitmap is null after createSignatureImage — nothing to upload"
                                    )
                                    showToast("Could not capture signature image. Please try again.")
                                }
                            } catch (e: Exception) {
                                // ✅ FIX: e.message can be null; fallback to class name for clarity
                                val errorMsg = e.message ?: e::class.simpleName ?: "Unknown error"
                                AppLogger.error("TakeDigitalSignature", "Upload exception: $errorMsg", e)
                                showToast("Error: $errorMsg")
                            } finally {
                                isLoading = false
                            }
                        }

                    } else if (transactionIdValue == null) {
                        AppLogger.error("TakeDigitalSignature", "transactionIdValue is null — cannot upload")
                        showToast("Error: Transaction ID not found.")
                    } else {
                        // Not signed — skip upload and proceed
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

    return runCatching<String?> {
        val transactionId = data.TransactionRefID
        if (transactionId.isNotBlank()) {
            AppLogger.debug(
                "TakeDigitalSignature", "Resolved transaction id from TransactionRefID=$transactionId"
            )
            return@runCatching transactionId
        }

        val dataJson = Json.parseToJsonElement(
            Json.encodeToString(PaymentDetailsResponseData.serializer(), data)
        ).jsonObject

        val directRef = dataJson["TransactionRefID"]
            ?.jsonPrimitive
            ?.contentOrNull
            ?.takeIf { it.isNotBlank() }

        if (directRef != null) {
            AppLogger.debug(
                "TakeDigitalSignature", "Resolved transaction id from TransactionRefID=$directRef"
            )
            return@runCatching directRef
        }

        AppLogger.warn(
            "TakeDigitalSignature", "TransactionRefID is null/blank, trying TransactionHistory.uuid"
        )

        val historyUuid = runCatching {
            dataJson["TransactionHistory"]
                ?.jsonArray
                ?.firstOrNull()
                ?.jsonObject
                ?.get("uuid")
                ?.jsonPrimitive
                ?.contentOrNull
                ?.takeIf { it.isNotBlank() }
        }.getOrNull()

        if (historyUuid != null) {
            AppLogger.debug(
                "TakeDigitalSignature",
                "Resolved transaction id from TransactionHistory.uuid=$historyUuid"
            )
            return@runCatching historyUuid
        }

        AppLogger.warn("TakeDigitalSignature", "TransactionHistory.uuid is also null/blank")
        null

    }.onFailure {
        AppLogger.error("TakeDigitalSignature", "Failed to resolve transaction id", it)
    }.getOrNull()
}
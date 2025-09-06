package com.paymentoptions.pos.utils.modifiers

//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.drawWithCache
//import androidx.compose.ui.graphics.ClipOp
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.Path
//import androidx.compose.ui.graphics.Shape
//import androidx.compose.ui.graphics.drawscope.clipPath
//import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
//import androidx.compose.ui.unit.Dp
//import androidx.compose.ui.unit.dp
//import android.graphics.Paint
//
//
//@Composable
//fun Modifier.outerShadow(
//    elevation: Dp,
//    shape: Shape = CircleShape,
//    spotColor: Color = Color.Gray.copy(alpha = 0.25f) // More translucent spot shadow
//): Modifier = this
//    .drawWithCache {
//        // Check if elevation is greater than 0
//        if (elevation > 0.dp) {
//            // Set the blur radius and offsets based on elevation
//            val blurRadiusPx = elevation.toPx()  // Blur radius matches the elevation
//            val dxPx = 0f                              // No horizontal offset
//            val dyPx = (elevation * 0.5f).toPx() // Vertical offset is half the elevation
//
//            // Create outline and path for the shape
//            val outline = shape.createOutline(size, layoutDirection, this)
//            val path = Path().apply { addOutline(outline) }
//
//            // Create shadow paint with the calculated blur radius and offsets
//            val shadowPaint = Paint().apply {
//                asFrameworkPaint().apply {
//                    isAntiAlias = false
//                    setShadowLayer(blurRadiusPx, dxPx, dyPx, spotColor.toArgb())
//                }
//            }
//
//            onDrawWithContent {
//                // Clip the shadow, draw shadow, and then draw content
//                clipPath(path, ClipOp.Difference) {
//                    // Draw the shadow outside the shape
//                    drawIntoCanvas { canvas ->
//                        canvas.drawPath(path, shadowPaint)
//                    }
//                }
//
//                // Draw the actual content inside the shape
//                drawContent()
//            }
//        } else {
//            // If elevation is 0.dp, just draw the content without the shadow
//            onDrawWithContent {
//                drawContent()
//            }
//        }
//    }
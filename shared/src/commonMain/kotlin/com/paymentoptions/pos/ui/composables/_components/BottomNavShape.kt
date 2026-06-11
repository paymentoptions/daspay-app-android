package com.paymentoptions.pos.ui.composables._components

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

class BottomNavShape(
    private val cornerRadius: Float,
    private val dockRadius: Float,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {

        val baseRect = Path().apply {
            addRoundRect(
                RoundRect(
                    Rect(Offset.Zero, Offset(size.width, size.height)),
                    topLeft = CornerRadius(cornerRadius, cornerRadius),
                    topRight = CornerRadius(cornerRadius, cornerRadius),
                ),
            )
        }

        val rect1 = Path().apply {
            addRoundRect(
                RoundRect(
                    Rect(Offset.Zero, Offset(size.width / 2 - dockRadius + 4f, size.height)),
                    topLeft = CornerRadius(cornerRadius, cornerRadius),
                ),
            )
        }

        val rect1A = Path().apply {
            addRoundRect(
                RoundRect(
                    Rect(Offset.Zero, Offset(size.width / 2 - dockRadius + 4f, size.height)),
                    topLeft = CornerRadius(cornerRadius, cornerRadius),
                    topRight = CornerRadius(32f, 32f),
                ),
            )
        }

        val rect1B = Path.combine(
            operation = PathOperation.Difference,
            path1 = rect1,
            path2 = rect1A,
        )

        val rect2 = Path().apply {
            addRoundRect(
                RoundRect(
                    Rect(
                        Offset(size.width / 2 + dockRadius - 4f, 0f),
                        Offset(size.width, size.height)
                    ),
                    topRight = CornerRadius(cornerRadius, cornerRadius),
                ),
            )
        }

        val rect2A = Path().apply {
            addRoundRect(
                RoundRect(
                    Rect(
                        Offset(size.width / 2 + dockRadius - 4f, 0f),
                        Offset(size.width, size.height)
                    ),
                    topRight = CornerRadius(cornerRadius, cornerRadius),
                    topLeft = CornerRadius(32f, 32f),
                ),
            )
        }

        val rect2B = Path.combine(
            operation = PathOperation.Difference,
            path1 = rect2,
            path2 = rect2A,
        )

        val circle = Path().apply {
            addOval(
                Rect(
                    Offset(size.width / 2 - dockRadius, -dockRadius),
                    Offset(size.width / 2 + dockRadius, dockRadius),
                ),
            )
        }

        val path1 = Path.combine(
            operation = PathOperation.Difference,
            path1 = baseRect,
            path2 = circle,
        )

        val path2 = Path.combine(
            operation = PathOperation.Difference,
            path1 = path1,
            path2 = rect1B,
        )

        val path = Path.combine(
            operation = PathOperation.Difference,
            path1 = path2,
            path2 = rect2B,
        )

        return Outline.Generic(path)
    }
}

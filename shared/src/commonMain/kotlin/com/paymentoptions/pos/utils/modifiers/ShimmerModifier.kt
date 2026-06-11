package com.paymentoptions.pos.utils.modifiers

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Creates a shimmer effect modifier for loading states
 */
@Composable
fun Modifier.shimmerEffect(): Modifier {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.3f),
        Color.LightGray.copy(alpha = 0.5f),
        Color.LightGray.copy(alpha = 0.3f),
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim)
    )

    return this.background(brush)
}

/**
 * Shimmer placeholder box with customizable dimensions
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    width: Dp? = null,
    height: Dp = 16.dp,
    cornerRadius: Dp = 8.dp
) {
    Box(
        modifier = modifier
            .then(if (width != null) Modifier.width(width) else Modifier.fillMaxWidth())
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .shimmerEffect()
    )
}

/**
 * Shimmer placeholder for circular items (like icons, avatars)
 */
@Composable
fun ShimmerCircle(
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .shimmerEffect()
    )
}

/**
 * Shimmer skeleton for a food item / product item
 */
@Composable
fun FoodItemShimmer(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Food image placeholder
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(8.dp))
                .shimmerEffect()
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Title
            ShimmerBox(
                width = 120.dp,
                height = 16.dp
            )
            // Description
            ShimmerBox(
                width = 180.dp,
                height = 12.dp
            )
            // Price
            ShimmerBox(
                width = 60.dp,
                height = 14.dp
            )
        }

        // Quantity controls placeholder
        Box(
            modifier = Modifier
                .size(width = 80.dp, height = 32.dp)
                .clip(RoundedCornerShape(16.dp))
                .shimmerEffect()
        )
    }
}

/**
 * Shimmer skeleton for food category chips
 */
@Composable
fun FoodCategoryShimmer(
    modifier: Modifier = Modifier,
    itemCount: Int = 5
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(itemCount) {
            Box(
                modifier = Modifier
                    .width((60 + it * 10).dp)
                    .height(36.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .shimmerEffect()
            )
        }
    }
}

/**
 * Shimmer skeleton for transaction summary item
 */
@Composable
fun TransactionItemShimmer(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon placeholder
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .shimmerEffect()
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Transaction ID
                ShimmerBox(
                    width = 100.dp,
                    height = 14.dp
                )
                // Date/Time
                ShimmerBox(
                    width = 80.dp,
                    height = 12.dp
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Amount
            ShimmerBox(
                width = 70.dp,
                height = 16.dp
            )
            // Status
            ShimmerBox(
                width = 50.dp,
                height = 12.dp
            )
        }
    }
}

/**
 * Shimmer skeleton for cart summary
 */
@Composable
fun CartSummaryShimmer(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(3) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ShimmerBox(width = 80.dp, height = 14.dp)
                ShimmerBox(width = 60.dp, height = 14.dp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Total row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ShimmerBox(width = 60.dp, height = 18.dp)
            ShimmerBox(width = 80.dp, height = 18.dp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Button placeholder
        ShimmerBox(
            height = 48.dp,
            cornerRadius = 24.dp
        )
    }
}

/**
 * Shimmer skeleton for a list of food items
 */
@Composable
fun FoodItemListShimmer(
    modifier: Modifier = Modifier,
    itemCount: Int = 4
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(itemCount) {
            FoodItemShimmer()
        }
    }
}

/**
 * Shimmer skeleton for a list of transactions
 */
@Composable
fun TransactionListShimmer(
    modifier: Modifier = Modifier,
    itemCount: Int = 5
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(itemCount) {
            TransactionItemShimmer()
        }
    }
}

/**
 * Shimmer skeleton for dashboard stats card
 */
@Composable
fun DashboardStatsShimmer(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Main amount
        ShimmerBox(
            width = 150.dp,
            height = 32.dp
        )

        // Stats row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            repeat(3) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ShimmerBox(width = 60.dp, height = 12.dp)
                    ShimmerBox(width = 80.dp, height = 16.dp)
                }
            }
        }
    }
}

/**
 * Shimmer skeleton for notification item
 */
@Composable
fun NotificationItemShimmer(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .shimmerEffect()
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ShimmerBox(width = 180.dp, height = 14.dp)
            ShimmerBox(height = 12.dp)
            ShimmerBox(width = 100.dp, height = 10.dp)
        }
    }
}

/**
 * Shimmer skeleton for notification list
 */
@Composable
fun NotificationListShimmer(
    modifier: Modifier = Modifier,
    itemCount: Int = 5
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(itemCount) {
            NotificationItemShimmer()
        }
    }
}

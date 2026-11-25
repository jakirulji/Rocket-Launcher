/*
 * Copyright (C) 2026 MistOS
 * SPDX-License-Identifier: Apache-2.0
 *
 * Compose adaptation of MistOS Launcher3's ExpressiveShape algorithm:
 * https://github.com/Project-Mist-OS/packages_apps_Launcher3
 * Original implementation: com.android.launcher3.settings.expressive.ExpressiveShape
 */

package app.lawnchair.ui.preferences.components.layout

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin

internal enum class MistExpressiveShape(
    private val lobes: Int,
    private val depth: Float,
) {
    CIRCLE(1, 0f),
    COOKIE(9, 0.07f),
    CLOVER(4, 0.16f),
    BURST(8, 0.10f),
    PENTAGON(5, 0.12f),
    FLOWER(6, 0.12f);

    fun buildPath(size: Size): Path {
        val path = Path()
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val radius = minOf(size.width, size.height) / 2f
        val points = max(96, (radius * 1.5f).roundToInt())

        for (index in 0..points) {
            val angle = 2.0 * PI * index / points
            val pointRadius = radius * (1f - depth + depth * cos(lobes * angle).toFloat())
            val x = centerX + (pointRadius * cos(angle)).toFloat()
            val y = centerY + (pointRadius * sin(angle)).toFloat()
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        path.close()
        return path
    }
}

@Composable
internal fun MistExpressiveGlyph(
    shape: MistExpressiveShape,
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
) {
    Canvas(modifier = modifier.size(size)) {
        drawPath(
            path = shape.buildPath(Size(this.size.width, this.size.height)),
            color = color,
        )
    }
}

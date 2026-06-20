package com.example.kmpllmdemonstration.ui.util

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * 下端を [color]（＝背景色）へフェードさせる。
 *
 * オフスクリーン合成（CompositingStrategy.Offscreen + BlendMode.DstIn）は使わず、
 * 背景色への不透明グラデーションを上から重ねるだけにしている。
 * これにより IME アニメ等でサイズが毎フレーム変化してもオフスクリーン層の
 * 再確保が発生せず、アニメーションが滑らかになる。背景が単色のため見た目は等価。
 */
fun Modifier.fadingEdgeBottom(
    color: Color,
    fractionStart: Float = 0.88f,
): Modifier = this.drawWithCache {
    val brush = Brush.verticalGradient(
        fractionStart to Color.Transparent,
        1f to color,
        startY = 0f,
        endY = size.height,
    )
    onDrawWithContent {
        drawContent()
        drawRect(brush = brush)
    }
}

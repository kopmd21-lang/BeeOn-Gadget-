package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.model.IllustrationType
import com.example.model.ProductVisual
import com.example.ui.theme.ElectricHoney
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.SlateNavy
import com.example.ui.theme.StudioBackdrop
import com.example.ui.theme.WhatsAppEmerald

@Composable
fun ProductArtwork(
    visual: ProductVisual,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    when (visual) {
        is ProductVisual.Photo -> {
            Image(
                painter = painterResource(id = visual.resId),
                contentDescription = contentDescription,
                modifier = modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        is ProductVisual.StudioIllustration -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFF3F5F9), StudioBackdrop)
                        )
                    )
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Subtle radial studio highlight behind product
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White.copy(alpha = 0.85f), Color.Transparent),
                            center = Offset(size.width * 0.5f, size.height * 0.46f),
                            radius = size.minDimension * 0.48f
                        ),
                        center = Offset(size.width * 0.5f, size.height * 0.46f),
                        radius = size.minDimension * 0.48f
                    )

                    // Ground shadow ellipse
                    drawOval(
                        color = MidnightNavy.copy(alpha = 0.12f),
                        topLeft = Offset(size.width * 0.25f, size.height * 0.78f),
                        size = Size(size.width * 0.5f, size.height * 0.08f)
                    )

                    when (visual.type) {
                        IllustrationType.HEADPHONES -> drawHeadphones()
                        IllustrationType.MINI_UPS -> drawMiniUps()
                        IllustrationType.PHONE_STAND -> drawPhoneStand()
                        IllustrationType.CHARGING_CABLE -> drawChargingCable()
                        IllustrationType.ACCESSORIES_COMBO -> drawAccessoriesCombo()
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawHeadphones() {
    val cx = size.width * 0.5f
    val cy = size.height * 0.48f
    val r = size.minDimension * 0.27f

    // Headband outer arc
    drawArc(
        color = MidnightNavy,
        startAngle = 195f,
        sweepAngle = 150f,
        useCenter = false,
        topLeft = Offset(cx - r, cy - r * 1.05f),
        size = Size(r * 2f, r * 2f),
        style = Stroke(width = r * 0.19f, cap = StrokeCap.Round)
    )
    // Inner cushion accent
    drawArc(
        color = ElectricHoney,
        startAngle = 225f,
        sweepAngle = 90f,
        useCenter = false,
        topLeft = Offset(cx - r * 0.92f, cy - r * 0.98f),
        size = Size(r * 1.84f, r * 1.84f),
        style = Stroke(width = r * 0.06f, cap = StrokeCap.Round)
    )

    // Left & right ear cups
    val cupW = r * 0.44f
    val cupH = r * 0.72f
    val leftCupX = cx - r * 1.04f
    val rightCupX = cx + r * 0.60f
    val cupY = cy - r * 0.1f

    listOf(leftCupX, rightCupX).forEach { x ->
        drawRoundRect(
            color = SlateNavy,
            topLeft = Offset(x, cupY),
            size = Size(cupW, cupH),
            cornerRadius = CornerRadius(cupW * 0.45f, cupW * 0.45f)
        )
        drawRoundRect(
            color = MidnightNavy,
            topLeft = Offset(x + cupW * 0.14f, cupY + cupH * 0.12f),
            size = Size(cupW * 0.72f, cupH * 0.76f),
            cornerRadius = CornerRadius(cupW * 0.35f, cupW * 0.35f)
        )
        drawCircle(
            color = ElectricHoney,
            radius = cupW * 0.12f,
            center = Offset(x + cupW * 0.5f, cupY + cupH * 0.5f)
        )
    }
}

private fun DrawScope.drawMiniUps() {
    val w = size.minDimension * 0.56f
    val h = size.minDimension * 0.44f
    val left = (size.width - w) / 2f
    val top = (size.height - h) / 2f - size.height * 0.03f

    // Device chassis
    drawRoundRect(
        color = MidnightNavy,
        topLeft = Offset(left, top),
        size = Size(w, h),
        cornerRadius = CornerRadius(w * 0.12f, w * 0.12f)
    )
    // Top bevel highlight
    drawRoundRect(
        color = SlateNavy,
        topLeft = Offset(left + w * 0.06f, top + h * 0.08f),
        size = Size(w * 0.88f, h * 0.36f),
        cornerRadius = CornerRadius(w * 0.08f, w * 0.08f)
    )
    // Battery indicator LEDs
    val ledY = top + h * 0.26f
    listOf(0.24f, 0.38f, 0.52f, 0.66f).forEachIndexed { idx, frac ->
        drawCircle(
            color = if (idx < 3) WhatsAppEmerald else ElectricHoney,
            radius = w * 0.035f,
            center = Offset(left + w * frac, ledY)
        )
    }
    // Power bolt badge on front
    val boltPath = Path().apply {
        val bx = left + w * 0.52f
        val by = top + h * 0.54f
        moveTo(bx + w * 0.04f, by)
        lineTo(bx - w * 0.06f, by + h * 0.16f)
        lineTo(bx + w * 0.01f, by + h * 0.16f)
        lineTo(bx - w * 0.04f, by + h * 0.33f)
        lineTo(bx + w * 0.08f, by + h * 0.13f)
        lineTo(bx + w * 0.01f, by + h * 0.13f)
        close()
    }
    drawPath(boltPath, color = ElectricHoney)
}

private fun DrawScope.drawPhoneStand() {
    val cx = size.width * 0.5f
    val baseY = size.height * 0.72f
    val baseW = size.minDimension * 0.42f

    // Weighted base
    drawRoundRect(
        color = SlateNavy,
        topLeft = Offset(cx - baseW / 2f, baseY),
        size = Size(baseW, size.minDimension * 0.07f),
        cornerRadius = CornerRadius(12f, 12f)
    )
    // Angled arm
    drawLine(
        color = Color(0xFF94A3B8),
        start = Offset(cx - baseW * 0.1f, baseY),
        end = Offset(cx + baseW * 0.16f, size.height * 0.42f),
        strokeWidth = size.minDimension * 0.06f,
        cap = StrokeCap.Round
    )
    // Smartphone held on stand
    val phoneW = size.minDimension * 0.28f
    val phoneH = size.minDimension * 0.46f
    val px = cx - phoneW * 0.45f
    val py = size.height * 0.22f
    drawRoundRect(
        color = MidnightNavy,
        topLeft = Offset(px, py),
        size = Size(phoneW, phoneH),
        cornerRadius = CornerRadius(phoneW * 0.16f, phoneW * 0.16f)
    )
    // Screen glow
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFF1E3A5F), Color(0xFF0F172A))
        ),
        topLeft = Offset(px + phoneW * 0.08f, py + phoneH * 0.06f),
        size = Size(phoneW * 0.84f, phoneH * 0.88f),
        cornerRadius = CornerRadius(phoneW * 0.1f, phoneW * 0.1f)
    )
    // Yellow UI accent on phone screen
    drawCircle(
        color = ElectricHoney,
        radius = phoneW * 0.16f,
        center = Offset(px + phoneW * 0.5f, py + phoneH * 0.44f)
    )
}

private fun DrawScope.drawChargingCable() {
    val cx = size.width * 0.5f
    val cy = size.height * 0.48f
    val r = size.minDimension * 0.23f

    // Coiled braided cable loop
    drawCircle(
        color = MidnightNavy,
        radius = r,
        center = Offset(cx, cy),
        style = Stroke(width = r * 0.22f)
    )
    drawCircle(
        color = ElectricHoney.copy(alpha = 0.85f),
        radius = r * 0.85f,
        center = Offset(cx, cy),
        style = Stroke(width = r * 0.07f)
    )

    // USB-C connector head
    val headW = r * 0.5f
    val headH = r * 0.65f
    drawRoundRect(
        color = SlateNavy,
        topLeft = Offset(cx + r * 0.65f, cy - headH * 0.5f),
        size = Size(headW, headH),
        cornerRadius = CornerRadius(10f, 10f)
    )
    drawRoundRect(
        color = ElectricHoney,
        topLeft = Offset(cx + r * 0.78f, cy - headH * 0.72f),
        size = Size(headW * 0.48f, headH * 0.26f),
        cornerRadius = CornerRadius(4f, 4f)
    )
}

private fun DrawScope.drawAccessoriesCombo() {
    val w = size.minDimension * 0.52f
    val h = size.minDimension * 0.44f
    val left = (size.width - w) / 2f
    val top = (size.height - h) / 2f

    // Gift/combo box body
    drawRoundRect(
        color = MidnightNavy,
        topLeft = Offset(left, top + h * 0.18f),
        size = Size(w, h * 0.82f),
        cornerRadius = CornerRadius(18f, 18f)
    )
    // Box lid
    drawRoundRect(
        color = SlateNavy,
        topLeft = Offset(left - w * 0.05f, top),
        size = Size(w * 1.1f, h * 0.26f),
        cornerRadius = CornerRadius(14f, 14f)
    )
    // Honey yellow ribbon stripe
    drawRect(
        color = ElectricHoney,
        topLeft = Offset(left + w * 0.43f, top),
        size = Size(w * 0.14f, h)
    )
    // Small circular badge
    drawCircle(
        color = ElectricHoney,
        radius = w * 0.14f,
        center = Offset(left + w * 0.5f, top + h * 0.58f)
    )
    // Checkmark / sparkle inside badge
    val p = Path().apply {
        val bx = left + w * 0.5f
        val by = top + h * 0.58f
        moveTo(bx - w * 0.05f, by)
        lineTo(bx - w * 0.01f, by + w * 0.04f)
        lineTo(bx + w * 0.06f, by - w * 0.04f)
    }
    drawPath(
        path = p,
        color = MidnightNavy,
        style = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}

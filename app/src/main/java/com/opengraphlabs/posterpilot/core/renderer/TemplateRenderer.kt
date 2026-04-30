package com.opengraphlabs.posterpilot.core.renderer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.opengraphlabs.posterpilot.core.model.BusinessProfile
import com.opengraphlabs.posterpilot.core.model.LayerType
import com.opengraphlabs.posterpilot.core.model.PlaceholderBinding
import com.opengraphlabs.posterpilot.core.model.PosterFormat
import com.opengraphlabs.posterpilot.core.model.PosterTemplate
import com.opengraphlabs.posterpilot.core.model.TemplateLayer
import com.opengraphlabs.posterpilot.core.model.TemplateTextStyle

private const val CanvasWidth = 1080f
private const val SquareCanvasHeight = 1080f
private const val StoryCanvasHeight = 1920f

data class TemplateCopy(
    val headline: String = "Special Offer Today",
    val caption: String = "Create beautiful posts for your business in seconds.",
    val cta: String = "Contact us now"
)

@Composable
fun TemplateRenderer(
    template: PosterTemplate,
    businessProfile: BusinessProfile?,
    modifier: Modifier = Modifier,
    copy: TemplateCopy = TemplateCopy()
) {
    val canvasHeight = template.format.canvasHeight()
    val aspectRatio = CanvasWidth / canvasHeight

    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(aspectRatio)
            .background(template.background.backgroundBrush())
            .border(1.dp, Color(0xFFE5E7EB))
    ) {
        val scaleX = maxWidth.value / CanvasWidth
        val scaleY = maxHeight.value / canvasHeight
        val fontScale = minOf(scaleX, scaleY)

        template.layers
            .sortedBy { it.zIndex }
            .forEach { layer ->
                RenderLayerSafely(
                    layer = layer,
                    scaleX = scaleX,
                    scaleY = scaleY,
                    fontScale = fontScale,
                    businessProfile = businessProfile,
                    copy = copy
                )
            }

        if (template.layers.none { it.binding == PlaceholderBinding.LOGO }) {
            LogoPlaceholder(
                businessProfile = businessProfile,
                modifier = Modifier
                    .offset(x = (CanvasWidth - 170f).scaledDp(scaleX), y = 64f.scaledDp(scaleY))
                    .size(104f.scaledDp(minOf(scaleX, scaleY)))
                    .zIndex(10f)
            )
        }
    }
}

@Composable
private fun RenderLayerSafely(
    layer: TemplateLayer,
    scaleX: Float,
    scaleY: Float,
    fontScale: Float,
    businessProfile: BusinessProfile?,
    copy: TemplateCopy
) {
    if (layer.width <= 0 || layer.height <= 0) return

    when (layer.type) {
        LayerType.TEXT -> RenderTextLayer(
            layer = layer,
            scaleX = scaleX,
            scaleY = scaleY,
            fontScale = fontScale,
            businessProfile = businessProfile,
            copy = copy
        )

        LayerType.IMAGE -> {
            if (layer.binding == PlaceholderBinding.LOGO) {
                LogoPlaceholder(
                    businessProfile = businessProfile,
                    modifier = layer.scaledModifier(scaleX, scaleY)
                )
            }
        }

        LayerType.SHAPE -> Unit
    }
}

@Composable
private fun RenderTextLayer(
    layer: TemplateLayer,
    scaleX: Float,
    scaleY: Float,
    fontScale: Float,
    businessProfile: BusinessProfile?,
    copy: TemplateCopy
) {
    val text = layer.binding.resolveText(businessProfile, copy)
    if (text.isBlank()) return

    val textStyle = layer.textStyle ?: return

    Text(
        modifier = layer.scaledModifier(scaleX, scaleY),
        text = text,
        color = parseColor(textStyle.color, Color(0xFF111827)),
        fontSize = (textStyle.fontSize.coerceAtLeast(1) * fontScale).sp,
        fontWeight = textStyle.fontWeight.toComposeFontWeight(),
        textAlign = textStyle.align.toComposeTextAlign(),
        maxLines = textStyle.maxLines.coerceAtLeast(1),
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
private fun LogoPlaceholder(
    businessProfile: BusinessProfile?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(parseColor(businessProfile?.brandColorHex, Color(0xFFF7B733))),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = businessProfile.initials(),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

private fun TemplateLayer.scaledModifier(scaleX: Float, scaleY: Float): Modifier =
    Modifier
        .offset(x = x.toFloat().scaledDp(scaleX), y = y.toFloat().scaledDp(scaleY))
        .size(width = width.toFloat().scaledDp(scaleX), height = height.toFloat().scaledDp(scaleY))
        .zIndex(zIndex.toFloat())

private fun Float.scaledDp(scale: Float): Dp =
    (coerceAtLeast(0f) * scale.coerceAtLeast(0f)).dp

private fun PosterFormat.canvasHeight(): Float =
    when (this) {
        PosterFormat.SQUARE_1_1 -> SquareCanvasHeight
        PosterFormat.STORY_9_16 -> StoryCanvasHeight
    }

private fun com.opengraphlabs.posterpilot.core.model.TemplateBackground.backgroundBrush(): Brush {
    val colors = colors.mapNotNull { parseColorOrNull(it) }.ifEmpty { listOf(Color.White) }
    return if (type.equals("gradient", ignoreCase = true) && colors.size >= 2) {
        Brush.linearGradient(colors)
    } else {
        Brush.linearGradient(listOf(colors.first(), colors.first()))
    }
}

private fun PlaceholderBinding?.resolveText(
    businessProfile: BusinessProfile?,
    copy: TemplateCopy
): String =
    when (this) {
        PlaceholderBinding.HEADLINE -> copy.headline
        PlaceholderBinding.CAPTION -> copy.caption
        PlaceholderBinding.CTA -> copy.cta
        PlaceholderBinding.BUSINESS_NAME -> businessProfile?.businessName.orEmpty().ifBlank { "Your Business" }
        PlaceholderBinding.PHONE -> businessProfile?.phone.orEmpty().ifBlank { "Phone number" }
        PlaceholderBinding.LOGO, null -> ""
    }

private fun BusinessProfile?.initials(): String {
    val name = this?.businessName.orEmpty()
    val initials = name
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercaseChar().toString() }

    return initials.ifBlank { "PP" }
}

private fun String.toComposeFontWeight(): FontWeight =
    when (lowercase()) {
        "bold" -> FontWeight.Bold
        "semibold", "semi_bold" -> FontWeight.SemiBold
        "medium" -> FontWeight.Medium
        "light" -> FontWeight.Light
        else -> FontWeight.Normal
    }

private fun String.toComposeTextAlign(): TextAlign =
    when (lowercase()) {
        "center" -> TextAlign.Center
        "end", "right" -> TextAlign.End
        else -> TextAlign.Start
    }

private fun parseColor(value: String?, fallback: Color): Color =
    parseColorOrNull(value) ?: fallback

private fun parseColorOrNull(value: String?): Color? =
    runCatching {
        if (value.isNullOrBlank()) {
            null
        } else {
            Color(android.graphics.Color.parseColor(value))
        }
    }.getOrNull()

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
import com.opengraphlabs.posterpilot.core.model.PosterDraft
import com.opengraphlabs.posterpilot.core.model.PosterFormat
import com.opengraphlabs.posterpilot.core.model.PosterTemplate
import com.opengraphlabs.posterpilot.core.model.TemplateLayer
import com.opengraphlabs.posterpilot.core.model.TemplateTextStyle

private const val CanvasWidth = 1080f
private const val SquareCanvasHeight = 1080f
private const val StoryCanvasHeight = 1920f

data class TemplateCopy(
    val headline: String = PosterDraft.DefaultHeadline,
    val caption: String = PosterDraft.DefaultCaption,
    val cta: String = PosterDraft.DefaultCta
)

@Composable
fun TemplateRenderer(
    template: PosterTemplate,
    businessProfile: BusinessProfile?,
    modifier: Modifier = Modifier,
    draft: PosterDraft? = null,
    copy: TemplateCopy = TemplateCopy()
) {
    val canvasHeight = template.format.canvasHeight()
    val aspectRatio = CanvasWidth / canvasHeight
    val resolvedCopy = draft.resolveCopy(copy)
    val logoScale = draft?.logoScale?.coerceIn(0.5f, 2f) ?: 1f
    val logoOffsetX = draft?.logoOffsetX ?: 0f
    val logoOffsetY = draft?.logoOffsetY ?: 0f
    val themeColorHex = draft?.themeColorHex?.takeIf { it.isNotBlank() }
        ?: businessProfile?.brandColorHex

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
                    copy = resolvedCopy,
                    themeColorHex = themeColorHex,
                    logoScale = logoScale,
                    logoOffsetX = logoOffsetX,
                    logoOffsetY = logoOffsetY
                )
            }

        if (template.layers.none { it.binding == PlaceholderBinding.LOGO }) {
            LogoPlaceholder(
                businessProfile = businessProfile,
                themeColorHex = themeColorHex,
                modifier = Modifier
                    .offset(
                        x = (CanvasWidth - 170f + logoOffsetX).scaledDp(scaleX),
                        y = (64f + logoOffsetY).scaledDp(scaleY)
                    )
                    .size((104f * logoScale).scaledDp(minOf(scaleX, scaleY)))
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
    copy: TemplateCopy,
    themeColorHex: String?,
    logoScale: Float,
    logoOffsetX: Float,
    logoOffsetY: Float
) {
    if (layer.width <= 0 || layer.height <= 0) return

    when (layer.type) {
        LayerType.TEXT -> RenderTextLayer(
            layer = layer,
            scaleX = scaleX,
            scaleY = scaleY,
            fontScale = fontScale,
            businessProfile = businessProfile,
            copy = copy,
            themeColorHex = themeColorHex
        )

        LayerType.IMAGE -> {
            if (layer.binding == PlaceholderBinding.LOGO) {
                LogoPlaceholder(
                    businessProfile = businessProfile,
                    themeColorHex = themeColorHex,
                    modifier = layer.scaledModifier(
                        scaleX = scaleX,
                        scaleY = scaleY,
                        sizeScale = logoScale,
                        offsetX = logoOffsetX,
                        offsetY = logoOffsetY
                    )
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
    copy: TemplateCopy,
    themeColorHex: String?
) {
    val text = layer.binding.resolveText(businessProfile, copy)
    if (text.isBlank()) return

    val textStyle = layer.textStyle ?: return

    Text(
        modifier = layer.scaledModifier(scaleX, scaleY),
        text = text,
        color = layer.resolveTextColor(textStyle, themeColorHex),
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
    themeColorHex: String?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(parseColor(themeColorHex, Color(0xFFF7B733))),
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

private fun TemplateLayer.scaledModifier(
    scaleX: Float,
    scaleY: Float,
    sizeScale: Float = 1f,
    offsetX: Float = 0f,
    offsetY: Float = 0f
): Modifier =
    Modifier
        .offset(
            x = (x.toFloat() + offsetX).scaledDp(scaleX),
            y = (y.toFloat() + offsetY).scaledDp(scaleY)
        )
        .size(
            width = (width.toFloat() * sizeScale.coerceAtLeast(0.1f)).scaledDp(scaleX),
            height = (height.toFloat() * sizeScale.coerceAtLeast(0.1f)).scaledDp(scaleY)
        )
        .zIndex(zIndex.toFloat())

private fun Float.scaledDp(scale: Float): Dp =
    (this * scale.coerceAtLeast(0f)).dp

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

private fun PosterDraft?.resolveCopy(fallback: TemplateCopy): TemplateCopy =
    TemplateCopy(
        headline = this?.headline?.ifBlank { PosterDraft.DefaultHeadline } ?: fallback.headline,
        caption = this?.caption?.ifBlank { PosterDraft.DefaultCaption } ?: fallback.caption,
        cta = this?.cta?.ifBlank { PosterDraft.DefaultCta } ?: fallback.cta
    )

private fun TemplateLayer.resolveTextColor(
    textStyle: TemplateTextStyle,
    themeColorHex: String?
): Color {
    val themedColor = if (binding == PlaceholderBinding.CTA) {
        parseColorOrNull(themeColorHex)
    } else {
        null
    }

    return themedColor ?: parseColor(textStyle.color, Color(0xFF111827))
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

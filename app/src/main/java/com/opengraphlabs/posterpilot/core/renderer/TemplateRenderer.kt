package com.opengraphlabs.posterpilot.core.renderer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
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
import com.opengraphlabs.posterpilot.core.model.TemplatePillBackground
import com.opengraphlabs.posterpilot.core.model.TemplateShapeStyle
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
            .border(1.dp, Color(0x14000000))
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
fun TemplateRendererPreviewFrame(
    template: PosterTemplate,
    businessProfile: BusinessProfile?,
    maxPreviewHeight: Dp,
    modifier: Modifier = Modifier,
    draft: PosterDraft? = null,
    copy: TemplateCopy = TemplateCopy()
) {
    val canvasHeight = template.format.canvasHeight()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(maxPreviewHeight),
        contentAlignment = Alignment.Center
    ) {
        val scale = minOf(
            maxWidth.value / CanvasWidth,
            maxHeight.value / canvasHeight
        ).coerceAtLeast(0f)

        TemplateRenderer(
            template = template,
            businessProfile = businessProfile,
            draft = draft,
            copy = copy,
            modifier = Modifier.size(
                width = (CanvasWidth * scale).dp,
                height = (canvasHeight * scale).dp
            )
        )
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

        LayerType.SHAPE -> RenderShapeLayer(
            layer = layer,
            scaleX = scaleX,
            scaleY = scaleY,
            themeColorHex = themeColorHex
        )
    }
}

@Composable
private fun RenderShapeLayer(
    layer: TemplateLayer,
    scaleX: Float,
    scaleY: Float,
    themeColorHex: String?
) {
    val style = layer.shapeStyle ?: return
    val brush = style.brush(themeColorHex) ?: return
    val cornerRadius = (style.cornerRadius.toFloat()).scaledDp(minOf(scaleX, scaleY))
    val strokeColor = parseColorOrNull(style.strokeColor)
    val baseModifier = layer.scaledModifier(scaleX, scaleY)
        .clip(RoundedCornerShape(cornerRadius))
        .background(brush)

    val finalModifier = if (strokeColor != null && style.strokeWidth > 0) {
        baseModifier.border(
            width = style.strokeWidth.toFloat().scaledDp(minOf(scaleX, scaleY)),
            color = strokeColor,
            shape = RoundedCornerShape(cornerRadius)
        )
    } else {
        baseModifier
    }
    Box(modifier = finalModifier)
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

    layer.pillBackground?.let { pill ->
        val brush = pill.brush(themeColorHex)
        if (brush != null) {
            val pillX = layer.x - pill.paddingX
            val pillY = layer.y - pill.paddingY
            val pillW = layer.width + pill.paddingX * 2
            val pillH = layer.height + pill.paddingY * 2
            val cornerRadius = pill.cornerRadius.toFloat().scaledDp(minOf(scaleX, scaleY))
            Box(
                modifier = Modifier
                    .offset(
                        x = pillX.toFloat().scaledDp(scaleX),
                        y = pillY.toFloat().scaledDp(scaleY)
                    )
                    .size(
                        width = pillW.toFloat().scaledDp(scaleX),
                        height = pillH.toFloat().scaledDp(scaleY)
                    )
                    .zIndex(layer.zIndex.toFloat() - 0.5f)
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(brush)
            )
        }
    }

    Text(
        modifier = layer.scaledModifier(scaleX, scaleY),
        text = text,
        color = layer.resolveTextColor(textStyle, themeColorHex),
        fontSize = (textStyle.fontSize.coerceAtLeast(1) * fontScale).sp,
        fontWeight = textStyle.fontWeight.toComposeFontWeight(),
        fontFamily = textStyle.fontFamily.toComposeFontFamily(),
        textAlign = textStyle.align.toComposeTextAlign(),
        letterSpacing = (textStyle.letterSpacing * fontScale).sp,
        lineHeight = (textStyle.fontSize.coerceAtLeast(1) * fontScale * 1.05f).sp,
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
            .background(parseColor(themeColorHex, Color(0xFFE8A33D))),
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

private fun TemplateShapeStyle.brush(themeColorHex: String?): Brush? = makeBrush(
    fillType = fillType,
    fillColors = fillColors,
    themeColorHex = themeColorHex
)

private fun TemplatePillBackground.brush(themeColorHex: String?): Brush? = makeBrush(
    fillType = fillType,
    fillColors = fillColors,
    themeColorHex = themeColorHex
)

private fun makeBrush(
    fillType: String,
    fillColors: List<String>,
    themeColorHex: String?
): Brush? {
    val resolved = when (fillType.lowercase()) {
        "themed" -> {
            val themed = parseColorOrNull(themeColorHex)
                ?: parseColorOrNull(fillColors.firstOrNull())
                ?: return null
            listOf(themed, themed)
        }
        "gradient" -> fillColors.mapNotNull { parseColorOrNull(it) }
            .takeIf { it.size >= 2 }
            ?: return null
        else -> {
            val solid = parseColorOrNull(fillColors.firstOrNull()) ?: return null
            listOf(solid, solid)
        }
    }
    return Brush.linearGradient(resolved)
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
    val themedColor = if (
        binding == PlaceholderBinding.CTA &&
        pillBackground == null &&
        textStyle.color.equals("themed", ignoreCase = true).not()
    ) {
        parseColorOrNull(themeColorHex)
    } else if (textStyle.color.equals("themed", ignoreCase = true)) {
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
        "extrabold", "extra_bold" -> FontWeight.ExtraBold
        "black" -> FontWeight.Black
        "semibold", "semi_bold" -> FontWeight.SemiBold
        "medium" -> FontWeight.Medium
        "light" -> FontWeight.Light
        else -> FontWeight.Normal
    }

private fun String.toComposeFontFamily(): FontFamily =
    when (lowercase()) {
        "serif" -> FontFamily.Serif
        "mono", "monospace" -> FontFamily.Monospace
        else -> FontFamily.SansSerif
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
        if (value.isNullOrBlank() || value.equals("themed", ignoreCase = true)) {
            null
        } else {
            Color(android.graphics.Color.parseColor(value))
        }
    }.getOrNull()

package com.opengraphlabs.posterpilot.core.export

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import com.opengraphlabs.posterpilot.core.model.BusinessProfile
import com.opengraphlabs.posterpilot.core.model.LayerType
import com.opengraphlabs.posterpilot.core.model.PlaceholderBinding
import com.opengraphlabs.posterpilot.core.model.PosterDraft
import com.opengraphlabs.posterpilot.core.model.PosterFormat
import com.opengraphlabs.posterpilot.core.model.PosterTemplate
import com.opengraphlabs.posterpilot.core.model.TemplateLayer
import com.opengraphlabs.posterpilot.core.model.TemplateTextStyle
import java.io.File
import java.io.FileOutputStream

class PosterBitmapExporter(private val context: Context) {
    fun export(
        template: PosterTemplate,
        businessProfile: BusinessProfile?,
        draft: PosterDraft
    ): File {
        val width = 1080
        val height = when (template.format) {
            PosterFormat.SQUARE_1_1 -> 1080
            PosterFormat.STORY_9_16 -> 1920
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        drawBackground(canvas, template, width, height)
        template.layers
            .sortedBy { it.zIndex }
            .forEach { layer ->
                runCatching {
                    when (layer.type) {
                        LayerType.TEXT -> drawTextLayer(canvas, layer, businessProfile, draft)
                        LayerType.IMAGE -> if (layer.binding == PlaceholderBinding.LOGO) {
                            drawLogo(canvas, layer, businessProfile, draft)
                        }
                        LayerType.SHAPE -> Unit
                    }
                }
            }

        if (template.layers.none { it.binding == PlaceholderBinding.LOGO }) {
            drawDefaultLogo(canvas, businessProfile, draft)
        }

        val exportDir = File(context.cacheDir, "poster_exports").apply { mkdirs() }
        val outputFile = File(exportDir, "posterpilot_${System.currentTimeMillis()}.png")
        FileOutputStream(outputFile).use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        }
        bitmap.recycle()
        return outputFile
    }

    private fun drawBackground(
        canvas: Canvas,
        template: PosterTemplate,
        width: Int,
        height: Int
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val colors = template.background.colors.mapNotNull { parseColorOrNull(it) }

        if (template.background.type.equals("gradient", ignoreCase = true) && colors.size >= 2) {
            paint.shader = LinearGradient(
                0f,
                0f,
                width.toFloat(),
                height.toFloat(),
                colors.toIntArray(),
                null,
                Shader.TileMode.CLAMP
            )
        } else {
            paint.color = colors.firstOrNull() ?: Color.WHITE
        }

        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    }

    private fun drawTextLayer(
        canvas: Canvas,
        layer: TemplateLayer,
        businessProfile: BusinessProfile?,
        draft: PosterDraft
    ) {
        if (layer.width <= 0 || layer.height <= 0) return

        val textStyle = layer.textStyle ?: return
        val text = layer.binding.resolveText(businessProfile, draft)
        if (text.isBlank()) return

        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = layer.resolveTextColor(textStyle, draft.themeColorHex)
            textSize = textStyle.fontSize.coerceAtLeast(1).toFloat()
            typeface = textStyle.fontWeight.toTypeface()
        }

        val alignment = textStyle.align.toLayoutAlignment()
        val layout = StaticLayout.Builder
            .obtain(text, 0, text.length, textPaint, layer.width.coerceAtLeast(1))
            .setAlignment(alignment)
            .setMaxLines(textStyle.maxLines.coerceAtLeast(1))
            .setEllipsize(TextUtils.TruncateAt.END)
            .setIncludePad(false)
            .build()

        val saveCount = canvas.save()
        canvas.clipRect(
            layer.x.toFloat(),
            layer.y.toFloat(),
            (layer.x + layer.width).toFloat(),
            (layer.y + layer.height).toFloat()
        )
        canvas.translate(layer.x.toFloat(), layer.y.toFloat())
        layout.draw(canvas)
        canvas.restoreToCount(saveCount)
    }

    private fun drawLogo(
        canvas: Canvas,
        layer: TemplateLayer,
        businessProfile: BusinessProfile?,
        draft: PosterDraft
    ) {
        val size = minOf(layer.width, layer.height).toFloat() * draft.logoScale.coerceIn(0.5f, 2f)
        if (size <= 0f) return

        val left = layer.x + draft.logoOffsetX
        val top = layer.y + draft.logoOffsetY
        drawLogoCircle(
            canvas = canvas,
            businessProfile = businessProfile,
            themeColorHex = draft.themeColorHex,
            bounds = RectF(left, top, left + size, top + size)
        )
    }

    private fun drawDefaultLogo(
        canvas: Canvas,
        businessProfile: BusinessProfile?,
        draft: PosterDraft
    ) {
        val size = 104f * draft.logoScale.coerceIn(0.5f, 2f)
        val left = 910f + draft.logoOffsetX
        val top = 64f + draft.logoOffsetY
        drawLogoCircle(
            canvas = canvas,
            businessProfile = businessProfile,
            themeColorHex = draft.themeColorHex,
            bounds = RectF(left, top, left + size, top + size)
        )
    }

    private fun drawLogoCircle(
        canvas: Canvas,
        businessProfile: BusinessProfile?,
        themeColorHex: String?,
        bounds: RectF
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = parseColor(themeColorHex ?: businessProfile?.brandColorHex, Color.rgb(247, 183, 51))
        }
        canvas.drawOval(bounds, paint)

        val initials = businessProfile.initials()
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            textSize = bounds.height() * 0.36f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val centerY = bounds.centerY() - (textPaint.descent() + textPaint.ascent()) / 2f
        canvas.drawText(initials, bounds.centerX(), centerY, textPaint)
    }
}

private fun PlaceholderBinding?.resolveText(
    businessProfile: BusinessProfile?,
    draft: PosterDraft
): String =
    when (this) {
        PlaceholderBinding.HEADLINE -> draft.headline.ifBlank { PosterDraft.DefaultHeadline }
        PlaceholderBinding.CAPTION -> draft.caption.ifBlank { PosterDraft.DefaultCaption }
        PlaceholderBinding.CTA -> draft.cta.ifBlank { PosterDraft.DefaultCta }
        PlaceholderBinding.BUSINESS_NAME -> businessProfile?.businessName.orEmpty().ifBlank { "Your Business" }
        PlaceholderBinding.PHONE -> businessProfile?.phone.orEmpty().ifBlank { "Phone number" }
        PlaceholderBinding.LOGO, null -> ""
    }

private fun TemplateLayer.resolveTextColor(
    textStyle: TemplateTextStyle,
    themeColorHex: String?
): Int {
    val themedColor = if (binding == PlaceholderBinding.CTA) {
        parseColorOrNull(themeColorHex)
    } else {
        null
    }
    return themedColor ?: parseColor(textStyle.color, Color.rgb(17, 24, 39))
}

private fun String.toTypeface(): Typeface =
    when (lowercase()) {
        "bold" -> Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        "semibold", "semi_bold", "medium" -> Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        else -> Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    }

private fun String.toLayoutAlignment(): Layout.Alignment =
    when (lowercase()) {
        "center" -> Layout.Alignment.ALIGN_CENTER
        "end", "right" -> Layout.Alignment.ALIGN_OPPOSITE
        else -> Layout.Alignment.ALIGN_NORMAL
    }

private fun BusinessProfile?.initials(): String {
    val initials = this?.businessName.orEmpty()
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercaseChar().toString() }

    return initials.ifBlank { "PP" }
}

private fun parseColor(value: String?, fallback: Int): Int =
    parseColorOrNull(value) ?: fallback

private fun parseColorOrNull(value: String?): Int? =
    runCatching {
        if (value.isNullOrBlank()) null else Color.parseColor(value)
    }.getOrNull()

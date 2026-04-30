package com.opengraphlabs.posterpilot.core.model

data class PosterTemplate(
    val id: String,
    val title: String,
    val category: PosterCategory,
    val format: PosterFormat,
    val background: TemplateBackground,
    val layers: List<TemplateLayer>
)

enum class PosterFormat(val displayName: String, val aspectRatioLabel: String) {
    SQUARE_1_1(displayName = "Square", aspectRatioLabel = "1:1"),
    STORY_9_16(displayName = "Story", aspectRatioLabel = "9:16")
}

enum class PosterCategory(val displayName: String) {
    FESTIVAL("Festival"),
    SALE_OFFER("Sale/Offer"),
    NEW_ARRIVAL("New Arrival"),
    THANK_YOU("Thank You")
}

data class TemplateLayer(
    val id: String,
    val type: LayerType,
    val binding: PlaceholderBinding?,
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
    val zIndex: Int,
    val textStyle: TemplateTextStyle? = null,
    val shapeStyle: TemplateShapeStyle? = null,
    val pillBackground: TemplatePillBackground? = null
)

enum class LayerType {
    TEXT,
    IMAGE,
    SHAPE
}

enum class PlaceholderBinding(val placeholder: String) {
    BUSINESS_NAME("{{business_name}}"),
    PHONE("{{phone}}"),
    LOGO("{{logo}}"),
    HEADLINE("{{headline}}"),
    CAPTION("{{caption}}"),
    CTA("{{cta}}")
}

data class TemplateBackground(
    val type: String,
    val colors: List<String>
)

data class TemplateTextStyle(
    val fontSize: Int,
    val fontWeight: String,
    val color: String,
    val align: String,
    val maxLines: Int,
    val letterSpacing: Float = 0f,
    val fontFamily: String = "sans"
)

data class TemplateShapeStyle(
    val fillType: String = "solid",
    val fillColors: List<String> = emptyList(),
    val cornerRadius: Int = 0,
    val strokeColor: String? = null,
    val strokeWidth: Int = 0
)

data class TemplatePillBackground(
    val fillType: String = "solid",
    val fillColors: List<String> = emptyList(),
    val cornerRadius: Int = 0,
    val paddingX: Int = 0,
    val paddingY: Int = 0
)

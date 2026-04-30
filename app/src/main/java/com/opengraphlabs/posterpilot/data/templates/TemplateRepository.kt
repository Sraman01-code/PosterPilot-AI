package com.opengraphlabs.posterpilot.data.templates

import android.content.Context
import com.opengraphlabs.posterpilot.core.model.LayerType
import com.opengraphlabs.posterpilot.core.model.PlaceholderBinding
import com.opengraphlabs.posterpilot.core.model.PosterCategory
import com.opengraphlabs.posterpilot.core.model.PosterFormat
import com.opengraphlabs.posterpilot.core.model.PosterTemplate
import com.opengraphlabs.posterpilot.core.model.TemplateBackground
import com.opengraphlabs.posterpilot.core.model.TemplateLayer
import com.opengraphlabs.posterpilot.core.model.TemplatePillBackground
import com.opengraphlabs.posterpilot.core.model.TemplateShapeStyle
import com.opengraphlabs.posterpilot.core.model.TemplateTextStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class TemplateRepository(private val context: Context) {
    suspend fun loadTemplates(): List<PosterTemplate> = withContext(Dispatchers.IO) {
        findTemplatePaths()
            .map { path -> parseTemplate(readAsset(path)) }
            .sortedWith(compareBy({ it.category.ordinal }, { it.format.ordinal }, { it.title }))
    }

    suspend fun getTemplate(templateId: String): PosterTemplate? =
        loadTemplates().firstOrNull { it.id == templateId }

    private fun findTemplatePaths(): List<String> {
        val root = "templates"
        val paths = mutableListOf<String>()

        fun visit(path: String) {
            val children = context.assets.list(path).orEmpty()
            if (children.isEmpty()) {
                if (path.endsWith(".json")) {
                    paths += path
                }
                return
            }

            children.forEach { child ->
                visit("$path/$child")
            }
        }

        visit(root)
        return paths
    }

    private fun readAsset(path: String): String =
        context.assets.open(path).bufferedReader().use { it.readText() }

    private fun parseTemplate(json: String): PosterTemplate {
        val root = JSONObject(json)
        return PosterTemplate(
            id = root.getString("id"),
            title = root.getString("title"),
            category = enumValueOf(root.getString("category")),
            format = enumValueOf(root.getString("format")),
            background = parseBackground(root.getJSONObject("background")),
            layers = parseLayers(root.getJSONArray("layers"))
        )
    }

    private fun parseBackground(json: JSONObject): TemplateBackground =
        TemplateBackground(
            type = json.getString("type"),
            colors = json.getJSONArray("colors").toStringList()
        )

    private fun parseLayers(json: JSONArray): List<TemplateLayer> =
        List(json.length()) { index ->
            val layer = json.getJSONObject(index)
            TemplateLayer(
                id = layer.getString("id"),
                type = enumValueOf(layer.getString("type")),
                binding = layer.optString("binding").toPlaceholderBindingOrNull(),
                x = layer.getInt("x"),
                y = layer.getInt("y"),
                width = layer.getInt("width"),
                height = layer.getInt("height"),
                zIndex = layer.optInt("zIndex", 0),
                textStyle = layer.optJSONObject("textStyle")?.let(::parseTextStyle),
                shapeStyle = layer.optJSONObject("shapeStyle")?.let(::parseShapeStyle),
                pillBackground = layer.optJSONObject("pillBackground")?.let(::parsePillBackground)
            )
        }

    private fun parseTextStyle(json: JSONObject): TemplateTextStyle =
        TemplateTextStyle(
            fontSize = json.getInt("fontSize"),
            fontWeight = json.optString("fontWeight", "Regular"),
            color = json.getString("color"),
            align = json.optString("align", "Start"),
            maxLines = json.optInt("maxLines", 1),
            letterSpacing = json.optDouble("letterSpacing", 0.0).toFloat(),
            fontFamily = json.optString("fontFamily", "sans")
        )

    private fun parseShapeStyle(json: JSONObject): TemplateShapeStyle =
        TemplateShapeStyle(
            fillType = json.optString("fillType", "solid"),
            fillColors = json.optJSONArray("fillColors")?.toStringList().orEmpty(),
            cornerRadius = json.optInt("cornerRadius", 0),
            strokeColor = json.optString("strokeColor").takeIf { it.isNotBlank() },
            strokeWidth = json.optInt("strokeWidth", 0)
        )

    private fun parsePillBackground(json: JSONObject): TemplatePillBackground =
        TemplatePillBackground(
            fillType = json.optString("fillType", "solid"),
            fillColors = json.optJSONArray("fillColors")?.toStringList().orEmpty(),
            cornerRadius = json.optInt("cornerRadius", 0),
            paddingX = json.optInt("paddingX", 0),
            paddingY = json.optInt("paddingY", 0)
        )

    private inline fun <reified T : Enum<T>> enumValueOf(value: String): T =
        enumValues<T>().first { it.name == value }

    private fun JSONArray.toStringList(): List<String> =
        List(length()) { index -> getString(index) }

    private fun String.toPlaceholderBindingOrNull(): PlaceholderBinding? =
        PlaceholderBinding.entries.firstOrNull { it.name == this }
}

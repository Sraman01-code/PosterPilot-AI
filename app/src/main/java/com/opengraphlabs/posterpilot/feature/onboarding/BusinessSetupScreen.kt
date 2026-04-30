package com.opengraphlabs.posterpilot.feature.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.opengraphlabs.posterpilot.core.model.AppLanguage
import com.opengraphlabs.posterpilot.core.model.BusinessCategory
import com.opengraphlabs.posterpilot.core.model.BusinessProfile
import com.opengraphlabs.posterpilot.core.ui.PosterPilotScaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessSetupScreen(
    selectedLanguage: AppLanguage?,
    onProfileSaved: (BusinessProfile) -> Unit
) {
    var businessName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<BusinessCategory?>(null) }
    var selectedBrandColorHex by remember { mutableStateOf<String?>(null) }
    var categoryExpanded by remember { mutableStateOf(false) }

    val brandColors = listOf("#F7B733", "#10B981", "#2563EB", "#DC2626")
    val canContinue = selectedLanguage != null &&
        businessName.isNotBlank() &&
        phone.isNotBlank() &&
        selectedCategory != null &&
        selectedBrandColorHex != null

    PosterPilotScaffold {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Business setup",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = businessName,
                onValueChange = { businessName = it },
                label = { Text(text = "Business name") },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded }
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    value = selectedCategory?.displayName.orEmpty(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(text = "Business category") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                    }
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    BusinessCategory.entries.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(text = category.displayName) },
                            onClick = {
                                selectedCategory = category
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = phone,
                onValueChange = { phone = it },
                label = { Text(text = "Phone") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Brand color",
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                brandColors.forEach { colorHex ->
                    ColorSwatch(
                        colorHex = colorHex,
                        selected = selectedBrandColorHex == colorHex,
                        onClick = { selectedBrandColorHex = colorHex }
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = canContinue,
                onClick = onClick@{
                    val category = selectedCategory ?: return@onClick
                    val language = selectedLanguage ?: return@onClick
                    val brandColorHex = selectedBrandColorHex ?: return@onClick

                    onProfileSaved(
                        BusinessProfile(
                            businessName = businessName.trim(),
                            category = category,
                            phone = phone.trim(),
                            language = language,
                            brandColorHex = brandColorHex
                        )
                    )
                }
            ) {
                Text(text = "Finish setup")
            }
        }
    }
}

@Composable
private fun ColorSwatch(
    colorHex: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val swatchColor = remember(colorHex) {
        runCatching { Color(android.graphics.Color.parseColor(colorHex)) }
            .getOrDefault(Color.LightGray)
    }

    Button(
        modifier = Modifier.size(52.dp),
        onClick = onClick,
        shape = CircleShape,
        border = if (selected) {
            BorderStroke(3.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, Color(0xFFE5E7EB))
        },
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(swatchColor)
                .border(1.dp, Color.White, CircleShape)
        )
    }
}

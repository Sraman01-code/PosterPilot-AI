package com.opengraphlabs.posterpilot.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.opengraphlabs.posterpilot.core.model.AppLanguage
import com.opengraphlabs.posterpilot.core.model.BusinessCategory
import com.opengraphlabs.posterpilot.core.model.BusinessProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.businessProfileDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "business_profile"
)

class BusinessProfileStore(private val context: Context) {
    val state: Flow<BusinessProfileState> = context.businessProfileDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map<Preferences, BusinessProfileState> { preferences ->
            val language = preferences[Keys.Language]?.toEnumOrNull<AppLanguage>()
            val category = preferences[Keys.BusinessCategory]?.toEnumOrNull<BusinessCategory>()
            val businessName = preferences[Keys.BusinessName].orEmpty()
            val phone = preferences[Keys.Phone].orEmpty()
            val brandColorHex = preferences[Keys.BrandColorHex].orEmpty()
            val onboardingCompleted = preferences[Keys.OnboardingCompleted] ?: false

            val profile = if (
                language != null &&
                category != null &&
                businessName.isNotBlank() &&
                phone.isNotBlank() &&
                brandColorHex.isNotBlank()
            ) {
                BusinessProfile(
                    businessName = businessName,
                    category = category,
                    phone = phone,
                    language = language,
                    brandColorHex = brandColorHex
                )
            } else {
                null
            }

            BusinessProfileState.Ready(
                selectedLanguage = language,
                profile = profile,
                onboardingCompleted = onboardingCompleted
            )
        }

    suspend fun saveLanguage(language: AppLanguage) {
        context.businessProfileDataStore.edit { preferences ->
            preferences[Keys.Language] = language.name
        }
    }

    suspend fun saveProfile(profile: BusinessProfile) {
        context.businessProfileDataStore.edit { preferences ->
            preferences[Keys.Language] = profile.language.name
            preferences[Keys.BusinessName] = profile.businessName
            preferences[Keys.BusinessCategory] = profile.category.name
            preferences[Keys.Phone] = profile.phone
            preferences[Keys.BrandColorHex] = profile.brandColorHex
            preferences[Keys.OnboardingCompleted] = true
        }
    }

    private object Keys {
        val Language = stringPreferencesKey("language")
        val BusinessName = stringPreferencesKey("business_name")
        val BusinessCategory = stringPreferencesKey("business_category")
        val Phone = stringPreferencesKey("phone")
        val BrandColorHex = stringPreferencesKey("brand_color_hex")
        val OnboardingCompleted = booleanPreferencesKey("onboarding_completed")
    }
}

private inline fun <reified T : Enum<T>> String.toEnumOrNull(): T? =
    enumValues<T>().firstOrNull { it.name == this }

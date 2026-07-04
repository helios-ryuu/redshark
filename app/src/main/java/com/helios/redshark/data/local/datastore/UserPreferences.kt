package com.helios.redshark.data.local.datastore

// File nay quan ly du lieu cuc bo tren thiet bi.

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
@Singleton
class UserPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    companion object {
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_DISPLAY_NAME = stringPreferencesKey("display_name")
    }

    val userId: Flow<String?> = dataStore.data.map { it[KEY_USER_ID] }
    val displayName: Flow<String?> = dataStore.data.map { it[KEY_DISPLAY_NAME] }

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    suspend fun saveUser(userId: String, displayName: String) {
        dataStore.edit { prefs ->
            prefs[KEY_USER_ID] = userId
            prefs[KEY_DISPLAY_NAME] = displayName
        }
    }

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    suspend fun clear() {
        dataStore.edit { it.clear() }
    }
}

package com.example.autologinapp

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first


val Context.dataStore by preferencesDataStore(
    name = "auth"
)

object AuthManager {

    private val TOKEN = stringPreferencesKey("token")

    suspend fun saveToken(
        context: Context,
        token: String
    ){
        context.dataStore.edit { preferences ->
            preferences[TOKEN] = token
        }
    }

    suspend fun getToken(context: Context): String? {
        val preferences = context.dataStore.data.first()

        return preferences[TOKEN]
    }

    suspend fun logout(context: Context){
        context.dataStore.edit { preferences ->
            preferences.remove(TOKEN)
        }
    }
}
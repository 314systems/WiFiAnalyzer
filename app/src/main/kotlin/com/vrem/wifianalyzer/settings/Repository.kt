/*
 * WiFiAnalyzer
 * Copyright (C) 2015 - 2026 VREM Software Development <VREMSoftwareDevelopment@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.vrem.wifianalyzer.settings

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onStart

class Repository(
    private val context: Context,
) {
    fun preferenceChanges(): Flow<String?> = callbackFlow {
        val listener = OnSharedPreferenceChangeListener { _, key ->
            trySend(key)
        }
        registerOnSharedPreferenceChangeListener(listener)
        awaitClose {
            unregisterOnSharedPreferenceChangeListener(listener)
        }
    }.onStart { emit(null) }

    fun registerOnSharedPreferenceChangeListener(
        onSharedPreferenceChangeListener: OnSharedPreferenceChangeListener,
    ): Unit = sharedPreferences().registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener)

    fun unregisterOnSharedPreferenceChangeListener(
        onSharedPreferenceChangeListener: OnSharedPreferenceChangeListener,
    ): Unit = sharedPreferences().unregisterOnSharedPreferenceChangeListener(
        onSharedPreferenceChangeListener
    )

    fun save(
        key: Int,
        value: Int,
    ): Unit = save(key, value.toString())

    fun save(
        key: Int,
        value: String,
    ): Unit = sharedPreferences().edit { putString(context.getString(key), value) }

    fun save(
        key: Int,
        value: Boolean,
    ): Unit = sharedPreferences().edit { putBoolean(context.getString(key), value) }

    fun stringAsInteger(
        key: Int,
        defaultValue: Int,
    ): Int = string(key, defaultValue.toString()).toInt()

    fun string(
        key: Int,
        defaultValue: String,
    ): String {
        val keyValue: String = context.getString(key)
        return runCatching {
            sharedPreferences().getString(keyValue, defaultValue) ?: defaultValue
        }.getOrElse {
            sharedPreferences().edit { putString(keyValue, defaultValue) }
            defaultValue
        }
    }

    fun boolean(
        key: Int,
        defaultValue: Boolean,
    ): Boolean {
        val keyValue: String = context.getString(key)
        return runCatching {
            sharedPreferences().getBoolean(keyValue, defaultValue)
        }.getOrElse {
            sharedPreferences().edit { putBoolean(keyValue, defaultValue) }
            return defaultValue
        }
    }

    fun resourceBoolean(key: Int): Boolean = context.resources.getBoolean(key)

    fun integer(
        key: Int,
        defaultValue: Int,
    ): Int {
        val keyValue: String = context.getString(key)
        return runCatching {
            sharedPreferences().getInt(keyValue, defaultValue)
        }.getOrElse {
            sharedPreferences().edit { putString(keyValue, defaultValue.toString()) }
            return defaultValue
        }
    }

    fun stringSet(
        key: Int,
        defaultValues: Set<String>,
    ): Set<String> {
        val keyValue: String = context.getString(key)
        return runCatching {
            sharedPreferences().getStringSet(keyValue, defaultValues)!!
        }.getOrElse {
            sharedPreferences().edit { putSet(keyValue, defaultValues) }
            return defaultValues
        }
    }

    private fun SharedPreferences.Editor.putSet(key: String, values: Set<String>) {
        putStringSet(key, values)
    }

    fun saveStringSet(
        key: Int,
        values: Set<String>,
    ): Unit = sharedPreferences().edit { putStringSet(context.getString(key), values) }

    fun clear(): Unit = sharedPreferences().edit { clear() }

    fun defaultSharedPreferences(context: Context): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    private fun sharedPreferences(): SharedPreferences = defaultSharedPreferences(context)
}

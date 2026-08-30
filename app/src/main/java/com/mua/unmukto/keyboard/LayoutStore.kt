/*
 * Copyright (C) 2021-2026 Maifee Ul Asad
 *
 * This file is part of Unmukto.
 *
 * Unmukto is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * Unmukto is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Unmukto. If not, see <https://www.gnu.org/licenses/>.
 */
package com.mua.unmukto.keyboard

import android.content.Context

/**
 * Remembers the layout the user picked.
 *
 * The keyboard's input view is torn down and rebuilt constantly -- every rotation, and
 * whenever the system reclaims the IME -- so a choice held only in memory would not
 * survive a session, let alone a reboot. Kept as an interface so the service can be
 * exercised without a real preferences file behind it.
 */
interface LayoutStore {

    /** Null until the user has chosen anything. */
    var selectedLayoutId: String?
}

class SharedPreferencesLayoutStore(context: Context) : LayoutStore {

    private val preferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override var selectedLayoutId: String?
        get() = preferences.getString(KEY_SELECTED_LAYOUT, null)
        set(value) {
            // apply() rather than commit(): this is written from a key press and read back
            // from the same in-memory map, so the disk write need not block the input path.
            preferences.edit().putString(KEY_SELECTED_LAYOUT, value).apply()
        }

    private companion object {
        const val PREFERENCES_NAME = "unmukto_settings"
        const val KEY_SELECTED_LAYOUT = "selected_layout"
    }
}

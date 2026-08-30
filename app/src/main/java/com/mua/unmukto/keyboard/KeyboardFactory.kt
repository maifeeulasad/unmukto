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
import android.inputmethodservice.Keyboard
import androidx.annotation.XmlRes

/**
 * Inflates keyboard layers, once each.
 *
 * Parsing the layout XML again on every shift press is wasted work on the input path, and
 * a [Keyboard] is immutable once built, so instances are held and handed back out.
 *
 * The cache is per display configuration, not per process: [Keyboard] measures its keys
 * against the display width when it is parsed, so a cached one is the wrong width after a
 * rotation. Building a factory alongside the input view -- which the framework recreates
 * on exactly those configuration changes -- keeps that from needing separate invalidation.
 */
class KeyboardFactory(private val context: Context) {

    private val inflated = mutableMapOf<Int, Keyboard>()

    fun keyboard(@XmlRes layer: Int): Keyboard = inflated.getOrPut(layer) { Keyboard(context, layer) }
}

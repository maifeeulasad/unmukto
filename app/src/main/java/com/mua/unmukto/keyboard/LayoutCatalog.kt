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

import com.mua.unmukto.R

/**
 * The set of layouts the keyboard can offer.
 *
 * Everything past [layouts] is derived, so an implementation declares what it has and
 * inherits how it is looked up and cycled through. That is the seam multiple languages
 * will be added behind: a catalogue assembled from enabled languages, or read from an
 * asset, is a different implementation and not a different service.
 */
interface LayoutCatalog {

    /** In the order the user cycles through them. Never empty. */
    val layouts: List<KeyboardLayout>

    /** Used when nothing is stored yet, or when what was stored no longer exists. */
    val default: KeyboardLayout
        get() = layouts.first()

    fun find(id: String?): KeyboardLayout? = layouts.firstOrNull { it.id == id }

    /**
     * Wraps around, and treats a layout that is not in this catalogue as no position at
     * all -- a stored id can outlive the layout it named across an upgrade.
     */
    fun next(current: KeyboardLayout): KeyboardLayout {
        val index = layouts.indexOfFirst { it.id == current.id }
        if (index < 0) return default
        return layouts[(index + 1) % layouts.size]
    }
}

/** The layouts that ship inside the app. The one place a built-in layout is declared. */
object BuiltInLayoutCatalog : LayoutCatalog {

    val PROBHAT = KeyboardLayout(
        id = "bn_probhat",
        labelRes = R.string.layout_probhat,
        baseLayer = R.xml.kbd_bn,
        shiftedLayer = R.xml.kbd_bn_shifted
    )

    val ALPHABETICAL = KeyboardLayout(
        id = "bn_alphabetical",
        labelRes = R.string.layout_alphabetical,
        baseLayer = R.xml.kbd_bn_alpha,
        shiftedLayer = R.xml.kbd_bn_alpha_shifted
    )

    override val layouts: List<KeyboardLayout> = listOf(PROBHAT, ALPHABETICAL)

    override val default: KeyboardLayout = PROBHAT
}

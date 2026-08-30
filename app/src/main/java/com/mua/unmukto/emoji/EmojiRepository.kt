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
package com.mua.unmukto.emoji

import android.graphics.Paint
import android.os.Build

/**
 * Turns a category's ranges into the emoji this particular device can actually draw.
 *
 * A range is a claim about Unicode, not about the phone in someone's hand. Ranges contain
 * unassigned code points, and every device's emoji font stops somewhere -- a five year old
 * one has no idea what most of the recent additions look like. Handing those to the panel
 * unfiltered fills it with the font's notdef box, and a grid of identical empty rectangles
 * is worse than a shorter grid.
 *
 * [Paint.hasGlyph] answers that per code point. It is a native call and there are a few
 * thousand of them, so each category is expanded once and kept.
 */
class EmojiRepository(private val paint: Paint = Paint()) {

    private val expanded = mutableMapOf<String, List<String>>()

    fun emoji(category: EmojiCategory): List<String> =
        expanded.getOrPut(category.id) { expand(category) }

    private fun expand(category: EmojiCategory): List<String> {
        val result = ArrayList<String>()
        for (range in category.ranges) {
            for (codePoint in range) {
                if (!Character.isDefined(codePoint))
                    continue
                val emoji = String(Character.toChars(codePoint))
                if (canDraw(emoji))
                    result.add(emoji)
            }
        }
        return result
    }

    /**
     * Before API 23 there is nothing to ask, so everything assigned is offered. Those
     * devices ship a font old enough that the ranges are close to fully covered anyway,
     * which is the case where guessing costs least.
     */
    private fun canDraw(emoji: String): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.M || paint.hasGlyph(emoji)
}

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

import com.mua.unmukto.R

/**
 * The categories the emoji panel offers.
 *
 * An interface for the same reason the keyboard's layout catalogue is one: what the panel
 * shows is a decision made in one place, and a different set -- a recently used tab, a
 * user-ordered one -- is a different implementation rather than a change to the panel.
 */
interface EmojiCatalog {

    /** In tab order. Never empty. */
    val categories: List<EmojiCategory>
}

/**
 * The standard categories, as Unicode ranges.
 *
 * The ranges follow the blocks Unicode assigns emoji to, grouped the way the categories in
 * the Unicode emoji ordering are, and they do not overlap: every code point belongs to one
 * tab. Gaps and unassigned code points inside a range are expected -- [EmojiRepository]
 * drops whatever the device cannot draw, so a range can be stated as the block it is and
 * left alone as fonts catch up.
 *
 * Only single code points are listed. Flags are pairs of regional indicators, and the
 * family, profession and skin-tone emoji are sequences joined by zero-width joiners; both
 * are enumerated by rule rather than by range, and neither belongs in a list of blocks.
 * They are the obvious next thing to add here, and adding them changes this file only.
 */
object UnicodeEmojiCatalog : EmojiCatalog {

    override val categories: List<EmojiCategory> = listOf(
        EmojiCategory(
            id = "smileys",
            labelRes = R.string.emoji_category_smileys,
            icon = "😀",
            ranges = listOf(
                0x1F600..0x1F64F, // Emoticons
                0x1F910..0x1F92F, // Supplemental faces
                0x1F970..0x1F97A
            )
        ),
        EmojiCategory(
            id = "people",
            labelRes = R.string.emoji_category_people,
            icon = "👋",
            ranges = listOf(
                0x1F440..0x1F450, // Eyes through hands
                0x1F464..0x1F487, // People and roles
                0x1F930..0x1F93E,
                0x1F9B0..0x1F9DF
            )
        ),
        EmojiCategory(
            id = "nature",
            labelRes = R.string.emoji_category_nature,
            icon = "🐻",
            ranges = listOf(
                0x1F300..0x1F32C, // Weather and sky
                0x1F330..0x1F344, // Plants
                0x1F400..0x1F43F, // Animals
                0x1F980..0x1F9AE
            )
        ),
        EmojiCategory(
            id = "food",
            labelRes = R.string.emoji_category_food,
            icon = "🍎",
            ranges = listOf(
                0x1F345..0x1F37F, // Food and drink
                0x1F950..0x1F96F
            )
        ),
        EmojiCategory(
            id = "travel",
            labelRes = R.string.emoji_category_travel,
            icon = "🚗",
            ranges = listOf(
                0x1F3E0..0x1F3F0, // Buildings
                0x1F5FA..0x1F5FF, // Landmarks
                0x1F680..0x1F6D5  // Transport
            )
        ),
        EmojiCategory(
            id = "objects",
            labelRes = R.string.emoji_category_objects,
            icon = "🎉",
            ranges = listOf(
                0x1F380..0x1F3A0, // Celebration
                0x1F3AB..0x1F3CA, // Activities
                0x1F4A0..0x1F4FF, // Everyday objects
                0x1F526..0x1F53D
            )
        ),
        EmojiCategory(
            id = "symbols",
            labelRes = R.string.emoji_category_symbols,
            icon = "❤️",
            ranges = listOf(
                0x2600..0x26FF, // Miscellaneous symbols
                0x2700..0x27BF, // Dingbats
                0x2B00..0x2BFF,
                0x1F500..0x1F525
            )
        )
    )
}

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

import androidx.annotation.StringRes

/**
 * One tab of the emoji panel, declared as the Unicode it covers rather than as a list of
 * characters.
 *
 * Ranges rather than a hand-picked list for two reasons. A list is a snapshot: it is
 * complete on the day it is written and quietly out of date at the next Unicode release,
 * while a range picks up whatever was added inside it as soon as the device's font has it.
 * And a list is a judgement about which emoji are worth having, which is not a judgement
 * this keyboard is in any position to make on someone else's behalf.
 *
 * Nothing here is language-specific, and deliberately so -- emoji are the same characters
 * whatever the keyboard around them is typing.
 *
 * @param icon the glyph shown on the tab. Drawn from the same font as the contents, so a
 *   device that cannot render the category cannot render its tab either, which is the
 *   honest thing for the tab to show.
 * @param ranges inclusive code point ranges, in the order they should appear.
 */
data class EmojiCategory(
    val id: String,
    @StringRes val labelRes: Int,
    val icon: String,
    val ranges: List<IntRange>
)

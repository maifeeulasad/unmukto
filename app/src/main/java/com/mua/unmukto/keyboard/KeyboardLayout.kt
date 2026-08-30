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

import androidx.annotation.StringRes
import androidx.annotation.XmlRes

/**
 * One keyboard layout, described rather than implemented.
 *
 * A layout is a pair of layers and the few facts about them the rest of the app needs:
 * how to name it to the user, and how to persist which one is chosen. Nothing here says
 * how a layout is drawn or how its keys behave, so adding one is a declaration plus its
 * XML rather than a change to the service.
 *
 * @param id stable across releases -- it is what gets written to storage, so renaming one
 *   silently resets the user's choice.
 * @param shiftedLayer null for a layout with nothing on its second layer, which shift then
 *   has no work to do on.
 */
data class KeyboardLayout(
    val id: String,
    @StringRes val labelRes: Int,
    @XmlRes val baseLayer: Int,
    @XmlRes val shiftedLayer: Int? = null
) {

    val hasShiftedLayer: Boolean
        get() = shiftedLayer != null

    /** The layer to show, falling back to the base one when there is nothing to shift to. */
    @XmlRes
    fun layer(shifted: Boolean): Int = if (shifted) shiftedLayer ?: baseLayer else baseLayer
}

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

import android.inputmethodservice.Keyboard

/**
 * Owns which layout is on screen and which of its layers.
 *
 * The service used to hold both layers in fields and swap between them, which worked only
 * because there was exactly one layout with exactly two layers. This holds the same state
 * as a position in a catalogue instead, so the number of layouts stops being something the
 * service is built around.
 *
 * Layer changes are pushed out through [listener] rather than returned, because they have
 * two sources -- a key press, and a choice made outside the keyboard entirely -- and only
 * one of them is a call the service makes.
 */
class LayoutController(
    private val catalog: LayoutCatalog,
    private val store: LayoutStore,
    private val keyboards: KeyboardFactory
) {

    fun interface Listener {
        fun onKeyboardChanged(keyboard: Keyboard)
    }

    var listener: Listener? = null

    var layout: KeyboardLayout = catalog.find(store.selectedLayoutId) ?: catalog.default
        private set

    var isShifted: Boolean = false
        private set

    val keyboard: Keyboard
        get() = keyboards.keyboard(layout.layer(isShifted))

    /**
     * Ignored for a layout with no second layer, so the shift key cannot leave the
     * keyboard in a shifted state that looks and types identically to the unshifted one.
     */
    fun setShifted(shifted: Boolean) {
        val target = shifted && layout.hasShiftedLayer
        if (target == isShifted)
            return
        isShifted = target
        notifyChanged()
    }

    fun select(layout: KeyboardLayout) {
        if (layout.id == this.layout.id)
            return
        this.layout = layout
        this.isShifted = false
        store.selectedLayoutId = layout.id
        notifyChanged()
    }

    fun selectNext() = select(catalog.next(layout))

    /**
     * Picks up a layout chosen somewhere other than the keyboard -- the setup screen writes
     * to the same store, and the keyboard is not running while the user is over there.
     */
    fun reloadSelection() {
        val stored = catalog.find(store.selectedLayoutId) ?: return
        select(stored)
    }

    private fun notifyChanged() {
        listener?.onKeyboardChanged(keyboard)
    }
}

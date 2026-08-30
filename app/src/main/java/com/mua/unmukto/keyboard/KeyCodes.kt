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

/**
 * The codes of the keys that act on the keyboard rather than typing into the field.
 *
 * Character keys carry an `android:keyOutputText` and are routed straight to the text
 * path, so their codes are arbitrary and stay in the layout XML. These do not: the
 * layouts and the service both have to agree on them, which makes one shared declaration
 * the only place they can honestly live.
 *
 * All are negative, which is what keeps them clear of the Unicode code points a
 * long-press popup emits.
 */
object KeyCodes {

    /** Leaves Unmukto for another keyboard, so a Bengali-only layer is never a dead end. */
    const val SWITCH_IME = -110

    const val SHIFT_ON = -120
    const val SHIFT_OFF = -121

    /** Leaves the letters for the emoji panel, and the panel for the letters again. */
    const val EMOJI = -112
    const val LETTERS = -113

    /** `Keyboard.KEYCODE_DELETE`, restated so this file lists every code the service acts on. */
    const val DELETE = -5

    /** Space. Positive, since it is a real code point, but the service treats it as its own key. */
    const val SPACE = 62

    private val KEYBOARD_LEVEL =
        setOf(SWITCH_IME, SHIFT_ON, SHIFT_OFF, DELETE, EMOJI, LETTERS)

    /**
     * True for keys that operate on the keyboard itself. They are drawn differently from
     * character keys, so that what a key does is visible before it is pressed.
     */
    fun isModifier(code: Int): Boolean = code in KEYBOARD_LEVEL
}

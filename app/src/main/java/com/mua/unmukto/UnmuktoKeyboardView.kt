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
package com.mua.unmukto

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import androidx.core.content.ContextCompat

/**
 * Draws the keyboard itself rather than leaning on [KeyboardView]'s stock rendering.
 *
 * The framework applies a single `keyBackground` drawable to every key, which leaves no way
 * to distinguish a modifier from a letter, and it stretches that drawable across the whole
 * key cell so the gap between keys has to come out of the layout's `horizontalGap` -- where
 * it both shrinks the touch target and overflows the row. Drawing here instead means the
 * cell stays the full touch target while the visible key is inset inside it, and each key
 * can be styled by role.
 */
// Deliberately delegates to KeyboardView's two-argument constructor rather than passing a
// defStyleAttr of 0: the two-argument form applies the platform's keyboardViewStyle, which
// is where preview offsets, vertical touch correction and the popup layout get their
// defaults. Passing 0 silently drops all of them.
class UnmuktoKeyboardView(context: Context, attrs: AttributeSet?) : KeyboardView(context, attrs) {

    private companion object {
        /** Keys that act on the keyboard rather than emitting text. */
        val MODIFIER_CODES = setOf(
            UnmuktoKeyboardService.KEYCODE_SHIFT_ON,
            UnmuktoKeyboardService.KEYCODE_SHIFT_OFF,
            UnmuktoKeyboardService.KEYCODE_SWITCH_IME,
            Keyboard.KEYCODE_DELETE
        )

        /** Labels longer than this are words, not glyphs, and need a smaller size to fit. */
        const val GLYPH_LABEL_MAX_LENGTH = 2
    }

    private val backgroundFill = color(R.color.keyboard_background)
    private val surfaceFill = color(R.color.key_surface)
    private val modifierFill = color(R.color.key_surface_modifier)
    private val pressedFill = color(R.color.key_surface_pressed)
    private val shadowFill = color(R.color.key_shadow)
    private val textFill = color(R.color.key_text)
    private val modifierTextFill = color(R.color.key_text_modifier)

    private val insetX = dp(3f)
    private val insetY = dp(4f)
    private val cornerRadius = dp(8f)
    private val shadowOffset = dp(1f)

    private val glyphTextSize = sp(21f)
    private val wordTextSize = sp(14f)

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }
    private val keyRect = RectF()

    /**
     * A long press the view resolves itself but cannot act on.
     *
     * [KeyboardView] treats a long press as "open this key's popup" and never reports it,
     * so a key whose long press means something other than a popup -- the input method
     * switch key -- needs this seam to reach the service.
     */
    fun interface OnKeyLongPressListener {
        /** Returns true once the press is handled, which suppresses the popup. */
        fun onKeyLongPress(primaryCode: Int): Boolean
    }

    var onKeyLongPressListener: OnKeyLongPressListener? = null

    init {
        isPreviewEnabled = true
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(backgroundFill)
        val keys = keyboard?.keys ?: return
        for (key in keys) {
            drawKey(canvas, key)
        }
    }

    private fun drawKey(canvas: Canvas, key: Keyboard.Key) {
        keyRect.set(
            key.x + insetX,
            key.y + insetY,
            key.x + key.width - insetX,
            key.y + key.height - insetY
        )

        val modifier = key.codes.isNotEmpty() && key.codes[0] in MODIFIER_CODES

        // A rounded rect offset downwards reads as a soft drop shadow and, unlike
        // Paint.setShadowLayer, is drawn identically on every hardware-accelerated canvas.
        if (!key.pressed) {
            fillPaint.color = shadowFill
            keyRect.offset(0f, shadowOffset)
            canvas.drawRoundRect(keyRect, cornerRadius, cornerRadius, fillPaint)
            keyRect.offset(0f, -shadowOffset)
        }

        fillPaint.color = when {
            key.pressed -> pressedFill
            modifier -> modifierFill
            else -> surfaceFill
        }
        canvas.drawRoundRect(keyRect, cornerRadius, cornerRadius, fillPaint)

        val label = key.label ?: return
        labelPaint.color = if (modifier) modifierTextFill else textFill
        labelPaint.textSize =
            if (label.length > GLYPH_LABEL_MAX_LENGTH) wordTextSize else glyphTextSize

        // Centre on the cap box rather than the layout box, so glyphs sit optically centred
        // whether or not they carry ascenders or below-baseline marks.
        val metrics = labelPaint.fontMetrics
        val baseline = keyRect.centerY() - (metrics.ascent + metrics.descent) / 2f
        canvas.drawText(label.toString(), keyRect.centerX(), baseline, labelPaint)
    }

    override fun onLongPress(popupKey: Keyboard.Key): Boolean {
        val primaryCode = popupKey.codes.firstOrNull()
        if (primaryCode != null && onKeyLongPressListener?.onKeyLongPress(primaryCode) == true)
            return true
        return super.onLongPress(popupKey)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val handled = super.onTouchEvent(event)
        // KeyboardView only invalidates the single key it thinks changed, which assumes its
        // own buffered rendering. Repaint the whole view so press states never go stale.
        invalidate()
        return handled
    }

    private fun color(resId: Int) = ContextCompat.getColor(context, resId)

    private fun dp(value: Float) = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, value, resources.displayMetrics
    )

    private fun sp(value: Float) = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP, value, resources.displayMetrics
    )
}

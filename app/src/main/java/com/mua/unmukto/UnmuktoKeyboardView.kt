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
class UnmuktoKeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : KeyboardView(context, attrs, defStyleAttr) {

    private companion object {
        /** Keys that act on the keyboard rather than emitting text. */
        val MODIFIER_CODES = setOf(
            UnmuktoKeyboardService.KEYCODE_SHIFT_ON,
            UnmuktoKeyboardService.KEYCODE_SHIFT_OFF,
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

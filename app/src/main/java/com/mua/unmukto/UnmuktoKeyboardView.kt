package com.mua.unmukto

import android.content.Context
import android.graphics.Canvas
import android.inputmethodservice.KeyboardView
import android.util.AttributeSet
import androidx.core.content.ContextCompat

class UnmuktoKeyboardView(context: Context?, attrs: AttributeSet?) : KeyboardView(context, attrs) {
    
    init {
        // Enable key preview for better UX
        isPreviewEnabled = true
        setBackgroundColor(ContextCompat.getColor(context!!, R.color.keyboard_background))
        
        // Ensure preview is shown for all keys
        setPopupParent(this)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
    }
} 
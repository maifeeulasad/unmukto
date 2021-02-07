package com.mua.unmukto

import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(), OnKeyboardActionListener {

    private lateinit var ukvTest: UnmuktoKeyboardView
    private lateinit var etTest: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ukvTest = findViewById(R.id.ukv_test)
        etTest = findViewById(R.id.et_test)

        val mKeyboard = Keyboard(this, R.xml.kbd_bn)
        ukvTest.setKeyboard(mKeyboard)
        ukvTest.setOnKeyboardActionListener(this)
    }

    override fun onPress(primaryCode: Int) {
        Log.d("d--mua", "press : $primaryCode")
    }

    override fun onRelease(primaryCode: Int) {
        Log.d("d--mua", "release : $primaryCode")
    }

    override fun onKey(primaryCode: Int, keyCodes: IntArray) {
        if (primaryCode == -120) {
            ukvTest.keyboard = Keyboard(this, R.xml.kbd_bn_shifted)
        } else if (primaryCode == -121) {
            ukvTest.keyboard = Keyboard(this, R.xml.kbd_bn)
        }
    }

    override fun onText(text: CharSequence) {
        etTest.setText(etTest!!.text.toString() + text.toString())
    }

    override fun swipeLeft() {
        Log.d("d--mua", "left")
    }

    override fun swipeRight() {
        Log.d("d--mua", "right")
    }

    override fun swipeDown() {
        Log.d("d--mua", "down")
    }

    override fun swipeUp() {
        Log.d("d--mua", "up")
    }
}
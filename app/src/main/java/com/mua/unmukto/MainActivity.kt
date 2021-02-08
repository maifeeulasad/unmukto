package com.mua.unmukto

import android.content.Context
import android.content.Intent
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity(), OnKeyboardActionListener {

    private lateinit var ukvTest: UnmuktoKeyboardView
    private lateinit var etTest: EditText

    private val UNMUKTO_SIGNATURE = "com.mua.unmukto/.UnmuktoKeyboardService"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ukvTest = findViewById(R.id.ukv_test)
        etTest = findViewById(R.id.et_test)

        val mKeyboard = Keyboard(this, R.xml.kbd_bn)
        ukvTest.setKeyboard(mKeyboard)
        ukvTest.setOnKeyboardActionListener(this)

        showSettings()
    }

    fun showSettings(){
        if(!checkIsUnmukto()){
            addKeyboard()
            setDefault()
        }
    }

    fun setDefault(){
        val imeManager = applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imeManager.showInputMethodPicker()
    }

    fun addKeyboard(){
        val enableIntent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
        enableIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        this.startActivity(enableIntent)
    }

    fun checkIsUnmukto():Boolean{
        return Settings.Secure.getString(contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD).equals(UNMUKTO_SIGNATURE)
    }

    override fun onPress(primaryCode: Int) {
    }

    override fun onRelease(primaryCode: Int) {
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
    }

    override fun swipeRight() {
    }

    override fun swipeDown() {
    }

    override fun swipeUp() {
    }
}
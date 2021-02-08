package com.mua.unmukto;


import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.view.View;
import android.view.inputmethod.InputConnection;

public class UnmuktoKeyboardService
        extends InputMethodService
        implements KeyboardView.OnKeyboardActionListener {

    private KeyboardView keyboardView;

    @Override
    public View onCreateInputView() {
        keyboardView = (KeyboardView) getLayoutInflater().inflate(R.layout.layout_unmukto, null);
        Keyboard keyboard = new Keyboard(this, R.xml.kbd_bn);
        keyboardView.setKeyboard(keyboard);
        keyboardView.setOnKeyboardActionListener(this);
        return keyboardView;
    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null)
            return;
        if (primaryCode == -120) {
            keyboardView.setKeyboard(new Keyboard(this, R.xml.kbd_bn_shifted));
        } else if (primaryCode == -121) {
            keyboardView.setKeyboard(new Keyboard(this, R.xml.kbd_bn));
        }
    }

    @Override
    public void onPress(int primaryCode) {
    }

    @Override
    public void onRelease(int primaryCode) {
    }

    @Override
    public void onText(CharSequence text) {
    }

    @Override
    public void swipeLeft() {
    }

    @Override
    public void swipeRight() {
    }

    @Override
    public void swipeDown() {
    }

    @Override
    public void swipeUp() {
    }
}
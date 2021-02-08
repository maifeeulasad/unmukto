package com.mua.unmukto;


import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputConnection;

public class UnmuktoKeyboardService
        extends InputMethodService
        implements KeyboardView.OnKeyboardActionListener {

    private final String shongkhaString = "০১২৩৪৫৬৭৮৯";
    private final String shoroBornoString = " অ আ ই ঈ উ ঊ ঋ এ ঐ ও ঔ";
    private final String karString = " া ি ী ু ূ ৃ ে ৈ ো ৌ";
    private final String benjonBornoString = "কখগঘঙচছজঝঞটঠডঢণতথদধনপফবভমযরলশষসহড়ঢ়য়ৎংঃঁ";
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
        } else if (primaryCode == Keyboard.KEYCODE_DELETE) {
            CharSequence selectedText = ic.getSelectedText(0);
            if (TextUtils.isEmpty(selectedText)) {
                ic.deleteSurroundingText(1, 0);
            } else {
                ic.commitText("", 1);
            }
        }
    }

    @Override
    public void onPress(int primaryCode) {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null)
            return;
        primaryCode = -primaryCode;
        if (primaryCode >= 130 && primaryCode <= 139) {
            char shongkha = shoroBornoString.charAt(primaryCode - 130);
            ic.commitText(String.valueOf(shongkha), 1);
        } else if (primaryCode >= 140 && primaryCode <= 151) {
            char shoroBorno = shoroBornoString.charAt((primaryCode - 140) * 2 + 1);
            ic.commitText(String.valueOf(shoroBorno), 1);
        } else if (primaryCode >= 160 && primaryCode <= 169) {
            char kar = karString.charAt((primaryCode - 160) * 2 + 1);
            ic.commitText(String.valueOf(kar), 1);
        } else if (primaryCode >= 170 && primaryCode <= 208) {
            char benjonBorno = benjonBornoString.charAt(primaryCode - 170);
            ic.commitText(String.valueOf(benjonBorno), 1);
        }
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
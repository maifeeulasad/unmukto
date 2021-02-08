package com.mua.unmukto;


import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputConnection;

public class UnmuktoKeyboardService
        extends InputMethodService
        implements KeyboardView.OnKeyboardActionListener {

    private final String shongkhaString = "০১২৩৪৫৬৭৮৯";
    private final String shoroBornoString = " অ আ ই ঈ উ ঊ ঋ এ ঐ ও ঔ";
    private final String karString = " া ি ী ু ূ ৃ ে ৈ ো ৌ";
    private final String benjonBornoString = "কখগঘঙচছজঝঞটঠডঢণতথদধনপফবভমযরলশষসহড়ঢ়য়ৎংঃঁ";

    private final int SHONGKHA_LOWER_BOUND = 130;
    private final int SHOROBORNO_LOWER_BOUND = 140;
    private final int KAR_LOWER_BOUND = 160;
    private final int BENJONBORNO_LOWER_BOUND = 170;

    private final int SHONGKHA_UPPER_BOUND = 139;
    private final int SHOROBORNO_UPPER_BOUND = 151;
    private final int KAR_UPPER_BOUND = 169;
    private final int BENJONBORNO_UPPER_BOUND = 208;

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
        if (primaryCode >= SHONGKHA_LOWER_BOUND && primaryCode <= SHONGKHA_UPPER_BOUND) {
            char shongkha = shongkhaString.charAt(primaryCode - SHONGKHA_LOWER_BOUND);
            ic.commitText(String.valueOf(shongkha), 1);
        } else if (primaryCode >= SHOROBORNO_LOWER_BOUND && primaryCode <= SHOROBORNO_UPPER_BOUND) {
            char shoroBorno = shoroBornoString.charAt((primaryCode - SHOROBORNO_LOWER_BOUND) * 2 + 1);
            ic.commitText(String.valueOf(shoroBorno), 1);
        } else if (primaryCode >= KAR_LOWER_BOUND && primaryCode <= KAR_UPPER_BOUND) {
            char kar = karString.charAt((primaryCode - KAR_LOWER_BOUND) * 2 + 1);
            ic.commitText(String.valueOf(kar), 1);
        } else if (primaryCode >= BENJONBORNO_LOWER_BOUND && primaryCode <= BENJONBORNO_UPPER_BOUND) {
            char benjonBorno = benjonBornoString.charAt(primaryCode - BENJONBORNO_LOWER_BOUND);
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
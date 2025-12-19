package com.mua.unmukto;


import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputConnection;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class UnmuktoKeyboardService
        extends InputMethodService
        implements KeyboardView.OnKeyboardActionListener {

    private final String SHONGKHA_STRING = "০১২৩৪৫৬৭৮৯";
    private final String SHORO_BORNO_STRING = " অ আ ই ঈ উ ঊ ঋ এ ঐ ও ঔ";
    private final String KAR_STRING = " া ি ী ু ূ ৃ ে ৈ ো ৌ";
    private final String BENJON_BORNO_STRING = "কখগঘঙচছজঝঞটঠডঢণতথদধনপফবভমযরলশষসহড়ঢ়য়ৎংঃঁ";
    private final String FOLA_STRING = " ্";

    private final int SHONGKHA_LOWER_BOUND = 130;
    private final int SHOROBORNO_LOWER_BOUND = 140;
    private final int KAR_LOWER_BOUND = 160;
    private final int BENJONBORNO_LOWER_BOUND = 170;
    private final int FOLA_LOWER_BOUND = 210;

    private final int SHONGKHA_UPPER_BOUND = 139;
    private final int SHOROBORNO_UPPER_BOUND = 151;
    private final int KAR_UPPER_BOUND = 169;
    private final int BENJONBORNO_UPPER_BOUND = 208;
    private final int FOLA_UPPER_BOUND = 210;

    private final int SPECIAL_CHAR_COMMA = -300;
    private final int SPECIAL_CHAR_DARI = -301;

    private KeyboardView ukvMain;
    private RecyclerView rvSuggestions;

    @Override
    public View onCreateInputView() {
        View view = getLayoutInflater().inflate(R.layout.layout_unmukto, null, false);
        ukvMain = view.findViewById(R.id.ukv_main);
        rvSuggestions = view.findViewById(R.id.rv_suggestions);
        LinearLayoutManager linearLayoutManager
                = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        rvSuggestions.setLayoutManager(linearLayoutManager);
        Keyboard keyboard = new Keyboard(this, R.xml.kbd_bn);
        ukvMain.setKeyboard(keyboard);
        ukvMain.setOnKeyboardActionListener(this);
        
        // Enable key preview popup for better UX
        ukvMain.setPreviewEnabled(true);
        
        return view;
    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null)
            return;
        if (primaryCode == -120) {
            ukvMain.setKeyboard(new Keyboard(this, R.xml.kbd_bn_shifted));
        } else if (primaryCode == -121) {
            ukvMain.setKeyboard(new Keyboard(this, R.xml.kbd_bn));
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
        if (primaryCode == 62) {
            ic.commitText(" ", 1);
        } else if (primaryCode == SPECIAL_CHAR_COMMA) {
            ic.commitText(",", 1);
        } else if (primaryCode == SPECIAL_CHAR_DARI) {
            ic.commitText("।", 1);
        }
        primaryCode = -primaryCode;
        if (primaryCode >= SHONGKHA_LOWER_BOUND && primaryCode <= SHONGKHA_UPPER_BOUND) {
            char shongkha = SHONGKHA_STRING.charAt(primaryCode - SHONGKHA_LOWER_BOUND);
            ic.commitText(String.valueOf(shongkha), 1);
        } else if (primaryCode >= SHOROBORNO_LOWER_BOUND && primaryCode <= SHOROBORNO_UPPER_BOUND) {
            char shoroBorno = SHORO_BORNO_STRING.charAt((primaryCode - SHOROBORNO_LOWER_BOUND) * 2 + 1);
            ic.commitText(String.valueOf(shoroBorno), 1);
        } else if (primaryCode >= KAR_LOWER_BOUND && primaryCode <= KAR_UPPER_BOUND) {
            char kar = KAR_STRING.charAt((primaryCode - KAR_LOWER_BOUND) * 2 + 1);
            ic.commitText(String.valueOf(kar), 1);
        } else if (primaryCode >= BENJONBORNO_LOWER_BOUND && primaryCode <= BENJONBORNO_UPPER_BOUND) {
            char benjonBorno = BENJON_BORNO_STRING.charAt(primaryCode - BENJONBORNO_LOWER_BOUND);
            ic.commitText(String.valueOf(benjonBorno), 1);
        } else if (primaryCode >= FOLA_LOWER_BOUND && primaryCode <= FOLA_UPPER_BOUND) {
            char fola = FOLA_STRING.charAt((primaryCode - FOLA_LOWER_BOUND) * 2 + 1);
            ic.commitText(String.valueOf(fola), 1);
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
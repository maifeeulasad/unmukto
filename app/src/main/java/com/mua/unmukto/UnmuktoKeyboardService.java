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

    static final int KEYCODE_SHIFT_ON = -120;
    static final int KEYCODE_SHIFT_OFF = -121;

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

    /**
     * Every character key in the layouts carries an {@code android:keyOutputText}, so the
     * framework routes it here rather than through {@link #onKey}. Committing the text the
     * framework already resolved keeps the layout XML the single source of truth for what
     * each key produces.
     */
    @Override
    public void onText(CharSequence text) {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null)
            return;
        ic.commitText(text, 1);
    }

    /**
     * Only keys without output text reach this: the modifiers, and any character picked from
     * a long-press popup (those arrive as a positive Unicode code point).
     */
    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null)
            return;
        if (primaryCode == KEYCODE_SHIFT_ON) {
            ukvMain.setKeyboard(new Keyboard(this, R.xml.kbd_bn_shifted));
        } else if (primaryCode == KEYCODE_SHIFT_OFF) {
            ukvMain.setKeyboard(new Keyboard(this, R.xml.kbd_bn));
        } else if (primaryCode == Keyboard.KEYCODE_DELETE) {
            handleDelete(ic);
        } else if (primaryCode > 0) {
            ic.commitText(String.valueOf((char) primaryCode), 1);
        }
    }

    private void handleDelete(InputConnection ic) {
        CharSequence selectedText = ic.getSelectedText(0);
        if (TextUtils.isEmpty(selectedText)) {
            ic.deleteSurroundingText(1, 0);
        } else {
            ic.commitText("", 1);
        }
    }

    @Override
    public void onPress(int primaryCode) {
    }

    @Override
    public void onRelease(int primaryCode) {
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

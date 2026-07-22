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
package com.mua.unmukto;


import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

public class UnmuktoKeyboardService
        extends InputMethodService
        implements KeyboardView.OnKeyboardActionListener {

    static final int KEYCODE_SHIFT_ON = -120;
    static final int KEYCODE_SHIFT_OFF = -121;

    private KeyboardView ukvMain;

    private Keyboard baseKeyboard;
    private Keyboard shiftedKeyboard;
    private boolean shifted;

    @Override
    public View onCreateInputView() {
        View view = getLayoutInflater().inflate(R.layout.layout_unmukto, null, false);
        ukvMain = view.findViewById(R.id.ukv_main);

        // Both layers are inflated once and reused; re-parsing the layout XML on every
        // shift press is wasted work on the input path.
        baseKeyboard = new Keyboard(this, R.xml.kbd_bn);
        shiftedKeyboard = new Keyboard(this, R.xml.kbd_bn_shifted);

        shifted = false;
        ukvMain.setKeyboard(baseKeyboard);
        ukvMain.setOnKeyboardActionListener(this);

        // Enable key preview popup for better UX
        ukvMain.setPreviewEnabled(true);

        return view;
    }

    /**
     * The input view is reused across every field in every app, so a layer left shifted by
     * the last session would otherwise carry over into the next one.
     */
    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);
        setShifted(false);
    }

    private void setShifted(boolean shift) {
        if (ukvMain == null || shifted == shift)
            return;
        shifted = shift;
        ukvMain.setKeyboard(shift ? shiftedKeyboard : baseKeyboard);
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
        // Shift is one-shot, as on every other soft keyboard: the shifted layer holds
        // individual letters you reach for mid-word, not a run of them.
        setShifted(false);
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
            setShifted(true);
        } else if (primaryCode == KEYCODE_SHIFT_OFF) {
            setShifted(false);
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

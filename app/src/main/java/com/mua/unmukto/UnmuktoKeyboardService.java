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


import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.KeyboardView;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.mua.unmukto.emoji.EmojiPanelView;
import com.mua.unmukto.keyboard.BuiltInLayoutCatalog;
import com.mua.unmukto.keyboard.KeyCodes;
import com.mua.unmukto.keyboard.KeyboardFactory;
import com.mua.unmukto.keyboard.LayoutController;
import com.mua.unmukto.keyboard.LayoutStore;
import com.mua.unmukto.keyboard.SharedPreferencesLayoutStore;

public class UnmuktoKeyboardService
        extends InputMethodService
        implements KeyboardView.OnKeyboardActionListener,
        UnmuktoKeyboardView.OnKeyLongPressListener {

    private UnmuktoKeyboardView ukvMain;
    private EmojiPanelView emojiPanel;

    private LayoutStore layoutStore;
    private LayoutController layouts;

    /**
     * Follows a layout chosen somewhere other than the keyboard.
     *
     * onStartInputView already re-reads the store, which covers the next field. It does not
     * cover the setup screen, which carries a field to try the keyboard out in: the radio
     * button and the keyboard are on screen together there, and without this the keyboard
     * ignores the choice until the user leaves and comes back.
     */
    private final LayoutStore.SelectionListener selectionListener = layoutId -> {
        if (layouts != null)
            layouts.reloadSelection();
    };

    @Override
    public void onCreate() {
        super.onCreate();
        // Outlives the input view, so a layout chosen in one session is still the one that
        // comes back in the next.
        layoutStore = new SharedPreferencesLayoutStore(this);
        layoutStore.addSelectionListener(selectionListener);
    }

    @Override
    public void onDestroy() {
        if (layoutStore != null)
            layoutStore.removeSelectionListener(selectionListener);
        super.onDestroy();
    }

    @Override
    public View onCreateInputView() {
        View view = getLayoutInflater().inflate(R.layout.layout_unmukto, null, false);
        ukvMain = view.findViewById(R.id.ukv_main);

        // Rebuilt with the input view rather than held for the life of the service: the
        // framework recreates this view on exactly the configuration changes that make an
        // already-inflated Keyboard the wrong width.
        KeyboardFactory keyboards = new KeyboardFactory(this);
        layouts = new LayoutController(BuiltInLayoutCatalog.INSTANCE, layoutStore, keyboards);
        layouts.setListener(keyboard -> ukvMain.setKeyboard(keyboard));

        ukvMain.setKeyboard(layouts.getKeyboard());
        ukvMain.setOnKeyboardActionListener(this);
        ukvMain.setOnKeyLongPressListener(this);

        // Enable key preview popup for better UX
        ukvMain.setPreviewEnabled(true);

        keepClearOfNavigationBar(view);

        // The panel's action row is a Keyboard like any other, so it goes through the same
        // listeners: the letters, switch and delete keys behave identically on both sides.
        emojiPanel = view.findViewById(R.id.emoji_panel);
        emojiPanel.setOnEmojiSelectedListener(this::commitText);
        UnmuktoKeyboardView actions = emojiPanel.getActionKeyboardView();
        actions.setKeyboard(keyboards.keyboard(R.xml.kbd_emoji_actions));
        actions.setOnKeyboardActionListener(this);
        actions.setOnKeyLongPressListener(this);

        return view;
    }

    /**
     * The input view is reused across every field in every app, so a layer left shifted by
     * the last session would otherwise carry over into the next one.
     */
    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);
        // Every field starts on the letters. Coming back to a keyboard still showing the
        // emoji panel, in another app entirely, reads as the keyboard having lost them.
        showEmojiPanel(false);
        if (layouts == null)
            return;
        layouts.reloadSelection();
        layouts.setShifted(false);
    }

    private void setShifted(boolean shift) {
        if (layouts != null)
            layouts.setShifted(shift);
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
        // These act on the keyboard itself, so they are handled before the input connection
        // is looked at: a field that has gone away must not take the way out with it.
        if (primaryCode == KeyCodes.SHIFT_ON) {
            setShifted(true);
            return;
        }
        if (primaryCode == KeyCodes.SHIFT_OFF) {
            setShifted(false);
            return;
        }
        if (primaryCode == KeyCodes.SWITCH_IME) {
            switchToNextKeyboard();
            return;
        }
        if (primaryCode == KeyCodes.EMOJI) {
            showEmojiPanel(true);
            return;
        }
        if (primaryCode == KeyCodes.LETTERS) {
            showEmojiPanel(false);
            return;
        }

        InputConnection ic = getCurrentInputConnection();
        if (ic == null)
            return;
        if (primaryCode == KeyCodes.DELETE) {
            handleDelete(ic);
        } else if (primaryCode > 0) {
            ic.commitText(String.valueOf((char) primaryCode), 1);
        }
    }

    /**
     * Hands input over to the next enabled keyboard.
     *
     * <p>Without this the user has no way back: reaching the system's input method settings
     * means searching for them, and searching needs a keyboard the system will only ever
     * bring up as Unmukto. When Unmukto is the only enabled keyboard there is no "next" one
     * to switch to, so the picker is shown rather than letting the key do nothing.
     */
    // The pre-P InputMethodManager.switchToNextInputMethod is deprecated in favour of the
    // InputMethodService one used above it, which only exists from P onwards.
    @SuppressWarnings("deprecation")
    private void switchToNextKeyboard() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (switchToNextInputMethod(false))
                return;
        } else if (imm() != null) {
            IBinder token = inputToken();
            if (token != null && imm().switchToNextInputMethod(token, false))
                return;
        }
        showKeyboardPicker();
    }

    private void showKeyboardPicker() {
        InputMethodManager imm = imm();
        if (imm != null)
            imm.showInputMethodPicker();
    }

    private InputMethodManager imm() {
        return (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    /** The IME window's token, which the pre-P switching API identifies the caller by. */
    private IBinder inputToken() {
        if (getWindow() == null || getWindow().getWindow() == null)
            return null;
        return getWindow().getWindow().getAttributes().token;
    }

    /**
     * Long presses that mean something other than "open this key's popup".
     *
     * <p>A tap on the switch key hops to the next keyboard, which is what someone who just
     * wants their previous keyboard back is after; holding it asks for the full system list
     * instead, for picking a specific one out of several.
     *
     * <p>Holding space cycles Unmukto's own layouts. It goes on a key that already exists
     * rather than a new one because the bottom row is full, and on space in particular
     * because that is where other keyboards put layout switching.
     */
    @Override
    public boolean onKeyLongPress(int primaryCode) {
        if (primaryCode == KeyCodes.SWITCH_IME) {
            showKeyboardPicker();
            return true;
        }
        // Only while the letters are on screen: in the emoji panel there is no layout on
        // display for the change to be visible in.
        if (primaryCode == KeyCodes.SPACE && layouts != null && !isEmojiPanelShown()) {
            layouts.selectNext();
            // The layout changes under the user's thumb, so it says which one it landed on.
            Toast.makeText(this, layouts.getLayout().getLabelRes(), Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    /**
     * Deletes one character, where a character is a code point rather than a Java char.
     *
     * Everything Bengali fits in one char, so deleting a single unit was right until it
     * was not: emoji live outside the basic plane and are stored as a surrogate pair, and
     * removing one half of one leaves an unpaired surrogate in the field, which renders as
     * a replacement glyph and takes a second press to clear.
     */
    /**
     * Keeps the bottom row above the navigation bar.
     *
     * The IME window is drawn edge to edge from API 35, so on a gesture-navigation device
     * the bar sits over the bottom row: the home pill lands on the space bar and the
     * system's own keyboard-switch button lands on backspace. Both are still touch targets
     * underneath, which makes the last row of keys unreliable rather than merely ugly.
     *
     * The inset is added to the padding the layout already has rather than replacing it. A
     * platform that hands the IME an already-inset window reports zero here, and the
     * keyboard then keeps exactly the 8dp it was designed with.
     */
    private void keepClearOfNavigationBar(View root) {
        final int basePaddingBottom = root.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) -> {
            // Fully qualified: InputMethodService has an inner Insets of its own, and it
            // wins over an import.
            androidx.core.graphics.Insets bars =
                    windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars());
            view.setPadding(
                    view.getPaddingLeft(),
                    view.getPaddingTop(),
                    view.getPaddingRight(),
                    basePaddingBottom + bars.bottom);
            return windowInsets;
        });
    }

    private void showEmojiPanel(boolean show) {
        if (ukvMain == null || emojiPanel == null)
            return;
        emojiPanel.setVisibility(show ? View.VISIBLE : View.GONE);
        ukvMain.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private boolean isEmojiPanelShown() {
        return emojiPanel != null && emojiPanel.getVisibility() == View.VISIBLE;
    }

    /** Emoji arrive as whole strings rather than as key codes, so they take the text path. */
    private void commitText(CharSequence text) {
        InputConnection ic = getCurrentInputConnection();
        if (ic != null)
            ic.commitText(text, 1);
    }

    private void handleDelete(InputConnection ic) {
        CharSequence selectedText = ic.getSelectedText(0);
        if (!TextUtils.isEmpty(selectedText)) {
            ic.commitText("", 1);
            return;
        }
        CharSequence before = ic.getTextBeforeCursor(2, 0);
        boolean surrogatePair = before != null
                && before.length() == 2
                && Character.isSurrogatePair(before.charAt(0), before.charAt(1));
        ic.deleteSurroundingText(surrogatePair ? 2 : 1, 0);
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

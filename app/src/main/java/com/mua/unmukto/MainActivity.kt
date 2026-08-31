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
package com.mua.unmukto

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Rect
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.mua.unmukto.keyboard.BuiltInLayoutCatalog
import com.mua.unmukto.keyboard.LayoutCatalog
import com.mua.unmukto.keyboard.LayoutStore
import com.mua.unmukto.keyboard.SharedPreferencesLayoutStore

/**
 * Setup screen for the keyboard. Enabling an IME and selecting it are both actions only the
 * user can take, from system UI, so this screen reports where they are in that process and
 * offers the two jumps rather than performing them.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var btnEnable: Button
    private lateinit var btnSwitch: Button
    private lateinit var rgLayout: RadioGroup

    private val catalog: LayoutCatalog = BuiltInLayoutCatalog
    private lateinit var layoutStore: LayoutStore

    private val selectionListener = LayoutStore.SelectionListener { layoutId ->
        checkLayout(layoutId)
    }

    private val imm: InputMethodManager
        get() = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fitSystemBars()

        tvStatus = findViewById(R.id.tv_status)
        btnEnable = findViewById(R.id.btn_enable)
        btnSwitch = findViewById(R.id.btn_switch)
        rgLayout = findViewById(R.id.rg_layout)

        layoutStore = SharedPreferencesLayoutStore(this)
        showLayoutChoices()

        btnEnable.setOnClickListener {
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        }
        btnSwitch.setOnClickListener {
            imm.showInputMethodPicker()
        }
    }

    /**
     * Keeps the content out from under the system bars.
     *
     * From API 35 an app is drawn edge to edge and cannot opt out, so the window now
     * reaches behind the status and navigation bars. Without this the title sits under the
     * clock. The insets are added to the padding the layout already has rather than
     * replacing it, so the 24dp margin survives on a device that reports no inset at all.
     *
     * The bar icons are told which way to contrast at the same time. They default to light,
     * which is invisible against this screen in the day theme.
     */
    private fun fitSystemBars() {
        val content = findViewById<View>(R.id.setup_content)
        val padding = Rect(
            content.paddingLeft, content.paddingTop, content.paddingRight, content.paddingBottom
        )
        ViewCompat.setOnApplyWindowInsetsListener(content) { view, windowInsets ->
            val bars = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            view.setPadding(
                padding.left + bars.left,
                padding.top + bars.top,
                padding.right + bars.right,
                padding.bottom + bars.bottom
            )
            windowInsets
        }

        val lightTheme = resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK != Configuration.UI_MODE_NIGHT_YES
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = lightTheme
            isAppearanceLightNavigationBars = lightTheme
        }
    }

    override fun onStart() {
        super.onStart()
        layoutStore.addSelectionListener(selectionListener)
    }

    override fun onStop() {
        layoutStore.removeSelectionListener(selectionListener)
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        refreshStatus()
    }

    private fun refreshStatus() {
        val enabled = isEnabled()
        val selected = isSelected()

        tvStatus.setText(
            when {
                selected -> R.string.status_ready
                enabled -> R.string.status_enabled_not_selected
                else -> R.string.status_not_enabled
            }
        )
        // Reachable whenever Unmukto is enabled, including once it is the active keyboard:
        // that is the state someone looking for a way back out is in, and disabling the
        // button there would take away the only exit this screen offers.
        btnEnable.isEnabled = !enabled
        btnSwitch.isEnabled = enabled
        btnSwitch.setText(
            if (selected) R.string.action_switch_away else R.string.action_switch
        )
    }

    /**
     * Builds the choice list from the catalogue rather than from a fixed set of views, so a
     * layout added to the catalogue appears here without this screen being touched.
     *
     * The choice is written straight to the store, which is the same one the keyboard reads
     * on its way into a field, and the store reports back when the keyboard is the one that
     * changed it -- which happens on this very screen, since it has a field to try the
     * keyboard out in.
     */
    private fun showLayoutChoices() {
        for (layout in catalog.layouts) {
            rgLayout.addView(
                RadioButton(this).apply {
                    id = View.generateViewId()
                    tag = layout.id
                    setText(layout.labelRes)
                    textSize = 16f
                },
                RadioGroup.LayoutParams(
                    RadioGroup.LayoutParams.MATCH_PARENT,
                    RadioGroup.LayoutParams.WRAP_CONTENT
                )
            )
        }
        // Attached after the initial checks, which would otherwise report themselves as a
        // choice the user made and write the default back out.
        checkLayout(layoutStore.selectedLayoutId)
        // Attached after the initial check, which would otherwise report itself as a choice
        // the user made and write the default back out.
        rgLayout.setOnCheckedChangeListener { group, checkedId ->
            val chosen = group.findViewById<RadioButton>(checkedId) ?: return@setOnCheckedChangeListener
            layoutStore.selectedLayoutId = chosen.tag as? String
        }
    }

    /**
     * Moves the check to whichever layout is selected, wherever the selection came from.
     *
     * The guard is what keeps this from looping: checking a button fires the change
     * listener, which writes to the store, which reports back here.
     */
    private fun checkLayout(layoutId: String?) {
        val selected = catalog.find(layoutId) ?: catalog.default
        val button = rgLayout.findViewWithTag<RadioButton>(selected.id) ?: return
        if (!button.isChecked)
            button.isChecked = true
    }

    /** True once the user has ticked Unmukto in the system's input method settings. */
    private fun isEnabled(): Boolean =
        imm.enabledInputMethodList.any { it.packageName == packageName }

    /** True once Unmukto is the keyboard the system will actually bring up. */
    private fun isSelected(): Boolean {
        val current = Settings.Secure.getString(
            contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD
        )
        return current != null && current.startsWith("$packageName/")
    }
}

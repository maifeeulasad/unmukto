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
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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

    private val imm: InputMethodManager
        get() = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
     * on its way into a field. There is no message to send: the keyboard is not running
     * while the user is on this screen, and it re-reads the store when it next comes up.
     */
    private fun showLayoutChoices() {
        val selected = catalog.find(layoutStore.selectedLayoutId) ?: catalog.default
        for (layout in catalog.layouts) {
            rgLayout.addView(
                RadioButton(this).apply {
                    id = View.generateViewId()
                    tag = layout.id
                    setText(layout.labelRes)
                    textSize = 16f
                    isChecked = layout.id == selected.id
                },
                RadioGroup.LayoutParams(
                    RadioGroup.LayoutParams.MATCH_PARENT,
                    RadioGroup.LayoutParams.WRAP_CONTENT
                )
            )
        }
        // Attached after the initial checks, which would otherwise report themselves as a
        // choice the user made and write the default back out.
        rgLayout.setOnCheckedChangeListener { group, checkedId ->
            val chosen = group.findViewById<RadioButton>(checkedId) ?: return@setOnCheckedChangeListener
            layoutStore.selectedLayoutId = chosen.tag as? String
        }
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

package com.mua.unmukto

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * Setup screen for the keyboard. Enabling an IME and selecting it are both actions only the
 * user can take, from system UI, so this screen reports where they are in that process and
 * offers the two jumps rather than performing them.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var btnEnable: Button
    private lateinit var btnSwitch: Button

    private val imm: InputMethodManager
        get() = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tv_status)
        btnEnable = findViewById(R.id.btn_enable)
        btnSwitch = findViewById(R.id.btn_switch)

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
        btnEnable.isEnabled = !enabled
        btnSwitch.isEnabled = enabled && !selected
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

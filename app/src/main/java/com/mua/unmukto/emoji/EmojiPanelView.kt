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
package com.mua.unmukto.emoji

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.mua.unmukto.R
import com.mua.unmukto.UnmuktoKeyboardView

/**
 * The emoji panel: a row of category tabs over a scrolling grid, with the keyboard's own
 * action row along the bottom.
 *
 * A grid that scrolls rather than pages of keys. There are a few thousand emoji and no
 * arrangement of them fits on a keyboard, so the choice is between showing a curated
 * handful and showing all of them in something that scrolls. Scrolling is also how every
 * other emoji picker on the phone behaves, which is worth more here than any layout this
 * keyboard could invent.
 *
 * Nothing in this class is language-specific. It does not know which layout the keyboard
 * came from or which it will return to, and the letters key that leaves it is handled by
 * the service like any other key. That is deliberate: emoji are the same characters
 * whatever the keyboard around them types, and a second language should reuse this panel
 * as it stands.
 */
class EmojiPanelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    fun interface OnEmojiSelectedListener {
        fun onEmojiSelected(emoji: String)
    }

    var onEmojiSelectedListener: OnEmojiSelectedListener? = null

    /** The action row, for the service to drive as it drives the keyboard's own. */
    val actionKeyboardView: UnmuktoKeyboardView

    private val tabScroll: HorizontalScrollView
    private val tabStrip: LinearLayout
    private val grid: GridView

    private val repository = EmojiRepository()
    private val tabs = mutableListOf<TextView>()

    private var catalog: EmojiCatalog = UnicodeEmojiCatalog

    /** The category to fill the grid with once there is a reason to fill it. */
    private var pendingCategory = 0

    // Declared before init, which reaches them through bind(): a property below an init
    // block is still zero while that block runs.
    private val tabPadding = dp(12f).toInt()
    private val itemHeight = dp(52f).toInt()

    init {
        orientation = VERTICAL
        setBackgroundColor(ContextCompat.getColor(context, R.color.keyboard_background))
        LayoutInflater.from(context).inflate(R.layout.view_emoji_panel, this, true)

        tabScroll = findViewById(R.id.emoji_tab_scroll)
        tabStrip = findViewById(R.id.emoji_tabs)
        grid = findViewById(R.id.emoji_grid)
        actionKeyboardView = findViewById(R.id.emoji_actions)

        grid.setOnItemClickListener { parent, _, position, _ ->
            val emoji = parent.getItemAtPosition(position) as? String ?: return@setOnItemClickListener
            onEmojiSelectedListener?.onEmojiSelected(emoji)
        }

        bind(catalog)
    }

    /** Rebuilds the tabs. Called once from [init]; public so a different catalogue can be set. */
    fun bind(catalog: EmojiCatalog) {
        this.catalog = catalog
        tabStrip.removeAllViews()
        tabs.clear()
        catalog.categories.forEachIndexed { index, category ->
            val tab = newTab(category)
            tab.setOnClickListener { show(index) }
            tabStrip.addView(tab)
            tabs.add(tab)
        }
        show(0)
    }

    private fun newTab(category: EmojiCategory): TextView = TextView(context).apply {
        text = category.icon
        contentDescription = context.getString(category.labelRes)
        gravity = Gravity.CENTER
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
        setPadding(tabPadding, 0, tabPadding, 0)
        layoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT, 1f)
    }

    private fun show(index: Int) {
        val category = catalog.categories.getOrNull(index) ?: return
        // Expanding a category walks a few thousand code points asking the font about each,
        // so it happens on the tab that is actually opened rather than for all of them up
        // front. The repository keeps the answer, so a tab is slow at most once.
        grid.adapter = EmojiAdapter(repository.emoji(category))
        grid.setSelection(0)
        tabs.forEachIndexed { position, tab ->
            val active = position == index
            // A drawable rather than a flat colour: a colour fills the whole cell, which
            // squares off against the panel edge and reads as a rendering fault.
            if (active) tab.setBackgroundResource(R.drawable.emoji_tab_selected)
            else tab.setBackgroundColor(Color.TRANSPARENT)
            tab.alpha = if (active) 1f else INACTIVE_TAB_ALPHA
        }
        // Posted because a tab that was just added has no position until the strip is laid
        // out, and a category can be selected before that has happened.
        tabs.getOrNull(index)?.let { tab -> tabScroll.post { tabScroll.smoothScrollTo(tab.left, 0) } }
    }

    private fun dp(value: Float) = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, value, resources.displayMetrics
    )

    private companion object {
        const val INACTIVE_TAB_ALPHA = 0.55f
        const val EMOJI_TEXT_SIZE_SP = 24f
    }

    /**
     * Flat list of strings, one code point each. There is no view state to recycle beyond
     * the text, so the adapter reuses the convertView and sets it.
     */
    private inner class EmojiAdapter(private val emoji: List<String>) : BaseAdapter() {

        override fun getCount() = emoji.size

        override fun getItem(position: Int) = emoji[position]

        override fun getItemId(position: Int) = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView as? TextView ?: TextView(context).apply {
                gravity = Gravity.CENTER
                setTextSize(TypedValue.COMPLEX_UNIT_SP, EMOJI_TEXT_SIZE_SP)
                layoutParams = AbsListView.LayoutParams(
                    AbsListView.LayoutParams.MATCH_PARENT, itemHeight
                )
                setBackgroundResource(selectableBackground())
            }
            view.text = emoji[position]
            return view
        }

        private fun selectableBackground(): Int {
            val value = TypedValue()
            context.theme.resolveAttribute(android.R.attr.selectableItemBackground, value, true)
            return value.resourceId
        }
    }
}

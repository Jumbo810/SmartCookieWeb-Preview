package com.cookiejarapps.android.smartcookieweb.browser.home

import android.content.Context
import androidx.core.content.ContextCompat.getColor
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.cookiejarapps.android.smartcookieweb.R
import com.cookiejarapps.android.smartcookieweb.preferences.UserPreferences
import mozilla.components.browser.menu.BrowserMenuBuilder
import mozilla.components.browser.menu.BrowserMenuHighlight
import mozilla.components.browser.menu.ext.getHighlight
import mozilla.components.browser.menu.item.BrowserMenuDivider
import mozilla.components.browser.menu.item.BrowserMenuHighlightableItem
import mozilla.components.browser.menu.item.BrowserMenuImageText

class HomeMenu(
    private val lifecycleOwner: LifecycleOwner,
    private val context: Context,
    private val onItemTapped: (Item) -> Unit = {},
    private val onMenuBuilderChanged: (BrowserMenuBuilder) -> Unit = {},
    private val onHighlightPresent: (BrowserMenuHighlight) -> Unit = {}
) {
    sealed class Item {
        object WhatsNew : Item()
        object Help : Item()
        object AddonsManager : Item()
        object Settings : Item()
        object SyncedTabs : Item()
        object History : Item()
        object Bookmarks : Item()
        object Downloads : Item()
        object Quit : Item()
    }

    // private val shouldUseBottomToolbar = UserPreferences(context).shouldUseBottomToolbar

    private val coreMenuItems by lazy {

        val bookmarksIcon = R.drawable.ic_baseline_bookmark

        val bookmarksItem = BrowserMenuImageText(
            context.getString(R.string.action_bookmarks),
            bookmarksIcon,
            R.color.primary_icon
        ) {
            onItemTapped.invoke(Item.Bookmarks)
        }

        val historyItem = BrowserMenuImageText(
            context.getString(R.string.action_history),
            R.drawable.ic_baseline_history,
            R.color.primary_icon
        ) {
            onItemTapped.invoke(Item.History)
        }

        val addons = BrowserMenuImageText(
            context.getString(R.string.mozac_browser_menu_addons),
            R.drawable.mozac_ic_extensions,
            R.color.primary_icon
        ) {
            onItemTapped.invoke(Item.AddonsManager)
        }

        val settingsItem = BrowserMenuImageText(
            context.getString(R.string.settings),
            R.drawable.ic_round_settings,
            R.color.primary_icon
        ) {
            onItemTapped.invoke(Item.Settings)
        }

        val downloadsItem = BrowserMenuImageText(
            context.getString(R.string.action_downloads),
            R.drawable.mozac_feature_download_ic_download,
            R.color.primary_icon
        ) {
            onItemTapped.invoke(Item.Downloads)
        }

        val menuItems = listOfNotNull(
            BrowserMenuDivider(),
            BrowserMenuDivider(),
            historyItem,
            downloadsItem,
            bookmarksItem,
            BrowserMenuDivider(),
            settingsItem,
            addons
        ).also { items ->
            items.getHighlight()?.let { onHighlightPresent(it) }
        }

        // if (shouldUseBottomToolbar) {
        //     menuItems.reversed()
        // } else {
            menuItems
        // }
    }

    init {
        onMenuBuilderChanged(BrowserMenuBuilder(coreMenuItems))
    }
}

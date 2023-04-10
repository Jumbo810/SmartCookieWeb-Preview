package com.cookiejarapps.android.smartcookieweb.browser.home

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Display.FLAG_SECURE
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.ConstraintSet.*
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.cookiejarapps.android.smartcookieweb.BrowserActivity
import com.cookiejarapps.android.smartcookieweb.R
// import com.cookiejarapps.android.smartcookieweb.addons.AddonsActivity
import com.cookiejarapps.android.smartcookieweb.browser.BrowsingMode
// import com.cookiejarapps.android.smartcookieweb.browser.shortcuts.ShortcutDatabase
// import com.cookiejarapps.android.smartcookieweb.browser.shortcuts.ShortcutEntity
// import com.cookiejarapps.android.smartcookieweb.browser.shortcuts.ShortcutGridAdapter
// import com.cookiejarapps.android.smartcookieweb.databinding.FragmentBookmarkBinding
import com.cookiejarapps.android.smartcookieweb.databinding.FragmentHomeBinding
import com.cookiejarapps.android.smartcookieweb.ext.components
import com.cookiejarapps.android.smartcookieweb.ext.nav
// import com.cookiejarapps.android.smartcookieweb.history.HistoryActivity
import com.cookiejarapps.android.smartcookieweb.preferences.UserPreferences
// import com.cookiejarapps.android.smartcookieweb.settings.activity.SettingsActivity
import com.google.android.material.appbar.AppBarLayout
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import mozilla.components.browser.menu.view.MenuButton
import mozilla.components.browser.state.selector.normalTabs
import mozilla.components.browser.state.selector.privateTabs
import mozilla.components.browser.state.state.BrowserState
import mozilla.components.browser.state.state.selectedOrDefaultSearchEngine
import mozilla.components.browser.state.store.BrowserStore
// import mozilla.components.browser.toolbar.behavior.ToolbarPosition
import mozilla.components.lib.state.ext.consumeFlow
import mozilla.components.lib.state.ext.consumeFrom
import mozilla.components.support.ktx.android.content.getColorFromAttr
import mozilla.components.support.ktx.kotlinx.coroutines.flow.ifChanged
import mozilla.components.ui.tabcounter.TabCounterMenu
import org.mozilla.gecko.util.ThreadUtils
import java.lang.ref.WeakReference


@ExperimentalCoroutinesApi
class HomeFragment : Fragment() {
    // private var database: ShortcutDatabase? = null

    private val args by navArgs<HomeFragmentArgs>()
    private lateinit var bundleArgs: Bundle

    private val browsingModeManager get() = (activity as BrowserActivity).browsingModeManager

    private val store: BrowserStore
        get() = components.store

    private var appBarLayout: AppBarLayout? = null

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    @VisibleForTesting
    internal var getMenuButton: () -> MenuButton? = { binding.menuButton }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bundleArgs = args.toBundle()
    }

    @Suppress("LongMethod")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        val activity = activity as BrowserActivity
        val components = requireContext().components

        // updateLayout(view)

        if(!UserPreferences(requireContext()).showShortcuts){
            binding.shortcutName.visibility = View.GONE
            binding.shortcutGrid.visibility = View.GONE
        }

        if(!UserPreferences(requireContext()).shortcutDrawerOpen){
            binding.shortcutName.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_baseline_shortcuts, 0, R.drawable.ic_baseline_chevron_up, 0)
            binding.shortcutGrid.visibility = View.GONE
        }

        binding.shortcutName.setOnClickListener {
            if(UserPreferences(requireContext()).shortcutDrawerOpen){
                UserPreferences(requireContext()).shortcutDrawerOpen = false
                binding.shortcutName.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_baseline_shortcuts, 0, R.drawable.ic_baseline_chevron_up, 0)
                binding.shortcutGrid.visibility = View.GONE
            }
            else{
                UserPreferences(requireContext()).shortcutDrawerOpen = true
                binding.shortcutName.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_baseline_shortcuts, 0, R.drawable.ic_baseline_chevron_down, 0)
                binding.shortcutGrid.visibility = View.VISIBLE
            }
        }

        // GlobalScope.launch {
        //     // Update shortcut database to hold name
        //     val MIGRATION_1_2: Migration = object : Migration(1, 2) {
        //         override fun migrate(database: SupportSQLiteDatabase) {
        //             database.execSQL("ALTER TABLE shortcutentity ADD COLUMN title TEXT")
        //         }
        //     }

        //     database = Room.databaseBuilder(
        //         requireContext(),
        //         ShortcutDatabase::class.java, "shortcut-database"
        //     ).addMigrations(MIGRATION_1_2).build()

        //     val shortcutDao = database?.shortcutDao()
        //     val shortcuts: MutableList<ShortcutEntity> = shortcutDao?.getAll() as MutableList

        //     val adapter = ShortcutGridAdapter(requireContext(), shortcuts)

        //     ThreadUtils.runOnUiThread {
        //         binding.shortcutGrid.adapter = adapter
        //     }
        // }

        // binding.shortcutGrid.setOnItemClickListener { _, _, position, _ ->
        //     findNavController().navigate(
        //             R.id.browserFragment
        //         )

        //         components.sessionUseCases.loadUrl(
        //             (binding.shortcutGrid.adapter.getItem(position) as ShortcutEntity).url!!)
        // }

        // binding.shortcutGrid.setOnItemLongClickListener { _, _, position, _ ->
        //     val items = arrayOf(resources.getString(R.string.edit_shortcut), resources.getString(R.string.delete_shortcut))

        //     AlertDialog.Builder(requireContext())
        //         .setTitle(resources.getString(R.string.edit_shortcut))
        //         .setItems(items) { _, which ->
        //             when(which){
        //                 0 -> showEditShortcutDialog(position, binding.shortcutGrid.adapter as ShortcutGridAdapter)
        //                 1 -> deleteShortcut(binding.shortcutGrid.adapter.getItem(position) as ShortcutEntity, binding.shortcutGrid.adapter as ShortcutGridAdapter)
        //             }
        //         }
        //         .show()

        //     return@setOnItemLongClickListener true
        // }

        // binding.addShortcut.setOnClickListener {
        //     showCreateShortcutDialog(binding.shortcutGrid.adapter as ShortcutGridAdapter)
        // }

        if(browsingModeManager.mode == BrowsingMode.Private) {
            binding.toolbarWrapper.background = context?.let { ContextCompat.getDrawable(it, R.drawable.toolbar_background_private) }
        }

        appBarLayout = binding.homeAppBar

        return view
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        getMenuButton()?.dismissMenu()
    }

    // private fun showEditShortcutDialog(position: Int, adapter: ShortcutGridAdapter){
    //     val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
    //     builder.setTitle(resources.getString(R.string.edit_shortcut))
    //     val viewInflated: View = LayoutInflater.from(context).inflate(R.layout.add_shortcut_dialog, view as ViewGroup?, false)
    //     val url = viewInflated.findViewById<View>(R.id.urlEditText) as EditText
    //     url.setText(adapter.list[position].url)
    //     val name = viewInflated.findViewById<View>(R.id.nameEditText) as EditText
    //     name.setText(adapter.list[position].title)
    //     builder.setView(viewInflated)

    //     builder.setPositiveButton(android.R.string.ok) { _, _ ->
    //         val item = adapter.list[position]
    //         item.url = url.text.toString()
    //         item.title = name.text.toString()
    //         adapter.notifyDataSetChanged()

    //         GlobalScope.launch {
    //             database?.shortcutDao()?.update(item)
    //         }
    //     }
    //     builder.setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.cancel() }

    //     builder.show()
    // }

    // private fun showCreateShortcutDialog(adapter: ShortcutGridAdapter){
    //     val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
    //     builder.setTitle(resources.getString(R.string.add_shortcut))
    //     val viewInflated: View = LayoutInflater.from(context).inflate(R.layout.add_shortcut_dialog, view as ViewGroup?, false)
    //     val url = viewInflated.findViewById<View>(R.id.urlEditText) as EditText
    //     val name = viewInflated.findViewById<View>(R.id.nameEditText) as EditText
    //     builder.setView(viewInflated)

    //     builder.setPositiveButton(android.R.string.ok) { dialog, _ ->
    //         dialog.dismiss()
    //         val list = adapter.list
    //         list.add(ShortcutEntity(url = url.text.toString(), title = name.text.toString()))
    //         adapter.list = list
    //         adapter.notifyDataSetChanged()

    //         GlobalScope.launch {
    //             database?.shortcutDao()?.insertAll(ShortcutEntity(url = url.text.toString(), title = name.text.toString()))
    //         }
    //     }
    //     builder.setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.cancel() }

    //     builder.show()
    // }

    // private fun deleteShortcut(shortcutEntity: ShortcutEntity, adapter: ShortcutGridAdapter) {
    //     val list = adapter.list
    //     list.remove(shortcutEntity)
    //     adapter.list = list
    //     adapter.notifyDataSetChanged()

    //     GlobalScope.launch {
    //         database?.shortcutDao()?.delete(shortcutEntity)
    //     }
    // }

    // private fun updateLayout(view: View) {
    //     when (UserPreferences(view.context).toolbarPosition) {
    //         ToolbarPosition.TOP.ordinal -> {
    //             binding.toolbarLayout.layoutParams = CoordinatorLayout.LayoutParams(
    //                 ConstraintLayout.LayoutParams.MATCH_PARENT,
    //                 ConstraintLayout.LayoutParams.WRAP_CONTENT
    //             ).apply {
    //                 gravity = Gravity.TOP
    //             }

    //             ConstraintSet().apply {
    //                 clone(binding.toolbarLayout)
    //                 clear(binding.bottomBar.id, BOTTOM)
    //                 clear(binding.bottomBarShadow.id, BOTTOM)
    //                 connect(binding.bottomBar.id, TOP, PARENT_ID, TOP)
    //                 connect(binding.bottomBarShadow.id, TOP, binding.bottomBar.id, BOTTOM)
    //                 connect(binding.bottomBarShadow.id, BOTTOM, PARENT_ID, BOTTOM)
    //                 applyTo(binding.toolbarLayout)
    //             }

    //             binding.homeAppBar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
    //                 topMargin =
    //                     resources.getDimensionPixelSize(R.dimen.home_fragment_top_toolbar_header_margin)
    //             }
    //         }
    //         ToolbarPosition.BOTTOM.ordinal -> {
    //         }
    //     }
    // }

    @Suppress("LongMethod", "ComplexMethod")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // observeSearchEngineChanges()
        // createHomeMenu(requireContext(), WeakReference(binding.menuButton))
        // createTabCounterMenu(view)

        binding.menuButton.setColorFilter(
            ContextCompat.getColor(
                requireContext(),
                R.color.main_icon
            )
        )

        // binding.toolbarWrapper.setOnClickListener {
        //     navigateToSearch()
        // }

        // binding.tabButton.setOnClickListener {
        //     openTabDrawer()
        // }

        if (browsingModeManager.mode.isPrivate) {
            requireActivity().window.addFlags(FLAG_SECURE)
        } else {
            requireActivity().window.clearFlags(FLAG_SECURE)
        }

        consumeFrom(components.store) {
            updateTabCounter(it)
        }

        updateTabCounter(components.store.state)

        // if (bundleArgs.getBoolean(FOCUS_ON_ADDRESS_BAR)) {
        //     navigateToSearch()
        // }
    }

    // private fun observeSearchEngineChanges() {
    //     consumeFlow(store) { flow ->
    //         flow.map { state -> state.search.selectedOrDefaultSearchEngine }
    //             .ifChanged()
    //             .collect { searchEngine ->
    //                 if (searchEngine != null) {
    //                     val iconSize =
    //                         requireContext().resources.getDimensionPixelSize(R.dimen.icon_width)
    //                     val searchIcon =
    //                         BitmapDrawable(requireContext().resources, searchEngine.icon)
    //                     searchIcon.setBounds(0, 0, iconSize, iconSize)
    //                     binding.searchEngineIcon?.setImageDrawable(searchIcon)
    //                 } else {
    //                     binding.searchEngineIcon.setImageDrawable(null)
    //                 }
    //             }
    //     }
    // }

    // private fun createTabCounterMenu(view: View) {
    //     val browsingModeManager = (activity as BrowserActivity).browsingModeManager
    //     val mode = browsingModeManager.mode

    //     val onItemTapped: (TabCounterMenu.Item) -> Unit = {
    //         if (it is TabCounterMenu.Item.NewTab) {
    //             browsingModeManager.mode = BrowsingMode.Normal
    //         } else if (it is TabCounterMenu.Item.NewPrivateTab) {
    //             browsingModeManager.mode = BrowsingMode.Private
    //         }
    //     }

    //     binding.tabButton.setOnLongClickListener {
    //         true
    //     }
    // }

    override fun onDestroyView() {
        super.onDestroyView()

        appBarLayout = null
        bundleArgs.clear()
        requireActivity().window.clearFlags(FLAG_SECURE)
    }

    // private fun navigateToSearch() {
    //     val directions =
    //         HomeFragmentDirections.actionGlobalSearchDialog(
    //             sessionId = null
    //         )

    //     // TODO: OPTIONS
    //     nav(R.id.homeFragment, directions, null)
    // }

    // @SuppressWarnings("ComplexMethod", "LongMethod")
    // private fun createHomeMenu(context: Context, menuButtonView: WeakReference<MenuButton>) =
    //     HomeMenu(
    //         this.viewLifecycleOwner,
    //         context,
    //         onItemTapped = {
    //             when (it) {
    //                 HomeMenu.Item.Settings -> {
    //                     val settings = Intent(activity, SettingsActivity::class.java)
    //                     settings.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    //                     requireActivity().startActivity(settings)
    //                 }
    //                 HomeMenu.Item.Bookmarks -> {
    //                     val drawerLayout = activity?.findViewById<DrawerLayout>(R.id.drawer_layout)
    //                     val bookmarksDrawer = if(UserPreferences(requireContext()).swapDrawers) requireActivity().findViewById<FrameLayout>(R.id.left_drawer) else requireActivity().findViewById<FrameLayout>(R.id.right_drawer)

    //                     if (bookmarksDrawer != null) {
    //                         drawerLayout?.openDrawer(bookmarksDrawer)
    //                     }
    //                 }
    //                 HomeMenu.Item.History -> {
    //                     val settings = Intent(activity, HistoryActivity::class.java)
    //                     settings.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    //                     activity?.startActivity(settings)
    //                 }
    //                 HomeMenu.Item.AddonsManager -> {
    //                     val settings = Intent(activity, AddonsActivity::class.java)
    //                     settings.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    //                     activity?.startActivity(settings)
    //                 }
    //                 else -> {}
    //             }
    //         },
    //         onHighlightPresent = { menuButtonView.get()?.setHighlight(it) },
    //         onMenuBuilderChanged = { menuButtonView.get()?.menuBuilder = it }
    //     )

    private fun openTabDrawer() {
        val drawerLayout = activity?.findViewById<DrawerLayout>(R.id.drawer_layout)
        val tabDrawer = if(UserPreferences(requireContext()).swapDrawers) requireActivity().findViewById<FrameLayout>(R.id.right_drawer) else requireActivity().findViewById<FrameLayout>(R.id.left_drawer)

        if (tabDrawer != null) {
            drawerLayout?.openDrawer(tabDrawer)
        }
    }

    private fun updateTabCounter(browserState: BrowserState) {
        val tabCount = if (browsingModeManager.mode.isPrivate) {
            browserState.privateTabs.size
        } else {
            browserState.normalTabs.size
        }

        binding?.tabButton?.setCountWithAnimation(tabCount)
    }

    companion object {
        private const val FOCUS_ON_ADDRESS_BAR = "focusOnAddressBar"
    }
}

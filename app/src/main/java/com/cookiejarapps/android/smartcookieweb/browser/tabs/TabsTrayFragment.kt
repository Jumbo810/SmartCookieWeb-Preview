// package com.cookiejarapps.android.smartcookieweb.browser.tabs

// import android.graphics.Color
// import android.os.Bundle
// import android.util.Log
// import android.view.LayoutInflater
// import android.view.View
// import android.view.ViewGroup
// import android.widget.FrameLayout
// import androidx.coordinatorlayout.widget.CoordinatorLayout
// import androidx.drawerlayout.widget.DrawerLayout
// import androidx.fragment.app.Fragment
// import androidx.navigation.findNavController
// import androidx.navigation.fragment.findNavController
// import androidx.recyclerview.widget.GridLayoutManager
// import androidx.recyclerview.widget.LinearLayoutManager
// import com.cookiejarapps.android.smartcookieweb.BrowserActivity
// import com.cookiejarapps.android.smartcookieweb.NavGraphDirections
// import com.cookiejarapps.android.smartcookieweb.R
// import com.cookiejarapps.android.smartcookieweb.browser.BrowsingMode
// import com.cookiejarapps.android.smartcookieweb.browser.BrowsingModeManager
// import com.cookiejarapps.android.smartcookieweb.browser.HomepageChoice
// import com.cookiejarapps.android.smartcookieweb.browser.home.HomeFragmentDirections
// import com.cookiejarapps.android.smartcookieweb.databinding.FragmentHomeBinding
// import com.cookiejarapps.android.smartcookieweb.databinding.FragmentTabstrayBinding
// import com.cookiejarapps.android.smartcookieweb.ext.components
// import com.cookiejarapps.android.smartcookieweb.ext.nav
// import com.cookiejarapps.android.smartcookieweb.preferences.UserPreferences
// import com.google.android.material.dialog.MaterialAlertDialogBuilder
// import com.google.android.material.snackbar.Snackbar
// import com.google.android.material.tabs.TabLayout
// import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
// import mozilla.components.browser.state.selector.findCustomTabOrSelectedTab
// import mozilla.components.browser.state.selector.findTabOrCustomTab
// import mozilla.components.browser.state.selector.getNormalOrPrivateTabs
// import mozilla.components.browser.state.selector.selectedTab
// import mozilla.components.browser.state.state.TabSessionState
// import mozilla.components.browser.tabstray.DefaultTabViewHolder
// import mozilla.components.browser.tabstray.TabsAdapter
// import mozilla.components.browser.tabstray.TabsTray
// import mozilla.components.browser.tabstray.TabsTrayStyling
// import mozilla.components.browser.thumbnails.loader.ThumbnailLoader
// import mozilla.components.feature.tabs.TabsUseCases
// import mozilla.components.feature.tabs.tabstray.TabsFeature
// import mozilla.components.support.base.feature.ViewBoundFeatureWrapper


// // A fragment for displaying the tabs tray.

// class TabsTrayFragment : Fragment() {
//     private val tabsFeature: ViewBoundFeatureWrapper<TabsFeature> = ViewBoundFeatureWrapper()

//     lateinit var browsingModeManager: BrowsingModeManager
//     lateinit var configuration: Configuration

//     private var _binding: FragmentTabstrayBinding? = null
//     private val binding get() = _binding!!

//     override fun onCreateView(
//         inflater: LayoutInflater,
//         container: ViewGroup?,
//         savedInstanceState: Bundle?
//     ): View{
//         _binding = FragmentTabstrayBinding.inflate(inflater, container, false)
//         val view = binding.root

//         return view
//     }

//     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//         super.onViewCreated(view, savedInstanceState)

//         browsingModeManager =  (activity as BrowserActivity).browsingModeManager
//         configuration = Configuration(if (browsingModeManager.mode == BrowsingMode.Normal) BrowserTabType.NORMAL else BrowserTabType.PRIVATE)

//         binding.toolbar.inflateMenu(R.menu.tabstray_menu)
//         binding.toolbar.setOnMenuItemClickListener {
//             when (it.itemId) {
//                 R.id.newTab -> {
//                     when (binding.tabLayout.selectedTabPosition) {
//                             0 -> {
//                                 browsingModeManager.mode = BrowsingMode.Normal
//                                 when(UserPreferences(requireContext()).homepageType){
//                                     HomepageChoice.VIEW.ordinal -> {
//                                         components.tabsUseCases.addTab.invoke(
//                                             "about:homepage",
//                                             selectTab = true
//                                         )
//                                     }
//                                     HomepageChoice.BLANK_PAGE.ordinal -> {
//                                         components.tabsUseCases.addTab.invoke(
//                                             "about:blank",
//                                             selectTab = true
//                                         )
//                                     }
//                                     HomepageChoice.CUSTOM_PAGE.ordinal -> {
//                                         components.tabsUseCases.addTab.invoke(
//                                             UserPreferences(requireContext()).customHomepageUrl,
//                                             selectTab = true
//                                         )
//                                     }
//                                 }
//                             }
//                             1 -> {
//                                 browsingModeManager.mode = BrowsingMode.Private
//                                 when(UserPreferences(requireContext()).homepageType){
//                                     HomepageChoice.VIEW.ordinal -> {
//                                         components.tabsUseCases.addPrivateTab.invoke(
//                                             "about:homepage",
//                                             selectTab = true
//                                         )
//                                     }
//                                     HomepageChoice.BLANK_PAGE.ordinal -> {
//                                         components.tabsUseCases.addPrivateTab.invoke(
//                                             "about:blank",
//                                             selectTab = true
//                                         )
//                                     }
//                                     HomepageChoice.CUSTOM_PAGE.ordinal -> {
//                                         components.tabsUseCases.addPrivateTab.invoke(
//                                             UserPreferences(requireContext()).customHomepageUrl,
//                                             selectTab = true
//                                         )
//                                     }
//                                 }
//                         }
//                     }
//                     closeTabsTray()
//                 }
//                 R.id.removeTabs -> {
//                    removeTabsDialog(view)
//                 }
//             }
//             true
//         }

//         val tabsAdapter = createTabsTray()

//         tabsFeature.set(
//             feature = TabsFeature(
//                 tabsTray = tabsAdapter,
//                 store = components.store,
//                 defaultTabsFilter = { it.filterFromConfig(configuration) },
//                 onCloseTray = ::closeTabsTray
//             ),
//             owner = this,
//             view = view
//         )

//         binding.tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
//             override fun onTabSelected(tab: TabLayout.Tab) {
//                 when (tab.position) {
//                     0 -> {
//                         tabsFeature.get()?.filterTabs {
//                             it.filterFromConfig(
//                                 Configuration(
//                                     BrowserTabType.NORMAL
//                                 )
//                             )
//                         }
//                     }
//                     1 -> {
//                         tabsFeature.get()?.filterTabs {
//                             it.filterFromConfig(
//                                 Configuration(
//                                     BrowserTabType.PRIVATE
//                                 )
//                             )
//                         }
//                     }
//                 }
//             }

//             override fun onTabUnselected(tab: TabLayout.Tab) {}
//             override fun onTabReselected(tab: TabLayout.Tab) {}
//         })

//         binding.tabLayout.selectTab(binding.tabLayout.getTabAt(browsingModeManager.mode.ordinal))
//     }

//     private fun removeTabsDialog(view: View) {
//         val items = arrayOf(
//             requireContext().resources.getString(R.string.close_current_tab),
//             requireContext().resources.getString(R.string.close_other_tabs),
//             requireContext().resources.getString(R.string.close_all_tabs),
//             requireContext().resources.getString(R.string.close_app)
//         )

//         MaterialAlertDialogBuilder(requireContext())
//             .setTitle(resources.getString(R.string.mozac_feature_addons_remove))
//             .setItems(items) { dialog, which ->
//                 when (which) {
//                     0 -> {
//                         components.store.state.selectedTabId?.let { id ->
//                             components.tabsUseCases.removeTab(
//                                 id
//                             )
//                         }
//                         if (view.context.components.store.state.tabs.isEmpty() && UserPreferences(
//                                 requireContext()
//                             ).homepageType == HomepageChoice.VIEW.ordinal
//                         ) {
//                             findNavController().navigate(
//                                 HomeFragmentDirections.actionGlobalHome(
//                                     focusOnAddressBar = false
//                                 )
//                             )
//                         }
//                         // TODO: this doesn't appear if the last tab is closed and bottom toolbar is on, as the toolbar view on the homepage is R.id.toolbarLayout
//                         val snackbar = Snackbar.make(
//                             view,
//                             view.resources.getString(R.string.tab_removed),
//                             Snackbar.LENGTH_LONG
//                         ).setAction(
//                             view.resources.getString(R.string.undo)
//                         ) {
//                             components.tabsUseCases.undo.invoke()
//                         }
//                         if(UserPreferences(requireContext()).shouldUseBottomToolbar) snackbar.anchorView =
//                             requireActivity().findViewById(R.id.toolbar)
//                         snackbar.show()
//                     }
//                     1 -> {
//                         val tabList = components.store.state.tabs.toMutableList()
//                         tabList.remove(components.store.state.selectedTab)
//                         val idList: MutableList<String> =
//                             emptyList<String>().toMutableList()
//                         for (i in tabList) idList.add(i.id)
//                         components.tabsUseCases.removeTabs.invoke(idList.toList())
//                     }
//                     2 -> {
//                         components.tabsUseCases.removeAllTabs.invoke()
//                         findNavController().navigate(
//                             HomeFragmentDirections.actionGlobalHome(
//                                 focusOnAddressBar = false
//                             )
//                         )
//                     }
//                     3 -> {
//                         requireActivity().finishAndRemoveTask()
//                     }
//                 }
//             }
//             .show()
//     }

//     private fun closeTabsTray() {
//         val drawerLayout = activity?.findViewById<DrawerLayout>(R.id.drawer_layout)
//         val tabsDrawer = activity?.findViewById<FrameLayout>(R.id.left_drawer)

//         if (tabsDrawer != null) {
//             drawerLayout?.closeDrawer(tabsDrawer)
//         }
//     }

//     private fun createTabsTray(): TabsTray {
//         val thumbnailLoader = ThumbnailLoader(components.thumbnailStorage)

//         val adapter = TabListAdapter(
//             thumbnailLoader = thumbnailLoader,
//             delegate = object : TabsTray.Delegate {
//                 override fun onTabSelected(tab: TabSessionState, source: String?) {
//                     components.tabsUseCases.selectTab(tab.id)
//                     closeTabsTray()

//                     if(tab.content.url == "about:homepage"){
//                         requireContext().components.sessionUseCases.reload(tab.id)
//                     }
//                     else if (requireActivity().findNavController(R.id.container).currentDestination?.id == R.id.browserFragment) {
//                         return
//                     } else if (!requireActivity().findNavController(R.id.container).popBackStack(R.id.browserFragment, false)) {
//                         requireActivity().findNavController(R.id.container).navigate(R.id.browserFragment)
//                     }
//                 }

//                 override fun onTabClosed(tab: TabSessionState, source: String?) {
//                     components.tabsUseCases.removeTab(tab.id)

//                     // Running the above line doesn't seem to immediately close the tab, so we can't check whether the newly selected tab is
//                     // a homepage tab or not, so we switch to the browser fragment just in case and reload so the browser will switch back to
//                     // the homepage if the new tab loads about:homepage
//                     // TODO
//                     if(requireContext().components.store.state.findCustomTabOrSelectedTab()?.content?.url == "about:homepage"){
//                         if (!requireActivity().findNavController(R.id.container).popBackStack(R.id.browserFragment, false)) {
//                             requireActivity().findNavController(R.id.container).navigate(R.id.browserFragment)
//                         }
//                     }

//                     if(requireActivity().components.store.state.tabs.isEmpty() && UserPreferences(requireActivity()).homepageType == HomepageChoice.VIEW.ordinal){
//                         requireActivity().finish()
//                     }
//                 }
//             }
//         )

//         binding.tabsTray.adapter = adapter
//         val layoutManager = if(UserPreferences(requireContext()).showTabsInGrid) GridLayoutManager(
//             context,
//             2
//         ) else LinearLayoutManager(context)
//         layoutManager.stackFromEnd = !UserPreferences(requireContext()).showTabsInGrid && UserPreferences(
//             requireContext()
//         ).stackFromBottom
//         binding.tabsTray.layoutManager = layoutManager

//         return adapter
//     }

//     fun notifyBrowsingModeStateChanged() {
//         browsingModeManager =  (activity as BrowserActivity).browsingModeManager
//         configuration = Configuration(if (browsingModeManager.mode == BrowsingMode.Normal) BrowserTabType.NORMAL else BrowserTabType.PRIVATE)

//         binding.tabLayout.selectTab(binding.tabLayout.getTabAt(browsingModeManager.mode.ordinal))
//     }
// }

// enum class BrowserTabType { NORMAL, PRIVATE }

// data class Configuration(val browserTabType: BrowserTabType)

// fun TabSessionState.filterFromConfig(configuration: Configuration): Boolean {
//     val isPrivate = configuration.browserTabType == BrowserTabType.PRIVATE

//     return content.private == isPrivate
// }
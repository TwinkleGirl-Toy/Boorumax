package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.SyncAlt
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.BooruDatabase
import com.example.data.BooruRepository
import com.example.data.NetworkEngine
import com.example.ui.BookmarksScreen
import com.example.ui.BooruViewModel
import com.example.ui.CoexistenceAndSyncScreen
import com.example.ui.CreatorsAndTagsScreen
import com.example.ui.ExploreScreen
import com.example.ui.PostDetailOverlay
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = BooruDatabase.getDatabase(applicationContext)
        val repository = BooruRepository(database.booruDao(), NetworkEngine())
        val initialSharedOrDeepLink = extractIncomingUrlOrText(intent)

        setContent {
            MyApplicationTheme(darkTheme = true) {
                val viewModel: BooruViewModel = viewModel(
                    factory = BooruViewModel.Factory(repository)
                )

                LaunchedEffect(initialSharedOrDeepLink) {
                    if (!initialSharedOrDeepLink.isNullOrBlank()) {
                        viewModel.handleIncomingUrlOrText(initialSharedOrDeepLink)
                    }
                }

                BoorusamaPlusApp(viewModel = viewModel)
            }
        }
    }

    private fun extractIncomingUrlOrText(intent: Intent?): String? {
        if (intent == null) return null
        return when (intent.action) {
            Intent.ACTION_VIEW -> intent.dataString
            Intent.ACTION_SEND -> intent.getStringExtra(Intent.EXTRA_TEXT)
            else -> null
        }
    }
}

@Composable
fun BoorusamaPlusApp(
    viewModel: BooruViewModel
) {
    val context = LocalContext.current
    val profiles by viewModel.profiles.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    val favoriteTags by viewModel.favoriteTags.collectAsStateWithLifecycle()
    val blacklistedTags by viewModel.blacklistedTags.collectAsStateWithLifecycle()
    val feedState by viewModel.feedState.collectAsStateWithLifecycle()
    val selectedPost by viewModel.selectedPost.collectAsStateWithLifecycle()
    val exportedJson by viewModel.exportedJson.collectAsStateWithLifecycle()
    val officialInstalled by viewModel.officialBoorusamaInstalled.collectAsStateWithLifecycle()

    var currentTab by rememberSaveable { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.checkOfficialAppInstalled(context)
    }

    LaunchedEffect(feedState.statusBannerMessage) {
        val msg = feedState.statusBannerMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearBanner()
        }
    }

    if (currentTab != 0 && selectedPost == null) {
        BackHandler {
            currentTab = 0
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentTab == 0,
                        onClick = { currentTab = 0 },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == 0) Icons.Filled.Explore else Icons.Outlined.Explore,
                                contentDescription = stringResource(R.string.nav_explore)
                            )
                        },
                        label = { Text(stringResource(R.string.nav_explore)) },
                        modifier = Modifier.testTag("nav_tab_explore")
                    )
                    NavigationBarItem(
                        selected = currentTab == 1,
                        onClick = { currentTab = 1 },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == 1) Icons.Filled.Hub else Icons.Outlined.Hub,
                                contentDescription = stringResource(R.string.nav_creators)
                            )
                        },
                        label = { Text(stringResource(R.string.nav_creators)) },
                        modifier = Modifier.testTag("nav_tab_creators")
                    )
                    NavigationBarItem(
                        selected = currentTab == 2,
                        onClick = { currentTab = 2 },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (bookmarks.isNotEmpty()) {
                                        Badge { Text("${bookmarks.size}") }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (currentTab == 2) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                    contentDescription = stringResource(R.string.nav_saved)
                                )
                            }
                        },
                        label = { Text(stringResource(R.string.nav_saved)) },
                        modifier = Modifier.testTag("nav_tab_bookmarks")
                    )
                    NavigationBarItem(
                        selected = currentTab == 3,
                        onClick = { currentTab = 3 },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == 3) Icons.Filled.SyncAlt else Icons.Outlined.SyncAlt,
                                contentDescription = stringResource(R.string.nav_coexistence)
                            )
                        },
                        label = { Text(stringResource(R.string.nav_coexistence)) },
                        modifier = Modifier.testTag("nav_tab_coexistence")
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    0 -> ExploreScreen(
                        profiles = profiles,
                        feedState = feedState,
                        bookmarks = bookmarks,
                        favoriteTags = favoriteTags,
                        autocompleteTags = viewModel.popularAutocompleteTags,
                        onSelectProfile = viewModel::selectProfile,
                        onSaveProfile = viewModel::saveBooruProfile,
                        onDeleteProfile = viewModel::deleteBooruProfile,
                        onSearchQueryChange = viewModel::updateSearchQuery,
                        onAppendTag = viewModel::appendTagToSearch,
                        onSaveFavoriteTag = { viewModel.saveFavoriteTag(it) },
                        onSetRatingFilter = viewModel::setRatingFilter,
                        onSetSortOrder = viewModel::setSortOrder,
                        onToggleGridColumns = viewModel::toggleGridColumns,
                        onChangePage = viewModel::changePage,
                        onRefresh = viewModel::refreshFeed,
                        onOpenPost = viewModel::openPostDetail,
                        onToggleBookmark = viewModel::toggleBookmark
                    )
                    1 -> CreatorsAndTagsScreen(
                        creators = viewModel.creators,
                        favoriteTags = favoriteTags,
                        blacklistedTags = blacklistedTags,
                        onLaunchCreatorOrHub = { engine, query ->
                            viewModel.switchToEngineAndSearch(engine, query)
                            currentTab = 0
                        },
                        onAddFavoriteTag = { viewModel.saveFavoriteTag(it) },
                        onAddBlacklistTag = viewModel::addBlacklistedTag,
                        onRemoveSavedTag = viewModel::removeSavedTag
                    )
                    2 -> BookmarksScreen(
                        bookmarks = bookmarks,
                        onOpenPost = viewModel::openPostDetail,
                        onToggleBookmark = viewModel::toggleBookmark,
                        onSearchTag = { tag ->
                            viewModel.updateSearchQuery(tag)
                            currentTab = 0
                        }
                    )
                    3 -> CoexistenceAndSyncScreen(
                        officialInstalled = officialInstalled,
                        exportedJson = exportedJson,
                        onGenerateBackupJson = viewModel::generateBackupJson,
                        onImportBackupJson = viewModel::importBackupJson,
                        onRefreshInstalledCheck = { viewModel.checkOfficialAppInstalled(context) }
                    )
                }
            }
        }

        // Full-Screen Post Detail Sheet / Overlay
        val activePost = selectedPost
        if (activePost != null) {
            val isBookmarked = bookmarks.any {
                it.uniqueKey == "${activePost.engineType.name}_${activePost.id}"
            }
            PostDetailOverlay(
                post = activePost,
                isBookmarked = isBookmarked,
                onClose = viewModel::closePostDetail,
                onToggleBookmark = { viewModel.toggleBookmark(activePost) },
                onSearchTag = { tag ->
                    viewModel.updateSearchQuery(tag)
                    currentTab = 0
                },
                onFavoriteTag = { tag, cat ->
                    viewModel.saveFavoriteTag(tag, cat)
                },
                onBlacklistTag = { tag ->
                    viewModel.addBlacklistedTag(tag)
                }
            )
        }
    }
}

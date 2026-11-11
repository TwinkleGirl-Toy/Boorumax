package com.example.ui

import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ArchiveSeedCatalog
import com.example.data.BookmarkedPostEntity
import com.example.data.BooruEngineType
import com.example.data.BooruPost
import com.example.data.BooruProfileEntity
import com.example.data.BooruRepository
import com.example.data.BooruTag
import com.example.data.ContentRating
import com.example.data.CreatorSpotlight
import com.example.data.NetworkSourceStatus
import com.example.data.SavedTagEntity
import com.example.data.SortOrder
import com.example.data.TagCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FeedUiState(
    val isLoading: Boolean = false,
    val posts: List<BooruPost> = emptyList(),
    val searchQuery: String = "",
    val page: Int = 1,
    val ratingFilter: ContentRating = ContentRating.ALL,
    val sortOrder: SortOrder = SortOrder.NEWEST,
    val gridColumns: Int = 2,
    val networkStatus: NetworkSourceStatus = NetworkSourceStatus.LIVE_API,
    val lastEndpoint: String = "https://joyreactor.cc/new/1",
    val latencyMs: Long = 42L,
    val statusBannerMessage: String? = null
)

class BooruViewModel(
    private val repository: BooruRepository
) : ViewModel() {

    val profiles: StateFlow<List<BooruProfileEntity>> = repository.allProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarks: StateFlow<List<BookmarkedPostEntity>> = repository.allBookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteTags: StateFlow<List<SavedTagEntity>> = repository.favoriteTags
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val blacklistedTags: StateFlow<List<SavedTagEntity>> = repository.blacklistedTags
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _feedState = MutableStateFlow(FeedUiState(isLoading = true))
    val feedState: StateFlow<FeedUiState> = _feedState.asStateFlow()

    private val _selectedPost = MutableStateFlow<BooruPost?>(null)
    val selectedPost: StateFlow<BooruPost?> = _selectedPost.asStateFlow()

    private val _exportedJson = MutableStateFlow("")
    val exportedJson: StateFlow<String> = _exportedJson.asStateFlow()

    private val _officialBoorusamaInstalled = MutableStateFlow(false)
    val officialBoorusamaInstalled: StateFlow<Boolean> = _officialBoorusamaInstalled.asStateFlow()

    val creators: List<CreatorSpotlight> = ArchiveSeedCatalog.spotlightCreators

    val popularAutocompleteTags: List<BooruTag> = listOf(
        BooruTag("sci-fi", TagCategory.COPYRIGHT, 18400),
        BooruTag("cyberpunk", TagCategory.COPYRIGHT, 14200),
        BooruTag("mecha", TagCategory.CHARACTER, 11900),
        BooruTag("pixel_art", TagCategory.GENERAL, 9800),
        BooruTag("anime", TagCategory.COPYRIGHT, 42000),
        BooruTag("fantasy", TagCategory.COPYRIGHT, 24100),
        BooruTag("concept_art", TagCategory.GENERAL, 15600),
        BooruTag("comics", TagCategory.META, 19500),
        BooruTag("creator:astrea_labs", TagCategory.ARTIST, 148),
        BooruTag("creator:kuro_mecha", TagCategory.ARTIST, 96),
        BooruTag("creator:lumina_brush", TagCategory.ARTIST, 64),
        BooruTag("creator:vesper_archive", TagCategory.ARTIST, 112),
        BooruTag("service:patreon", TagCategory.META, 14200),
        BooruTag("service:fanbox", TagCategory.META, 11800),
        BooruTag("service:gumroad", TagCategory.META, 4100),
        BooruTag("service:fantia", TagCategory.META, 6800),
        BooruTag("4k_wallpaper", TagCategory.META, 9100),
        BooruTag("valkyrie", TagCategory.CHARACTER, 3800),
        BooruTag("kitsune_spirit", TagCategory.CHARACTER, 4100)
    )

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
            combine(
                repository.allProfiles,
                repository.blacklistedTags
            ) { profileList, blacklist ->
                val active = profileList.firstOrNull { it.isActive } ?: profileList.firstOrNull()
                active to blacklist.map { it.tagQuery }.toSet()
            }.collect { (activeProfile, blacklistSet) ->
                if (activeProfile != null) {
                    reloadFeedInternal(activeProfile, blacklistSet)
                }
            }
        }
    }

    fun checkOfficialAppInstalled(context: Context) {
        val pm = context.packageManager
        val installed = listOf("com.degenk.boorusama", "com.degenk.boorusama.dev").any { pkg ->
            runCatching {
                pm.getPackageInfo(pkg, PackageManager.GET_ACTIVITIES)
                true
            }.getOrDefault(false)
        }
        _officialBoorusamaInstalled.value = installed
    }

    fun selectProfile(profileId: Int) {
        viewModelScope.launch {
            _feedState.value = _feedState.value.copy(page = 1, isLoading = true)
            repository.selectActiveProfile(profileId)
        }
    }

    fun switchToEngineAndSearch(engineType: BooruEngineType, query: String) {
        viewModelScope.launch {
            val match = profiles.value.firstOrNull { it.engine == engineType }
            _feedState.value = _feedState.value.copy(searchQuery = query, page = 1, isLoading = true)
            if (match != null && !match.isActive) {
                repository.selectActiveProfile(match.id)
            } else {
                refreshFeed()
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _feedState.value = _feedState.value.copy(searchQuery = query, page = 1)
        refreshFeed()
    }

    fun appendTagToSearch(tag: String) {
        val current = _feedState.value.searchQuery.trim()
        val tokens = current.split(Regex("\\s+")).filter { it.isNotBlank() }
        val newQuery = if (tag in tokens) current else (tokens + tag).joinToString(" ")
        updateSearchQuery(newQuery)
    }

    fun setRatingFilter(rating: ContentRating) {
        _feedState.value = _feedState.value.copy(ratingFilter = rating, page = 1)
        refreshFeed()
    }

    fun setSortOrder(sortOrder: SortOrder) {
        _feedState.value = _feedState.value.copy(sortOrder = sortOrder, page = 1)
        refreshFeed()
    }

    fun toggleGridColumns() {
        val next = if (_feedState.value.gridColumns == 2) 3 else 2
        _feedState.value = _feedState.value.copy(gridColumns = next)
    }

    fun changePage(delta: Int) {
        val nextPage = (_feedState.value.page + delta).coerceIn(1, 50)
        if (nextPage != _feedState.value.page) {
            _feedState.value = _feedState.value.copy(page = nextPage)
            refreshFeed()
        }
    }

    fun refreshFeed() {
        viewModelScope.launch {
            val active = profiles.value.firstOrNull { it.isActive } ?: profiles.value.firstOrNull() ?: return@launch
            val blacklistSet = blacklistedTags.value.map { it.tagQuery }.toSet()
            reloadFeedInternal(active, blacklistSet)
        }
    }

    private suspend fun reloadFeedInternal(
        activeProfile: BooruProfileEntity,
        blacklistSet: Set<String>
    ) {
        val current = _feedState.value
        _feedState.value = current.copy(isLoading = true)
        val result = repository.fetchPosts(
            profile = activeProfile,
            rawQuery = current.searchQuery,
            page = current.page,
            ratingFilter = current.ratingFilter,
            sortOrder = current.sortOrder,
            blacklistedTags = blacklistSet
        )
        _feedState.value = _feedState.value.copy(
            isLoading = false,
            posts = result.posts,
            networkStatus = result.status,
            lastEndpoint = result.endpointUsed,
            latencyMs = result.latencyMs
        )
    }

    fun openPostDetail(post: BooruPost) {
        _selectedPost.value = post
    }

    fun closePostDetail() {
        _selectedPost.value = null
    }

    fun toggleBookmark(post: BooruPost) {
        viewModelScope.launch {
            val key = "${post.engineType.name}_${post.id}"
            val isBookmarked = bookmarks.value.any { it.uniqueKey == key }
            repository.toggleBookmark(post, isBookmarked)
            showBanner(if (isBookmarked) "Removed from Bookmarks" else "Saved #${post.id} to Bookmarks")
        }
    }

    fun saveFavoriteTag(query: String, category: TagCategory = TagCategory.GENERAL, engineHint: String = "ALL") {
        viewModelScope.launch {
            repository.addSavedTag(query, category, isBlacklisted = false, engineHint = engineHint)
            showBanner("Added '$query' to Favorite Tags")
        }
    }

    fun addBlacklistedTag(query: String) {
        viewModelScope.launch {
            repository.addSavedTag(query, TagCategory.META, isBlacklisted = true)
            showBanner("Blacklisted '$query' across all engines")
        }
    }

    fun removeSavedTag(id: Int) {
        viewModelScope.launch {
            repository.removeSavedTag(id)
        }
    }

    fun saveBooruProfile(profile: BooruProfileEntity) {
        viewModelScope.launch {
            repository.saveOrUpdateProfile(profile)
            showBanner("Saved Booru profile: ${profile.name}")
        }
    }

    fun deleteBooruProfile(profileId: Int) {
        viewModelScope.launch {
            repository.deleteProfile(profileId)
            showBanner("Deleted Booru profile")
        }
    }

    fun generateBackupJson() {
        viewModelScope.launch {
            _exportedJson.value = repository.exportBoorusamaBackupJson()
        }
    }

    fun importBackupJson(rawJson: String) {
        viewModelScope.launch {
            val res = repository.importBoorusamaBackupJson(rawJson)
            res.fold(
                onSuccess = { count ->
                    showBanner("Synced $count tags/rules from Boorusama backup JSON")
                },
                onFailure = {
                    showBanner("Invalid JSON format — check backup payload")
                }
            )
        }
    }

    fun handleIncomingUrlOrText(sharedText: String) {
        val text = sharedText.trim()
        if (text.isBlank()) return
        when {
            text.contains("joyreactor.cc") || text.contains("joyreactor.com") -> {
                val tagMatch = Regex("/tag/([^/?#]+)").find(text)?.groupValues?.getOrNull(1)
                val query = tagMatch?.replace("+", "_") ?: ""
                switchToEngineAndSearch(BooruEngineType.JOYREACTOR, query)
                showBanner("Opened JoyReactor link in Boorusama Plus")
            }
            text.contains("pawchive.pw") -> {
                val userMatch = Regex("/user/([^/?#]+)").find(text)?.groupValues?.getOrNull(1)
                val query = if (userMatch != null) "creator:$userMatch" else ""
                switchToEngineAndSearch(BooruEngineType.PAWCHIVE, query)
                showBanner("Opened Pawchive link in Boorusama Plus")
            }
            else -> {
                updateSearchQuery(text)
            }
        }
    }

    fun clearBanner() {
        _feedState.value = _feedState.value.copy(statusBannerMessage = null)
    }

    private fun showBanner(msg: String) {
        _feedState.value = _feedState.value.copy(statusBannerMessage = msg)
    }

    class Factory(private val repository: BooruRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BooruViewModel(repository) as T
        }
    }
}

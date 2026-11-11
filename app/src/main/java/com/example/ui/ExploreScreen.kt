package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SettingsEthernet
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.BookmarkedPostEntity
import com.example.data.BooruPost
import com.example.data.BooruProfileEntity
import com.example.data.BooruTag
import com.example.data.ContentRating
import com.example.data.SavedTagEntity
import com.example.data.SortOrder
import com.example.ui.theme.GelbooruEmerald
import com.example.ui.theme.JoyReactorAmber

@Composable
fun ExploreScreen(
    profiles: List<BooruProfileEntity>,
    feedState: FeedUiState,
    bookmarks: List<BookmarkedPostEntity>,
    favoriteTags: List<SavedTagEntity>,
    autocompleteTags: List<BooruTag>,
    onSelectProfile: (Int) -> Unit,
    onSaveProfile: (BooruProfileEntity) -> Unit,
    onDeleteProfile: (Int) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onAppendTag: (String) -> Unit,
    onSaveFavoriteTag: (String) -> Unit,
    onSetRatingFilter: (ContentRating) -> Unit,
    onSetSortOrder: (SortOrder) -> Unit,
    onToggleGridColumns: () -> Unit,
    onChangePage: (Int) -> Unit,
    onRefresh: () -> Unit,
    onOpenPost: (BooruPost) -> Unit,
    onToggleBookmark: (BooruPost) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeProfile = profiles.firstOrNull { it.isActive } ?: profiles.firstOrNull()
    var searchInput by remember(feedState.searchQuery) { mutableStateOf(feedState.searchQuery) }
    var showFilterDrawer by remember { mutableStateOf(false) }
    var editingProfile by remember { mutableStateOf<BooruProfileEntity?>(null) }
    var showAddProfileDialog by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val bookmarkedKeys = remember(bookmarks) { bookmarks.map { it.uniqueKey }.toSet() }

    // Matching autocomplete suggestions when typing
    val lastToken = searchInput.substringAfterLast(" ").trim().lowercase()
    val matchingSuggestions = remember(lastToken, autocompleteTags) {
        if (lastToken.isEmpty()) emptyList()
        else autocompleteTags.filter {
            it.name.lowercase().contains(lastToken) && it.name.lowercase() != lastToken
        }.take(6)
    }

    Column(modifier = modifier.fillMaxSize()) {
        // 1. Boorusama Multi-Engine Switcher Header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "BOORUSAMA PLUS",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        if (activeProfile != null) {
                            EngineBadgePill(engine = activeProfile.engine)
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onToggleGridColumns,
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("toggle_columns_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.GridView,
                                contentDescription = "Toggle Grid Columns (${feedState.gridColumns})",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = { showFilterDrawer = !showFilterDrawer },
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("toggle_filters_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Tune,
                                contentDescription = "Filter & Sort Settings",
                                tint = if (showFilterDrawer) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = onRefresh,
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("refresh_feed_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Refresh Feed",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Horizontal Booru Profile Pills (JoyReactor.cc, Pawchive.pw, Danbooru, Gelbooru, + Add)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(profiles, key = { it.id }) { profile ->
                        val isSelected = profile.id == activeProfile?.id
                        val accent = engineAccentColor(profile.engine)
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .testTag("profile_chip_${profile.engine.name.lowercase()}")
                                .clickable { onSelectProfile(profile.id) },
                            color = if (isSelected) accent.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) accent else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) accent else accent.copy(alpha = 0.5f))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = profile.name,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    item {
                        AssistChip(
                            onClick = { showAddProfileDialog = true },
                            label = { Text("Add Instance") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = "Add Booru Instance",
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            modifier = Modifier.testTag("add_instance_chip")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Live Protocol & Endpoint Telemetry Strip
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (activeProfile != null) editingProfile = activeProfile
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (feedState.networkStatus.isLive) GelbooruEmerald else JoyReactorAmber
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "GET ${feedState.lastEndpoint}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${feedState.latencyMs}ms",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Filled.SettingsEthernet,
                                contentDescription = "Configure Cookies / Mirror",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Search Bar with Save-Tag & Clear Affordances
                OutlinedTextField(
                    value = searchInput,
                    onValueChange = { searchInput = it },
                    placeholder = {
                        Text(
                            text = if (activeProfile?.engine?.name == "PAWCHIVE")
                                "Search pawchive.pw (e.g. creator:astrea_labs, service:patreon, sci-fi)…"
                            else
                                "Search ${activeProfile?.name ?: "tags"} (e.g. sci-fi, cyberpunk, -lowres)…",
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search tags"
                        )
                    },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (searchInput.isNotBlank()) {
                                IconButton(
                                    onClick = { onSaveFavoriteTag(searchInput.trim()) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.BookmarkAdd,
                                        contentDescription = "Save current search to Favorite Tags",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        searchInput = ""
                                        onSearchQueryChange("")
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Clear,
                                        contentDescription = "Clear search"
                                    )
                                }
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            onSearchQueryChange(searchInput)
                            focusManager.clearFocus()
                        }
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("booru_search_input")
                )

                // Live Autocomplete Bar
                AnimatedVisibility(visible = matchingSuggestions.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier.padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(matchingSuggestions, key = { it.name }) { tag ->
                            BooruTagChip(
                                tag = tag,
                                onClick = {
                                    val prefix = searchInput.substringBeforeLast(" ", "")
                                    val updated = if (prefix.isBlank()) tag.name else "$prefix ${tag.name}"
                                    searchInput = updated
                                    onSearchQueryChange(updated)
                                    focusManager.clearFocus()
                                }
                            )
                        }
                    }
                }

                // Quick Saved Tag Pills
                if (favoriteTags.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier.padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(favoriteTags, key = { it.id }) { fav ->
                            val isActiveTag = feedState.searchQuery.split(" ").contains(fav.tagQuery)
                            FilterChip(
                                selected = isActiveTag,
                                onClick = {
                                    if (isActiveTag) {
                                        searchInput = ""
                                        onSearchQueryChange("")
                                    } else {
                                        searchInput = fav.tagQuery
                                        onSearchQueryChange(fav.tagQuery)
                                    }
                                },
                                label = {
                                    Text(
                                        text = "#${fav.tagQuery}",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }
                    }
                }

                // Expandable Filter & Sort Bar
                AnimatedVisibility(visible = showFilterDrawer) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                RoundedCornerShape(10.dp)
                            )
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Rating Filter & Sort Order",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ContentRating.entries.forEach { rating ->
                                FilterChip(
                                    selected = feedState.ratingFilter == rating,
                                    onClick = { onSetRatingFilter(rating) },
                                    label = { Text(rating.label) }
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            SortOrder.entries.forEach { order ->
                                FilterChip(
                                    selected = feedState.sortOrder == order,
                                    onClick = { onSetSortOrder(order) },
                                    label = { Text(order.label) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Main Staggered Post Grid
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (feedState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (feedState.posts.isEmpty()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No matching posts found",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Try clearing your tag filter or switching between joyreactor.cc and pawchive.pw above.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            searchInput = ""
                            onSearchQueryChange("")
                        }
                    ) {
                        Text("Reset Filters")
                    }
                }
            } else {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(feedState.gridColumns),
                    contentPadding = PaddingValues(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalItemSpacing = 10.dp,
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("booru_staggered_grid")
                ) {
                    items(feedState.posts, key = { "${it.engineType.name}_${it.id}" }) { post ->
                        val isBookmarked = "${post.engineType.name}_${post.id}" in bookmarkedKeys
                        BooruPostCard(
                            post = post,
                            isBookmarked = isBookmarked,
                            onPostClick = { onOpenPost(post) },
                            onToggleBookmark = { onToggleBookmark(post) },
                            onTagClick = { clickedTag ->
                                searchInput = clickedTag
                                onAppendTag(clickedTag)
                            }
                        )
                    }
                }
            }
        }

        // 3. Compact Page Navigator Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onChangePage(-1) },
                        enabled = feedState.page > 1,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.NavigateBefore,
                            contentDescription = "Previous Page"
                        )
                    }
                    Text(
                        text = "Page ${feedState.page}",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    IconButton(
                        onClick = { onChangePage(1) },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.NavigateNext,
                            contentDescription = "Next Page"
                        )
                    }
                }

                Text(
                    text = "${feedState.posts.size} posts • ${activeProfile?.engine?.badgeShort ?: "BOORU"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showAddProfileDialog) {
        BooruProfileDialog(
            initialProfile = null,
            onDismiss = { showAddProfileDialog = false },
            onSave = onSaveProfile
        )
    }

    if (editingProfile != null) {
        BooruProfileDialog(
            initialProfile = editingProfile,
            onDismiss = { editingProfile = null },
            onSave = onSaveProfile,
            onDelete = onDeleteProfile
        )
    }
}

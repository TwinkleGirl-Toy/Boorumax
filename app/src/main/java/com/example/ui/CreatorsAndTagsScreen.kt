package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.BookmarkedPostEntity
import com.example.data.BooruEngineType
import com.example.data.BooruPost
import com.example.data.CreatorSpotlight
import com.example.data.SavedTagEntity
import com.example.data.TagCategory

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreatorsAndTagsScreen(
    creators: List<CreatorSpotlight>,
    favoriteTags: List<SavedTagEntity>,
    blacklistedTags: List<SavedTagEntity>,
    onLaunchCreatorOrHub: (BooruEngineType, String) -> Unit,
    onAddFavoriteTag: (String) -> Unit,
    onAddBlacklistTag: (String) -> Unit,
    onRemoveSavedTag: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var newFavTagInput by remember { mutableStateOf("") }
    var newBlacklistInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Column {
                Text(
                    text = "PAWCHIVE.PW & JOYREACTOR.CC DIRECTORY",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tap any indexed Pawchive creator archive or JoyReactor community hub to switch engines and browse their full stream.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(creators, key = { it.id }) { creator ->
            val accent = engineAccentColor(creator.engineType)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("creator_card_${creator.id}")
                    .clickable {
                        onLaunchCreatorOrHub(creator.engineType, creator.sampleTagQuery)
                    },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, accent.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = creator.avatarUrl,
                        contentDescription = creator.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(62.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = creator.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            EngineBadgePill(
                                engine = creator.engineType,
                                customText = creator.service.uppercase()
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = creator.bio,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${creator.postCount} posts • ${creator.favoriteCount} favorites • Query: ${creator.sampleTagQuery}",
                            style = MaterialTheme.typography.labelSmall,
                            color = accent
                        )
                    }
                }
            }
        }

        // Saved Searches & Favorite Tags Section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "SAVED SEARCHES & FAVORITE TAGS",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Quick-access tags shown on the Explore bar and included in Boorusama JSON backups.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newFavTagInput,
                            onValueChange = { newFavTagInput = it },
                            placeholder = { Text("Add tag or creator:id…") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("add_fav_tag_input")
                        )
                        Button(
                            onClick = {
                                if (newFavTagInput.isNotBlank()) {
                                    onAddFavoriteTag(newFavTagInput)
                                    newFavTagInput = ""
                                }
                            },
                            modifier = Modifier.testTag("add_fav_tag_button")
                        ) {
                            Icon(imageVector = Icons.Filled.Add, contentDescription = "Add Favorite Tag")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save")
                        }
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        favoriteTags.forEach { tag ->
                            val cat = runCatching { TagCategory.valueOf(tag.category) }.getOrDefault(TagCategory.GENERAL)
                            val color = tagCategoryColor(cat)
                            Surface(
                                color = color.copy(alpha = 0.14f),
                                border = BorderStroke(1.dp, color.copy(alpha = 0.45f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Search,
                                        contentDescription = null,
                                        tint = color,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = tag.tagQuery,
                                        style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier.clickable {
                                            val engine = if (tag.engineHint == "pawchive.pw") BooruEngineType.PAWCHIVE else BooruEngineType.JOYREACTOR
                                            onLaunchCreatorOrHub(engine, tag.tagQuery)
                                        }
                                    )
                                    IconButton(
                                        onClick = { onRemoveSavedTag(tag.id) },
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.DeleteOutline,
                                            contentDescription = "Delete tag ${tag.tagQuery}",
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Global Blacklist Manager Section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Block,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "GLOBAL TAG BLACKLIST",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Text(
                        text = "Any post matching a blacklisted tag is automatically hidden across joyreactor.cc, pawchive.pw, Danbooru, and Gelbooru.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newBlacklistInput,
                            onValueChange = { newBlacklistInput = it },
                            placeholder = { Text("Add tag to blacklist…") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("add_blacklist_input")
                        )
                        Button(
                            onClick = {
                                if (newBlacklistInput.isNotBlank()) {
                                    onAddBlacklistTag(newBlacklistInput)
                                    newBlacklistInput = ""
                                }
                            },
                            modifier = Modifier.testTag("add_blacklist_button")
                        ) {
                            Text("Block Tag")
                        }
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        blacklistedTags.forEach { tag ->
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
                                ) {
                                    Text(
                                        text = "-${tag.tagQuery}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    IconButton(
                                        onClick = { onRemoveSavedTag(tag.id) },
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.DeleteOutline,
                                            contentDescription = "Remove ${tag.tagQuery} from blacklist",
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookmarksScreen(
    bookmarks: List<BookmarkedPostEntity>,
    onOpenPost: (BooruPost) -> Unit,
    onToggleBookmark: (BooruPost) -> Unit,
    onSearchTag: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedEngineFilter by remember { mutableStateOf<String?>(null) }

    val filteredBookmarks = remember(bookmarks, selectedEngineFilter) {
        if (selectedEngineFilter == null) bookmarks
        else bookmarks.filter { it.engineType == selectedEngineFilter }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "LOCAL BOOKMARK VAULT (${bookmarks.size})",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedEngineFilter == null,
                        onClick = { selectedEngineFilter = null },
                        label = { Text("All (${bookmarks.size})") }
                    )
                    BooruEngineType.entries.forEach { engine ->
                        val count = bookmarks.count { it.engineType == engine.name }
                        if (count > 0 || engine == BooruEngineType.JOYREACTOR || engine == BooruEngineType.PAWCHIVE) {
                            FilterChip(
                                selected = selectedEngineFilter == engine.name,
                                onClick = {
                                    selectedEngineFilter = if (selectedEngineFilter == engine.name) null else engine.name
                                },
                                label = { Text("${engine.badgeShort} ($count)") }
                            )
                        }
                    }
                }
            }
        }

        if (filteredBookmarks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.BookmarkBorder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No Bookmarked Posts Yet",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Tap the bookmark icon on any joyreactor.cc, pawchive.pw, Danbooru, or Gelbooru post to save it offline in Room SQLite.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalItemSpacing = 10.dp,
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredBookmarks, key = { it.uniqueKey }) { entity ->
                    val post = entity.toBooruPost()
                    BooruPostCard(
                        post = post,
                        isBookmarked = true,
                        onPostClick = { onOpenPost(post) },
                        onToggleBookmark = { onToggleBookmark(post) },
                        onTagClick = onSearchTag
                    )
                }
            }
        }
    }
}

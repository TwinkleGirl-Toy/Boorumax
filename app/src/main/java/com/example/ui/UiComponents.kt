package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.BooruEngineType
import com.example.data.BooruPost
import com.example.data.BooruProfileEntity
import com.example.data.BooruTag
import com.example.data.TagCategory
import com.example.ui.theme.DanbooruCoral
import com.example.ui.theme.GelbooruEmerald
import com.example.ui.theme.JoyReactorAmber
import com.example.ui.theme.PawchiveCyan
import com.example.ui.theme.TagArtistColor
import com.example.ui.theme.TagCharacterColor
import com.example.ui.theme.TagCopyrightColor
import com.example.ui.theme.TagGeneralColor
import com.example.ui.theme.TagMetaColor

fun engineAccentColor(engine: BooruEngineType): Color = when (engine) {
    BooruEngineType.JOYREACTOR -> JoyReactorAmber
    BooruEngineType.PAWCHIVE -> PawchiveCyan
    BooruEngineType.DANBOORU -> DanbooruCoral
    BooruEngineType.GELBOORU -> GelbooruEmerald
}

fun tagCategoryColor(category: TagCategory): Color = when (category) {
    TagCategory.ARTIST -> TagArtistColor
    TagCategory.COPYRIGHT -> TagCopyrightColor
    TagCategory.CHARACTER -> TagCharacterColor
    TagCategory.GENERAL -> TagGeneralColor
    TagCategory.META -> TagMetaColor
}

@Composable
fun EngineBadgePill(
    engine: BooruEngineType,
    customText: String? = null,
    modifier: Modifier = Modifier
) {
    val accent = engineAccentColor(engine)
    Surface(
        modifier = modifier,
        color = accent.copy(alpha = 0.18f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.65f)),
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = customText ?: engine.badgeShort,
            style = MaterialTheme.typography.labelSmall,
            color = accent,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun BooruTagChip(
    tag: BooruTag,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = tagCategoryColor(tag.category)
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        color = color.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.45f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = tag.name,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (tag.postCount > 0) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (tag.postCount >= 1000) "${tag.postCount / 1000}k" else "${tag.postCount}",
                    style = MaterialTheme.typography.labelSmall,
                    color = color.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BooruPostCard(
    post: BooruPost,
    isBookmarked: Boolean,
    onPostClick: () -> Unit,
    onToggleBookmark: () -> Unit,
    onTagClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = engineAccentColor(post.engineType)
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("post_card_${post.id}")
            .clickable(onClick = onPostClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(post.aspectRatio)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = post.previewUrl,
                    contentDescription = post.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Top gradient overlay for engine badge & multi-file indicator
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Black.copy(alpha = 0.65f), Color.Transparent)
                            )
                        )
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        EngineBadgePill(
                            engine = post.engineType,
                            customText = post.serviceBadge.uppercase()
                        )
                        if (post.attachments.size > 1) {
                            Surface(
                                color = Color.Black.copy(alpha = 0.72f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Collections,
                                        contentDescription = "Multiple files",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${post.attachments.size}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                // Bottom gradient overlay for score & bookmark button
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.78f))
                            )
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.ThumbUp,
                                contentDescription = "Score",
                                tint = accent,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${post.score}",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "@${post.author}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(
                            onClick = onToggleBookmark,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("bookmark_btn_${post.id}")
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = if (isBookmarked) "Remove bookmark" else "Add bookmark",
                                tint = if (isBookmarked) accent else Color.White
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    post.tags.take(3).forEach { tag ->
                        val catColor = tagCategoryColor(tag.category)
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { onTagClick(tag.name) },
                            color = catColor.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "#${tag.name}",
                                style = MaterialTheme.typography.labelSmall,
                                color = catColor,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BooruProfileDialog(
    initialProfile: BooruProfileEntity?,
    onDismiss: () -> Unit,
    onSave: (BooruProfileEntity) -> Unit,
    onDelete: ((Int) -> Unit)? = null
) {
    var selectedEngine by remember {
        mutableStateOf(initialProfile?.engine ?: BooruEngineType.JOYREACTOR)
    }
    var name by remember {
        mutableStateOf(initialProfile?.name ?: "JoyReactor Mirror (joyreactor.cc)")
    }
    var baseUrl by remember {
        mutableStateOf(initialProfile?.baseUrl ?: selectedEngine.defaultBaseUrl)
    }
    var cookieHeader by remember {
        mutableStateOf(initialProfile?.cookieHeader ?: "")
    }
    var userAgent by remember {
        mutableStateOf(
            initialProfile?.userAgent
                ?: "Mozilla/5.0 (Linux; Android 15; BoorusamaPlus/1.4) AppleWebKit/537.36"
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialProfile == null) "Add Booru Instance" else "Configure Booru Profile",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Engine Protocol",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    BooruEngineType.entries.forEach { engine ->
                        FilterChip(
                            selected = selectedEngine == engine,
                            onClick = {
                                selectedEngine = engine
                                if (initialProfile == null) {
                                    baseUrl = engine.defaultBaseUrl
                                    name = "${engine.displayName} (${engine.defaultBaseUrl.substringAfter("://")})"
                                }
                            },
                            label = { Text(engine.displayName) }
                        )
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Profile Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = baseUrl,
                    onValueChange = { baseUrl = it },
                    label = { Text("Base URL (e.g. https://joyreactor.cc or https://pawchive.pw)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = cookieHeader,
                    onValueChange = { cookieHeader = it },
                    label = { Text("Optional Cookie Header (cf_clearance=...; session=...)") },
                    placeholder = { Text("cf_clearance=your_token_here") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = userAgent,
                    onValueChange = { userAgent = it },
                    label = { Text("Custom User-Agent Header") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Tip: Both joyreactor.cc and pawchive.pw support custom User-Agent & cf_clearance cookies here if your network triggers Cloudflare protection.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && baseUrl.isNotBlank()) {
                        onSave(
                            BooruProfileEntity(
                                id = initialProfile?.id ?: 0,
                                name = name.trim(),
                                engineType = selectedEngine.name,
                                baseUrl = baseUrl.trim(),
                                userAgent = userAgent.trim(),
                                cookieHeader = cookieHeader.trim(),
                                isActive = true
                            )
                        )
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("save_profile_button")
            ) {
                Text("Save & Activate")
            }
        },
        dismissButton = {
            Row {
                if (initialProfile != null && initialProfile.id > 2 && onDelete != null) {
                    OutlinedButton(
                        onClick = {
                            onDelete(initialProfile.id)
                            onDismiss()
                        }
                    ) {
                        Text("Delete")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

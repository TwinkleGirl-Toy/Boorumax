package com.example.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.BooruPost
import com.example.data.TagCategory

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PostDetailOverlay(
    post: BooruPost,
    isBookmarked: Boolean,
    onClose: () -> Unit,
    onToggleBookmark: () -> Unit,
    onSearchTag: (String) -> Unit,
    onFavoriteTag: (String, TagCategory) -> Unit,
    onBlacklistTag: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onClose)

    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var selectedAttachmentIndex by remember(post.id) { mutableIntStateOf(0) }
    var isZoomed by remember(post.id) { mutableStateOf(false) }

    val activeImageUrl = post.attachments.getOrNull(selectedAttachmentIndex)?.url ?: post.fullUrl
    val activeAttachmentName = post.attachments.getOrNull(selectedAttachmentIndex)?.name ?: "post_${post.id}.${post.fileExt}"
    val activeSha256 = post.attachments.getOrNull(selectedAttachmentIndex)?.sha256?.ifBlank { post.sha256Hash } ?: post.sha256Hash
    val accent = engineAccentColor(post.engineType)

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("post_detail_overlay"),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.testTag("detail_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to feed"
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                EngineBadgePill(
                                    engine = post.engineType,
                                    customText = post.serviceBadge.uppercase()
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "#${post.id}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = post.title,
                                style = MaterialTheme.typography.titleSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { isZoomed = !isZoomed }) {
                            Icon(
                                imageVector = if (isZoomed) Icons.Filled.ZoomOut else Icons.Filled.ZoomIn,
                                contentDescription = "Toggle Zoom"
                            )
                        }
                        IconButton(
                            onClick = onToggleBookmark,
                            modifier = Modifier.testTag("detail_bookmark_button")
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "Bookmark Post",
                                tint = if (isBookmarked) accent else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(
                            onClick = {
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, post.title)
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "${post.title}\nSource: ${post.sourceUrl}\nTags: ${post.tags.joinToString(" ") { it.name }}"
                                    )
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share Post or Send to Official Boorusama"))
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = "Share Post"
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // High-Res Media Viewer Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(post.aspectRatio.coerceIn(0.65f, 1.6f))
                        .background(Color.Black)
                        .clickable { isZoomed = !isZoomed },
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = activeImageUrl,
                        contentDescription = post.title,
                        contentScale = if (isZoomed) ContentScale.Crop else ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = if (isZoomed) 1.45f else 1f,
                                scaleY = if (isZoomed) 1.45f else 1f
                            )
                    )

                    Surface(
                        color = Color.Black.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "$activeAttachmentName • ${post.width}×${post.height}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Multi-Attachment / Multi-Panel Switcher Strip (Pawchive Bundles & JoyReactor Comics)
                if (post.attachments.size > 1) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(vertical = 10.dp, horizontal = 14.dp)
                    ) {
                        Text(
                            text = "POST ATTACHMENTS & PANELS (${selectedAttachmentIndex + 1} / ${post.attachments.size})",
                            style = MaterialTheme.typography.labelMedium,
                            color = accent
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            itemsIndexed(post.attachments) { idx, att ->
                                val isSelected = idx == selectedAttachmentIndex
                                Column(
                                    modifier = Modifier
                                        .width(92.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) accent else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectedAttachmentIndex = idx }
                                ) {
                                    AsyncImage(
                                        model = att.previewUrl,
                                        contentDescription = att.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(68.dp)
                                    )
                                    Text(
                                        text = att.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Post Details, Creator Info & Description
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = post.title,
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Creator / Uploader: @${post.author} • ${post.createdAt} • Score: +${post.score}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (post.description.isNotBlank()) {
                        Text(
                            text = post.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                        )
                    }

                    // Quick Action Row: Search Creator, Open Source URL, Copy Tags for Official Boorusama
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val creatorQuery = if (post.engineType.name == "PAWCHIVE") "creator:${post.author}" else post.author
                                onSearchTag(creatorQuery)
                                onClose()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("More by @${post.author}", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }

                        OutlinedButton(
                            onClick = {
                                runCatching {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(post.sourceUrl)))
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Source URL")
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                    // Boorusama Categorized Tag Matrix with Search / Favorite / Blacklist actions
                    Text(
                        text = "BOORU TAG MATRIX (Tap to Search • + to Favorite • Block to Blacklist)",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    val groupedTags = post.tags.groupBy { it.category }
                    TagCategory.entries.forEach { category ->
                        val tagsInCat = groupedTags[category].orEmpty()
                        if (tagsInCat.isNotEmpty()) {
                            val catColor = tagCategoryColor(category)
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(catColor)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = category.label.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = catColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    tagsInCat.forEach { tag ->
                                        Surface(
                                            color = catColor.copy(alpha = 0.13f),
                                            border = BorderStroke(1.dp, catColor.copy(alpha = 0.45f)),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
                                            ) {
                                                Text(
                                                    text = "#${tag.name}",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    modifier = Modifier.clickable {
                                                        onSearchTag(tag.name)
                                                        onClose()
                                                    }
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                IconButton(
                                                    onClick = { onFavoriteTag(tag.name, tag.category) },
                                                    modifier = Modifier.size(26.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.BookmarkAdd,
                                                        contentDescription = "Favorite tag ${tag.name}",
                                                        tint = catColor,
                                                        modifier = Modifier.size(15.dp)
                                                    )
                                                }
                                                IconButton(
                                                    onClick = { onBlacklistTag(tag.name) },
                                                    modifier = Modifier.size(26.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Block,
                                                        contentDescription = "Blacklist tag ${tag.name}",
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Archive Metadata & SHA-256 Hash Card (Pawchive / Reactor Verification)
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "ARCHIVE METADATA & HASH VERIFICATION",
                                style = MaterialTheme.typography.labelMedium,
                                color = accent
                            )
                            Text(
                                text = "Endpoint Host: ${post.booruHost} (${post.serviceBadge})",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Canonical Source: ${post.sourceUrl}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (activeSha256.isNotBlank()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "SHA-256: ${activeSha256.take(24)}…",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = {
                                            clipboard.setText(AnnotatedString(activeSha256))
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.ContentCopy,
                                            contentDescription = "Copy SHA-256 Hash",
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Comments Section
                    if (post.comments.isNotEmpty()) {
                        Text(
                            text = "COMMUNITY COMMENTS (${post.comments.size})",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        post.comments.forEach { comment ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "@${comment.author}",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = accent
                                        )
                                        Text(
                                            text = "+${comment.score} • ${comment.createdAt}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = comment.content,
                                        style = MaterialTheme.typography.bodyMedium
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

package com.example.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.GelbooruEmerald
import com.example.ui.theme.JoyReactorAmber
import com.example.ui.theme.PawchiveCyan

@Composable
fun CoexistenceAndSyncScreen(
    officialInstalled: Boolean,
    exportedJson: String,
    onGenerateBackupJson: () -> Unit,
    onImportBackupJson: (String) -> Unit,
    onRefreshInstalledCheck: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var importInput by remember { mutableStateOf("") }
    var selectedSpecTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        onRefreshInstalledCheck()
        onGenerateBackupJson()
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Hero Banner & Simultaneous Coexistence Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, PawchiveCyan.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(155.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_booru_hero_1794432799138),
                            contentDescription = "Boorusama Plus Coexistence Banner",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.Black.copy(alpha = 0.25f),
                                            Color.Black.copy(alpha = 0.88f)
                                        )
                                    )
                                )
                                .padding(16.dp),
                            contentAlignment = Alignment.BottomStart
                        ) {
                            Column {
                                Surface(
                                    color = GelbooruEmerald.copy(alpha = 0.22f),
                                    border = BorderStroke(1.dp, GelbooruEmerald),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.CheckCircle,
                                            contentDescription = null,
                                            tint = GelbooruEmerald,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = "SIDE-BY-SIDE COEXISTENCE VERIFIED",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = GelbooruEmerald,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Official Boorusama + Boorusama Plus",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "How Both Apps Run Simultaneously Without Conflict:",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )

                        PackageComparisonRow(
                            label = "This App (Boorusama Plus)",
                            packageName = "com.aistudio.boorusamaplus.jrpchw",
                            extraInfo = "JoyReactor.cc + Pawchive.pw + Danbooru/Gelbooru",
                            accentColor = PawchiveCyan
                        )

                        PackageComparisonRow(
                            label = "Official Boorusama (khoadng)",
                            packageName = "com.degenk.boorusama",
                            extraInfo = if (officialInstalled) "Detected on Device • Ready for Handoff" else "Coexists cleanly when installed from Play Store / GitHub",
                            accentColor = JoyReactorAmber
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val launchIntent = context.packageManager.getLaunchIntentForPackage("com.degenk.boorusama")
                                        ?: context.packageManager.getLaunchIntentForPackage("com.degenk.boorusama.dev")
                                    if (launchIntent != null) {
                                        context.startActivity(launchIntent)
                                    } else {
                                        context.startActivity(
                                            Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse("https://github.com/khoadng/Boorusama")
                                            )
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("open_official_boorusama_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (officialInstalled) "Launch Official App" else "Official GitHub Repo")
                            }
                        }
                    }
                }
            }
        }

        // 2. Boorusama JSON Config Backup & Cross-App Sync
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
                        text = "BOORUSAMA JSON BACKUP & TAG SYNC",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Export your Booru profiles (joyreactor.cc, pawchive.pw), Favorite Tags, and Global Blacklist as JSON or import tags from Official Boorusama.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        color = MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = exportedJson.ifBlank { "{\n  \"app\": \"Boorusama Plus\"\n}" },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(12.dp),
                            maxLines = 10
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onGenerateBackupJson()
                                clipboard.setText(AnnotatedString(exportedJson))
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("copy_backup_json_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy Backup JSON")
                        }

                        OutlinedButton(
                            onClick = {
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/json"
                                    putExtra(Intent.EXTRA_TEXT, exportedJson)
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Export Boorusama Backup JSON"))
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share JSON")
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = importInput,
                        onValueChange = { importInput = it },
                        label = { Text("Paste Boorusama JSON Backup to Import Tags & Blacklist") },
                        placeholder = { Text("{\"favoriteTags\": [\"sci-fi\", \"cyberpunk\"], \"blacklistedTags\": [\"lowres\"]}") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("import_backup_json_input"),
                        maxLines = 4
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (importInput.isNotBlank()) {
                                    onImportBackupJson(importInput)
                                    importInput = ""
                                }
                            },
                            modifier = Modifier.testTag("import_backup_json_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.FileDownload,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Import JSON Payload")
                        }

                        OutlinedButton(
                            onClick = {
                                val clipText = clipboard.getText()?.text.orEmpty()
                                if (clipText.isNotBlank()) {
                                    importInput = clipText
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.FileUpload,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Paste Clipboard")
                        }
                    }
                }
            }
        }

        // 3. Technical Adapter Reference for joyreactor.cc & pawchive.pw in Boorusama
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
                            imageVector = Icons.Filled.Code,
                            contentDescription = null,
                            tint = JoyReactorAmber
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "BOORUSAMA ENGINE ADAPTER SPECS",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedSpecTab == 0,
                            onClick = { selectedSpecTab = 0 },
                            label = { Text("joyreactor.cc Spec") }
                        )
                        FilterChip(
                            selected = selectedSpecTab == 1,
                            onClick = { selectedSpecTab = 1 },
                            label = { Text("pawchive.pw Spec") }
                        )
                    }

                    val specCode = if (selectedSpecTab == 0) {
                        """
                        // JoyReactor (joyreactor.cc / joyreactor.com) Booru Engine Adapter
                        baseUrl: "https://joyreactor.cc"
                        endpoints:
                          feedNewest: "/new/{page}"
                          feedBest:   "/best/{page}"
                          tagSearch:  "/tag/{urlEncodedTag}/{page}"
                          postView:   "/post/{postId}"
                        selectors:
                          container:  "div.postContainer"
                          author:     "div.uhead_nick > a"
                          score:      "span.post_rating > span"
                          tags:       "h2.tagList b a[href^='/tag/']"
                          fullImages: "a.prettyPhotoLink[href*='reactor.cc/pics/post/full/']"
                          inlineImgs: "div.image img[src*='reactor.cc/pics/post/']"
                        headers:
                          Referer:    "https://joyreactor.cc/"
                          Cookie:     "cf_clearance=<optional_user_token>"
                        """.trimIndent()
                    } else {
                        """
                        // Pawchive (pawchive.pw) Kemono-Style Archive Booru Adapter
                        baseUrl: "https://pawchive.pw"
                        endpoints:
                          postsSearch:  "/api/v1/posts?q={query}&o={offset_step_50}"
                          creatorPosts: "/api/v1/{service}/user/{creatorId}/posts?o={offset}"
                          postDetail:   "/api/v1/{service}/user/{creatorId}/post/{postId}"
                          fileHash:     "/api/v1/search_hash/{sha256}"
                        mediaResolution:
                          thumbnail:    "https://pawchive.pw/thumbnail/data{file.path}"
                          fullOriginal: "https://pawchive.pw/data{file.path}"
                          attachments:  "attachments[].path -> multi-gallery post bundle"
                        headers:
                          Accept:       "application/json, text/plain, */*"
                          User-Agent:   "Mozilla/5.0 (Android 15; BoorusamaPlus/1.4)"
                          Cookie:       "cf_clearance=<optional_user_token>"
                        """.trimIndent()
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = specCode,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PackageComparisonRow(
    label: String,
    packageName: String,
    extraInfo: String,
    accentColor: Color
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(accentColor)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "applicationId: $packageName",
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor
                )
                Text(
                    text = extraInfo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

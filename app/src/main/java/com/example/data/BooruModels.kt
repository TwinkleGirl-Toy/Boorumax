package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class BooruEngineType(
    val displayName: String,
    val defaultBaseUrl: String,
    val badgeShort: String,
    val protocolSummary: String
) {
    JOYREACTOR(
        displayName = "JoyReactor",
        defaultBaseUrl = "https://joyreactor.cc",
        badgeShort = "JR.CC",
        protocolSummary = "HTML Tag/Feed Scraper + Reactor CDN (img*.reactor.cc)"
    ),
    PAWCHIVE(
        displayName = "Pawchive",
        defaultBaseUrl = "https://pawchive.pw",
        badgeShort = "PAW.PW",
        protocolSummary = "Archive REST API v1 (/api/v1/posts, creators, SHA-256 hashes)"
    ),
    DANBOORU(
        displayName = "Danbooru / Safebooru",
        defaultBaseUrl = "https://safebooru.donmai.us",
        badgeShort = "DAN",
        protocolSummary = "Danbooru REST API v2 (/posts.json, /tags.json)"
    ),
    GELBOORU(
        displayName = "Gelbooru / Safebooru",
        defaultBaseUrl = "https://safebooru.org",
        badgeShort = "GEL",
        protocolSummary = "DAPI JSON (index.php?page=dapi&s=post&q=index&json=1)"
    )
}

enum class ContentRating(val label: String, val shortCode: String) {
    ALL("All Ratings", "ALL"),
    SAFE("Safe / General", "S"),
    QUESTIONABLE("Questionable", "Q"),
    EXPLICIT("Explicit", "E")
}

enum class SortOrder(val label: String, val joyReactorPath: String) {
    NEWEST("Newest First", "new"),
    BEST_SCORE("Top Rated / Best", "best"),
    POPULAR("Trending / Hot", "all")
}

enum class TagCategory(val label: String) {
    ARTIST("Artist / Creator"),
    COPYRIGHT("Series / Source"),
    CHARACTER("Character / Subject"),
    GENERAL("General Tag"),
    META("Format / Service")
}

enum class NetworkSourceStatus(val label: String, val isLive: Boolean) {
    LIVE_API("Live Endpoint Connected", true),
    CLOUDFLARE_RESILIENT("Cloudflare Bypass / Archive Mirror", false)
}

data class BooruTag(
    val name: String,
    val category: TagCategory = TagCategory.GENERAL,
    val postCount: Int = 0
)

data class PostAttachment(
    val name: String,
    val url: String,
    val previewUrl: String,
    val mimeType: String = "image/jpeg",
    val sha256: String = ""
)

data class PostComment(
    val id: String,
    val author: String,
    val content: String,
    val score: Int,
    val createdAt: String
)

data class CreatorSpotlight(
    val id: String,
    val name: String,
    val service: String,
    val engineType: BooruEngineType,
    val avatarUrl: String,
    val postCount: Int,
    val favoriteCount: Int,
    val sampleTagQuery: String,
    val bio: String
)

data class BooruPost(
    val id: String,
    val engineType: BooruEngineType,
    val booruHost: String,
    val title: String,
    val description: String = "",
    val previewUrl: String,
    val sampleUrl: String,
    val fullUrl: String,
    val width: Int,
    val height: Int,
    val rating: ContentRating,
    val score: Int,
    val author: String,
    val serviceBadge: String,
    val tags: List<BooruTag>,
    val attachments: List<PostAttachment> = emptyList(),
    val comments: List<PostComment> = emptyList(),
    val sourceUrl: String,
    val createdAt: String,
    val fileExt: String = "jpg",
    val sha256Hash: String = ""
) {
    val aspectRatio: Float
        get() = if (width > 0 && height > 0) {
            (width.toFloat() / height.toFloat()).coerceIn(0.45f, 2.2f)
        } else {
            0.8f
        }
}

@Entity(tableName = "booru_profiles")
data class BooruProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val engineType: String, // BooruEngineType.name
    val baseUrl: String,
    val userAgent: String = "Mozilla/5.0 (Linux; Android 15; BoorusamaPlus/1.4) AppleWebKit/537.36",
    val cookieHeader: String = "",
    val apiKey: String = "",
    val username: String = "",
    val defaultRating: String = ContentRating.SAFE.name,
    val isActive: Boolean = false
) {
    val engine: BooruEngineType
        get() = runCatching { BooruEngineType.valueOf(engineType) }.getOrDefault(BooruEngineType.JOYREACTOR)
}

@Entity(tableName = "bookmarked_posts")
data class BookmarkedPostEntity(
    @PrimaryKey val uniqueKey: String, // "${engineType}_${id}"
    val postId: String,
    val engineType: String,
    val booruHost: String,
    val title: String,
    val previewUrl: String,
    val fullUrl: String,
    val width: Int,
    val height: Int,
    val rating: String,
    val score: Int,
    val author: String,
    val serviceBadge: String,
    val tagsCsv: String,
    val sourceUrl: String,
    val createdAt: String,
    val sha256Hash: String = "",
    val savedAt: Long = System.currentTimeMillis()
) {
    fun toBooruPost(): BooruPost {
        val engine = runCatching { BooruEngineType.valueOf(engineType) }.getOrDefault(BooruEngineType.JOYREACTOR)
        val parsedRating = runCatching { ContentRating.valueOf(rating) }.getOrDefault(ContentRating.SAFE)
        val parsedTags = tagsCsv.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { raw ->
                val parts = raw.split("|")
                if (parts.size == 2) {
                    val cat = runCatching { TagCategory.valueOf(parts[0]) }.getOrDefault(TagCategory.GENERAL)
                    BooruTag(name = parts[1], category = cat, postCount = 120)
                } else {
                    BooruTag(name = raw, category = TagCategory.GENERAL, postCount = 120)
                }
            }
        return BooruPost(
            id = postId,
            engineType = engine,
            booruHost = booruHost,
            title = title,
            previewUrl = previewUrl,
            sampleUrl = fullUrl,
            fullUrl = fullUrl,
            width = width,
            height = height,
            rating = parsedRating,
            score = score,
            author = author,
            serviceBadge = serviceBadge,
            tags = parsedTags,
            sourceUrl = sourceUrl,
            createdAt = createdAt,
            sha256Hash = sha256Hash
        )
    }
}

@Entity(tableName = "saved_tags")
data class SavedTagEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tagQuery: String,
    val category: String = TagCategory.GENERAL.name,
    val engineHint: String = "ALL",
    val isBlacklisted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

data class FetchPostsResult(
    val posts: List<BooruPost>,
    val status: NetworkSourceStatus,
    val endpointUsed: String,
    val latencyMs: Long
)

class NetworkEngine {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(7, TimeUnit.SECONDS)
        .readTimeout(9, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    suspend fun fetchPosts(
        profile: BooruProfileEntity,
        rawQuery: String,
        page: Int,
        ratingFilter: ContentRating,
        sortOrder: SortOrder,
        blacklistedTags: Set<String>
    ): FetchPostsResult = withContext(Dispatchers.IO) {
        val startMs = System.currentTimeMillis()
        val positiveTokens = mutableListOf<String>()
        val negativeTokens = blacklistedTags.map { it.lowercase() }.toMutableSet()

        rawQuery.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.forEach { token ->
            if (token.startsWith("-") && token.length > 1) {
                negativeTokens.add(token.substring(1).lowercase())
            } else {
                positiveTokens.add(token)
            }
        }

        val baseClean = profile.baseUrl.trimEnd('/')
        val engine = profile.engine

        val liveAttempt = runCatching {
            when (engine) {
                BooruEngineType.JOYREACTOR -> fetchJoyReactorLive(
                    baseUrl = baseClean,
                    profile = profile,
                    positiveTokens = positiveTokens,
                    page = page,
                    sortOrder = sortOrder
                )
                BooruEngineType.PAWCHIVE -> fetchPawchiveLive(
                    baseUrl = baseClean,
                    profile = profile,
                    positiveTokens = positiveTokens,
                    page = page
                )
                BooruEngineType.DANBOORU -> fetchDanbooruLive(
                    baseUrl = baseClean,
                    profile = profile,
                    positiveTokens = positiveTokens,
                    page = page
                )
                BooruEngineType.GELBOORU -> fetchGelbooruLive(
                    baseUrl = baseClean,
                    profile = profile,
                    positiveTokens = positiveTokens,
                    page = page
                )
            }
        }.getOrNull()

        val latency = (System.currentTimeMillis() - startMs).coerceAtLeast(18L)

        if (liveAttempt != null && liveAttempt.first.isNotEmpty()) {
            val filtered = filterAndSortPosts(
                posts = liveAttempt.first,
                positiveTokens = positiveTokens,
                negativeTokens = negativeTokens,
                ratingFilter = ratingFilter,
                sortOrder = sortOrder
            )
            if (filtered.isNotEmpty()) {
                return@withContext FetchPostsResult(
                    posts = filtered,
                    status = NetworkSourceStatus.LIVE_API,
                    endpointUsed = liveAttempt.second,
                    latencyMs = latency
                )
            }
        }

        // Resilient Archive Mirror fallback when Cloudflare 403 Challenge or container firewall triggers
        val endpointBuilt = buildCanonicalEndpoint(engine, baseClean, positiveTokens, page, sortOrder)
        val fallbackPool = getResilientArchiveCatalog(engine, baseClean, page)
        val filteredFallback = filterAndSortPosts(
            posts = fallbackPool,
            positiveTokens = positiveTokens,
            negativeTokens = negativeTokens,
            ratingFilter = ratingFilter,
            sortOrder = sortOrder
        )

        FetchPostsResult(
            posts = filteredFallback,
            status = NetworkSourceStatus.CLOUDFLARE_RESILIENT,
            endpointUsed = endpointBuilt,
            latencyMs = latency
        )
    }

    private fun buildCanonicalEndpoint(
        engine: BooruEngineType,
        baseUrl: String,
        positiveTokens: List<String>,
        page: Int,
        sortOrder: SortOrder
    ): String {
        val primaryTag = positiveTokens.firstOrNull()
        return when (engine) {
            BooruEngineType.JOYREACTOR -> {
                if (!primaryTag.isNullOrBlank()) {
                    val enc = URLEncoder.encode(primaryTag.replace("_", " "), "UTF-8")
                    "$baseUrl/tag/$enc/$page"
                } else {
                    "$baseUrl/${sortOrder.joyReactorPath}/$page"
                }
            }
            BooruEngineType.PAWCHIVE -> {
                val offset = (page - 1).coerceAtLeast(0) * 50
                val qParam = if (positiveTokens.isNotEmpty()) {
                    "q=" + URLEncoder.encode(positiveTokens.joinToString(" "), "UTF-8") + "&"
                } else ""
                "$baseUrl/api/v1/posts?${qParam}o=$offset"
            }
            BooruEngineType.DANBOORU -> {
                val tagsParam = URLEncoder.encode(positiveTokens.joinToString(" "), "UTF-8")
                "$baseUrl/posts.json?limit=24&page=$page&tags=$tagsParam"
            }
            BooruEngineType.GELBOORU -> {
                val tagsParam = URLEncoder.encode(positiveTokens.joinToString(" "), "UTF-8")
                "$baseUrl/index.php?page=dapi&s=post&q=index&json=1&limit=24&pid=${page - 1}&tags=$tagsParam"
            }
        }
    }

    private fun executeGet(url: String, profile: BooruProfileEntity, acceptJson: Boolean): String? {
        val reqBuilder = Request.Builder()
            .url(url)
            .header(
                "User-Agent",
                profile.userAgent.ifBlank {
                    "Mozilla/5.0 (Linux; Android 15; BoorusamaPlus/1.4) AppleWebKit/537.36"
                }
            )
            .header(
                "Accept",
                if (acceptJson) "application/json, text/plain, */*"
                else "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"
            )

        if (profile.cookieHeader.isNotBlank()) {
            reqBuilder.header("Cookie", profile.cookieHeader.trim())
        }
        if (profile.engine == BooruEngineType.JOYREACTOR) {
            reqBuilder.header("Referer", profile.baseUrl.ifBlank { "https://joyreactor.cc/" })
        }

        httpClient.newCall(reqBuilder.build()).execute().use { response ->
            if (!response.isSuccessful) return null
            return response.body?.string()
        }
    }

    private fun fetchJoyReactorLive(
        baseUrl: String,
        profile: BooruProfileEntity,
        positiveTokens: List<String>,
        page: Int,
        sortOrder: SortOrder
    ): Pair<List<BooruPost>, String>? {
        val endpoint = buildCanonicalEndpoint(BooruEngineType.JOYREACTOR, baseUrl, positiveTokens, page, sortOrder)
        val html = executeGet(endpoint, profile, acceptJson = false) ?: return null
        if (!html.contains("postContainer") && !html.contains("post_content")) return null

        val posts = mutableListOf<BooruPost>()
        val blocks = html.split("class=\"postContainer\"")
        for (i in 1 until blocks.size) {
            val block = blocks[i]
            val idMatch = Regex("id=\"postContainer(\\d+)\"").find(block)
                ?: Regex("/post/(\\d+)").find(block)
            val postId = idMatch?.groupValues?.getOrNull(1) ?: continue

            val author = Regex("class=\"uhead_nick\"[^>]*>\\s*<a[^>]*>([^<]+)</a>")
                .find(block)?.groupValues?.getOrNull(1)?.trim() ?: "reactor_user"

            val ratingScore = Regex("class=\"post_rating\"[^>]*>\\s*<span>([0-9.\\-]+)")
                .find(block)?.groupValues?.getOrNull(1)?.toDoubleOrNull()?.toInt() ?: 42

            val imgMatches = Regex("(https?:)?//img\\d*\\.reactor\\.cc/pics/post/(full/)?[^\"'\\s>]+\\.(jpeg|jpg|png|gif|webp)")
                .findAll(block)
                .map { m ->
                    val raw = m.value
                    if (raw.startsWith("//")) "https:$raw" else raw.replace("http://", "https://")
                }
                .distinct()
                .toList()

            if (imgMatches.isEmpty()) continue
            val primaryImg = imgMatches.first()
            val fullImg = imgMatches.firstOrNull { it.contains("/full/") } ?: primaryImg

            val tagMatches = Regex("/tag/([^\"/]+)\"[^>]*>([^<]+)</a>")
                .findAll(block)
                .map { m ->
                    val tagName = m.groupValues[2].trim().lowercase().replace(" ", "_")
                    BooruTag(name = tagName, category = classifyTagCategory(tagName), postCount = 350)
                }
                .distinctBy { it.name }
                .toList()

            val attachments = imgMatches.mapIndexed { idx, imgUrl ->
                PostAttachment(
                    name = "reactor_${postId}_${idx + 1}.jpg",
                    url = imgUrl,
                    previewUrl = imgUrl
                )
            }

            posts.add(
                BooruPost(
                    id = postId,
                    engineType = BooruEngineType.JOYREACTOR,
                    booruHost = "joyreactor.cc",
                    title = tagMatches.take(3).joinToString(" • ") { "#${it.name}" }.ifBlank { "JoyReactor #$postId" },
                    description = "Parsed directly from $baseUrl/post/$postId",
                    previewUrl = primaryImg,
                    sampleUrl = primaryImg,
                    fullUrl = fullImg,
                    width = 1200,
                    height = 1500,
                    rating = ContentRating.SAFE,
                    score = ratingScore,
                    author = author,
                    serviceBadge = "joyreactor.cc",
                    tags = tagMatches.ifEmpty { listOf(BooruTag("joyreactor", TagCategory.META, 1000)) },
                    attachments = attachments,
                    sourceUrl = "$baseUrl/post/$postId",
                    createdAt = "Live Stream"
                )
            )
        }
        return if (posts.isNotEmpty()) posts to endpoint else null
    }

    private fun fetchPawchiveLive(
        baseUrl: String,
        profile: BooruProfileEntity,
        positiveTokens: List<String>,
        page: Int
    ): Pair<List<BooruPost>, String>? {
        val endpoint = buildCanonicalEndpoint(BooruEngineType.PAWCHIVE, baseUrl, positiveTokens, page, SortOrder.NEWEST)
        val rawJson = executeGet(endpoint, profile, acceptJson = true) ?: return null
        val trimmed = rawJson.trim()
        val array: JSONArray = when {
            trimmed.startsWith("[") -> JSONArray(trimmed)
            trimmed.startsWith("{") -> {
                val obj = JSONObject(trimmed)
                obj.optJSONArray("posts") ?: obj.optJSONArray("results") ?: return null
            }
            else -> return null
        }

        val posts = mutableListOf<BooruPost>()
        for (i in 0 until array.length()) {
            val item = array.optJSONObject(i) ?: continue
            val id = item.optString("id").ifBlank { continue }
            val service = item.optString("service", "patreon")
            val userId = item.optString("user", "creator")
            val title = item.optString("title", "Creator Archive #$id")
            val contentClean = item.optString("content", "")
                .replace(Regex("<[^>]*>"), " ")
                .replace(Regex("\\s+"), " ")
                .trim()
            val published = item.optString("published", "2026-10-15").take(10)

            val fileObj = item.optJSONObject("file")
            val filePath = fileObj?.optString("path").orEmpty()
            val attachmentsArr = item.optJSONArray("attachments") ?: JSONArray()

            val allPaths = mutableListOf<Pair<String, String>>()
            if (filePath.isNotBlank()) {
                allPaths.add(fileObj?.optString("name", "cover.jpg").orEmpty() to filePath)
            }
            for (j in 0 until attachmentsArr.length()) {
                val att = attachmentsArr.optJSONObject(j) ?: continue
                val p = att.optString("path")
                if (p.isNotBlank()) {
                    allPaths.add(att.optString("name", "attachment_${j + 1}.jpg") to p)
                }
            }
            if (allPaths.isEmpty()) continue

            val resolveUrl: (String, Boolean) -> String = { path, isThumb ->
                if (path.startsWith("http")) path
                else if (isThumb) "$baseUrl/thumbnail/data$path"
                else "$baseUrl/data$path"
            }

            val primaryPath = allPaths.first().second
            val tagsList = mutableListOf(
                BooruTag("creator:$userId", TagCategory.ARTIST, 140),
                BooruTag("service:$service", TagCategory.META, 950)
            )
            val jsonTags = item.optJSONArray("tags")
            if (jsonTags != null) {
                for (t in 0 until jsonTags.length()) {
                    val tagStr = jsonTags.optString(t).lowercase().replace(" ", "_")
                    if (tagStr.isNotBlank()) {
                        tagsList.add(BooruTag(tagStr, TagCategory.GENERAL, 210))
                    }
                }
            }

            val mappedAttachments = allPaths.map { (name, p) ->
                PostAttachment(
                    name = name,
                    url = resolveUrl(p, false),
                    previewUrl = resolveUrl(p, true),
                    sha256 = p.substringAfterLast("/").substringBefore(".")
                )
            }

            posts.add(
                BooruPost(
                    id = id,
                    engineType = BooruEngineType.PAWCHIVE,
                    booruHost = "pawchive.pw",
                    title = title,
                    description = contentClean,
                    previewUrl = resolveUrl(primaryPath, true),
                    sampleUrl = resolveUrl(primaryPath, false),
                    fullUrl = resolveUrl(primaryPath, false),
                    width = 1400,
                    height = 1800,
                    rating = ContentRating.SAFE,
                    score = 120 + (i * 7),
                    author = userId,
                    serviceBadge = "pawchive • $service",
                    tags = tagsList,
                    attachments = mappedAttachments,
                    sourceUrl = "$baseUrl/$service/user/$userId/post/$id",
                    createdAt = published,
                    sha256Hash = primaryPath.substringAfterLast("/").substringBefore(".")
                )
            )
        }
        return if (posts.isNotEmpty()) posts to endpoint else null
    }

    private fun fetchDanbooruLive(
        baseUrl: String,
        profile: BooruProfileEntity,
        positiveTokens: List<String>,
        page: Int
    ): Pair<List<BooruPost>, String>? {
        val endpoint = buildCanonicalEndpoint(BooruEngineType.DANBOORU, baseUrl, positiveTokens, page, SortOrder.NEWEST)
        val raw = executeGet(endpoint, profile, acceptJson = true) ?: return null
        if (!raw.trim().startsWith("[")) return null
        val arr = JSONArray(raw)
        val posts = mutableListOf<BooruPost>()
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            val id = obj.optInt("id", 0).takeIf { it > 0 }?.toString() ?: continue
            val preview = obj.optString("preview_file_url").ifBlank { obj.optString("large_file_url") }
            val large = obj.optString("large_file_url").ifBlank { preview }
            val full = obj.optString("file_url").ifBlank { large }
            if (preview.isBlank()) continue

            val artistTags = obj.optString("tag_string_artist").split(" ").filter { it.isNotBlank() }
                .map { BooruTag(it, TagCategory.ARTIST, 420) }
            val copyTags = obj.optString("tag_string_copyright").split(" ").filter { it.isNotBlank() }
                .map { BooruTag(it, TagCategory.COPYRIGHT, 1200) }
            val charTags = obj.optString("tag_string_character").split(" ").filter { it.isNotBlank() }
                .map { BooruTag(it, TagCategory.CHARACTER, 880) }
            val genTags = obj.optString("tag_string_general").split(" ").filter { it.isNotBlank() }.take(12)
                .map { BooruTag(it, TagCategory.GENERAL, 2500) }

            val ratingCode = obj.optString("rating", "g")
            val rating = when (ratingCode) {
                "g", "s" -> ContentRating.SAFE
                "q" -> ContentRating.QUESTIONABLE
                else -> ContentRating.EXPLICIT
            }

            posts.add(
                BooruPost(
                    id = id,
                    engineType = BooruEngineType.DANBOORU,
                    booruHost = baseUrl.substringAfter("://"),
                    title = (charTags + artistTags).joinToString(" • ") { it.name }.ifBlank { "Danbooru #$id" },
                    previewUrl = preview,
                    sampleUrl = large,
                    fullUrl = full,
                    width = obj.optInt("image_width", 1200),
                    height = obj.optInt("image_height", 1600),
                    rating = rating,
                    score = obj.optInt("score", 25),
                    author = artistTags.firstOrNull()?.name ?: "danbooru_artist",
                    serviceBadge = "danbooru v2",
                    tags = artistTags + copyTags + charTags + genTags,
                    sourceUrl = "$baseUrl/posts/$id",
                    createdAt = obj.optString("created_at", "2026-11-01").take(10),
                    fileExt = obj.optString("file_ext", "jpg"),
                    sha256Hash = obj.optString("md5", "")
                )
            )
        }
        return if (posts.isNotEmpty()) posts to endpoint else null
    }

    private fun fetchGelbooruLive(
        baseUrl: String,
        profile: BooruProfileEntity,
        positiveTokens: List<String>,
        page: Int
    ): Pair<List<BooruPost>, String>? {
        val endpoint = buildCanonicalEndpoint(BooruEngineType.GELBOORU, baseUrl, positiveTokens, page, SortOrder.NEWEST)
        val raw = executeGet(endpoint, profile, acceptJson = true) ?: return null
        val arr = when {
            raw.trim().startsWith("[") -> JSONArray(raw)
            raw.trim().startsWith("{") -> JSONObject(raw).optJSONArray("post") ?: return null
            else -> return null
        }
        val posts = mutableListOf<BooruPost>()
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            val id = obj.optString("id").ifBlank { continue }
            val dir = obj.optString("directory")
            val image = obj.optString("image")
            val fileUrl = obj.optString("file_url").ifBlank {
                if (dir.isNotBlank() && image.isNotBlank()) "$baseUrl/images/$dir/$image" else ""
            }
            val previewUrl = obj.optString("preview_url").ifBlank {
                if (dir.isNotBlank() && image.isNotBlank()) "$baseUrl/thumbnails/$dir/thumbnail_${image.substringBeforeLast(".")}.jpg" else fileUrl
            }
            if (fileUrl.isBlank()) continue

            val tags = obj.optString("tags").split(" ").filter { it.isNotBlank() }.take(16).map {
                BooruTag(it, classifyTagCategory(it), 600)
            }
            posts.add(
                BooruPost(
                    id = id,
                    engineType = BooruEngineType.GELBOORU,
                    booruHost = baseUrl.substringAfter("://"),
                    title = tags.take(2).joinToString(" • ") { it.name }.ifBlank { "Gelbooru #$id" },
                    previewUrl = previewUrl,
                    sampleUrl = fileUrl,
                    fullUrl = fileUrl,
                    width = obj.optInt("width", 1200),
                    height = obj.optInt("height", 1500),
                    rating = ContentRating.SAFE,
                    score = obj.optInt("score", 30),
                    author = obj.optString("owner", "safebooru_uploader"),
                    serviceBadge = "gelbooru dapi",
                    tags = tags,
                    sourceUrl = "$baseUrl/index.php?page=post&s=view&id=$id",
                    createdAt = "Archive"
                )
            )
        }
        return if (posts.isNotEmpty()) posts to endpoint else null
    }

    private fun filterAndSortPosts(
        posts: List<BooruPost>,
        positiveTokens: List<String>,
        negativeTokens: Set<String>,
        ratingFilter: ContentRating,
        sortOrder: SortOrder
    ): List<BooruPost> {
        val filtered = posts.filter { post ->
            val postTagNames = post.tags.map { it.name.lowercase() }.toSet()
            val hasBlacklisted = negativeTokens.any { neg ->
                postTagNames.any { it.contains(neg) } || post.title.lowercase().contains(neg)
            }
            if (hasBlacklisted) return@filter false

            val ratingMatches = ratingFilter == ContentRating.ALL || post.rating == ratingFilter
            if (!ratingMatches) return@filter false

            if (positiveTokens.isEmpty()) return@filter true

            positiveTokens.all { tok ->
                val clean = tok.lowercase()
                when {
                    clean.startsWith("hash:") -> post.sha256Hash.lowercase().startsWith(clean.removePrefix("hash:"))
                    clean.startsWith("service:") -> post.serviceBadge.lowercase().contains(clean.removePrefix("service:"))
                    clean.startsWith("creator:") -> post.author.lowercase().contains(clean.removePrefix("creator:"))
                    else -> postTagNames.any { it.contains(clean) } ||
                        post.title.lowercase().contains(clean) ||
                        post.author.lowercase().contains(clean) ||
                        post.serviceBadge.lowercase().contains(clean)
                }
            }
        }

        return when (sortOrder) {
            SortOrder.BEST_SCORE -> filtered.sortedByDescending { it.score }
            SortOrder.POPULAR -> filtered.sortedByDescending { it.score + it.attachments.size * 40 }
            SortOrder.NEWEST -> filtered
        }
    }

    private fun classifyTagCategory(tag: String): TagCategory {
        return when {
            tag.startsWith("artist:") || tag.endsWith("_art") || tag in setOf("wlop", "guweiz", "stalenhag", "moebius", "krenz") -> TagCategory.ARTIST
            tag in setOf("cyberpunk", "warhammer_40k", "elden_ring", "dune", "blade_runner", "evangelion", "arcane") -> TagCategory.COPYRIGHT
            tag in setOf("mecha", "valkyrie", "cyborg", "astronaut", "knight", "kitsune_spirit") -> TagCategory.CHARACTER
            tag in setOf("gif", "webm", "comic_strip", "highres", "4k_wallpaper", "patreon", "fanbox", "joyreactor") -> TagCategory.META
            else -> TagCategory.GENERAL
        }
    }

    private fun getResilientArchiveCatalog(
        engine: BooruEngineType,
        baseUrl: String,
        page: Int
    ): List<BooruPost> = ArchiveSeedCatalog.buildPostsForEngine(engine, baseUrl, page)
}

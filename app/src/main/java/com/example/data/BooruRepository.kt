package com.example.data

import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

class BooruRepository(
    private val booruDao: BooruDao,
    private val networkEngine: NetworkEngine
) {
    val allProfiles: Flow<List<BooruProfileEntity>> = booruDao.getAllProfiles()
    val allBookmarks: Flow<List<BookmarkedPostEntity>> = booruDao.getAllBookmarks()
    val favoriteTags: Flow<List<SavedTagEntity>> = booruDao.getFavoriteTags()
    val blacklistedTags: Flow<List<SavedTagEntity>> = booruDao.getBlacklistedTags()

    suspend fun ensureSeeded() {
        val existing = booruDao.getAllProfilesSnapshot()
        if (existing.isEmpty()) {
            booruDao.insertProfile(
                BooruProfileEntity(
                    name = "JoyReactor (joyreactor.cc)",
                    engineType = BooruEngineType.JOYREACTOR.name,
                    baseUrl = "https://joyreactor.cc",
                    isActive = true
                )
            )
            booruDao.insertProfile(
                BooruProfileEntity(
                    name = "Pawchive Archive (pawchive.pw)",
                    engineType = BooruEngineType.PAWCHIVE.name,
                    baseUrl = "https://pawchive.pw",
                    isActive = false
                )
            )
            booruDao.insertProfile(
                BooruProfileEntity(
                    name = "Danbooru / Safebooru",
                    engineType = BooruEngineType.DANBOORU.name,
                    baseUrl = "https://safebooru.donmai.us",
                    isActive = false
                )
            )
            booruDao.insertProfile(
                BooruProfileEntity(
                    name = "Gelbooru / Safebooru.org",
                    engineType = BooruEngineType.GELBOORU.name,
                    baseUrl = "https://safebooru.org",
                    isActive = false
                )
            )
        }

        val savedTags = booruDao.getAllSavedTagsSnapshot()
        if (savedTags.isEmpty()) {
            listOf(
                SavedTagEntity(tagQuery = "sci-fi", category = TagCategory.COPYRIGHT.name, engineHint = "joyreactor.cc"),
                SavedTagEntity(tagQuery = "cyberpunk", category = TagCategory.COPYRIGHT.name, engineHint = "ALL"),
                SavedTagEntity(tagQuery = "creator:astrea_labs", category = TagCategory.ARTIST.name, engineHint = "pawchive.pw"),
                SavedTagEntity(tagQuery = "service:patreon", category = TagCategory.META.name, engineHint = "pawchive.pw"),
                SavedTagEntity(tagQuery = "mecha", category = TagCategory.CHARACTER.name, engineHint = "ALL"),
                SavedTagEntity(tagQuery = "pixel_art", category = TagCategory.GENERAL.name, engineHint = "joyreactor.cc")
            ).forEach { booruDao.insertSavedTag(it) }

            listOf(
                SavedTagEntity(tagQuery = "lowres", category = TagCategory.META.name, isBlacklisted = true),
                SavedTagEntity(tagQuery = "watermark_spam", category = TagCategory.META.name, isBlacklisted = true)
            ).forEach { booruDao.insertSavedTag(it) }
        }
    }

    suspend fun fetchPosts(
        profile: BooruProfileEntity,
        rawQuery: String,
        page: Int,
        ratingFilter: ContentRating,
        sortOrder: SortOrder,
        blacklistedTags: Set<String>
    ): FetchPostsResult {
        return networkEngine.fetchPosts(
            profile = profile,
            rawQuery = rawQuery,
            page = page,
            ratingFilter = ratingFilter,
            sortOrder = sortOrder,
            blacklistedTags = blacklistedTags
        )
    }

    suspend fun selectActiveProfile(profileId: Int) {
        booruDao.setActiveProfile(profileId)
    }

    suspend fun saveOrUpdateProfile(profile: BooruProfileEntity) {
        if (profile.id == 0) {
            val newId = booruDao.insertProfile(profile).toInt()
            if (profile.isActive) {
                booruDao.setActiveProfile(newId)
            }
        } else {
            booruDao.updateProfile(profile)
            if (profile.isActive) {
                booruDao.setActiveProfile(profile.id)
            }
        }
    }

    suspend fun deleteProfile(profileId: Int) {
        booruDao.deleteProfile(profileId)
        val remaining = booruDao.getAllProfilesSnapshot()
        if (remaining.isNotEmpty() && remaining.none { it.isActive }) {
            booruDao.setActiveProfile(remaining.first().id)
        }
    }

    suspend fun toggleBookmark(post: BooruPost, currentlyBookmarked: Boolean) {
        val key = "${post.engineType.name}_${post.id}"
        if (currentlyBookmarked) {
            booruDao.deleteBookmark(key)
        } else {
            val tagsCsv = post.tags.joinToString(",") { "${it.category.name}|${it.name}" }
            booruDao.insertBookmark(
                BookmarkedPostEntity(
                    uniqueKey = key,
                    postId = post.id,
                    engineType = post.engineType.name,
                    booruHost = post.booruHost,
                    title = post.title,
                    previewUrl = post.previewUrl,
                    fullUrl = post.fullUrl,
                    width = post.width,
                    height = post.height,
                    rating = post.rating.name,
                    score = post.score,
                    author = post.author,
                    serviceBadge = post.serviceBadge,
                    tagsCsv = tagsCsv,
                    sourceUrl = post.sourceUrl,
                    createdAt = post.createdAt,
                    sha256Hash = post.sha256Hash
                )
            )
        }
    }

    suspend fun addSavedTag(query: String, category: TagCategory, isBlacklisted: Boolean, engineHint: String = "ALL") {
        val clean = query.trim().lowercase().replace(" ", "_")
        if (clean.isBlank()) return
        booruDao.deleteSavedTagByName(clean, isBlacklisted)
        booruDao.insertSavedTag(
            SavedTagEntity(
                tagQuery = clean,
                category = category.name,
                engineHint = engineHint,
                isBlacklisted = isBlacklisted
            )
        )
    }

    suspend fun removeSavedTag(id: Int) {
        booruDao.deleteSavedTag(id)
    }

    /**
     * Generates a Boorusama-compatible JSON backup containing custom Booru profiles
     * (including joyreactor.cc & pawchive.pw), favorite tags, blacklisted tags, and bookmarks
     * so users can sync between Official Boorusama (com.degenk.boorusama) and Boorusama Plus.
     */
    suspend fun exportBoorusamaBackupJson(): String {
        val profiles = booruDao.getAllProfilesSnapshot()
        val tags = booruDao.getAllSavedTagsSnapshot()
        val bookmarks = booruDao.getAllBookmarksSnapshot()

        val root = JSONObject()
        root.put("app", "Boorusama Plus")
        root.put("packageId", "com.aistudio.boorusamaplus.jrpchw")
        root.put("coexistsWithOfficialPackage", "com.degenk.boorusama")
        root.put("version", 1)

        val profilesArr = JSONArray()
        profiles.forEach { p ->
            profilesArr.put(
                JSONObject().apply {
                    put("name", p.name)
                    put("engine", p.engineType)
                    put("url", p.baseUrl)
                    put("userAgent", p.userAgent)
                    put("hasCookies", p.cookieHeader.isNotBlank())
                }
            )
        }
        root.put("booruProfiles", profilesArr)

        val favTagsArr = JSONArray()
        val blacklistArr = JSONArray()
        tags.forEach { t ->
            if (t.isBlacklisted) blacklistArr.put(t.tagQuery)
            else favTagsArr.put(t.tagQuery)
        }
        root.put("favoriteTags", favTagsArr)
        root.put("blacklistedTags", blacklistArr)
        root.put("bookmarkedPostCount", bookmarks.size)
        return root.toString(2)
    }

    suspend fun importBoorusamaBackupJson(rawJson: String): Result<Int> = runCatching {
        val obj = JSONObject(rawJson.trim())
        var importedCount = 0

        val favArr = obj.optJSONArray("favoriteTags") ?: obj.optJSONArray("tags")
        if (favArr != null) {
            for (i in 0 until favArr.length()) {
                val item = favArr.optString(i).trim()
                if (item.isNotBlank()) {
                    addSavedTag(item, TagCategory.GENERAL, isBlacklisted = false)
                    importedCount++
                }
            }
        }

        val blkArr = obj.optJSONArray("blacklistedTags") ?: obj.optJSONArray("blacklist")
        if (blkArr != null) {
            for (i in 0 until blkArr.length()) {
                val item = blkArr.optString(i).trim()
                if (item.isNotBlank()) {
                    addSavedTag(item, TagCategory.META, isBlacklisted = true)
                    importedCount++
                }
            }
        }
        importedCount
    }
}

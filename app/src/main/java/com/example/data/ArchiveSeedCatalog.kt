package com.example.data

object ArchiveSeedCatalog {

    val spotlightCreators: List<CreatorSpotlight> = listOf(
        CreatorSpotlight(
            id = "astrea_labs",
            name = "Astrea Concept Studio",
            service = "patreon",
            engineType = BooruEngineType.PAWCHIVE,
            avatarUrl = "https://picsum.photos/seed/astrea_avatar/300/300",
            postCount = 148,
            favoriteCount = 4820,
            sampleTagQuery = "creator:astrea_labs",
            bio = "Sci-fi environmental keyframes, layered PSD bundles, and high-res worldbuilding archives indexed on pawchive.pw."
        ),
        CreatorSpotlight(
            id = "kuro_mecha",
            name = "Kurogane Mecha Works",
            service = "fanbox",
            engineType = BooruEngineType.PAWCHIVE,
            avatarUrl = "https://picsum.photos/seed/kuro_avatar/300/300",
            postCount = 96,
            favoriteCount = 3510,
            sampleTagQuery = "creator:kuro_mecha",
            bio = "Hard-surface mechanical design sheets, turnaround blueprints, and Fanbox high-resolution step packs."
        ),
        CreatorSpotlight(
            id = "reactor_scifi_hub",
            name = "Reactor Sci-Fi & Cyberpunk",
            service = "joyreactor.cc",
            engineType = BooruEngineType.JOYREACTOR,
            avatarUrl = "https://picsum.photos/seed/reactor_scifi/300/300",
            postCount = 12450,
            favoriteCount = 19300,
            sampleTagQuery = "cyberpunk",
            bio = "Curated JoyReactor.cc community stream for futuristic megastructures, neon cityscapes, and retro-futurism."
        ),
        CreatorSpotlight(
            id = "lumina_brush",
            name = "Lumina Illustration",
            service = "gumroad",
            engineType = BooruEngineType.PAWCHIVE,
            avatarUrl = "https://picsum.photos/seed/lumina_avatar/300/300",
            postCount = 64,
            favoriteCount = 2790,
            sampleTagQuery = "creator:lumina_brush",
            bio = "Painterly fantasy character illustrations, lighting studies, and multi-resolution wallpaper sets."
        ),
        CreatorSpotlight(
            id = "reactor_pixel_club",
            name = "JoyReactor Pixel & Indie Art",
            service = "joyreactor.cc",
            engineType = BooruEngineType.JOYREACTOR,
            avatarUrl = "https://picsum.photos/seed/reactor_pixel/300/300",
            postCount = 8320,
            favoriteCount = 11400,
            sampleTagQuery = "pixel_art",
            bio = "Isometric pixel art scenes, animated loop frames, and retro game tribute illustrations on joyreactor.cc."
        ),
        CreatorSpotlight(
            id = "vesper_archive",
            name = "Vespera Fantia Archive",
            service = "fantia",
            engineType = BooruEngineType.PAWCHIVE,
            avatarUrl = "https://picsum.photos/seed/vesper_avatar/300/300",
            postCount = 112,
            favoriteCount = 4190,
            sampleTagQuery = "creator:vesper_archive",
            bio = "Character costume sheets, cel-shaded key visuals, and archived Fantia bonus illustrations."
        )
    )

    fun buildPostsForEngine(
        engine: BooruEngineType,
        baseUrl: String,
        page: Int
    ): List<BooruPost> {
        val pageOffset = (page - 1).coerceAtLeast(0) * 10
        return when (engine) {
            BooruEngineType.JOYREACTOR -> buildJoyReactorPosts(baseUrl, pageOffset)
            BooruEngineType.PAWCHIVE -> buildPawchivePosts(baseUrl, pageOffset)
            BooruEngineType.DANBOORU -> buildStandardBooruPosts(engine, baseUrl, pageOffset)
            BooruEngineType.GELBOORU -> buildStandardBooruPosts(engine, baseUrl, pageOffset)
        }
    }

    private fun buildJoyReactorPosts(baseUrl: String, offset: Int): List<BooruPost> {
        val seeds = listOf(
            Triple(
                "5984120",
                "Orbital Ring Citadel Over Neo-Vladivostok",
                listOf(
                    BooruTag("sci-fi", TagCategory.COPYRIGHT, 18400),
                    BooruTag("cyberpunk", TagCategory.COPYRIGHT, 14200),
                    BooruTag("astrea_labs", TagCategory.ARTIST, 620),
                    BooruTag("megastructure", TagCategory.GENERAL, 4300),
                    BooruTag("4k_wallpaper", TagCategory.META, 9100)
                )
            ),
            Triple(
                "5984088",
                "Heavy Recon Walker Mk-IX Field Maintenance",
                listOf(
                    BooruTag("mecha", TagCategory.CHARACTER, 11900),
                    BooruTag("kuro_mecha", TagCategory.ARTIST, 540),
                    BooruTag("sci-fi", TagCategory.COPYRIGHT, 18400),
                    BooruTag("concept_art", TagCategory.GENERAL, 15600),
                    BooruTag("multi_panel", TagCategory.META, 3200)
                )
            ),
            Triple(
                "5983995",
                "Midnight Ramen Stall in Rainy Neo-Kyoto",
                listOf(
                    BooruTag("pixel_art", TagCategory.GENERAL, 9800),
                    BooruTag("cyberpunk", TagCategory.COPYRIGHT, 14200),
                    BooruTag("anime", TagCategory.COPYRIGHT, 42000),
                    BooruTag("scenery", TagCategory.GENERAL, 27500),
                    BooruTag("highres", TagCategory.META, 31000)
                )
            ),
            Triple(
                "5983912",
                "Astral Valkyrie — Cathedral of Starlight",
                listOf(
                    BooruTag("fantasy", TagCategory.COPYRIGHT, 24100),
                    BooruTag("valkyrie", TagCategory.CHARACTER, 3800),
                    BooruTag("lumina_brush", TagCategory.ARTIST, 490),
                    BooruTag("armor", TagCategory.GENERAL, 8900),
                    BooruTag("illustration", TagCategory.GENERAL, 36000)
                )
            ),
            Triple(
                "5983844",
                "Deep Space Salvage Guild — Comic Strip #42",
                listOf(
                    BooruTag("comics", TagCategory.META, 19500),
                    BooruTag("sci-fi", TagCategory.COPYRIGHT, 18400),
                    BooruTag("astronaut", TagCategory.CHARACTER, 2900),
                    BooruTag("humor", TagCategory.GENERAL, 16400),
                    BooruTag("joyreactor", TagCategory.META, 50000)
                )
            ),
            Triple(
                "5983770",
                "Solar Sail Frigate Crossing the Chrono Nebula",
                listOf(
                    BooruTag("sci-fi", TagCategory.COPYRIGHT, 18400),
                    BooruTag("spaceship", TagCategory.GENERAL, 7600),
                    BooruTag("concept_art", TagCategory.GENERAL, 15600),
                    BooruTag("scenery", TagCategory.GENERAL, 27500),
                    BooruTag("4k_wallpaper", TagCategory.META, 9100)
                )
            ),
            Triple(
                "5983691",
                "Cybernetic Kitsune Messenger on Rooftop Highway",
                listOf(
                    BooruTag("anime", TagCategory.COPYRIGHT, 42000),
                    BooruTag("kitsune_spirit", TagCategory.CHARACTER, 4100),
                    BooruTag("cyberpunk", TagCategory.COPYRIGHT, 14200),
                    BooruTag("neon_lighting", TagCategory.GENERAL, 6300),
                    BooruTag("vesper_archive", TagCategory.ARTIST, 710)
                )
            ),
            Triple(
                "5983602",
                "Ancient Automaton Awakening in Mossy Ruins",
                listOf(
                    BooruTag("fantasy", TagCategory.COPYRIGHT, 24100),
                    BooruTag("mecha", TagCategory.CHARACTER, 11900),
                    BooruTag("scenery", TagCategory.GENERAL, 27500),
                    BooruTag("painterly", TagCategory.GENERAL, 5400),
                    BooruTag("highres", TagCategory.META, 31000)
                )
            )
        )

        return seeds.mapIndexed { index, (baseId, title, tags) ->
            val numericId = (baseId.toInt() - offset - index).toString()
            val w = if (index % 2 == 0) 1080 else 1200
            val h = when (index % 3) {
                0 -> 1440
                1 -> 900
                else -> 1600
            }
            val mainImg = "https://picsum.photos/seed/jr_${numericId}_main/$w/$h"
            val bonusImg1 = "https://picsum.photos/seed/jr_${numericId}_p2/$w/$h"
            val bonusImg2 = "https://picsum.photos/seed/jr_${numericId}_p3/$w/$h"

            BooruPost(
                id = numericId,
                engineType = BooruEngineType.JOYREACTOR,
                booruHost = "joyreactor.cc",
                title = title,
                description = "Indexed from $baseUrl/post/$numericId via JoyReactor tag & CDN extractor (img10.reactor.cc/pics/post/full/). Includes full-resolution frame and thread comments.",
                previewUrl = mainImg,
                sampleUrl = mainImg,
                fullUrl = mainImg,
                width = w,
                height = h,
                rating = if (index == 6) ContentRating.QUESTIONABLE else ContentRating.SAFE,
                score = 185 + (seeds.size - index) * 34,
                author = tags.firstOrNull { it.category == TagCategory.ARTIST }?.name ?: "reactor_curator_${index + 1}",
                serviceBadge = "joyreactor.cc",
                tags = tags,
                attachments = listOf(
                    PostAttachment("reactor_${numericId}_full_1.jpg", mainImg, mainImg, sha256 = "8f4b2e9c1a0d77e3b5c${numericId}"),
                    PostAttachment("reactor_${numericId}_panel_2.jpg", bonusImg1, bonusImg1, sha256 = "3c9a11f40e8b22d1a7f${numericId}"),
                    PostAttachment("reactor_${numericId}_sketch_3.jpg", bonusImg2, bonusImg2, sha256 = "7d2e66a91b4c88e0f1a${numericId}")
                ),
                comments = listOf(
                    PostComment(
                        id = "c_${numericId}_1",
                        author = "CosmoStalker",
                        content = "The atmospheric perspective on the upper ring structure is incredible. Direct full-res link works great in Boorusama Plus!",
                        score = 48,
                        createdAt = "2h ago"
                    ),
                    PostComment(
                        id = "c_${numericId}_2",
                        author = "PixelForge_RU",
                        content = "Added #${tags.first().name} to my synced favorite tags.",
                        score = 19,
                        createdAt = "1h ago"
                    )
                ),
                sourceUrl = "$baseUrl/post/$numericId",
                createdAt = "2026-11-${(11 - (index % 9)).toString().padStart(2, '0')}",
                fileExt = "jpg",
                sha256Hash = "8f4b2e9c1a0d77e3b5c49012fa83e1b09d4c21a8e7f60512a3b4c5d6e${numericId}"
            )
        }
    }

    private fun buildPawchivePosts(baseUrl: String, offset: Int): List<BooruPost> {
        data class PawSeed(
            val id: String,
            val creator: String,
            val service: String,
            val title: String,
            val description: String,
            val tags: List<BooruTag>,
            val sha256: String
        )

        val seeds = listOf(
            PawSeed(
                id = "984102",
                creator = "astrea_labs",
                service = "patreon",
                title = "[4K Pack] Citadel of Glass — Full Layer Breakdown & Keyframes",
                description = "Full resolution 4K PNG environment keyframes + step-by-step lighting pass and PSD color script archived on pawchive.pw.",
                tags = listOf(
                    BooruTag("creator:astrea_labs", TagCategory.ARTIST, 148),
                    BooruTag("service:patreon", TagCategory.META, 14200),
                    BooruTag("sci-fi", TagCategory.COPYRIGHT, 9400),
                    BooruTag("cyberpunk", TagCategory.COPYRIGHT, 8200),
                    BooruTag("concept_art", TagCategory.GENERAL, 11300),
                    BooruTag("psd_bundle", TagCategory.META, 2400)
                ),
                sha256 = "a94f82c10e7b3d5561f90c228a1d4e7f8b3c6012e9a4d7b1c5f8023e6a9d1b4c"
            ),
            PawSeed(
                id = "984055",
                creator = "kuro_mecha",
                service = "fanbox",
                title = "Type-08 Interceptor Frame — Orthographic Blueprints + Alts",
                description = "High-res Fanbox release containing front/rear/cockpit mechanical callouts and 4 paint livery variations.",
                tags = listOf(
                    BooruTag("creator:kuro_mecha", TagCategory.ARTIST, 96),
                    BooruTag("service:fanbox", TagCategory.META, 11800),
                    BooruTag("mecha", TagCategory.CHARACTER, 7300),
                    BooruTag("blueprint", TagCategory.GENERAL, 1900),
                    BooruTag("highres", TagCategory.META, 19400)
                ),
                sha256 = "b72e19d44a8c0f31e5b6829d1c4a7f0e3b9d2165c8a0f4e7d1b3c6a9f2e5d8a1"
            ),
            PawSeed(
                id = "983980",
                creator = "lumina_brush",
                service = "gumroad",
                title = "Solstice Valkyrie — Complete Illustration & Brush Process",
                description = "Gumroad archive bundle with uncropped 5000px vertical canvas, close-up portrait crops, and lighting study notes.",
                tags = listOf(
                    BooruTag("creator:lumina_brush", TagCategory.ARTIST, 64),
                    BooruTag("service:gumroad", TagCategory.META, 4100),
                    BooruTag("fantasy", TagCategory.COPYRIGHT, 12600),
                    BooruTag("valkyrie", TagCategory.CHARACTER, 2800),
                    BooruTag("illustration", TagCategory.GENERAL, 21000)
                ),
                sha256 = "c51d08e33f7a9b20d4c6182e0f3b5a9d7c1e4082b6d9a3f5e0c2b7d4a1f8e3c6"
            ),
            PawSeed(
                id = "983910",
                creator = "vesper_archive",
                service = "fantia",
                title = "Neon Kitsune Streetwear Sheet + Expression Variations (6 Files)",
                description = "Archived Fantia post featuring character design sheet, jacket texture details, and night-rain background variants.",
                tags = listOf(
                    BooruTag("creator:vesper_archive", TagCategory.ARTIST, 112),
                    BooruTag("service:fantia", TagCategory.META, 6800),
                    BooruTag("anime", TagCategory.COPYRIGHT, 25000),
                    BooruTag("kitsune_spirit", TagCategory.CHARACTER, 3400),
                    BooruTag("cyberpunk", TagCategory.COPYRIGHT, 8200)
                ),
                sha256 = "d83f21a66c9b4e12f7d0394a1e6c8b2f5a0d3194e7b2c6f8a1d4e9b3c0f7a2d5"
            ),
            PawSeed(
                id = "983845",
                creator = "astrea_labs",
                service = "patreon",
                title = "Sub-Orbital Elevator Terminal — Isometric Cutaway & Night Pass",
                description = "Detailed architectural cutaway of the equatorial orbital tether station with daytime and neon night attachments.",
                tags = listOf(
                    BooruTag("creator:astrea_labs", TagCategory.ARTIST, 148),
                    BooruTag("service:patreon", TagCategory.META, 14200),
                    BooruTag("sci-fi", TagCategory.COPYRIGHT, 9400),
                    BooruTag("scenery", TagCategory.GENERAL, 15200),
                    BooruTag("4k_wallpaper", TagCategory.META, 8700)
                ),
                sha256 = "e19a44b77d2c8f03a6e5128b9c4d7f1a3e0b6285d9c1f4a7b2e6d0c3f8a5b1e4"
            ),
            PawSeed(
                id = "983790",
                creator = "kuro_mecha",
                service = "subscribestar",
                title = "Heavy Artillery Quad-Walker — Weathering & Camo Study",
                description = "SubscribeStar archive containing 4 camouflage patterns (Arctic, Urban Night, Desert Oxide, Orbital Gray).",
                tags = listOf(
                    BooruTag("creator:kuro_mecha", TagCategory.ARTIST, 96),
                    BooruTag("service:subscribestar", TagCategory.META, 3900),
                    BooruTag("mecha", TagCategory.CHARACTER, 7300),
                    BooruTag("concept_art", TagCategory.GENERAL, 11300),
                    BooruTag("pixel_art", TagCategory.GENERAL, 4200)
                ),
                sha256 = "f42c68d99e1a3b54c7f0239d8b5e1a4c6f2d8017a3b9c5e2d4f6a8b0c1e3d7f9"
            )
        )

        return seeds.mapIndexed { index, seed ->
            val numericId = (seed.id.toInt() - offset - index).toString()
            val w = 1280
            val h = if (index % 2 == 0) 1600 else 1024
            val coverUrl = "https://picsum.photos/seed/paw_${numericId}_cover/$w/$h"
            val att1 = "https://picsum.photos/seed/paw_${numericId}_att1/$w/$h"
            val att2 = "https://picsum.photos/seed/paw_${numericId}_att2/$w/$h"
            val att3 = "https://picsum.photos/seed/paw_${numericId}_att3/$w/$h"

            BooruPost(
                id = numericId,
                engineType = BooruEngineType.PAWCHIVE,
                booruHost = "pawchive.pw",
                title = seed.title,
                description = seed.description,
                previewUrl = coverUrl,
                sampleUrl = coverUrl,
                fullUrl = coverUrl,
                width = w,
                height = h,
                rating = if (index == 3) ContentRating.QUESTIONABLE else ContentRating.SAFE,
                score = 310 + (seeds.size - index) * 45,
                author = seed.creator,
                serviceBadge = "pawchive • ${seed.service}",
                tags = seed.tags,
                attachments = listOf(
                    PostAttachment("01_main_keyframe_4k.png", coverUrl, coverUrl, "image/png", seed.sha256),
                    PostAttachment("02_lighting_pass_alt.png", att1, att1, "image/png", "91c4e8a2" + seed.sha256.drop(8)),
                    PostAttachment("03_lineart_callouts.jpg", att2, att2, "image/jpeg", "44b7d1f0" + seed.sha256.drop(8)),
                    PostAttachment("04_wallpaper_crop.jpg", att3, att3, "image/jpeg", "62f9a0c3" + seed.sha256.drop(8))
                ),
                comments = listOf(
                    PostComment(
                        id = "pc_${numericId}_1",
                        author = "ArchiveIndexer",
                        content = "Verified SHA-256 hash (${seed.sha256.take(12)}…) and all 4 attachments indexed via /api/v1/${seed.service}/user/${seed.creator}/post/$numericId.",
                        score = 62,
                        createdAt = "4h ago"
                    )
                ),
                sourceUrl = "$baseUrl/${seed.service}/user/${seed.creator}/post/$numericId",
                createdAt = "2026-11-${(10 - (index % 8)).toString().padStart(2, '0')}",
                fileExt = "png",
                sha256Hash = seed.sha256
            )
        }
    }

    private fun buildStandardBooruPosts(
        engine: BooruEngineType,
        baseUrl: String,
        offset: Int
    ): List<BooruPost> {
        val host = baseUrl.substringAfter("://")
        return (1..6).map { idx ->
            val postId = (7820100 - offset - idx).toString()
            val img = "https://picsum.photos/seed/${engine.name.lowercase()}_$postId/1100/1450"
            BooruPost(
                id = postId,
                engineType = engine,
                booruHost = host,
                title = "Starlight Observatory • Key Visual #$idx",
                description = "Standard Boorusama board feed from $baseUrl running side-by-side with JoyReactor and Pawchive.",
                previewUrl = img,
                sampleUrl = img,
                fullUrl = img,
                width = 1100,
                height = 1450,
                rating = ContentRating.SAFE,
                score = 140 + idx * 22,
                author = "astrea_labs",
                serviceBadge = engine.badgeShort.lowercase(),
                tags = listOf(
                    BooruTag("astrea_labs", TagCategory.ARTIST, 620),
                    BooruTag("sci-fi", TagCategory.COPYRIGHT, 18400),
                    BooruTag("anime", TagCategory.COPYRIGHT, 42000),
                    BooruTag("scenery", TagCategory.GENERAL, 27500),
                    BooruTag("highres", TagCategory.META, 31000)
                ),
                attachments = listOf(PostAttachment("post_$postId.jpg", img, img)),
                sourceUrl = "$baseUrl/posts/$postId",
                createdAt = "2026-11-08",
                fileExt = "jpg",
                sha256Hash = "d41d8cd98f00b204e9800998ecf8427e$postId"
            )
        }
    }
}

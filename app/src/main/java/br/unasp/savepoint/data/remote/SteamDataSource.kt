
package br.unasp.savepoint.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class SteamDataSource(
    private val client: HttpClient
) : GameDataSource {

    override suspend fun searchGames(
        query: String
    ): List<RemoteGame> {

        if (query.isBlank()) return emptyList()

        val response: SteamSearchResponse = client.get(
            "https://store.steampowered.com/api/storesearch/"
        ) {
            parameter("term", query)
            parameter("l", "brazilian")
            parameter("cc", "br")
        }.body()

        return response.items
            .filter { item ->
                !item.name.contains("soundtrack", ignoreCase = true) &&
                        !item.name.contains("DLC", ignoreCase = true) &&
                        !item.name.contains("OST", ignoreCase = true)
            }
            .map { item ->
                RemoteGame(
                    externalId = item.id.toString(),
                    title = item.name,
                    coverUrl = item.tinyImage,
                    platform = "Steam"
                )
            }
    }

    override suspend fun getGameDetails(
        externalId: String
    ): RemoteGame? {

        val response: Map<String, SteamDetailsResponse> = client.get(
            "https://store.steampowered.com/api/appdetails"
        ) {
            parameter("appids", externalId)
            parameter("l", "brazilian")
            parameter("cc", "br")
        }.body()

        val details = response[externalId]
            ?.takeIf { it.success }
            ?.data ?: return null

        return RemoteGame(
            externalId = externalId,
            title = details.name,
            coverUrl = details.header_image,
            genre = details.genres.joinToString(", ") {
                it.description
            }.ifBlank { null },
            platform = "Steam",
            description = details.short_description,
            rating = details.metacritic?.score?.toFloat()
        )
    }

    override suspend fun getFeaturedGames(): List<RemoteGame> {

        val response: SteamFeaturedResponse = client.get(
            "https://store.steampowered.com/api/featuredcategories/"
        ) {
            parameter("l", "brazilian")
            parameter("cc", "br")
        }.body()

        return response.topSellers?.items
            .orEmpty()
            .filter { item ->
                item.type == 0 || item.type == null
            }
            .take(12)
            .map { item ->
                RemoteGame(
                    externalId = item.id.toString(),
                    title = item.name,
                    coverUrl = item.largeCapsuleImage
                        ?: item.smallCapsuleImage,
                    platform = "Steam",
                    rating = item.metascore
                        ?.toFloatOrNull()
                )
            }
    }
}

@Serializable
data class SteamSearchResponse(
    val total: Int = 0,
    val items: List<SteamSearchItem> = emptyList()
)

@Serializable
data class SteamSearchItem(
    val id: Int,
    val name: String,
    @SerialName("tiny_image")
    val tinyImage: String? = null,
    val type: String? = null,
    val metascore: String? = null
)

@Serializable
data class SteamDetailsResponse(
    val success: Boolean = false,
    val data: SteamGameDetails? = null
)

@Serializable
data class SteamGameDetails(
    val name: String = "",
    val short_description: String = "",
    val header_image: String? = null,
    val metacritic: SteamMetacritic? = null,
    val genres: List<SteamGenre> = emptyList()
)

@Serializable
data class SteamMetacritic(
    val score: Int? = null
)

@Serializable
data class SteamGenre(
    val description: String
)

@Serializable
data class SteamFeaturedResponse(
    @SerialName("top_sellers")
    val topSellers: SteamFeaturedCategory? = null
)

@Serializable
data class SteamFeaturedCategory(
    val items: List<SteamFeaturedItem> = emptyList()
)

@Serializable
data class SteamFeaturedItem(
    val id: Int,
    val name: String,
    val type: Int? = null,
    @SerialName("large_capsule_image")
    val largeCapsuleImage: String? = null,
    @SerialName("small_capsule_image")
    val smallCapsuleImage: String? = null,
    val metascore: String? = null
)

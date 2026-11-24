
package br.unasp.savepoint.data.remote

import br.unasp.savepoint.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.HttpHeaders
import io.ktor.http.encodeURLParameter
import kotlinx.serialization.Serializable

class SteamGridDBDataSource(
    private val client: HttpClient
) {

    private val baseUrl =
        "https://www.steamgriddb.com/api/v2"

    private fun authHeaders(): Map<String, String> {
        return mapOf(
            HttpHeaders.Authorization to
                    "Bearer ${BuildConfig.STEAMGRIDDB_API_KEY}"
        )
    }

    suspend fun searchGameId(gameName: String): Int? {

        val response: SteamGridSearchResponse = client.get(
            "$baseUrl/search/autocomplete/${gameName.encodeURLParameter()}"
        ) {
            authHeaders().forEach { (key, value) ->
                header(key, value)
            }
        }.body()

        return response.data.firstOrNull()?.id
    }

    suspend fun getGameCover(gameId: Int): String? {

        val response: SteamGridCoverResponse = client.get(
            "$baseUrl/grids/game/$gameId"
        ) {
            authHeaders().forEach { (key, value) ->
                header(key, value)
            }

            parameter("dimensions", "600x900")
            parameter("types", "static")
            parameter("limit", 5)
        }.body()

        return response.data.firstOrNull()?.url
    }

    suspend fun getCoverByGameName(gameName: String): String? {

        if (gameName.isBlank()) return null

        val gameId = searchGameId(gameName) ?: return null

        return getGameCover(gameId)
    }
}

@Serializable
data class SteamGridSearchResponse(
    val success: Boolean = false,
    val data: List<SteamGridGame> = emptyList()
)

@Serializable
data class SteamGridGame(
    val id: Int,
    val name: String
)

@Serializable
data class SteamGridCoverResponse(
    val success: Boolean = false,
    val data: List<SteamGridImage> = emptyList()
)

@Serializable
data class SteamGridImage(
    val id: Int,
    val url: String,
    val thumb: String? = null,
    val width: Int? = null,
    val height: Int? = null
)

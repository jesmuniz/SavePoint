
package br.unasp.savepoint.data.remote

data class RemoteGame(
    val externalId: String,
    val title: String,
    val coverUrl: String? = null,
    val genre: String? = null,
    val platform: String? = null,
    val description: String? = null,
    val rating: Float? = null
)

interface GameDataSource {

    suspend fun searchGames(query: String): List<RemoteGame>

    suspend fun getGameDetails(externalId: String): RemoteGame?

    suspend fun getFeaturedGames(): List<RemoteGame>
}

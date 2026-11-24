
package br.unasp.savepoint.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders

class SteamLibraryRemoteDataSource(
    private val client: HttpClient
) {

    suspend fun getOwnedGames(
        accessToken: String
    ): SteamOwnedGamesResponse {

        return client.get("${ApiConfig.BASE_URL}api/steam/games") {
            header(
                HttpHeaders.Authorization,
                "Bearer $accessToken"
            )
        }.body()
    }
}

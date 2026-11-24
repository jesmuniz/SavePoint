
package br.unasp.savepoint.data.repository

import br.unasp.savepoint.data.local.AuthDataStore
import br.unasp.savepoint.data.remote.AuthRemoteDataSource
import kotlinx.coroutines.flow.Flow

import br.unasp.savepoint.data.remote.SteamLibraryRemoteDataSource
import br.unasp.savepoint.data.remote.SteamOwnedGamesResponse

class AuthRepository(
    private val authRemoteDataSource: AuthRemoteDataSource,
    private val authDataStore: AuthDataStore,
    private val steamLibraryRemoteDataSource: SteamLibraryRemoteDataSource
) {

    // AUTENTICAÇÃO COM A STEAM

    suspend fun loginWithSteam(code: String): Result<Unit> {
        return try {
            val response = authRemoteDataSource.exchangeCode(code)

            if (response.tokenType != "Bearer") {
                throw IllegalStateException(
                    "Tipo de token não reconhecido."
                )
            }

            authDataStore.saveSession(
                accessToken = response.accessToken,
                userId = response.userId,
                steamId = response.steamId
            )

            Result.success(Unit)

        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    // SESSÃO DO USUÁRIO

    suspend fun getAccessToken(): String? {
        return authDataStore.getAccessToken()
    }

    suspend fun getUserId(): String? {
        return authDataStore.getUserId()
    }

    suspend fun getSteamId(): String? {
        return authDataStore.getSteamId()
    }

    // BIBLIOTECA STEAM

    suspend fun getSteamLibrary(): Result<SteamOwnedGamesResponse> {
        return try {
            val accessToken = authDataStore.getAccessToken()
                ?: throw IllegalStateException(
                    "Você precisa conectar sua conta Steam primeiro."
                )

            val response = steamLibraryRemoteDataSource.getOwnedGames(
                accessToken = accessToken
            )

            Result.success(response)

        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    suspend fun logout() {
        authDataStore.clearSession()
    }

    suspend fun isLoggedIn(): Boolean {
        return authDataStore.getAccessToken() != null
    }
}

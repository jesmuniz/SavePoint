
package br.unasp.savepoint

import android.app.Application

import br.unasp.savepoint.data.database.SavePointDatabase
import br.unasp.savepoint.data.local.AuthDataStore
import br.unasp.savepoint.data.remote.AuthRemoteDataSource
import br.unasp.savepoint.data.remote.HttpClientProvider
import br.unasp.savepoint.data.remote.SteamDataSource
import br.unasp.savepoint.data.remote.SteamGridDBDataSource
import br.unasp.savepoint.data.remote.SteamLibraryRemoteDataSource
import br.unasp.savepoint.data.repository.AuthRepository
import br.unasp.savepoint.data.repository.GameRepository
import br.unasp.savepoint.data.preferences.UserPreferencesDataStore

class SavePointApplication : Application() {

    // BANCO DE DADOS LOCAL
    val database by lazy {
        SavePointDatabase.getDatabase(this)
    }

    // APIs EXTERNAS
    val remoteDataSource by lazy {
        SteamDataSource(HttpClientProvider.client)
    }

    val steamGridDBDataSource by lazy {
        SteamGridDBDataSource(HttpClientProvider.client)
    }

    val steamLibraryRemoteDataSource by lazy {
        SteamLibraryRemoteDataSource(HttpClientProvider.client)
    }

    // REPOSITÓRIO DE JOGOS
    val repository by lazy {
        GameRepository(
            gameDao = database.gameDao(),
            remoteDataSource = remoteDataSource,
            steamGridDBDataSource = steamGridDBDataSource
        )
    }

    // PREFERÊNCIAS DO USUÁRIO
    val userPreferences by lazy {
        UserPreferencesDataStore(this)
    }

    // AUTENTICAÇÃO
    val authDataStore by lazy {
        AuthDataStore(this)
    }

    val authRemoteDataSource by lazy {
        AuthRemoteDataSource(HttpClientProvider.client)
    }

    // REPOSITÓRIO DE AUTENTICAÇÃO
    val authRepository by lazy {
        AuthRepository(
            authRemoteDataSource = authRemoteDataSource,
            authDataStore = authDataStore,
            steamLibraryRemoteDataSource = steamLibraryRemoteDataSource
        )
    }
}

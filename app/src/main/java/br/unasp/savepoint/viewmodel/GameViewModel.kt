
package br.unasp.savepoint.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.unasp.savepoint.data.model.GameEntity
import br.unasp.savepoint.data.remote.RemoteGame
import br.unasp.savepoint.data.repository.GameRepository
import br.unasp.savepoint.data.model.GamePlatformEntity
import br.unasp.savepoint.data.remote.SteamGameRemote
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import br.unasp.savepoint.data.repository.AuthRepository

class GameViewModel(
    private val repository: GameRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    // CARREGAMENTO DOS DETALHES

    private val _isLoadingGameDetails = MutableStateFlow(false)

    val isLoadingGameDetails: StateFlow<Boolean> =
        _isLoadingGameDetails.asStateFlow()

    private var gameDetailsJob: Job? = null
    private var requestedGameDetailsId: String? = null

    // CAPAS ALTERNATIVAS DO STEAMGRIDDB

    private val _alternativeCover = MutableStateFlow<String?>(null)
    val alternativeCover = _alternativeCover.asStateFlow()

    private val _isLoadingAlternativeCover = MutableStateFlow(false)
    val isLoadingAlternativeCover = _isLoadingAlternativeCover.asStateFlow()

    // JOGOS DA BIBLIOTECA LOCAL

    val games: StateFlow<List<GameEntity>> =
        repository.getAllGames()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    // VÍNCULOS DAS PLATAFORMAS

    val gamePlatforms: StateFlow<List<GamePlatformEntity>> =
        repository.getAllGamePlatforms()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    // JOGOS FAVORITOS

    val favoriteGames: StateFlow<List<GameEntity>> =
        repository.getFavoriteGames()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    // JOGOS POPULARES / EM DESTAQUE

    private val _featuredGames =
        MutableStateFlow<List<RemoteGame>>(emptyList())

    val featuredGames = _featuredGames.asStateFlow()

    private val _isLoadingFeatured = MutableStateFlow(false)

    val isLoadingFeatured = _isLoadingFeatured.asStateFlow()

    private val _featuredError = MutableStateFlow<String?>(null)

    val featuredError = _featuredError.asStateFlow()

    // RESULTADOS DA PESQUISA EXTERNA

    private val _searchResults =
        MutableStateFlow<List<RemoteGame>>(emptyList())

    val searchResults = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)

    val isSearching = _isSearching.asStateFlow()

    private val _searchError = MutableStateFlow<String?>(null)

    val searchError = _searchError.asStateFlow()

    private val _gameDetails = MutableStateFlow<RemoteGame?>(null)

    val gameDetails = _gameDetails.asStateFlow()

    // OPERAÇÕES DA BIBLIOTECA
    fun addGame(game: GameEntity) {
        viewModelScope.launch {
            val apiId = game.apiId

            val alreadyExists = if (apiId != null) {
                repository.getGameByApiId(apiId) != null
            } else {
                false
            }

            if (!alreadyExists) {
                val customCover = game.customCoverUrl
                    ?: repository.getAlternativeCover(game.title)

                val gameWithCover = game.copy(
                    customCoverUrl = customCover
                )

                repository.insertGame(gameWithCover)

                Log.d(
                    "SavePointLibrary",
                    "Jogo adicionado: ${game.title} | Capa personalizada: $customCover"
                )
            }
        }
    }


    fun updateGame(game: GameEntity) {
        viewModelScope.launch {
            repository.updateGame(game)
        }
    }

    fun deleteGame(game: GameEntity) {
        viewModelScope.launch {
            repository.deleteGame(game)
        }
    }

    // CARREGAR JOGOS EM DESTAQUE DA STEAM

    fun loadFeaturedGames() {
        if (_isLoadingFeatured.value) return

        viewModelScope.launch {
            _isLoadingFeatured.value = true
            _featuredError.value = null

            try {
                _featuredGames.value = repository.getFeaturedGames()

                Log.d(
                    "SavePointFeatured",
                    "Jogos em destaque carregados: ${_featuredGames.value.size}"
                )

            } catch (exception: Exception) {
                Log.e(
                    "SavePointFeatured",
                    "Erro ao carregar jogos em destaque",
                    exception
                )

                _featuredError.value =
                    "Não foi possível carregar os jogos. Tente novamente."
            } finally {
                _isLoadingFeatured.value = false
            }
        }
    }

    // PESQUISA NA API EXTERNA

    fun searchGames(query: String) {
        viewModelScope.launch {
            _isSearching.value = true
            _searchError.value = null

            try {
                _searchResults.value = repository.searchGames(query)

            } catch (exception: Exception) {
                Log.e(
                    "SavePointSearch",
                    "Erro ao pesquisar jogos na Steam",
                    exception
                )

                _searchResults.value = emptyList()
                _searchError.value =
                    "Não foi possível pesquisar jogos. Tente novamente."
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun clearSearchResults() {
        _searchResults.value = emptyList()
        _searchError.value = null
    }

    // DETALHES DO JOGO NA API

    fun loadGameDetails(externalId: String) {
        if (
            requestedGameDetailsId == externalId &&
            (_isLoadingGameDetails.value ||
                    _gameDetails.value?.externalId == externalId)
        ) {
            return
        }

        gameDetailsJob?.cancel()
        requestedGameDetailsId = externalId

        gameDetailsJob = viewModelScope.launch {
            _gameDetails.value = null
            _isLoadingGameDetails.value = true

            try {
                val details = repository.getRemoteGameDetails(externalId)

                if (requestedGameDetailsId == externalId && details != null) {
                    _gameDetails.value = details

                    Log.d(
                        "SavePointDetails",
                        "Detalhes carregados: ${details.title}"
                    )
                }
            } catch (exception: Exception) {
                Log.e(
                    "SavePointDetails",
                    "Erro ao carregar detalhes do jogo",
                    exception
                )
            } finally {
                if (requestedGameDetailsId == externalId) {
                    _isLoadingGameDetails.value = false
                }
            }
        }
    }

    // BUSCAR CAPA ALTERNATIVA NO STEAMGRIDDB

    fun loadAlternativeCover(gameName: String) {
        _isLoadingAlternativeCover.value = true
        _alternativeCover.value = null

        viewModelScope.launch {
            try {
                val cover = repository.getAlternativeCover(gameName)
                _alternativeCover.value = cover

                Log.d(
                    "SavePointGrid",
                    "Capa encontrada para $gameName: $cover"
                )
            } catch (exception: Exception) {
                Log.e(
                    "SavePointGrid",
                    "Erro ao buscar capa alternativa",
                    exception
                )
            } finally {
                _isLoadingAlternativeCover.value = false
            }
        }
    }

    // SOBRESCRITA CENTRALIZADA PELO ID LOCAL DO JOGO

    private val _isOverridingSteamGame = MutableStateFlow(false)

    val isOverridingSteamGame: StateFlow<Boolean> =
        _isOverridingSteamGame.asStateFlow()

    fun overrideSteamGameById(
        gameId: Int,
        onResult: (Boolean, String) -> Unit
    ) {
        if (_isOverridingSteamGame.value) return

        viewModelScope.launch {
            _isOverridingSteamGame.value = true

            try {
                // 1. Localiza os vínculos do jogo.
                val platforms = repository.getPlatformsByGameId(gameId)

                // 2. Procura o vínculo com a Steam.
                val steamPlatform = platforms.firstOrNull {
                    it.platform.equals("Steam", ignoreCase = true)
                }

                val steamAppId = steamPlatform?.externalId?.toIntOrNull()
                    ?: throw IllegalStateException(
                        "Este jogo não possui vínculo com a Steam."
                    )

                // 3. Busca a biblioteca atualizada da conta conectada.
                val libraryResult = authRepository.getSteamLibrary()

                val steamGames = libraryResult.getOrElse {
                    throw it
                }.response.games

                // 4. Localiza o jogo pelo AppID.
                val steamGame = steamGames.firstOrNull {
                    it.appId == steamAppId
                } ?: throw IllegalStateException(
                    "Este jogo não foi encontrado na sua biblioteca Steam."
                )

                // 5. Executa a sobrescrita no banco local.
                val success = repository.overrideSteamGame(steamGame)

                if (success) {
                    onResult(true, "Dados de ${steamGame.name} atualizados!")
                } else {
                    onResult(false, "Não foi possível atualizar este jogo.")
                }

            } catch (exception: Exception) {
                Log.e(
                    "SavePointSync",
                    "Erro ao sobrescrever jogo pelo ID local",
                    exception
                )

                onResult(
                    false,
                    exception.message ?: "Erro ao atualizar dados da Steam."
                )

            } finally {
                _isOverridingSteamGame.value = false
            }
        }
    }


    // SOBRESCRITA INDIVIDUAL DE JOGO DA STEAM

    fun overrideSteamGame(
        steamGame: SteamGameRemote,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val success = repository.overrideSteamGame(steamGame)

                onResult(success)

            } catch (exception: Exception) {
                Log.e(
                    "SavePointSync",
                    "Erro ao sobrescrever jogo da Steam",
                    exception
                )

                onResult(false)
            }
        }
    }

    // SOBRESCRITA DE TODOS OS JOGOS DA STEAM

    fun overrideAllSteamGames(
        steamGames: List<SteamGameRemote>,
        onResult: (Int) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val updatedCount =
                    repository.overrideAllSteamGames(steamGames)

                Log.d(
                    "SavePointSync",
                    "Jogos sobrescritos: $updatedCount"
                )

                onResult(updatedCount)

            } catch (exception: Exception) {
                Log.e(
                    "SavePointSync",
                    "Erro ao sobrescrever biblioteca Steam",
                    exception
                )

                onResult(-1)
            }
        }
    }


}

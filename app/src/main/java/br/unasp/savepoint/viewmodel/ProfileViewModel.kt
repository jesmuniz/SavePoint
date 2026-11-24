
package br.unasp.savepoint.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.unasp.savepoint.data.preferences.UserPreferencesDataStore
import br.unasp.savepoint.data.remote.SteamGameRemote
import br.unasp.savepoint.data.repository.AuthRepository
import br.unasp.savepoint.data.repository.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userPreferences: UserPreferencesDataStore,
    private val authRepository: AuthRepository,
    private val gameRepository: GameRepository
) : ViewModel() {

    // BIBLIOTECA STEAM

    private val _steamGames =
        MutableStateFlow<List<SteamGameRemote>>(emptyList())

    private val _steamGameCovers =
        MutableStateFlow<Map<Int, String>>(emptyMap())

    val steamGameCovers = _steamGameCovers.asStateFlow()
    val steamGames = _steamGames.asStateFlow()

    private val _isLoadingSteamGames = MutableStateFlow(false)
    val isLoadingSteamGames = _isLoadingSteamGames.asStateFlow()

    private val _isOverridingSteamGames = MutableStateFlow(false)
    val isOverridingSteamGames = _isOverridingSteamGames.asStateFlow()

    private val _steamError = MutableStateFlow<String?>(null)
    val steamError = _steamError.asStateFlow()

    fun loadSteamGameCovers() {
        viewModelScope.launch {
            val games = _steamGames.value.take(10)

            games.forEach { game ->
                val gameName = game.name

                if (!gameName.isNullOrBlank()) {
                    val coverUrl =
                        gameRepository.getAlternativeCover(gameName)

                    if (!coverUrl.isNullOrBlank()) {
                        _steamGameCovers.value =
                            _steamGameCovers.value +
                                    (game.appId to coverUrl)
                    }
                }
            }
        }
    }

    // CARREGAMENTO INICIAL DA BIBLIOTECA

    fun loadSteamLibrary() {
        viewModelScope.launch {
            if (_isLoadingSteamGames.value) return@launch

            _isLoadingSteamGames.value = true
            _steamError.value = null

            try {
                authRepository.getSteamLibrary()
                    .onSuccess { response ->
                        val games = response.response.games

                        _steamGames.value = games

                        // Importa automaticamente apenas na primeira vez
                        if (!userPreferences.hasImportedSteamLibrary()) {
                            gameRepository.importSteamGames(games)

                            userPreferences.markSteamLibraryAsImported()
                        }

                        loadSteamGameCovers()
                    }
                    .onFailure { exception ->
                        _steamError.value =
                            exception.message
                                ?: "Erro ao consultar biblioteca Steam."
                    }
            } catch (exception: Exception) {
                _steamError.value =
                    exception.message
                        ?: "Erro ao importar biblioteca Steam."
            } finally {
                _isLoadingSteamGames.value = false
            }
        }
    }

    // SINCRONIZAÇÃO MANUAL DA BIBLIOTECA

    fun syncSteamLibrary() {
        viewModelScope.launch {
            if (_isLoadingSteamGames.value) return@launch

            _isLoadingSteamGames.value = true
            _steamError.value = null

            try {
                authRepository.getSteamLibrary()
                    .onSuccess { response ->
                        val games = response.response.games

                        _steamGames.value = games

                        // Atualiza os jogos existentes e adiciona os novos
                        gameRepository.importSteamGames(games)

                        // Atualiza as capas exibidas no perfil
                        _steamGameCovers.value = emptyMap()
                        loadSteamGameCovers()
                    }
                    .onFailure { exception ->
                        _steamError.value =
                            exception.message
                                ?: "Erro ao sincronizar biblioteca Steam."
                    }
            } catch (exception: Exception) {
                _steamError.value =
                    exception.message
                        ?: "Erro durante a sincronização."
            } finally {
                _isLoadingSteamGames.value = false
            }
        }
    }

    // PERFIL DO USUÁRIO

    val userName: StateFlow<String> = userPreferences.userName
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            "Kaluak"
        )

    val userTag: StateFlow<String> = userPreferences.userTag
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            "@kaluak"
        )

    fun saveProfile(name: String, tag: String) {
        viewModelScope.launch {
            userPreferences.saveProfile(name, tag)
        }
    }

    val avatarUri: StateFlow<String?> = userPreferences.avatarUri
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )

    val isProfileLoaded: StateFlow<Boolean> = combine(
        userPreferences.userName,
        userPreferences.userTag,
        userPreferences.avatarUri
    ) { _, _, _ ->
        true
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        false
    )

    fun saveAvatar(uri: String?) {
        viewModelScope.launch {
            userPreferences.saveAvatar(uri)
        }
    }

    fun overrideSteamGame(appId: Int) {
        viewModelScope.launch {

            val steamGame = _steamGames.value
                .find { it.appId == appId }
                ?: return@launch

            if (_isOverridingSteamGames.value) return@launch

            _isOverridingSteamGames.value = true

            try {
                gameRepository.overrideSteamGame(steamGame)

            } catch (exception: Exception) {
                _steamError.value =
                    exception.message ?: "Erro ao sobrescrever jogo."

            } finally {
                _isOverridingSteamGames.value = false
            }
        }
    }

    fun overrideAllSteamGames() {
        viewModelScope.launch {

            if (_isOverridingSteamGames.value) return@launch

            val games = _steamGames.value

            if (games.isEmpty()) {
                _steamError.value = "Nenhum jogo da Steam foi carregado."
                return@launch
            }

            _isOverridingSteamGames.value = true

            try {
                gameRepository.overrideAllSteamGames(games)

            } catch (exception: Exception) {
                _steamError.value =
                    exception.message ?: "Erro ao sobrescrever biblioteca."

            } finally {
                _isOverridingSteamGames.value = false
            }
        }
    }
}

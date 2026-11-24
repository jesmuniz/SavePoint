
package br.unasp.savepoint.data.repository

import br.unasp.savepoint.data.dao.GameDao
import br.unasp.savepoint.data.model.GameEntity
import br.unasp.savepoint.data.model.GamePlatformEntity
import br.unasp.savepoint.data.remote.GameDataSource
import br.unasp.savepoint.data.remote.RemoteGame
import br.unasp.savepoint.data.remote.SteamGameRemote
import br.unasp.savepoint.data.remote.SteamGridDBDataSource
import kotlinx.coroutines.flow.Flow

class GameRepository(
    private val gameDao: GameDao,
    private val remoteDataSource: GameDataSource,
    private val steamGridDBDataSource: SteamGridDBDataSource
) {

    // BANCO DE DADOS LOCAL (ROOM)

    suspend fun insertGame(game: GameEntity) {
        gameDao.insertGame(game)
    }

    suspend fun updateGame(game: GameEntity) {
        val previousGame = gameDao.getGameById(game.id)

        val updatedGame = if (previousGame != null) {
            game.copy(
                statusManuallySet = previousGame.statusManuallySet ||
                        previousGame.status != game.status,

                hoursManuallySet = previousGame.hoursManuallySet ||
                        previousGame.hoursPlayed != game.hoursPlayed
            )
        } else {
            game
        }

        gameDao.updateGame(updatedGame)
    }

    suspend fun deleteGame(game: GameEntity) {
        gameDao.deleteGame(game)
    }

    fun getAllGames(): Flow<List<GameEntity>> {
        return gameDao.getAllGames()
    }

    suspend fun getGameById(id: Int): GameEntity? {
        return gameDao.getGameById(id)
    }

    suspend fun getGameByApiId(apiId: Int): GameEntity? {
        return gameDao.getGameByApiId(apiId)
    }

    fun getGamesByStatus(status: String): Flow<List<GameEntity>> {
        return gameDao.getGamesByStatus(status)
    }

    fun getFavoriteGames(): Flow<List<GameEntity>> {
        return gameDao.getFavoriteGames()
    }

    // PLATAFORMAS DOS JOGOS

    suspend fun insertGamePlatform(
        gamePlatform: GamePlatformEntity
    ): Long {
        return gameDao.insertGamePlatform(gamePlatform)
    }

    suspend fun getPlatformsByGameId(
        gameId: Int
    ): List<GamePlatformEntity> {
        return gameDao.getPlatformsByGameId(gameId)
    }

    suspend fun getGamePlatform(
        platform: String,
        externalId: String
    ): GamePlatformEntity? {
        return gameDao.getGamePlatform(platform, externalId)
    }

    fun getAllGamePlatforms(): Flow<List<GamePlatformEntity>> {
        return gameDao.getAllGamePlatforms()
    }

    suspend fun getGameByPlatformId(
        platform: String,
        externalId: String
    ): GameEntity? {
        return gameDao.getGameByPlatformId(
            platform,
            externalId
        )
    }

    suspend fun getGamesByPlatform(
        platform: String
    ): List<GameEntity> {
        return gameDao.getGamesByPlatform(platform)
    }



    suspend fun overrideSteamGame(steamGame: SteamGameRemote): Boolean {
        val steamAppId = steamGame.appId.toString()

        // Localiza o vínculo com a Steam.
        val platformGame = gameDao.getGamePlatform(
            platform = "Steam",
            externalId = steamAppId
        ) ?: return false

        // Localiza o jogo no banco local.
        val localGame = gameDao.getGameById(
            platformGame.gameId
        ) ?: return false

        val steamHours = steamGame.playtimeForeverMinutes / 60f
        val lastSyncedAt = System.currentTimeMillis() / 1000

        // Atualiza as horas, preservando o status manual.
        val updatedGame = localGame.copy(
            hoursPlayed = steamHours,
            status = if (localGame.statusManuallySet) {
                localGame.status
            } else {
                if (steamHours > 0f) "JOGANDO" else "QUERO_JOGAR"
            },
            hoursManuallySet = false
        )

        gameDao.updateGame(updatedGame)

        // Atualiza os dados específicos da plataforma.
        gameDao.updatePlatformSyncData(
            id = platformGame.id,
            hoursPlayed = steamHours,
            lastPlayedAt = steamGame.lastPlayedEpochSeconds,
            lastSyncedAt = lastSyncedAt
        )

        return true
    }


    suspend fun overrideAllSteamGames(
        steamGames: List<SteamGameRemote>
    ): Int {

        var updatedCount = 0

        steamGames.forEach { steamGame ->

            val updated = overrideSteamGame(steamGame)

            if (updated) {
                updatedCount++
            }
        }

        return updatedCount
    }

    suspend fun updatePlatformSyncData(
        id: Int,
        hoursPlayed: Float,
        lastPlayedAt: Long?,
        lastSyncedAt: Long?
    ) {
        gameDao.updatePlatformSyncData(
            id,
            hoursPlayed,
            lastPlayedAt,
            lastSyncedAt
        )
    }

    // API EXTERNA (STEAM)

    suspend fun searchGames(query: String): List<RemoteGame> {
        return remoteDataSource.searchGames(query)
    }

    suspend fun getRemoteGameDetails(
        externalId: String
    ): RemoteGame? {
        return remoteDataSource.getGameDetails(externalId)
    }

    suspend fun getFeaturedGames(): List<RemoteGame> {
        return remoteDataSource.getFeaturedGames()
    }

    // API EXTERNA (STEAMGRIDDB)

    suspend fun getAlternativeCover(gameName: String): String? {
        return try {
            steamGridDBDataSource.getCoverByGameName(gameName)
        } catch (exception: Exception) {
            null
        }
    }

    // IMPORTAÇÃO DA BIBLIOTECA STEAM


    suspend fun importSteamGames(
        steamGames: List<SteamGameRemote>
    ): Int {

        var importedCount = 0

        steamGames.forEach { steamGame ->

            val gameName = steamGame.name
                ?.takeIf { it.isNotBlank() }
                ?: return@forEach

            val steamHours = steamGame.playtimeForeverMinutes / 60f
            val steamAppId = steamGame.appId.toString()

            val existingPlatform = gameDao.getGamePlatform(
                platform = "Steam",
                externalId = steamAppId
            )

            // Primeiro, verifica se o jogo já está vinculado à Steam.
            val localGame = if (existingPlatform != null) {

                gameDao.getGameByPlatformId(
                    platform = "Steam",
                    externalId = steamAppId
                ) ?: return@forEach

            } else {

                // Se não estiver vinculado, procura pelo título.
                val existingGames = gameDao.getGamesByTitle(gameName)

                // Não associa automaticamente títulos ambíguos.
                if (existingGames.size > 1) {
                    return@forEach
                }

                if (existingGames.size == 1) {

                    existingGames.first()

                } else {

                    // Novo jogo: recebe os dados iniciais da Steam.
                    val coverUrl = getAlternativeCover(gameName)

                    val newGame = GameEntity(
                        title = gameName,
                        coverUrl = coverUrl,
                        status = if (steamHours > 0f) {
                            "JOGANDO"
                        } else {
                            "QUERO_JOGAR"
                        },
                        hoursPlayed = steamHours,
                        statusManuallySet = false,
                        hoursManuallySet = false
                    )

                    val newId = gameDao.insertGameIfNotExists(newGame)

                    if (newId == -1L) {
                        return@forEach
                    }

                    gameDao.getGameById(newId.toInt())
                        ?: return@forEach
                }
            }

            // Atualiza somente os campos automáticos ainda não
            // protegidos por uma escolha manual do usuário.
            if (!localGame.statusManuallySet || !localGame.hoursManuallySet) {

                val updatedGame = localGame.copy(
                    status = if (!localGame.statusManuallySet) {
                        if (steamHours > 0f) "JOGANDO" else "QUERO_JOGAR"
                    } else {
                        localGame.status
                    },

                    hoursPlayed = if (!localGame.hoursManuallySet) {
                        steamHours
                    } else {
                        localGame.hoursPlayed
                    }
                )

                gameDao.updateGame(updatedGame)
            }

            val lastSyncedAt = System.currentTimeMillis() / 1000

            if (existingPlatform == null) {

                gameDao.insertGamePlatform(
                    GamePlatformEntity(
                        gameId = localGame.id,
                        platform = "Steam",
                        externalId = steamAppId,
                        hoursPlayed = steamHours,
                        lastPlayedAt = steamGame.lastPlayedEpochSeconds,
                        lastSyncedAt = lastSyncedAt
                    )
                )

            } else {

                gameDao.updatePlatformSyncData(
                    id = existingPlatform.id,
                    hoursPlayed = steamHours,
                    lastPlayedAt = steamGame.lastPlayedEpochSeconds,
                    lastSyncedAt = lastSyncedAt
                )
            }

            importedCount++
        }

        return importedCount
    }

}

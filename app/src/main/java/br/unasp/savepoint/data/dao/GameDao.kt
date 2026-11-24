
package br.unasp.savepoint.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.unasp.savepoint.data.model.GameEntity
import br.unasp.savepoint.data.model.GamePlatformEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface GameDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: GameEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertGameIfNotExists(game: GameEntity): Long

    @Update
    suspend fun updateGame(game: GameEntity)

    @Delete
    suspend fun deleteGame(game: GameEntity)

    @Query("SELECT * FROM games ORDER BY title ASC")
    fun getAllGames(): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE id = :id")
    suspend fun getGameById(id: Int): GameEntity?

    @Query("""
    SELECT * FROM games
    WHERE title = :title COLLATE NOCASE
""")
    suspend fun getGamesByTitle(
        title: String
    ): List<GameEntity>

    @Query("SELECT * FROM games WHERE apiId = :apiId LIMIT 1")
    suspend fun getGameByApiId(apiId: Int): GameEntity?

    @Query("SELECT * FROM games WHERE status = :status ORDER BY title ASC")
    fun getGamesByStatus(status: String): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE isFavorite = 1 ORDER BY title ASC")
    fun getFavoriteGames(): Flow<List<GameEntity>>


    // PLATAFORMAS DOS JOGOS

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertGamePlatform(
        gamePlatform: GamePlatformEntity
    ): Long

    @Query("""
        SELECT * FROM game_platforms
        WHERE gameId = :gameId
        ORDER BY platform ASC
    """)
    suspend fun getPlatformsByGameId(
        gameId: Int
    ): List<GamePlatformEntity>

    @Query("""
        SELECT * FROM game_platforms
        WHERE platform = :platform
        AND externalId = :externalId
        LIMIT 1
    """)
    suspend fun getGamePlatform(
        platform: String,
        externalId: String
    ): GamePlatformEntity?

    // CONSULTAS DOS JOGOS POR PLATAFORMA

    @Query("""
    SELECT * FROM game_platforms
    ORDER BY platform ASC
""")
    fun getAllGamePlatforms(): Flow<List<GamePlatformEntity>>

    @Query("""
    SELECT games.* FROM games
    INNER JOIN game_platforms
    ON games.id = game_platforms.gameId
    WHERE game_platforms.platform = :platform
    AND game_platforms.externalId = :externalId
    LIMIT 1
""")
    suspend fun getGameByPlatformId(
        platform: String,
        externalId: String
    ): GameEntity?

    @Query("""
    SELECT games.* FROM games
    INNER JOIN game_platforms
    ON games.id = game_platforms.gameId
    WHERE game_platforms.platform = :platform
    ORDER BY games.title ASC
""")
    suspend fun getGamesByPlatform(
        platform: String
    ): List<GameEntity>

    @Query("""
        UPDATE game_platforms
        SET hoursPlayed = :hoursPlayed,
            lastPlayedAt = :lastPlayedAt,
            lastSyncedAt = :lastSyncedAt
        WHERE id = :id
    """)
    suspend fun updatePlatformSyncData(
        id: Int,
        hoursPlayed: Float,
        lastPlayedAt: Long?,
        lastSyncedAt: Long?
    )

}

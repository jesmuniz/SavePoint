
package br.unasp.savepoint.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "game_platforms",
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = ["id"],
            childColumns = ["gameId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["gameId"]),
        Index(
            value = ["platform", "externalId"],
            unique = true
        )
    ]
)
data class GamePlatformEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val gameId: Int,

    val platform: String,

    val externalId: String,

    val hoursPlayed: Float = 0f,

    val lastPlayedAt: Long? = null,

    val lastSyncedAt: Long? = null
)



package br.unasp.savepoint.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val apiId: Int? = null,
    val title: String,
    val coverUrl: String? = null,
    val customCoverUrl: String? = null,
    val genre: String? = null,
    val platform: String? = null,
    val description: String? = null,
    val status: String = "QUERO_JOGAR",
    val progress: Int = 0,
    val hoursPlayed: Float = 0f,

    // Avaliação do Metacritic
    val rating: Float? = null,
    // Avaliação pessoal do usuário (0 a 5 estrelas)
    val userRating: Float? = null,

    val notes: String? = null,
    val isFavorite: Boolean = false,

    @ColumnInfo(defaultValue = "1")
    val statusManuallySet: Boolean = false,

    @ColumnInfo(defaultValue = "1")
    val hoursManuallySet: Boolean = false
)


package br.unasp.savepoint.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SteamOwnedGamesResponse(
    val response: SteamOwnedGamesData
)

@Serializable
data class SteamOwnedGamesData(
    @SerialName("game_count")
    val gameCount: Int? = 0,

    val games: List<SteamGameRemote> = emptyList()
)

@Serializable
data class SteamGameRemote(
    @SerialName("appid")
    val appId: Int,
    val name: String? = null,

    @SerialName("playtime_forever")
    val playtimeForeverMinutes: Int = 0,

    @SerialName("img_icon_url")
    val imageIconHash: String? = null,

    @SerialName("rtime_last_played")
    val lastPlayedEpochSeconds: Long? = null
)

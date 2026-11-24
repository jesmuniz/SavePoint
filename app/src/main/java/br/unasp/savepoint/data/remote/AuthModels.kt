
package br.unasp.savepoint.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class AuthCodeRequest(
    val code: String
)

@Serializable
data class AuthResponse(
    val userId: String,
    val steamId: String,
    val accessToken: String,
    val tokenType: String,
    val expiresIn: Long
)

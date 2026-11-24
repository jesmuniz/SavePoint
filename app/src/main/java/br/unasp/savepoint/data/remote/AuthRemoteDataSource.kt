
package br.unasp.savepoint.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class AuthRemoteDataSource(
    private val client: HttpClient
) {

    suspend fun exchangeCode(code: String): AuthResponse {

        return client.post(ApiConfig.AUTH_EXCHANGE) {
            contentType(ContentType.Application.Json)
            setBody(AuthCodeRequest(code))
        }.body()
    }
}

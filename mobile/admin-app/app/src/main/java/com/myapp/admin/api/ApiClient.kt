package com.myapp.admin.api

import androidx.annotation.Keep
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

@Keep data class LoginRequest(val password: String)
@Keep data class LoginResponse(val token: String)

@Keep data class StatsResponse(
    @SerializedName("totalUsers") val totalUsers: Int,
    val uptime: Double
)

@Keep data class ErrorResponse(
    val error: ErrorDetail?
) {
    @Keep data class ErrorDetail(val message: String)
}

object ApiClient {
    private const val BASE_URL = "https://my-app-gvd3.onrender.com/api"
    private val gson = Gson()
    var authToken: String? = null

    suspend fun login(password: String): Result<LoginResponse> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$BASE_URL/admin/login")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json")
            conn.connectTimeout = 15000
            conn.readTimeout = 15000

            val body = gson.toJson(LoginRequest(password))
            OutputStreamWriter(conn.outputStream).use { it.write(body) }

            val response = readResponse(conn)
            if (conn.responseCode == 200) {
                val loginResp = gson.fromJson(response, LoginResponse::class.java)
                authToken = loginResp.token
                Result.success(loginResp)
            } else {
                val error = try { gson.fromJson(response, ErrorResponse::class.java) } catch (_: Exception) { null }
                Result.failure(Exception(error?.error?.message ?: "Invalid credentials"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getStats(): Result<StatsResponse> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$BASE_URL/admin/stats")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Authorization", "Bearer ${authToken.orEmpty()}")
            conn.connectTimeout = 15000
            conn.readTimeout = 15000

            val response = readResponse(conn)
            if (conn.responseCode == 200) {
                Result.success(gson.fromJson(response, StatsResponse::class.java))
            } else {
                Result.failure(Exception("Unauthorized"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun readResponse(conn: HttpURLConnection): String {
        return try {
            BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
        } catch (e: Exception) {
            val errorStream = conn.errorStream
            if (errorStream != null) {
                BufferedReader(InputStreamReader(errorStream)).use { it.readText() }
            } else {
                ""
            }
        }
    }
}

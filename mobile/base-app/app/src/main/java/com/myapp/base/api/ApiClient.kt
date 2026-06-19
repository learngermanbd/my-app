package com.myapp.base.api

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val created_at: String
)

data class UsersResponse(
    val users: List<User>,
    val page: Int,
    val limit: Int,
    val total: Int
)

data class HealthResponse(
    val status: String,
    val timestamp: String?,
    val uptime: Double?
)

object ApiClient {
    private const val BASE_URL = "https://my-app-gvd3.onrender.com/api"
    private val gson = Gson()

    suspend fun getHealth(): Result<HealthResponse> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$BASE_URL/health")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 15000
            conn.readTimeout = 15000

            val response = readResponse(conn)
            if (conn.responseCode == 200) {
                Result.success(gson.fromJson(response, HealthResponse::class.java))
            } else {
                Result.failure(Exception("Server error: ${conn.responseCode}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUsers(page: Int = 1, limit: Int = 10): Result<UsersResponse> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$BASE_URL/users?page=$page&limit=$limit")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 15000
            conn.readTimeout = 15000

            val response = readResponse(conn)
            if (conn.responseCode == 200) {
                val type = object : TypeToken<UsersResponse>() {}.type
                Result.success(gson.fromJson(response, type))
            } else {
                Result.failure(Exception("Server error: ${conn.responseCode}"))
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

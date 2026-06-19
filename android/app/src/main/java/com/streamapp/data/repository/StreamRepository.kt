package com.streamapp.data.repository

import com.streamapp.data.api.*
import com.streamapp.data.local.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.*
import javax.inject.Singleton

@Singleton
class StreamRepository @Inject constructor(
    private val api: StreamApi,
    private val dao: StreamDao
) {

    suspend fun getConfig(): Result<AppConfig> = runCatching {
        val response = api.getConfig()
        if (response.isSuccessful) response.body()!!
        else throw Exception("Config fetch failed: ${response.code()}")
    }

    suspend fun getCategories(forceRefresh: Boolean = false): Flow<List<CachedCategory>> {
        if (forceRefresh || dao.getCategoryCount() == 0) {
            refreshCategories()
        }
        return dao.getCategories()
    }

    private suspend fun refreshCategories() {
        val response = api.getCategories()
        if (response.isSuccessful) {
            val categories = response.body() ?: return
            dao.clearCategories()
            dao.insertCategories(categories.map { cat ->
                CachedCategory(
                    id = cat.id,
                    name = cat.name,
                    slug = cat.slug,
                    iconUrl = cat.iconUrl,
                    sortOrder = cat.sortOrder,
                    channelCount = cat.channelCount
                )
            })
        }
    }

    suspend fun getChannels(categoryId: Int, forceRefresh: Boolean = false): Flow<List<CachedChannel>> {
        if (forceRefresh) refreshChannels(categoryId)
        return dao.getChannels(categoryId)
    }

    suspend fun getAllChannels(forceRefresh: Boolean = false): Flow<List<CachedChannel>> {
        if (forceRefresh || dao.getChannelCount() == 0) {
            refreshAllChannels()
        }
        return dao.getAllChannels()
    }

    private suspend fun refreshChannels(categoryId: Int) {
        val response = api.getChannels(categoryId)
        if (response.isSuccessful) {
            val channels = response.body() ?: return
            dao.clearChannels()
            dao.insertChannels(channels.map { ch ->
                CachedChannel(
                    id = ch.id,
                    categoryId = ch.categoryId,
                    name = ch.name,
                    logo = ch.logo,
                    isLive = ch.isLive,
                    featured = ch.featured,
                    categoryName = ch.categoryName
                )
            })
        }
    }

    private suspend fun refreshAllChannels() {
        val categories = dao.getCategories().first()
        dao.clearChannels()
        for (cat in categories) {
            val response = api.getChannels(cat.id)
            if (response.isSuccessful) {
                val channels = response.body() ?: continue
                dao.insertChannels(channels.map { ch ->
                    CachedChannel(
                        id = ch.id,
                        categoryId = ch.categoryId,
                        name = ch.name,
                        logo = ch.logo,
                        isLive = ch.isLive,
                        featured = ch.featured,
                        categoryName = ch.categoryName
                    )
                })
            }
        }
    }

    suspend fun getStreamLinks(channelId: Int): Result<StreamResponse> = runCatching {
        val response = api.getStreamLinks(channelId)
        if (response.isSuccessful) response.body()!!
        else throw Exception("Stream fetch failed: ${response.code()}")
    }

    suspend fun getLive(): Result<LiveResponse> = runCatching {
        val response = api.getLive()
        if (response.isSuccessful) response.body()!!
        else throw Exception("Live fetch failed: ${response.code()}")
    }

    suspend fun getMatches(): Result<MatchesResponse> = runCatching {
        val response = api.getMatches()
        if (response.isSuccessful) response.body()!!
        else throw Exception("Matches fetch failed: ${response.code()}")
    }

    suspend fun search(query: String): Result<List<Channel>> = runCatching {
        val response = api.search(query)
        if (response.isSuccessful) response.body()!!
        else throw Exception("Search failed: ${response.code()}")
    }

    suspend fun trackEvent(eventName: String, params: Map<String, String> = emptyMap(), deviceId: String = "unknown") {
        runCatching {
            api.trackEvent(AnalyticsPayload(eventName, params, deviceId))
        }
    }

    suspend fun reportCrash(message: String?, stack: String?, deviceId: String = "unknown") {
        runCatching {
            api.reportCrash(CrashReport(message, stack, deviceId))
        }
    }

    // === Session Tracking ===

    suspend fun startSession(deviceId: String, deviceType: String, channelId: Int? = null, channelName: String? = null): String? {
        return runCatching {
            val response = api.startSession(SessionStartRequest(deviceId, deviceType, channelId, channelName))
            if (response.isSuccessful) response.body()?.sessionId else null
        }.getOrNull()
    }

    suspend fun heartbeatSession(sessionId: String) {
        runCatching { api.heartbeatSession(SessionHeartbeatRequest(sessionId)) }
    }

    suspend fun endSession(sessionId: String) {
        runCatching { api.endSession(SessionEndRequest(sessionId)) }
    }

    suspend fun securityCheck(request: SecurityCheckRequest): Result<SecurityCheckResponse> = runCatching {
        val response = api.securityCheck(request)
        if (response.isSuccessful) response.body()!!
        else throw Exception("Security check failed: ${response.code()}")
    }
}

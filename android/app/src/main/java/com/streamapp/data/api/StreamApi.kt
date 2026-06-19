package com.streamapp.data.api

import retrofit2.Response
import retrofit2.http.*

interface StreamApi {

    @GET("config")
    suspend fun getConfig(): Response<AppConfig>

    @GET("categories")
    suspend fun getCategories(): Response<List<Category>>

    @GET("channels/{categoryId}")
    suspend fun getChannels(@Path("categoryId") categoryId: Int): Response<List<Channel>>

    @GET("stream/{channelId}")
    suspend fun getStreamLinks(@Path("channelId") channelId: Int): Response<StreamResponse>

    @GET("live")
    suspend fun getLive(): Response<LiveResponse>

    @GET("matches")
    suspend fun getMatches(): Response<MatchesResponse>

    @GET("search")
    suspend fun search(@Query("q") query: String): Response<List<Channel>>

    @POST("analytics")
    suspend fun trackEvent(@Body payload: AnalyticsPayload): Response<ApiResponse<Unit>>

    @GET("legal")
    suspend fun getLegal(): Response<LegalResponse>

    @POST("security-check")
    suspend fun securityCheck(@Body body: SecurityCheckRequest): Response<SecurityCheckResponse>

    @POST("crash-report")
    suspend fun reportCrash(@Body report: CrashReport): Response<ApiResponse<Unit>>

    // Session tracking
    @POST("analytics/session/start")
    suspend fun startSession(@Body body: SessionStartRequest): Response<SessionStartResponse>

    @POST("analytics/session/heartbeat")
    suspend fun heartbeatSession(@Body body: SessionHeartbeatRequest): Response<ApiResponse<Unit>>

    @POST("analytics/session/end")
    suspend fun endSession(@Body body: SessionEndRequest): Response<ApiResponse<Unit>>

    @GET("admin/stats")
    suspend fun getAdminStats(): Response<AdminStats>

    @GET("admin/channels")
    suspend fun getAdminChannels(): Response<List<AdminChannel>>

    @GET("admin/analytics")
    suspend fun getAdminAnalytics(): Response<List<AnalyticsEvent>>
}

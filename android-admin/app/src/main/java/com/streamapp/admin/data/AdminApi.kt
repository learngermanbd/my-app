package com.streamapp.admin.data

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

// --- Models ---
data class Stats(
    val categories: Int = 0, val channels: Int = 0,
    @SerializedName("stream_links") val streamLinks: Int = 0,
    @SerializedName("healthy_links") val healthyLinks: Int = 0,
    @SerializedName("analytics_events") val analyticsEvents: Int = 0
)
data class Category(val id: Int = 0, val name: String = "", val slug: String = "",
    @SerializedName("icon_url") val iconUrl: String? = null,
    @SerializedName("sort_order") val sortOrder: Int = 0,
    @SerializedName("channel_count") val channelCount: Int = 0)
data class AdminChannel(val id: Int = 0, val name: String = "",
    @SerializedName("category_id") val categoryId: Int = 0,
    @SerializedName("category_name") val categoryName: String? = null,
    @SerializedName("is_live") val isLive: Int = 0, val featured: Int = 0,
    @SerializedName("total_links") val totalLinks: Int = 0,
    @SerializedName("healthy_links") val healthyLinks: Int = 0, val logo: String? = null)
data class StreamLink(val id: Int = 0, val label: String = "", val url: String = "",
    val quality: String = "Auto", @SerializedName("is_active") val isActive: Int = 1,
    @SerializedName("is_healthy") val isHealthy: Int = 1,
    @SerializedName("last_checked") val lastChecked: String? = null)
data class LiveEvent(val id: Int = 0, val title: String = "",
    @SerializedName("channel_id") val channelId: Int? = null,
    @SerializedName("channel_name") val channelName: String? = null,
    @SerializedName("starts_at") val startsAt: String? = null,
    @SerializedName("is_live") val isLive: Int = 0)
data class LiveResponse(val events: List<LiveEvent> = emptyList(), val liveChannels: List<Any> = emptyList())
data class ConfigItem(val key: String, val value: String)
data class AnalyticsEvent(@SerializedName("event_name") val eventName: String, val count: Int)
data class CrashLog(val id: Int, @SerializedName("event_name") val eventName: String,
    val params: Any? = null, @SerializedName("device_id") val deviceId: String? = null,
    @SerializedName("created_at") val createdAt: String? = null)
data class CrashResponse(val total: Int, val logs: List<CrashLog>)
data class M3uResult(val ok: Boolean, @SerializedName("totalParsed") val totalParsed: Int = 0,
    val stats: M3uStats = M3uStats(), @SerializedName("sampleChannels") val sampleChannels: List<Any> = emptyList())
data class M3uStats(@SerializedName("categoriesCreated") val categoriesCreated: Int = 0,
    @SerializedName("channelsCreated") val channelsCreated: Int = 0,
    @SerializedName("channelsSkipped") val channelsSkipped: Int = 0,
    @SerializedName("linksAdded") val linksAdded: Int = 0)
data class Schedule(val id: Int = 0, val name: String = "", val url: String = "",
    @SerializedName("target_category_id") val targetCategoryId: Int? = null,
    @SerializedName("interval_hours") val intervalHours: Int = 24,
    @SerializedName("last_run_at") val lastRunAt: String? = null,
    @SerializedName("last_result") val lastResult: String? = null,
    @SerializedName("is_active") val isActive: Int = 1)
data class OkResponse(val ok: Boolean, val error: String? = null)
data class HealthResult(val ok: Boolean, val checked: Int = 0, val healthy: Int = 0, val dead: Int = 0)

// --- Api Interface ---
interface AdminApi {
    @GET("admin/stats") suspend fun getStats(): Response<Stats>
    @GET("categories") suspend fun getCategories(): Response<List<Category>>
    @POST("admin/categories") suspend fun createCategory(@Body body: Map<String, Any?>): Response<Category>
    @PUT("admin/categories/{id}") suspend fun updateCategory(@Path("id") id: Int, @Body body: Map<String, Any?>): Response<Category>
    @DELETE("admin/categories/{id}") suspend fun deleteCategory(@Path("id") id: Int): Response<OkResponse>
    @GET("admin/channels") suspend fun getAdminChannels(): Response<List<AdminChannel>>
    @GET("channels/{catId}") suspend fun getChannels(@Path("catId") catId: Int): Response<List<Any>>
    @POST("admin/channels") suspend fun createChannel(@Body body: Map<String, Any?>): Response<AdminChannel>
    @PUT("admin/channels/{id}") suspend fun updateChannel(@Path("id") id: Int, @Body body: Map<String, Any?>): Response<AdminChannel>
    @DELETE("admin/channels/{id}") suspend fun deleteChannel(@Path("id") id: Int): Response<OkResponse>
    @GET("admin/links/{chId}") suspend fun getLinks(@Path("chId") chId: Int): Response<List<StreamLink>>
    @POST("admin/links") suspend fun createLink(@Body body: Map<String, Any?>): Response<StreamLink>
    @PUT("admin/links/{id}") suspend fun updateLink(@Path("id") id: Int, @Body body: Map<String, Any?>): Response<StreamLink>
    @DELETE("admin/links/{id}") suspend fun deleteLink(@Path("id") id: Int): Response<OkResponse>
    @GET("live") suspend fun getLive(): Response<LiveResponse>
    @POST("admin/live-events") suspend fun createEvent(@Body body: Map<String, Any?>): Response<LiveEvent>
    @PUT("admin/live-events/{id}") suspend fun updateEvent(@Path("id") id: Int, @Body body: Map<String, Any?>): Response<LiveEvent>
    @DELETE("admin/live-events/{id}") suspend fun deleteEvent(@Path("id") id: Int): Response<OkResponse>
    @GET("admin/config") suspend fun getConfig(): Response<List<ConfigItem>>
    @PUT("admin/config") suspend fun upsertConfig(@Body body: Map<String, String>): Response<OkResponse>
    @DELETE("admin/config/{key}") suspend fun deleteConfig(@Path("key") key: String): Response<OkResponse>
    @GET("admin/analytics") suspend fun getAnalytics(): Response<List<AnalyticsEvent>>
    @POST("admin/check-health") suspend fun checkHealth(): Response<HealthResult>
    @POST("admin/import-m3u") suspend fun importM3u(@Body body: Map<String, Any?>): Response<M3uResult>
    @POST("admin/import-m3u-text") suspend fun importM3uText(@Body body: Map<String, Any?>): Response<M3uResult>
    @GET("admin/schedules") suspend fun getSchedules(): Response<List<Schedule>>
    @POST("admin/schedules") suspend fun createSchedule(@Body body: Map<String, Any?>): Response<Schedule>
    @PUT("admin/schedules/{id}") suspend fun updateSchedule(@Path("id") id: Int, @Body body: Map<String, Any?>): Response<Schedule>
    @DELETE("admin/schedules/{id}") suspend fun deleteSchedule(@Path("id") id: Int): Response<OkResponse>
    @POST("admin/schedules/{id}/run") suspend fun runSchedule(@Path("id") id: Int): Response<Any>
    @GET("admin/crash-logs") suspend fun getCrashLogs(@Query("limit") limit: Int = 50): Response<CrashResponse>
}

fun createAdminApi(serverUrl: String): AdminApi {
    val baseUrl = "${serverUrl.trimEnd('/')}/api/v1/"
    val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
    val client = OkHttpClient.Builder()
        .addInterceptor(logging).connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS).build()
    return Retrofit.Builder().baseUrl(baseUrl).client(client)
        .addConverterFactory(GsonConverterFactory.create()).build().create(AdminApi::class.java)
}

package com.streamapp.data.api

import com.google.gson.annotations.SerializedName

data class AppConfig(
    @SerializedName("app_name") val appName: String = "StreamApp",
    @SerializedName("app_version") val appVersion: String = "1.0.0",
    @SerializedName("min_version") val minVersion: String = "1.0.0",
    @SerializedName("api_base") val apiBase: String = "",
    val features: Map<String, Boolean>? = null,
    val theme: Map<String, String>? = null
)

data class Category(
    val id: Int,
    val name: String,
    val slug: String,
    @SerializedName("icon_url") val iconUrl: String? = null,
    @SerializedName("sort_order") val sortOrder: Int = 0,
    @SerializedName("channel_count") val channelCount: Int = 0,
    @SerializedName("badge_count") val badgeCount: Int = 0
)

data class Channel(
    val id: Int,
    @SerializedName("category_id") val categoryId: Int,
    val name: String,
    val logo: String? = null,
    @SerializedName("is_live") val isLive: Int = 0,
    val featured: Int = 0,
    @SerializedName("category_name") val categoryName: String? = null,
    @SerializedName("link_count") val linkCount: Int = 0
)

data class StreamLink(
    val id: Int,
    val label: String = "Auto",
    val url: String,
    val quality: String = "Auto",
    @SerializedName("is_healthy") val isHealthy: Int = 1,
    @SerializedName("last_checked") val lastChecked: String? = null
)

data class StreamResponse(
    val channel: Channel,
    val links: List<StreamLink>
)

data class LiveEvent(
    val id: Int,
    val title: String,
    @SerializedName("channel_id") val channelId: Int? = null,
    @SerializedName("starts_at") val startsAt: String? = null,
    @SerializedName("is_live") val isLive: Int = 0,
    @SerializedName("channel_name") val channelName: String? = null,
    @SerializedName("channel_logo") val channelLogo: String? = null
)

data class LiveResponse(
    val events: List<LiveEvent>,
    val liveChannels: List<Channel>
)

// Match data for home screen match cards
// Backend sends: id, league, leagueIcon, sport, time, live, home, away, homeFlag, awayFlag, women
data class SportsMatch(
    val id: String = "",
    @SerializedName("league") val title: String = "",
    @SerializedName("home") val team1Name: String = "",
    @SerializedName("away") val team2Name: String = "",
    @SerializedName("homeFlag") val team1Flag: String? = null,
    @SerializedName("awayFlag") val team2Flag: String? = null,
    @SerializedName("time") val startTime: String? = null,
    val live: Boolean = false,
    @SerializedName("channel_id") val channelId: Int? = null,
    @SerializedName("channel_name") val channelName: String? = null,
    @SerializedName("category_id") val categoryId: Int? = null
) {
    val status: String get() = if (live) "live" else "upcoming"
}

data class MatchesResponse(
    val recent: List<SportsMatch> = emptyList(),
    val live: List<SportsMatch> = emptyList(),
    val upcoming: List<SportsMatch> = emptyList()
)

data class AdminStats(
    val categories: Int = 0,
    val channels: Int = 0,
    @SerializedName("stream_links") val streamLinks: Int = 0,
    @SerializedName("healthy_links") val healthyLinks: Int = 0,
    @SerializedName("analytics_events") val analyticsEvents: Int = 0
)

data class AdminChannel(
    val id: Int,
    val name: String,
    @SerializedName("category_name") val categoryName: String? = null,
    @SerializedName("is_live") val isLive: Int = 0,
    @SerializedName("total_links") val totalLinks: Int = 0,
    @SerializedName("healthy_links") val healthyLinks: Int = 0
)

data class AnalyticsEvent(
    @SerializedName("event_name") val eventName: String,
    val count: Int
)

data class AnalyticsPayload(
    @SerializedName("event_name") val eventName: String,
    val params: Map<String, String> = emptyMap(),
    @SerializedName("device_id") val deviceId: String = "unknown"
)

data class CrashReport(
    val message: String? = null,
    val stack: String? = null,
    @SerializedName("device_id") val deviceId: String = "unknown"
)

data class ApiResponse<T>(
    val ok: Boolean = true,
    val data: T? = null,
    val error: String? = null
)

// Session tracking models
data class SessionStartRequest(
    @SerializedName("device_id") val deviceId: String,
    @SerializedName("device_type") val deviceType: String = "android",
    @SerializedName("channel_id") val channelId: Int? = null,
    @SerializedName("channel_name") val channelName: String? = null
)

data class SessionStartResponse(
    val ok: Boolean,
    @SerializedName("session_id") val sessionId: String? = null
)

data class SessionHeartbeatRequest(
    @SerializedName("session_id") val sessionId: String,
    @SerializedName("channel_id") val channelId: Int? = null
)

data class SessionEndRequest(
    @SerializedName("session_id") val sessionId: String
)

data class LegalResponse(
    val notice: String = "No notices at this time.",
    val copyright: String = "© 2024 StreamApp. All rights reserved."
)

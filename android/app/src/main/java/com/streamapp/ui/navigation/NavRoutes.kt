package com.streamapp.ui.navigation

object NavRoutes {
    const val HOME = "home"
    const val CATEGORIES = "categories"
    const val CHANNELS_TAB = "channels_tab"
    const val SEARCH = "search"
    const val CHANNELS = "channels/{categoryId}/{categoryName}"
    const val PLAYER = "player/{channelId}/{channelName}"

    fun channels(categoryId: Int, categoryName: String): String =
        "channels/$categoryId/${java.net.URLEncoder.encode(categoryName, "UTF-8")}"

    fun player(channelId: Int, channelName: String): String =
        "player/$channelId/${java.net.URLEncoder.encode(channelName, "UTF-8")}"

    val bottomNavTabs = listOf(HOME, CATEGORIES, CHANNELS_TAB, SEARCH)
}

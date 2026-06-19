package com.streamapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.streamapp.ui.screens.channels.AllChannelsScreen
import com.streamapp.ui.screens.channels.ChannelsScreen
import com.streamapp.ui.screens.home.HomeScreen
import com.streamapp.ui.screens.player.PlayerScreen
import com.streamapp.ui.screens.search.SearchScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    onMenuClick: () -> Unit = {}
) {
    NavHost(navController = navController, startDestination = NavRoutes.HOME) {
        composable(NavRoutes.HOME) {
            HomeScreen(
                onMenuClick = onMenuClick,
                onCategoryClick = { categoryId, categoryName ->
                    navController.navigate(NavRoutes.channels(categoryId, categoryName))
                },
                onChannelClick = { channelId, channelName ->
                    navController.navigate(NavRoutes.player(channelId, channelName))
                },
                onViewAllCategories = { navController.navigate(NavRoutes.CATEGORIES) }
            )
        }

        composable(NavRoutes.CATEGORIES) {
            ChannelsScreen(
                onCategoryClick = { categoryId, categoryName ->
                    navController.navigate(NavRoutes.channels(categoryId, categoryName))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.CHANNELS_TAB) {
            AllChannelsScreen(
                onChannelClick = { channelId, channelName ->
                    navController.navigate(NavRoutes.player(channelId, channelName))
                }
            )
        }

        composable(NavRoutes.SEARCH) {
            SearchScreen(
                onChannelClick = { channelId, channelName ->
                    navController.navigate(NavRoutes.player(channelId, channelName))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = NavRoutes.CHANNELS,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.IntType },
                navArgument("categoryName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getInt("categoryId") ?: return@composable
            val categoryName = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("categoryName") ?: "",
                "UTF-8"
            )
            ChannelsScreen(
                categoryId = categoryId,
                categoryName = categoryName,
                onChannelClick = { channelId, channelName ->
                    navController.navigate(NavRoutes.player(channelId, channelName))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = NavRoutes.PLAYER,
            arguments = listOf(
                navArgument("channelId") { type = NavType.IntType },
                navArgument("channelName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val channelId = backStackEntry.arguments?.getInt("channelId") ?: return@composable
            val channelName = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("channelName") ?: "",
                "UTF-8"
            )
            PlayerScreen(
                channelId = channelId,
                channelName = channelName,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

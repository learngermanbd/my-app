package com.streamapp.admin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.streamapp.admin.data.createAdminApi
import com.streamapp.admin.ui.theme.AdminTheme
import com.streamapp.admin.ui.screens.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("admin_prefs", MODE_PRIVATE)
        val savedUrl = prefs.getString("server_url", BuildConfig.DEFAULT_SERVER_URL) ?: BuildConfig.DEFAULT_SERVER_URL
        setContent {
            AdminTheme {
                AdminApp(prefs, savedUrl)
            }
        }
    }
}

@Composable
fun AdminApp(prefs: android.content.SharedPreferences, initialUrl: String) {
    var serverUrl by remember { mutableStateOf(initialUrl) }
    var api = remember(serverUrl) { createAdminApi(serverUrl) }
    var showSetup by remember { mutableStateOf(initialUrl.isEmpty() || initialUrl == BuildConfig.DEFAULT_SERVER_URL) }
    var currentTab by remember { mutableStateOf("dashboard") }

    if (showSetup) {
        ServerSetupScreen(
            initialUrl = serverUrl,
            onConnect = { url ->
                serverUrl = url.trimEnd('/')
                prefs.edit().putString("server_url", serverUrl).apply()
                api = createAdminApi(serverUrl)
                showSetup = false
                currentTab = "dashboard"
            }
        )
    } else {
        MainAdminScreen(
            api = api,
            serverUrl = serverUrl,
            currentTab = currentTab,
            onTabChange = { currentTab = it },
            onDisconnect = { showSetup = true },
            onServerUrlChange = { url ->
                serverUrl = url.trimEnd('/')
                prefs.edit().putString("server_url", serverUrl).apply()
                api = createAdminApi(serverUrl)
            }
        )
    }
}

package com.streamapp.admin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streamapp.admin.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ServerSetupScreen(initialUrl: String, onConnect: (String) -> Unit) {
    var url by remember { mutableStateOf(initialUrl) }
    var testing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize().background(Background), contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier.widthIn(max = 400.dp).padding(24.dp),
            colors = CardDefaults.cardColors(containerColor = Surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("StreamApp Admin", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Primary)
                Spacer(Modifier.height(4.dp))
                Text("Connect to your server", fontSize = 13.sp, color = TextSecondary)
                Spacer(Modifier.height(24.dp))
                OutlinedTextField(
                    value = url, onValueChange = { url = it; error = null },
                    label = { Text("Server URL") },
                    placeholder = { Text("http://192.168.0.191:3000") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary, unfocusedBorderColor = GlassBorder,
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                        cursorColor = Primary, focusedLabelColor = Primary
                    ),
                    enabled = !testing
                )
                if (error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(error!!, color = LiveRed, fontSize = 12.sp, textAlign = TextAlign.Center)
                }
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = {
                        testing = true; error = null
                        scope.launch {
                            try {
                                val api = com.streamapp.admin.data.createAdminApi(url.trimEnd('/'))
                                val res = api.getStats()
                                if (res.isSuccessful) onConnect(url.trimEnd('/'))
                                else error = "Server responded: ${res.code()}"
                            } catch (e: Exception) { error = "Connection failed: ${e.message}" }
                            finally { testing = false }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !testing && url.isNotBlank()
                ) {
                    if (testing) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Background)
                    else Text("Connect", color = Background, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

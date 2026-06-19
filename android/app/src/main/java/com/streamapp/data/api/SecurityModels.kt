package com.streamapp.data.api

import com.google.gson.annotations.SerializedName

data class SecurityCheckRequest(
    @SerializedName("package_name") val packageName: String,
    @SerializedName("cert_hash") val certHash: String,
    @SerializedName("version_name") val versionName: String,
    val signature: String? = null,
    val nonce: String
)

data class SecurityCheckResponse(
    val ok: Boolean = false,
    val blocked: Boolean = false,
    @SerializedName("security_enabled") val securityEnabled: Boolean = true,
    val message: String? = null,
    @SerializedName("download_url") val downloadUrl: String? = null
)

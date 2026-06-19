package com.streamapp.security

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.Signature
import java.security.MessageDigest

/**
 * Layer 2: Validates the app's signing certificate.
 * A modified APK will have a different signature.
 * Uses PackageManager to get the signing certs and computes SHA-256 hash.
 */
object SignatureValidator {

    fun getSigningCertHash(context: Context): String? {
        return try {
            val packageInfo = if (android.os.Build.VERSION.SDK_INT >= 28) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES
                )
            }

            if (android.os.Build.VERSION.SDK_INT >= 28) {
                val signatures = packageInfo.signingInfo?.apkContentsSigners
                if (signatures != null && signatures.isNotEmpty()) {
                    val digest = MessageDigest.getInstance("SHA-256")
                    digest.update(signatures[0].toByteArray())
                    digest.digest().joinToString("") { "%02x".format(it) }
                } else null
            } else {
                @Suppress("DEPRECATION")
                val signatures = packageInfo.signatures
                if (signatures != null && signatures.isNotEmpty()) {
                    val digest = MessageDigest.getInstance("SHA-256")
                    digest.update(signatures[0].toByteArray())
                    digest.digest().joinToString("") { "%02x".format(it) }
                } else null
            }
        } catch (_: Exception) {
            null
        }
    }

    fun verifySignature(context: Context, expectedHash: String): Boolean {
        val actual = getSigningCertHash(context) ?: return false
        return actual == expectedHash
    }
}

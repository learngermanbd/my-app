package com.streamapp.security

import android.content.Context
import android.content.pm.PackageManager
import java.security.MessageDigest

/**
 * Layer 1: Validates package identity and installation source.
 * If the app was side-loaded or repackaged, this catches it.
 */
object IdentityValidator {

    fun checkPackageName(context: Context): Boolean {
        return try {
            val pkg = context.packageName
            pkg == "com.streamapp"
        } catch (_: Exception) {
            false
        }
    }

    fun checkInstallSource(context: Context): Boolean {
        return try {
            val installer = context.packageManager.getInstallerPackageName(context.packageName)
            // null means side-loaded (adb install, unknown sources)
            installer != null || android.os.Build.VERSION.SDK_INT < 30
        } catch (_: Exception) {
            false
        }
    }
}

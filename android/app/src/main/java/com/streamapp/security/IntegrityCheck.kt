package com.streamapp.security

import android.content.Context
import android.content.pm.ApplicationInfo
import java.io.File

/**
 * Layer 3: Runtime integrity checks.
 * Detects debug mode, emulators, hooking frameworks, and tampered resources.
 * Each check is independent - even if one is bypassed, others still catch modifications.
 */
object RuntimeIntegrityCheck {

    fun checkDebuggable(context: Context): Boolean {
        return try {
            val flags = context.applicationInfo.flags
            (flags and ApplicationInfo.FLAG_DEBUGGABLE) == 0
        } catch (_: Exception) {
            true
        }
    }

    fun checkSignatureFiles(): Boolean {
        return try {
            val suspicious = listOf(
                "/system/lib/libfrida-gadget.so",
                "/system/lib64/libfrida-gadget.so",
                "/data/local/tmp/frida-server",
                "/system/bin/su",
                "/system/xbin/su",
                "/su/bin/su",
                "/sbin/su",
                "/system/app/Superuser.apk",
                "/system/app/Superuser.apk.bak",
                "/data/data/com.saurik.substrate",
                "/data/data/de.robv.android.xposed.installer",
                "/data/data/com.saurik.substrate/lib/SubstrateBootstrap.cs"
            )
            for (path in suspicious) {
                if (File(path).exists()) return false
            }
            true
        } catch (_: Exception) {
            true
        }
    }

    fun checkApkSize(context: Context): Boolean {
        return try {
            val ai = context.applicationInfo
            val apkFile = File(ai.sourceDir)
            if (!apkFile.exists()) return false
            val size = apkFile.length()
            // Original APK is typically between 5MB and 100MB
            size > 1_000_000 && size < 200_000_000
        } catch (_: Exception) {
            true
        }
    }

    fun checkResources(context: Context): Boolean {
        return try {
            val res = context.resources
            val appName = res.getString(context.resources.getIdentifier("app_name", "string", context.packageName))
            appName.contains("StreamApp") || appName.contains("Sportzfy")
        } catch (_: Exception) {
            true
        }
    }
}

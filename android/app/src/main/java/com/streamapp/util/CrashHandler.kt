package com.streamapp.util

import android.os.Process
import com.streamapp.data.repository.StreamRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.PrintWriter
import java.io.StringWriter

class CrashHandler(
    private val repository: StreamRepository?
) : Thread.UncaughtExceptionHandler {

    private val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        // Report crash to backend
        scope.launch {
            try {
                val sw = StringWriter()
                val pw = PrintWriter(sw)
                throwable.printStackTrace(pw)
                pw.flush()
                val stackTrace = sw.toString()

                repository?.reportCrash(
                    message = throwable.message ?: "Unknown error",
                    stack = stackTrace.substring(0, minOf(stackTrace.length, 5000)),
                    deviceId = android.provider.Settings.Secure.ANDROID_ID
                )
            } catch (_: Exception) {
                // Don't crash inside the crash handler
            }
        }

        // Let the previous handler handle it (shows the force close dialog)
        previousHandler?.uncaughtException(thread, throwable)
            ?: Process.killProcess(Process.myPid())
    }
}

package com.streamapp

import android.app.Application
import com.streamapp.data.repository.StreamRepository
import com.streamapp.util.CrashHandler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class StreamApp : Application() {

    @Inject
    lateinit var repository: StreamRepository

    override fun onCreate() {
        super.onCreate()
        // Register global crash handler to send crashes to backend
        Thread.setDefaultUncaughtExceptionHandler(CrashHandler(repository))
    }
}

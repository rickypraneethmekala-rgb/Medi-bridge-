package com.example

import android.app.Application
import com.example.core.tts.MultilingualTtsManager
import com.example.data.local.MediBridgeDatabase

class MediBridgeApp : Application() {
    lateinit var database: MediBridgeDatabase
        private set
    lateinit var ttsManager: MultilingualTtsManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = MediBridgeDatabase.getDatabase(this)
        ttsManager = MultilingualTtsManager(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        ttsManager.shutdown()
    }

    companion object {
        lateinit var instance: MediBridgeApp
            private set
    }
}

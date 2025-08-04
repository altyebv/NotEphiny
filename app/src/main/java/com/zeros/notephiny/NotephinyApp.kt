package com.zeros.notephiny

import android.app.Application
import com.zeros.notephiny.ai.embedder.OnnxEmbedder
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NotephinyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        OnnxEmbedder.initialize(this)
    }

    override fun onTerminate() {
        OnnxEmbedder.close()
        super.onTerminate()
    }
}

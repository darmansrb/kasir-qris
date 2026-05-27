package com.moluccasdev.poskasirqris

import android.app.Application
import com.moluccasdev.poskasirqris.data.AppContainer
import com.moluccasdev.poskasirqris.data.AppContainerImpl

class POSApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainerImpl(this)
    }
}

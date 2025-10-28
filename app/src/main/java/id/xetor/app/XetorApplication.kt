package id.xetor.app

import android.app.Application
import id.xetor.app.di.AppContainer

class XetorApplication : Application() {

    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }
}
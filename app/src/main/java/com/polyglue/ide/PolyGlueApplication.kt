package com.polyglue.ide

import android.app.Application
import com.polyglue.ide.core.engine.NativeEngine
import com.polyglue.ide.data.local.PolyGlueDatabase
import com.polyglue.ide.di.AppModule
import com.polyglue.ide.di.AppViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class PolyGlueApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    lateinit var engine: NativeEngine
        private set

    val database by lazy { PolyGlueDatabase.getDatabase(this) }
    val appModule by lazy { AppModule(this, database, applicationScope) }
    val viewModelFactory by lazy { AppViewModelFactory(appModule, engine) }

    companion object {
        lateinit var instance: PolyGlueApplication
            private set

        fun getContext() = instance.applicationContext
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        engine = NativeEngine()
        engine.initialize(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        engine.shutdown()
    }
}

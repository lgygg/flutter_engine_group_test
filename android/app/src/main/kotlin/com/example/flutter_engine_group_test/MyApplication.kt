package com.example.flutter_engine_group_test

import android.app.Application
import io.flutter.embedding.engine.FlutterEngineGroup

class MyApplication : Application() {
    lateinit var engineGroup: FlutterEngineGroup // Create a FlutterEngineGroup whose child engines will share resources

    override fun onCreate() {
        super.onCreate()
        engineGroup = FlutterEngineGroup(this)
    }
}
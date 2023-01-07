package com.example.flutter_engine_group_test

import android.os.Build
import androidx.annotation.RequiresApi
import java.lang.ref.WeakReference

// A singleton/observable data model for the data shared between Flutter and the host platform.
object DataModel {
    private val observers: MutableList<WeakReference<(Int) -> Unit>> = mutableListOf()

    var data = 0
        set(value) {
            field = value
            observers.mapNotNull { it.get() }.forEach { it.invoke(value) }
        }

    fun addObserver(observer: (Int) -> Unit) = observers.add(WeakReference(observer))

    @RequiresApi(Build.VERSION_CODES.N)
    fun removeObserver(observer: (Int) -> Unit): Boolean = observers.removeIf {
        if (it.get() != null) it.get() == observer else true
    }
}
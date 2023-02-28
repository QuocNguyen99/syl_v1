package com.hqnguyen.syl

import android.app.Application
import com.hqnguyen.syl.utils.TimberLog
import timber.log.Timber

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(TimberLog())
    }
}
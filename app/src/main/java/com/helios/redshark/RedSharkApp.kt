package com.helios.redshark

// File nay giu logic chinh cua thanh phan nay trong ung dung.

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

// Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
@HiltAndroidApp
class RedSharkApp : Application() {
    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}

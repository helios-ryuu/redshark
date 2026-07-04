package com.helios.redshark.core

// File nay chua tien ich va ha tang dung chung trong ung dung.

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

// Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
@Singleton
class NetworkChecker @Inject constructor(
    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    @ApplicationContext private val context: Context
) {
    fun isOnline(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        // VALIDATED can be flaky on emulators; INTERNET is sufficient for Firestore.
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}

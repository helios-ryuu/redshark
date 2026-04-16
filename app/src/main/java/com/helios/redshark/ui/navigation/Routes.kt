package com.helios.redshark.ui.navigation

object Routes {
    const val AUTH_GOOGLE = "auth/google"
    const val PROFILE_SETUP = "profile/setup"
    const val PROFILE_VIEW = "profile/{userId}"
    const val PROFILE_EDIT = "profile/edit"
    const val HOME = "home"
    const val SETTINGS = "settings"

    fun profileView(userId: String) = "profile/$userId"
}

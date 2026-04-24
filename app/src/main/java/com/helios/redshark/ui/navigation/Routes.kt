package com.helios.redshark.ui.navigation

import android.net.Uri

object Routes {
    const val AUTH_GOOGLE = "auth/google"
    const val PROFILE_SETUP = "profile/setup"
    const val PROFILE_VIEW = "profile/{userId}"
    const val PROFILE_EDIT = "profile/edit"
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val NOTIFICATIONS = "notifications"
    const val MESSAGES = "messages"
    const val CONVERSATION = "conversation/{id}?title={title}"
    const val CONVERSATION_NEW = "conversation/new?peerId={peerId}"

    const val IDEA_DETAIL = "ideas/{ideaId}"
    const val IDEA_CREATE = "ideas/create"
    const val IDEA_EDIT = "ideas/{ideaId}/edit"
    const val ISSUE_CREATE = "ideas/{ideaId}/issues/create"
    const val ISSUE_DETAIL = "issues/{issueId}"
    const val ISSUE_EDIT = "issues/{issueId}/edit"

    fun profileView(userId: String) = "profile/$userId"
    fun ideaDetail(ideaId: String) = "ideas/$ideaId"
    fun ideaEdit(ideaId: String) = "ideas/$ideaId/edit"
    fun issueCreate(ideaId: String) = "ideas/$ideaId/issues/create"
    fun issueDetail(issueId: String) = "issues/$issueId"
    fun issueEdit(issueId: String) = "issues/$issueId/edit"
    fun conversation(id: String, title: String? = null): String {
        val encodedTitle = title?.takeIf { it.isNotBlank() }?.let(Uri::encode)
        return if (encodedTitle == null) {
            "conversation/$id"
        } else {
            "conversation/$id?title=$encodedTitle"
        }
    }
    fun conversationNew(peerId: String) = "conversation/new?peerId=$peerId"
}

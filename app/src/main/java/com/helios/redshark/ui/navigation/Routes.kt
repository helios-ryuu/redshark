package com.helios.redshark.ui.navigation

object Routes {
    const val AUTH_GOOGLE = "auth/google"
    const val PROFILE_SETUP = "profile/setup"
    const val PROFILE_VIEW = "profile/{userId}"
    const val PROFILE_EDIT = "profile/edit"
    const val HOME = "home"
    const val SETTINGS = "settings"

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
}

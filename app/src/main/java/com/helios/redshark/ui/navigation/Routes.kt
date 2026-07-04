package com.helios.redshark.ui.navigation

// File nay dieu phoi route va man hinh trong ung dung mot Activity.

object Routes {
    const val AUTH_GATE = "auth/gate"
    const val AUTH_GOOGLE = "auth/google"
    const val REGISTER = "auth/register"
    const val PROFILE_SETUP = "profile/setup"
    const val PROFILE_VIEW = "profile/{userId}"
    const val PROFILE_EDIT = "profile/edit"
    const val HOME = "home"
    const val SETTINGS = "settings"

    const val IDEA_DETAIL = "ideas/{ideaId}"
    const val IDEA_DEEP_LINK = "redshark://idea/{ideaId}"
    const val IDEA_CREATE = "ideas/create"
    const val IDEA_EDIT = "ideas/{ideaId}/edit"
    const val IDEA_COMMENTS = "ideas/{ideaId}/comments"
    const val ISSUE_CREATE = "ideas/{ideaId}/issues/create"
    const val ISSUE_DETAIL = "issues/{issueId}"
    const val ISSUE_EDIT = "issues/{issueId}/edit"

    const val CONVERSATION = "conversation/{conversationId}"
    const val CONVERSATION_NEW = "conversation/new?peerId={peerId}"

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    fun profileView(userId: String) = "profile/$userId"
    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    fun ideaDetail(ideaId: String) = "ideas/$ideaId"
    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    fun ideaDeepLink(ideaId: String) = "redshark://idea/$ideaId"
    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    fun ideaEdit(ideaId: String) = "ideas/$ideaId/edit"
    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    fun ideaComments(ideaId: String) = "ideas/$ideaId/comments"
    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    fun issueCreate(ideaId: String) = "ideas/$ideaId/issues/create"
    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    fun issueDetail(issueId: String) = "issues/$issueId"
    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    fun issueEdit(issueId: String) = "issues/$issueId/edit"
    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    fun conversation(conversationId: String) = "conversation/$conversationId"
    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    fun conversationNew(peerId: String) = "conversation/new?peerId=$peerId"
}

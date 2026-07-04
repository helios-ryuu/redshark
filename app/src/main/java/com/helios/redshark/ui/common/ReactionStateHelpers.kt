package com.helios.redshark.ui.common

// File nay chua composable dung lai de cac man hinh khong lap boilerplate.

import com.helios.redshark.domain.model.IdeaReaction
import java.util.UUID

// Model du lieu nay giu cac truong can truyen giua cac lop.
data class ReactionUpdate(
    val nextReaction: IdeaReaction,
    val deltaChange: Int,
)

// Model du lieu nay giu cac truong can truyen giua cac lop.
data class ReactionUiMaps(
    val reactionStates: Map<UUID, IdeaReaction>,
    val upvoteDeltas: Map<UUID, Int>,
)

// Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
fun nextUpvoteUpdate(currentReaction: IdeaReaction): ReactionUpdate {
    val nextReaction = when (currentReaction) {
        IdeaReaction.UPVOTED -> IdeaReaction.NONE
        IdeaReaction.DOWNVOTED, IdeaReaction.NONE -> IdeaReaction.UPVOTED
    }
    val deltaChange = when (currentReaction) {
        IdeaReaction.UPVOTED -> -1
        IdeaReaction.DOWNVOTED, IdeaReaction.NONE -> 1
    }
    return ReactionUpdate(nextReaction, deltaChange)
}

// Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
fun nextDownvoteUpdate(currentReaction: IdeaReaction): ReactionUpdate {
    val nextReaction = when (currentReaction) {
        IdeaReaction.DOWNVOTED -> IdeaReaction.NONE
        IdeaReaction.UPVOTED, IdeaReaction.NONE -> IdeaReaction.DOWNVOTED
    }
    val deltaChange = when (currentReaction) {
        IdeaReaction.UPVOTED -> -1
        IdeaReaction.DOWNVOTED, IdeaReaction.NONE -> 0
    }
    return ReactionUpdate(nextReaction, deltaChange)
}

// Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
fun applyReactionUpdate(
    reactionStates: Map<UUID, IdeaReaction>,
    upvoteDeltas: Map<UUID, Int>,
    ideaId: UUID,
    update: ReactionUpdate,
): ReactionUiMaps {
    val updatedReactions = reactionStates.toMutableMap()
    val updatedDeltas = upvoteDeltas.toMutableMap()
    val currentDelta = updatedDeltas[ideaId] ?: 0
    val newDelta = currentDelta + update.deltaChange

    if (update.nextReaction == IdeaReaction.NONE) updatedReactions.remove(ideaId)
    else updatedReactions[ideaId] = update.nextReaction

    if (newDelta == 0) updatedDeltas.remove(ideaId)
    else updatedDeltas[ideaId] = newDelta

    return ReactionUiMaps(updatedReactions, updatedDeltas)
}

// Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
fun observedReactionState(
    reactionStates: Map<UUID, IdeaReaction>,
    ideaId: UUID,
    reaction: IdeaReaction,
): Map<UUID, IdeaReaction> =
    reactionStates.toMutableMap().apply {
        if (reaction == IdeaReaction.NONE) remove(ideaId) else put(ideaId, reaction)
    }

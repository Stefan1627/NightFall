package com.nightfall.domain.usecase.game

import com.nightfall.core.result.Result
import com.nightfall.domain.model.Vote
import com.nightfall.domain.repo.GameRepository
import javax.inject.Inject

class SubmitVoteUseCase @Inject constructor(
    private val gameRepository: GameRepository
) {
    suspend operator fun invoke(
        gameId: String,
        vote: Vote,
        currentPhase: String,
        existingVotes: List<Vote>
    ): Result<Unit> {
        if (currentPhase != "voting") {
            return Result.Error(IllegalStateException("Voting is not active"))
        }
        if (existingVotes.any { it.voterId == vote.voterId }) {
            return Result.Error(IllegalStateException("You have already voted"))
        }
        if (vote.voterId == vote.targetId) {
            return Result.Error(IllegalStateException("You cannot vote for yourself"))
        }
        return gameRepository.submitVote(gameId, vote)
    }
}
package com.nightfall.engine

import com.nightfall.domain.model.Player
import com.nightfall.domain.model.Vote
import javax.inject.Inject

class VoteManager @Inject constructor() {

    /**
     * Tallies votes and returns the targetId with the most votes.
     * Returns null on a tie (no elimination).
     */
    fun tally(votes: List<Vote>): String? {
        if (votes.isEmpty()) return null

        val voteCounts = votes.groupingBy { it.targetId }.eachCount()
        val maxVotes = voteCounts.values.maxOrNull() ?: return null
        val topTargets = voteCounts.filter { it.value == maxVotes }

        return if (topTargets.size == 1) {
            topTargets.keys.first()
        } else {
            null // Tie — no elimination
        }
    }

    /**
     * Checks if there is a tie in the vote results.
     */
    fun isTie(votes: List<Vote>): Boolean {
        if (votes.isEmpty()) return false

        val voteCounts = votes.groupingBy { it.targetId }.eachCount()
        val maxVotes = voteCounts.values.maxOrNull() ?: return false
        return voteCounts.count { it.value == maxVotes } > 1
    }

    /**
     * Checks if all alive players have voted.
     */
    fun hasAllVoted(votes: List<Vote>, alivePlayers: List<Player>): Boolean {
        val voterIds = votes.map { it.voterId }.toSet()
        return alivePlayers.all { it.playerId in voterIds }
    }
}
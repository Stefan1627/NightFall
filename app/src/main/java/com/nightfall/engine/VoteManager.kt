package com.nightfall.engine

import com.nightfall.domain.model.Player
import com.nightfall.domain.model.Vote

class VoteManager {

    fun tally(votes: List<Vote>): String? {
        if (votes.isEmpty()) return null
        val counts = votes.groupingBy { it.targetId }.eachCount()
        val maxCount = counts.values.max()
        val topTargets = counts.filter { it.value == maxCount }
        return if (topTargets.size == 1) topTargets.keys.first() else null
    }

    fun isTie(votes: List<Vote>): Boolean = votes.isNotEmpty() && tally(votes) == null

    fun hasAllVoted(votes: List<Vote>, alivePlayers: List<Player>): Boolean {
        val voterIds = votes.map { it.voterId }.toSet()
        val alivePlayerIds = alivePlayers.filter { it.isAlive }.map { it.playerId }.toSet()
        return alivePlayerIds.isNotEmpty() && alivePlayerIds.all { it in voterIds }
    }
}

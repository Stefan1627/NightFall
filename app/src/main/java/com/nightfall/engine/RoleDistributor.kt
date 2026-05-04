package com.nightfall.engine

import com.nightfall.domain.model.Player

class RoleDistributor {

    fun distribute(players: List<Player>, gameMode: String): Map<String, String> {
        val shuffled = players.shuffled()
        val mafiaCount = shuffled.size / 3
        return shuffled.mapIndexed { index, player ->
            player.playerId to when {
                index < mafiaCount -> "mafia"
                index == mafiaCount -> "detective"
                index == mafiaCount + 1 -> "doctor"
                else -> "villager"
            }
        }.toMap()
    }
}

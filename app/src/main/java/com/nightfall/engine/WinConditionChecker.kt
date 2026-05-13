package com.nightfall.engine

import com.nightfall.domain.model.Player
import javax.inject.Inject

sealed class GameOutcome {
    data class Winner(val faction: String) : GameOutcome()
    object NoWinner : GameOutcome()
}

class WinConditionChecker @Inject constructor() {

    /**
     * Checks win conditions based on alive players.
     * - Village wins if mafiaAlive == 0
     * - Mafia wins if mafiaAlive >= villagersAlive
     */
    fun check(players: List<Player>): GameOutcome {
        val alivePlayers = players.filter { it.isAlive }
        val mafiaAlive = alivePlayers.count { it.role == "mafia" }
        val villagersAlive = alivePlayers.count { it.role != "mafia" }

        return when {
            mafiaAlive == 0 -> GameOutcome.Winner("village")
            mafiaAlive >= villagersAlive -> GameOutcome.Winner("mafia")
            else -> GameOutcome.NoWinner
        }
    }
}
package com.nightfall.engine

import com.nightfall.domain.model.Player
import com.nightfall.roles.RoleRegistry

sealed class GameOutcome {
    data class Winner(val faction: String) : GameOutcome()
    object NoWinner : GameOutcome()
}

class WinConditionChecker {

    fun check(players: List<Player>): GameOutcome {
        val alive = players.filter { it.isAlive }
        val mafiaAlive = alive.count { RoleRegistry.roles[it.role]?.faction == "mafia" }
        val villagersAlive = alive.size - mafiaAlive
        return when {
            mafiaAlive == 0 -> GameOutcome.Winner("village")
            mafiaAlive >= villagersAlive -> GameOutcome.Winner("mafia")
            else -> GameOutcome.NoWinner
        }
    }
}

package com.nightfall.domain.usecase.game

import com.nightfall.domain.model.Player
import javax.inject.Inject

class CheckWinConditionUseCase @Inject constructor() {
    /**
     * Checks win conditions based on alive players.
     * @return "mafia" if mafia wins, "village" if village wins, null if no winner yet
     */
    operator fun invoke(players: List<Player>): String? {
        val alivePlayers = players.filter { it.isAlive }
        val mafiaAlive = alivePlayers.count { it.role == "mafia" }
        val villagerAlive = alivePlayers.count { it.role != "mafia" }

        return when {
            mafiaAlive == 0 -> "village"
            mafiaAlive >= villagerAlive -> "mafia"
            else -> null
        }
    }
}
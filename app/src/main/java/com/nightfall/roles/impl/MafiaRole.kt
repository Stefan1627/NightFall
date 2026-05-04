package com.nightfall.roles.impl

import com.nightfall.domain.model.GameState
import com.nightfall.domain.model.Player
import com.nightfall.roles.RoleDefinition

class MafiaRole : RoleDefinition {
    override val roleId = "mafia"
    override val displayName = "Mafia"
    override val faction = "mafia"
    override val hasNightAction = true

    override fun applyAbility(actor: Player, target: Player, state: GameState): GameState {
        if (target.isProtected) return state
        val updatedPlayers = state.players.map { player ->
            if (player.playerId == target.playerId) player.copy(isAlive = false) else player
        }
        return state.copy(players = updatedPlayers)
    }

    override fun getRoleInfo(): String = "Eliminate villagers each night to take control."
}

package com.nightfall.roles.impl

import com.nightfall.domain.model.GameState
import com.nightfall.domain.model.Player
import com.nightfall.roles.RoleDefinition

class DoctorRole : RoleDefinition {
    override val roleId = "doctor"
    override val displayName = "Doctor"
    override val faction = "village"
    override val hasNightAction = true

    override fun applyAbility(actor: Player, target: Player, state: GameState): GameState {
        val updatedPlayers = state.players.map { player ->
            if (player.playerId == target.playerId) player.copy(isProtected = true) else player
        }
        return state.copy(players = updatedPlayers)
    }

    override fun getRoleInfo(): String = "Protect one player each night from elimination."
}

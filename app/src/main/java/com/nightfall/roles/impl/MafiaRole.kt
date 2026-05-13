package com.nightfall.roles.impl

import com.nightfall.domain.model.GameState
import com.nightfall.domain.model.Player
import com.nightfall.roles.RoleDefinition

class MafiaRole : RoleDefinition {
    override val roleId: String = "mafia"
    override val displayName: String = "Mafia"
    override val faction: String = "mafia"
    override val hasNightAction: Boolean = true

    override fun applyAbility(actor: Player, target: Player, state: GameState): GameState {
        // Mafia eliminates target — target's isAlive becomes false
        // The actual update to the player list is handled by NightPhaseProcessor
        return state
    }

    override fun getRoleInfo(): String {
        return "You are a Mafia member. During the night, choose a player to eliminate. " +
                "Work with your fellow Mafia to avoid detection!"
    }
}
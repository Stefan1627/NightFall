package com.nightfall.roles.impl

import com.nightfall.domain.model.GameState
import com.nightfall.domain.model.Player
import com.nightfall.roles.RoleDefinition

class DoctorRole : RoleDefinition {
    override val roleId: String = "doctor"
    override val displayName: String = "Doctor"
    override val faction: String = "village"
    override val hasNightAction: Boolean = true

    override fun applyAbility(actor: Player, target: Player, state: GameState): GameState {
        // Doctor protects target — if Mafia targets the same player, elimination is canceled
        // The protection logic is handled by NightPhaseProcessor
        return state
    }

    override fun getRoleInfo(): String {
        return "You are a Doctor. During the night, choose a player to protect. " +
                "If the Mafia targets the same player, their elimination will be prevented!"
    }
}
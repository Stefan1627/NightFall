package com.nightfall.roles.impl

import com.nightfall.domain.model.GameState
import com.nightfall.domain.model.Player
import com.nightfall.roles.RoleDefinition

class DetectiveRole : RoleDefinition {
    override val roleId: String = "detective"
    override val displayName: String = "Detective"
    override val faction: String = "village"
    override val hasNightAction: Boolean = true

    override fun applyAbility(actor: Player, target: Player, state: GameState): GameState {
        // Detective investigates target — reveals target's faction
        // The result is stored in the NightAction.abilityType field
        // Does NOT eliminate the target
        return state
    }

    override fun getRoleInfo(): String {
        return "You are a Detective. During the night, choose a player to investigate. " +
                "You will learn whether they belong to the Mafia or the Village."
    }
}
package com.nightfall.roles.impl

import com.nightfall.domain.model.GameState
import com.nightfall.domain.model.Player
import com.nightfall.roles.RoleDefinition

class VillagerRole : RoleDefinition {
    override val roleId: String = "villager"
    override val displayName: String = "Villager"
    override val faction: String = "village"
    override val hasNightAction: Boolean = false

    override fun applyAbility(actor: Player, target: Player, state: GameState): GameState {
        // Villager has no night action — returns state unchanged
        return state
    }

    override fun getRoleInfo(): String {
        return "You are a Villager. You have no special abilities. " +
                "Use your wits during the day to find the Mafia and vote them out!"
    }
}
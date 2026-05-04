package com.nightfall.roles.impl

import com.nightfall.domain.model.GameState
import com.nightfall.domain.model.Player
import com.nightfall.roles.RoleDefinition

class VillagerRole : RoleDefinition {
    override val roleId = "villager"
    override val displayName = "Villager"
    override val faction = "village"
    override val hasNightAction = false

    override fun applyAbility(actor: Player, target: Player, state: GameState): GameState = state

    override fun getRoleInfo(): String = "Find and eliminate the mafia before they outnumber you."
}

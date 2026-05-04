package com.nightfall.roles.impl

import com.nightfall.domain.model.GameState
import com.nightfall.domain.model.NightAction
import com.nightfall.domain.model.Player
import com.nightfall.roles.RoleDefinition
import com.nightfall.roles.RoleRegistry

class DetectiveRole : RoleDefinition {
    override val roleId = "detective"
    override val displayName = "Detective"
    override val faction = "village"
    override val hasNightAction = true

    override fun applyAbility(actor: Player, target: Player, state: GameState): GameState {
        val targetFaction = RoleRegistry.roles[target.role]?.faction ?: "unknown"
        val action = NightAction(
            actorId = actor.playerId,
            targetId = target.playerId,
            abilityType = targetFaction
        )
        return state.copy(nightActions = state.nightActions + action)
    }

    override fun getRoleInfo(): String = "Investigate a player each night to learn their faction."
}

package com.nightfall.roles

import com.nightfall.domain.model.GameState
import com.nightfall.domain.model.Player

/**
 * RoleEngine applies abilities sequentially with priority ordering:
 * 1. Doctor protects first
 * 2. Mafia eliminates second
 * 3. Detective investigates third
 */
object RoleEngine {
    fun applyAbility(
        role: RoleDefinition,
        actor: Player,
        target: Player,
        state: GameState
    ): GameState {
        return role.applyAbility(actor, target, state)
    }
}
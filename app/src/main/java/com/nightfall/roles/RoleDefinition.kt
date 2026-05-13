package com.nightfall.roles

import com.nightfall.domain.model.GameState
import com.nightfall.domain.model.Player

interface RoleDefinition {
    val roleId: String
    val displayName: String
    val faction: String        // "village" or "mafia"
    val hasNightAction: Boolean
    fun applyAbility(actor: Player, target: Player, state: GameState): GameState
    fun getRoleInfo(): String  // description shown to player
}
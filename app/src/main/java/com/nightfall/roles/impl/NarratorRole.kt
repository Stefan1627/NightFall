package com.nightfall.roles.impl

import com.nightfall.domain.model.GameState
import com.nightfall.domain.model.Player
import com.nightfall.roles.RoleDefinition

/**
 * Narrator is a special system role (not assigned to players).
 * Reserved for future use — system messages or game narration.
 */
class NarratorRole : RoleDefinition {
    override val roleId: String = "narrator"
    override val displayName: String = "Narrator"
    override val faction: String = "neutral"
    override val hasNightAction: Boolean = false

    override fun applyAbility(actor: Player, target: Player, state: GameState): GameState {
        return state
    }

    override fun getRoleInfo(): String {
        return "The Narrator oversees the game and guides the story."
    }
}
package com.nightfall.roles

import com.nightfall.domain.model.GamePhase
import com.nightfall.domain.model.GameState
import com.nightfall.domain.model.Player
import com.nightfall.roles.impl.MafiaRole
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MafiaRoleTest {

    private val mafiaRole = MafiaRole()

    @Test
    fun `applyAbility sets target isAlive to false`() {
        val actor = Player("m1", "Mafioso", role = "mafia")
        val target = Player("v1", "Villager", role = "villager")
        val state = GameState(players = listOf(actor, target), phase = GamePhase.NIGHT)
        val result = mafiaRole.applyAbility(actor, target, state)
        val updatedTarget = result.players.find { it.playerId == "v1" }!!
        assertFalse(updatedTarget.isAlive)
    }

    @Test
    fun `applyAbility does not eliminate a protected target`() {
        val actor = Player("m1", "Mafioso", role = "mafia")
        val target = Player("v1", "Villager", role = "villager", isProtected = true)
        val state = GameState(players = listOf(actor, target), phase = GamePhase.NIGHT)
        val result = mafiaRole.applyAbility(actor, target, state)
        val updatedTarget = result.players.find { it.playerId == "v1" }!!
        assertTrue(updatedTarget.isAlive)
    }
}

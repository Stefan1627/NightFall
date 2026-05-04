package com.nightfall.engine

import com.nightfall.domain.model.Player
import com.nightfall.util.Constants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoleDistributorTest {

    private val distributor = RoleDistributor()

    @Test
    fun `for 6 players assigns 2 mafia 1 detective 1 doctor 2 villagers`() {
        val players = (1..6).map { Player("p$it", "Player $it") }
        val distribution = distributor.distribute(players, Constants.GAME_MODE_CLASSIC)
        assertEquals(6, distribution.size)
        assertEquals(2, distribution.values.count { it == "mafia" })
        assertEquals(1, distribution.values.count { it == "detective" })
        assertEquals(1, distribution.values.count { it == "doctor" })
        assertEquals(2, distribution.values.count { it == "villager" })
    }

    @Test
    fun `all players receive a role`() {
        val players = (1..9).map { Player("p$it", "Player $it") }
        val distribution = distributor.distribute(players, Constants.GAME_MODE_CLASSIC)
        assertEquals(players.size, distribution.size)
        assertTrue(distribution.values.all { it.isNotEmpty() })
    }

    @Test
    fun `for 9 players assigns 3 mafia 1 detective 1 doctor 4 villagers`() {
        val players = (1..9).map { Player("p$it", "Player $it") }
        val distribution = distributor.distribute(players, Constants.GAME_MODE_CLASSIC)
        assertEquals(3, distribution.values.count { it == "mafia" })
        assertEquals(1, distribution.values.count { it == "detective" })
        assertEquals(1, distribution.values.count { it == "doctor" })
        assertEquals(4, distribution.values.count { it == "villager" })
    }
}

package com.nightfall.engine

import com.nightfall.domain.model.Player
import org.junit.Assert.assertTrue
import org.junit.Test

class WinConditionCheckerTest {

    private val checker = WinConditionChecker()

    @Test
    fun `village wins when all mafia are dead`() {
        val players = listOf(
            Player("v1", "Alice", isAlive = true, role = "villager"),
            Player("v2", "Bob", isAlive = true, role = "villager"),
            Player("m1", "Carol", isAlive = false, role = "mafia")
        )
        val outcome = checker.check(players)
        assertTrue(outcome is GameOutcome.Winner && outcome.faction == "village")
    }

    @Test
    fun `mafia wins when mafia outnumber villagers`() {
        val players = listOf(
            Player("v1", "Alice", isAlive = true, role = "villager"),
            Player("m1", "Bob", isAlive = true, role = "mafia"),
            Player("m2", "Carol", isAlive = true, role = "mafia")
        )
        val outcome = checker.check(players)
        assertTrue(outcome is GameOutcome.Winner && outcome.faction == "mafia")
    }

    @Test
    fun `mafia wins when mafia equal villagers`() {
        val players = listOf(
            Player("v1", "Alice", isAlive = true, role = "villager"),
            Player("m1", "Bob", isAlive = true, role = "mafia")
        )
        val outcome = checker.check(players)
        assertTrue(outcome is GameOutcome.Winner && outcome.faction == "mafia")
    }

    @Test
    fun `no winner when mafia are outnumbered by villagers`() {
        val players = listOf(
            Player("v1", "Alice", isAlive = true, role = "villager"),
            Player("v2", "Bob", isAlive = true, role = "villager"),
            Player("m1", "Carol", isAlive = true, role = "mafia")
        )
        assertTrue(checker.check(players) is GameOutcome.NoWinner)
    }
}

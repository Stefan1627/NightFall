package com.nightfall.util

import com.nightfall.domain.model.Player
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExtensionsTest {

    // region isValidEmail

    @Test
    fun `isValidEmail returns true for standard valid email`() {
        assertTrue("user@example.com".isValidEmail())
    }

    @Test
    fun `isValidEmail returns true for email with dots and plus`() {
        assertTrue("first.last+tag@sub.domain.org".isValidEmail())
    }

    @Test
    fun `isValidEmail returns false for empty string`() {
        assertFalse("".isValidEmail())
    }

    @Test
    fun `isValidEmail returns false for missing at sign`() {
        assertFalse("userexample.com".isValidEmail())
    }

    @Test
    fun `isValidEmail returns false for missing domain`() {
        assertFalse("user@".isValidEmail())
    }

    @Test
    fun `isValidEmail returns false for missing tld`() {
        assertFalse("user@domain".isValidEmail())
    }

    // endregion

    // region aliveCount

    @Test
    fun `aliveCount returns correct count for mixed alive and dead players`() {
        val players = mapOf(
            "1" to Player("1", "Alice", isAlive = true),
            "2" to Player("2", "Bob", isAlive = false),
            "3" to Player("3", "Carol", isAlive = true)
        )
        assertEquals(2, players.aliveCount())
    }

    @Test
    fun `aliveCount returns zero when all players are dead`() {
        val players = mapOf(
            "1" to Player("1", "Alice", isAlive = false),
            "2" to Player("2", "Bob", isAlive = false)
        )
        assertEquals(0, players.aliveCount())
    }

    @Test
    fun `aliveCount returns zero for empty map`() {
        assertEquals(0, emptyMap<String, Player>().aliveCount())
    }

    // endregion

    // region mafiaCount

    @Test
    fun `mafiaCount returns correct count for mixed roles`() {
        val players = mapOf(
            "1" to Player("1", "Alice", role = "mafia"),
            "2" to Player("2", "Bob", role = "villager"),
            "3" to Player("3", "Carol", role = "mafia")
        )
        assertEquals(2, players.mafiaCount())
    }

    @Test
    fun `mafiaCount returns zero when no mafia players`() {
        val players = mapOf("1" to Player("1", "Alice", role = "villager"))
        assertEquals(0, players.mafiaCount())
    }

    @Test
    fun `mafiaCount ignores players with no role assigned`() {
        val players = mapOf(
            "1" to Player("1", "Alice"),
            "2" to Player("2", "Bob", role = "mafia")
        )
        assertEquals(1, players.mafiaCount())
    }

    @Test
    fun `mafiaCount returns zero for empty map`() {
        assertEquals(0, emptyMap<String, Player>().mafiaCount())
    }

    // endregion

    // region collectIn

    @Test
    fun `collectIn collects all values emitted by the flow`() = runBlocking {
        val collected = mutableListOf<Int>()
        val job = flow { emit(1); emit(2); emit(3) }
            .collectIn(this) { collected.add(it) }
        job.join()
        assertEquals(listOf(1, 2, 3), collected)
    }

    @Test
    fun `collectIn collects no values from empty flow`() = runBlocking {
        val collected = mutableListOf<Int>()
        val job = flow<Int> {}.collectIn(this) { collected.add(it) }
        job.join()
        assertTrue(collected.isEmpty())
    }

    // endregion
}

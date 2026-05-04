package com.nightfall.engine

import com.nightfall.domain.model.Player
import com.nightfall.domain.model.Vote
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VoteManagerTest {

    private val voteManager = VoteManager()

    @Test
    fun `tally returns player with most votes`() {
        val votes = listOf(
            Vote("p1", "p3"),
            Vote("p2", "p3"),
            Vote("p3", "p2")
        )
        assertEquals("p3", voteManager.tally(votes))
    }

    @Test
    fun `tally returns null on exact two-way tie`() {
        val votes = listOf(Vote("p1", "p2"), Vote("p2", "p1"))
        assertNull(voteManager.tally(votes))
    }

    @Test
    fun `tally returns null on three-way tie`() {
        val votes = listOf(
            Vote("p1", "p2"),
            Vote("p2", "p3"),
            Vote("p3", "p1")
        )
        assertNull(voteManager.tally(votes))
    }

    @Test
    fun `isTie returns true for a tied vote`() {
        val votes = listOf(Vote("p1", "p2"), Vote("p2", "p1"))
        assertTrue(voteManager.isTie(votes))
    }

    @Test
    fun `isTie returns false when there is a clear winner`() {
        val votes = listOf(Vote("p1", "p2"), Vote("p2", "p2"))
        assertFalse(voteManager.isTie(votes))
    }

    @Test
    fun `hasAllVoted returns true when all alive players voted`() {
        val alivePlayers = listOf(
            Player("p1", "Alice", isAlive = true),
            Player("p2", "Bob", isAlive = true)
        )
        val votes = listOf(Vote("p1", "p2"), Vote("p2", "p1"))
        assertTrue(voteManager.hasAllVoted(votes, alivePlayers))
    }

    @Test
    fun `hasAllVoted returns false when not all alive players voted`() {
        val alivePlayers = listOf(
            Player("p1", "Alice", isAlive = true),
            Player("p2", "Bob", isAlive = true)
        )
        val votes = listOf(Vote("p1", "p2"))
        assertFalse(voteManager.hasAllVoted(votes, alivePlayers))
    }
}

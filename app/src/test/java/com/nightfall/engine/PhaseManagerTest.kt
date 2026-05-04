package com.nightfall.engine

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PhaseManagerTest {

    private val phaseManager = PhaseManager()

    @Test
    fun `3-second timer calls onTick 3 times and onExpire once`() = runBlocking {
        val ticks = mutableListOf<Long>()
        var expired = false
        val job = phaseManager.startTimer(
            durationMs = 3000L,
            onTick = { ticks.add(it) },
            onExpire = { expired = true }
        )
        job.join()
        assertEquals(3, ticks.size)
        assertTrue(expired)
    }

    @Test
    fun `cancelTimer stops timer before expiry`() = runBlocking {
        var expired = false
        val job = phaseManager.startTimer(
            durationMs = 5000L,
            onTick = {},
            onExpire = { expired = true }
        )
        phaseManager.cancelTimer(job)
        job.join()
        assertFalse(expired)
    }
}

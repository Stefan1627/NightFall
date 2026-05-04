package com.nightfall.engine

import com.nightfall.domain.model.GamePhase
import org.junit.Assert.assertEquals
import org.junit.Test

class GamePhaseSerializerTest {

    @Test
    fun `serialize and deserialize round-trips all 7 phases`() {
        GamePhase.values().forEach { phase ->
            val serialized = GamePhaseSerializer.serialize(phase)
            val deserialized = GamePhaseSerializer.deserialize(serialized)
            assertEquals(phase, deserialized)
        }
    }

    @Test
    fun `serialize returns name string for LOBBY`() {
        assertEquals("LOBBY", GamePhaseSerializer.serialize(GamePhase.LOBBY))
    }

    @Test
    fun `deserialize returns correct phase for NIGHT`() {
        assertEquals(GamePhase.NIGHT, GamePhaseSerializer.deserialize("NIGHT"))
    }

    @Test
    fun `deserialize returns correct phase for END_GAME`() {
        assertEquals(GamePhase.END_GAME, GamePhaseSerializer.deserialize("END_GAME"))
    }
}

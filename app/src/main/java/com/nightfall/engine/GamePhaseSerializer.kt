package com.nightfall.engine

import com.nightfall.domain.model.GamePhase

object GamePhaseSerializer {
    fun serialize(phase: GamePhase): String = phase.name

    fun deserialize(raw: String): GamePhase = GamePhase.valueOf(raw)
}

package com.nightfall.domain.model

data class Role(
    val id: String,
    val name: String,
    val isMafia: Boolean,
    val hasNightAction: Boolean
)

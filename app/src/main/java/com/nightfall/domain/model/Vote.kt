package com.nightfall.domain.model

data class Vote(
    val voteId: String = "",
    val gameId: String = "",
    val voterId: String = "",
    val targetId: String = ""
)
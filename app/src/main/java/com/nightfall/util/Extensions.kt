package com.nightfall.util

import com.nightfall.domain.model.Player
import com.nightfall.roles.RoleRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

private val emailRegex = Regex("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$")

fun <T> Flow<T>.collectIn(scope: CoroutineScope, action: suspend (T) -> Unit): Job =
    scope.launch { collect { action(it) } }

fun String.isValidEmail(): Boolean = emailRegex.matches(this)

fun Map<String, Player>.aliveCount(): Int = values.count { it.isAlive }

fun Map<String, Player>.mafiaCount(): Int = values.count {
    RoleRegistry.roles[it.role]?.faction == "mafia"
}

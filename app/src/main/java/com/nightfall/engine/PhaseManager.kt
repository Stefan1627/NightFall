package com.nightfall.engine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class PhaseManager {

    fun startTimer(
        durationMs: Long,
        onTick: (remainingMs: Long) -> Unit,
        onExpire: () -> Unit
    ): Job = CoroutineScope(Dispatchers.Default).launch {
        flow {
            var remaining = durationMs
            while (remaining > 0) {
                emit(remaining)
                delay(1000L)
                remaining -= 1000L
            }
        }.collect { remaining -> onTick(remaining) }
        onExpire()
    }

    fun cancelTimer(job: Job) {
        job.cancel()
    }
}

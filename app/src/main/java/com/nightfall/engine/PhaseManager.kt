package com.nightfall.engine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

class PhaseManager @Inject constructor() {

    fun startTimer(
        scope: CoroutineScope,
        durationMs: Long,
        onTick: (remainingMs: Long) -> Unit,
        onExpire: () -> Unit
    ): Job {
        return scope.launch {
            val tickFlow = flow {
                var remaining = durationMs
                while (remaining > 0) {
                    emit(remaining)
                    delay(1000L)
                    remaining -= 1000L
                }
                emit(0L)
            }

            tickFlow.collect { remaining ->
                if (remaining > 0) {
                    onTick(remaining)
                } else {
                    onTick(0L)
                    onExpire()
                }
            }
        }
    }

    fun cancelTimer(job: Job) {
        job.cancel()
    }
}
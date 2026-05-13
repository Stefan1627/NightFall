package com.nightfall.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nightfall.ui.theme.NightFallTheme
import com.nightfall.ui.theme.TimerGreen
import com.nightfall.ui.theme.TimerRed
import com.nightfall.ui.theme.TimerYellow

@Composable
fun PhaseTimer(
    remainingMs: Long,
    totalMs: Long,
    modifier: Modifier = Modifier
) {
    val progress = if (totalMs > 0) remainingMs.toFloat() / totalMs.toFloat() else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 300),
        label = "timer_progress"
    )

    // Color shifts: >50% green, 20-50% yellow, <20% red
    val timerColor = when {
        progress > 0.5f -> TimerGreen
        progress > 0.2f -> TimerYellow
        else -> TimerRed
    }

    val seconds = (remainingMs / 1000).toInt()

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(80.dp)
    ) {
        Canvas(modifier = Modifier.size(80.dp)) {
            // Background circle
            drawArc(
                color = timerColor.copy(alpha = 0.2f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
            )
            // Progress arc
            drawArc(
                color = timerColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        Text(
            text = "${seconds}s",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = timerColor,
            fontSize = 18.sp
        )
    }
}

@Preview
@Composable
private fun PhaseTimerFullPreview() {
    NightFallTheme {
        PhaseTimer(remainingMs = 45000L, totalMs = 60000L)
    }
}

@Preview
@Composable
private fun PhaseTimerMidPreview() {
    NightFallTheme {
        PhaseTimer(remainingMs = 20000L, totalMs = 60000L)
    }
}

@Preview
@Composable
private fun PhaseTimerLowPreview() {
    NightFallTheme {
        PhaseTimer(remainingMs = 5000L, totalMs = 60000L)
    }
}
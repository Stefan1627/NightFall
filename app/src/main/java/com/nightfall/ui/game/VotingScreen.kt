package com.nightfall.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nightfall.ui.component.PhaseTimer
import com.nightfall.ui.component.PlayerCard
import com.nightfall.ui.theme.NightFallTheme
import com.nightfall.util.Constants

@Composable
fun VotingScreen(
    gameViewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val players by gameViewModel.players.collectAsState()
    val votes by gameViewModel.votes.collectAsState()
    val hasVoted by gameViewModel.hasVoted.collectAsState()
    val phaseRemainingMs by gameViewModel.phaseRemainingMs.collectAsState()
    val currentUserId = gameViewModel.currentUserId

    val alivePlayers = players.filter { it.isAlive }

    // Aggregate vote counts (don't reveal who voted for whom)
    val voteCounts = votes.groupingBy { it.targetId }.eachCount()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "🗳️ Voting Phase",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (hasVoted) "Vote submitted ✓" else "Vote to eliminate a player",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            PhaseTimer(
                remainingMs = phaseRemainingMs,
                totalMs = Constants.VOTING_PHASE_DURATION_MS
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(alivePlayers, key = { it.playerId }) { player ->
                val voteCount = voteCounts[player.playerId] ?: 0
                val isSelf = player.playerId == currentUserId

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                ) {
                    PlayerCard(
                        player = player,
                        isCurrentUser = isSelf
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$voteCount vote${if (voteCount != 1) "s" else ""}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Button(
                            onClick = { gameViewModel.submitVote(player.playerId) },
                            enabled = !hasVoted && !isSelf,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                text = if (hasVoted) "Voted ✓" else if (isSelf) "Can't vote self" else "Vote",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }
    }
}
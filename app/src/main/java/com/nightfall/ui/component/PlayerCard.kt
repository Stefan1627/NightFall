package com.nightfall.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nightfall.domain.model.Player
import com.nightfall.ui.theme.AliveGreen
import com.nightfall.ui.theme.DeadGray
import com.nightfall.ui.theme.NightFallTheme
import com.nightfall.ui.theme.OffWhite
import com.nightfall.ui.theme.OnlineGreen
import com.nightfall.ui.theme.OfflineGray

@Composable
fun PlayerCard(
    player: Player,
    isCurrentUser: Boolean,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isCurrentUser) MaterialTheme.colorScheme.primary else Color.Transparent
    val backgroundColor = if (player.isAlive) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(
                width = if (isCurrentUser) 2.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar placeholder with online indicator
        Box {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (player.isAlive) Icons.Default.Person else Icons.Default.Close,
                    contentDescription = null,
                    tint = if (player.isAlive) OffWhite else DeadGray,
                    modifier = Modifier.size(24.dp)
                )
            }
            // Online indicator dot
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(if (player.isConnected) OnlineGreen else OfflineGray)
                    .align(Alignment.BottomEnd)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = player.displayName + if (isCurrentUser) " (You)" else "",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (player.isAlive) OffWhite else DeadGray,
                textDecoration = if (!player.isAlive) TextDecoration.LineThrough else null
            )
            Text(
                text = if (player.isAlive) "Alive" else "Eliminated",
                style = MaterialTheme.typography.bodySmall,
                color = if (player.isAlive) AliveGreen else DeadGray
            )
        }
    }
}

@Preview
@Composable
private fun PlayerCardAlivePreview() {
    NightFallTheme {
        PlayerCard(
            player = Player(
                playerId = "1",
                displayName = "Alice",
                isAlive = true,
                isConnected = true
            ),
            isCurrentUser = true
        )
    }
}

@Preview
@Composable
private fun PlayerCardDeadPreview() {
    NightFallTheme {
        PlayerCard(
            player = Player(
                playerId = "2",
                displayName = "Bob",
                isAlive = false,
                isConnected = false
            ),
            isCurrentUser = false
        )
    }
}
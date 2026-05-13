package com.nightfall.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nightfall.domain.model.ChatMessage
import com.nightfall.ui.theme.MoonlightSilver
import com.nightfall.ui.theme.NightFallTheme
import com.nightfall.ui.theme.OffWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatBubble(
    message: ChatMessage,
    isOwnMessage: Boolean,
    modifier: Modifier = Modifier
) {
    val alignment = if (isOwnMessage) Alignment.End else Alignment.Start
    val backgroundColor = if (isOwnMessage) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = if (isOwnMessage) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalAlignment = alignment
    ) {
        // Sender name (only for other messages)
        if (!isOwnMessage) {
            Text(
                text = message.senderName,
                style = MaterialTheme.typography.labelSmall,
                color = MoonlightSilver,
                modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
            )
        }

        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isOwnMessage) 16.dp else 4.dp,
                        bottomEnd = if (isOwnMessage) 4.dp else 16.dp
                    )
                )
                .background(backgroundColor)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = formatTimestamp(message.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = textColor.copy(alpha = 0.6f),
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@Preview
@Composable
private fun ChatBubbleOwnPreview() {
    NightFallTheme {
        ChatBubble(
            message = ChatMessage(
                messageId = "1",
                senderId = "me",
                senderName = "Me",
                text = "I think it's the detective!",
                timestamp = System.currentTimeMillis()
            ),
            isOwnMessage = true
        )
    }
}

@Preview
@Composable
private fun ChatBubbleOtherPreview() {
    NightFallTheme {
        ChatBubble(
            message = ChatMessage(
                messageId = "2",
                senderId = "other",
                senderName = "Alice",
                text = "No way, I'm a villager",
                timestamp = System.currentTimeMillis()
            ),
            isOwnMessage = false
        )
    }
}
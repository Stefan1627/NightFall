package com.nightfall.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nightfall.roles.RoleDefinition
import com.nightfall.roles.RoleRegistry
import com.nightfall.ui.theme.MafiaRed
import com.nightfall.ui.theme.MoonlightSilver
import com.nightfall.ui.theme.NightFallTheme
import com.nightfall.ui.theme.OffWhite
import com.nightfall.ui.theme.VillageBlue

@Composable
fun RoleBadge(
    role: RoleDefinition,
    revealed: Boolean,
    modifier: Modifier = Modifier
) {
    val factionColor = if (role.faction == "mafia") MafiaRed else VillageBlue
    val icon = when (role.roleId) {
        "mafia" -> Icons.Default.Shield
        "detective" -> Icons.Default.Visibility
        "doctor" -> Icons.Default.LocalHospital
        else -> Icons.Default.Person
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(factionColor.copy(alpha = 0.2f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = role.displayName,
            tint = factionColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = role.displayName,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = factionColor
            )
            if (revealed) {
                Text(
                    text = role.getRoleInfo(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MoonlightSilver
                )
            } else {
                Text(
                    text = role.faction.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodySmall,
                    color = MoonlightSilver
                )
            }
        }
    }
}

@Preview
@Composable
private fun RoleBadgeMafiaPreview() {
    NightFallTheme {
        RoleBadge(
            role = RoleRegistry.roles["mafia"]!!,
            revealed = true
        )
    }
}

@Preview
@Composable
private fun RoleBadgeVillagerPreview() {
    NightFallTheme {
        RoleBadge(
            role = RoleRegistry.roles["villager"]!!,
            revealed = false
        )
    }
}
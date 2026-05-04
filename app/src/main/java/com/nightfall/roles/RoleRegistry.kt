package com.nightfall.roles

import com.nightfall.roles.impl.DetectiveRole
import com.nightfall.roles.impl.DoctorRole
import com.nightfall.roles.impl.MafiaRole
import com.nightfall.roles.impl.VillagerRole

object RoleRegistry {
    val roles: Map<String, RoleDefinition> = mapOf(
        "villager" to VillagerRole(),
        "mafia" to MafiaRole(),
        "detective" to DetectiveRole(),
        "doctor" to DoctorRole()
    )
}

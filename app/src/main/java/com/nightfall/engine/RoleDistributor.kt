package com.nightfall.engine

import com.nightfall.domain.model.Player
import com.nightfall.util.Constants
import javax.inject.Inject

class RoleDistributor @Inject constructor() {

    /**
     * Distributes roles to players based on game mode.
     * @return Map of playerId to roleId
     *
     * Classic mode: 1 mafia per 3 players, 1 detective, 1 doctor, rest are villagers
     */
    fun distribute(players: List<Player>, gameMode: String): Map<String, String> {
        val shuffled = players.shuffled()
        val roleMap = mutableMapOf<String, String>()

        when (gameMode) {
            Constants.GAME_MODE_CLASSIC -> distributeClassic(shuffled, roleMap)
            Constants.GAME_MODE_CHAOS -> distributeChaos(shuffled, roleMap)
            else -> distributeClassic(shuffled, roleMap)
        }

        return roleMap
    }

    private fun distributeClassic(players: List<Player>, roleMap: MutableMap<String, String>) {
        val totalPlayers = players.size
        val mafiaCount = maxOf(1, totalPlayers / 3)

        var index = 0

        // Assign mafia
        repeat(mafiaCount) {
            if (index < totalPlayers) {
                roleMap[players[index].playerId] = "mafia"
                index++
            }
        }

        // Assign detective (1)
        if (index < totalPlayers) {
            roleMap[players[index].playerId] = "detective"
            index++
        }

        // Assign doctor (1)
        if (index < totalPlayers) {
            roleMap[players[index].playerId] = "doctor"
            index++
        }

        // Rest are villagers
        while (index < totalPlayers) {
            roleMap[players[index].playerId] = "villager"
            index++
        }
    }

    private fun distributeChaos(players: List<Player>, roleMap: MutableMap<String, String>) {
        val totalPlayers = players.size
        // Chaos mode: more mafia, more special roles
        val mafiaCount = maxOf(2, totalPlayers / 2)

        var index = 0

        // Assign mafia
        repeat(minOf(mafiaCount, totalPlayers)) {
            if (index < totalPlayers) {
                roleMap[players[index].playerId] = "mafia"
                index++
            }
        }

        // Assign detective (1)
        if (index < totalPlayers) {
            roleMap[players[index].playerId] = "detective"
            index++
        }

        // Assign doctor (1)
        if (index < totalPlayers) {
            roleMap[players[index].playerId] = "doctor"
            index++
        }

        // Rest are villagers
        while (index < totalPlayers) {
            roleMap[players[index].playerId] = "villager"
            index++
        }
    }
}
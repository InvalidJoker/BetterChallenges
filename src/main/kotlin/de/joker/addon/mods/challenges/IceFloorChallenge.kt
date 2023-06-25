package de.joker.addon.mods.challenges

import de.joker.addon.AddonManager
import de.joker.addon.utils.AddonMod
import de.miraculixx.challenge.api.modules.challenges.Challenge
import net.axay.kspigot.event.listen
import net.axay.kspigot.event.register
import net.axay.kspigot.event.unregister
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerToggleSneakEvent

class IceFloorChallenge : Challenge {
    private var sneakToggle: Boolean = false
    private var playerModes = mutableMapOf<Player, Boolean>() // player, mode

    override fun start(): Boolean {
        val settings = AddonManager.getSettings(AddonMod.ICE_FLOOR_CHALLENGE).settings
        sneakToggle = settings["sneak"]?.toBool()?.getValue() ?: true
        return true
    }

    override fun register() {
        moveEvent.register()
        if (sneakToggle) sneakEvent.register()
    }

    override fun unregister() {
        moveEvent.unregister()
        if (sneakToggle) sneakEvent.unregister()
    }

    private val moveEvent = listen<PlayerMoveEvent>(register = false) {
        if (it.player in playerModes && sneakToggle) {
            if (playerModes[it.player] != true) return@listen
        }
        val middleBlock = it.player.location.clone().subtract(0.00, 1.00, 0.00).block
        for (x in -1..1) {
            for (z in -1..1) {
                val iceLocation = middleBlock.location.add(x.toDouble(), 0.0, z.toDouble())
                val iceBlock = iceLocation.block
                if (iceBlock.type.isSolid) continue
                if (!iceBlock.type.isAir) {
                    iceBlock.breakNaturally()
                }
                iceBlock.type = Material.PACKED_ICE
            }
        }
    }

    private val sneakEvent = listen<PlayerToggleSneakEvent>(register = false) {
        if (it.isSneaking) return@listen
        if (!sneakToggle) return@listen

        if (it.player in playerModes) {
            playerModes[it.player] = !playerModes[it.player]!!
        } else {
            playerModes[it.player] = true
        }
    }
}

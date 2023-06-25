package de.joker.addon.mods.challenges

import de.miraculixx.challenge.api.modules.challenges.Challenge
import net.axay.kspigot.event.listen
import net.axay.kspigot.event.register
import net.axay.kspigot.event.unregister
import org.bukkit.event.world.LootGenerateEvent

class NoLootChallenge : Challenge {
    override fun register() {
        chestLootGenerate.register()
    }

    override fun unregister() {
        chestLootGenerate.unregister()
    }

    private val chestLootGenerate = listen<LootGenerateEvent> {
        if (it.lootTable.key.toString().contains("chest")) {
            it.isCancelled = true
        }
    }

}
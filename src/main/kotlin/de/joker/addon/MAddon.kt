package de.joker.addon

import net.axay.kspigot.main.KSpigot

class MAddon : KSpigot() {
    companion object {
        lateinit var INSTANCE: KSpigot
        lateinit var addonName: String
    }

    override fun load() {
        INSTANCE = this
        addonName = this.name
    }

    override fun startup() {
        AddonManager.loadMods()
    }

    override fun shutdown() {
        AddonManager.saveMods()
    }
}

val PluginInstance by lazy { MAddon.INSTANCE }
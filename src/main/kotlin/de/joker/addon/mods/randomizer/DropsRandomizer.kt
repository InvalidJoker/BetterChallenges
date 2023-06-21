package de.joker.addon.mods.randomizer

import de.joker.addon.AddonManager
import de.joker.addon.utils.*
import de.miraculixx.challenge.api.modules.challenges.Challenge
import net.axay.kspigot.event.listen
import net.axay.kspigot.event.register
import net.axay.kspigot.event.unregister
import net.axay.kspigot.extensions.broadcast
import net.axay.kspigot.extensions.worlds
import org.bukkit.Bukkit
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.loot.LootContext
import org.bukkit.loot.LootTables
import kotlin.random.Random

/**
 * DropsRandomizer
 * @author InvalidJokerDE, Miraculixx
 */
class DropsRandomizer : Challenge {
    private var random: Boolean = false
    private var playerRandom: Boolean = false
    private val announced: MutableSet<EntityType> = mutableSetOf()
    private val map: MutableMap<EntityType, EntityType> = mutableMapOf() // map
    private val list: MutableList<EntityType> = mutableListOf() // list
    private val playerMap: MutableMap<Player, MutableMap<EntityType, EntityType>> = mutableMapOf() // player, map
    private val playerList: MutableMap<Player, MutableList<EntityType>> = mutableMapOf() // player, list

    override fun start(): Boolean {
        val settings = AddonManager.getSettings(AddonMod.DROPS_RANDOMIZER_EXTENDED).settings
        random = settings["random"]?.toBool()?.getValue() ?: false
        playerRandom = settings["player"]?.toBool()?.getValue() ?: false
        val rnd = Random(worlds.first().seed)
        val l = getLivingMobs(false).shuffled(rnd)
        if (!random) {
            val drops = l.toMutableList()
            drops.shuffle(rnd)
            l.forEach { kill ->
                val type = drops.random(rnd)
                if (playerRandom) {
                    for (p in Bukkit.getOnlinePlayers()) {
                        val pMap = playerMap.getOrPut(p) { mutableMapOf() }
                        pMap[kill] = type
                    }
                } else map[kill] = type
                drops.remove(type)
            }
        } else {
            if (playerRandom) {
                for (p in Bukkit.getOnlinePlayers()) {
                    val pList = playerList.getOrPut(p) { l.toMutableList() }
                    pList.addAll(l)
                }
            } else list.addAll(l)
        }
        return true
    }

    override fun register() {
        onKill.register()
    }

    override fun unregister() {
        onKill.unregister()
    }

    private val onKill = listen<EntityDeathEvent>(register = false) {
        it.drops.clear()
        val entity = it.entity
        val eType = entity.type
        val targetType = if (random) {
            if (playerRandom) {
                val pList = playerList[entity.killer] ?: return@listen
                pList[Random.nextInt(0, list.size - 1)]
            } else {
                list[Random.nextInt(0, list.size - 1)]
            }
        } else {
            if (playerRandom) {
                val pMap = playerMap[entity.killer] ?: return@listen
                pMap[eType] ?: EntityType.ZOMBIE
            } else {
                map[eType] ?: EntityType.ZOMBIE
            }

        }
        val loc = entity.location
        val context = LootContext.Builder(loc).killer(entity.killer).lootedEntity(entity).lootingModifier(2).build()
        val tableName = targetType.name
        val lootTable = LootTables.valueOf(tableName).lootTable
        if (announced.contains(eType)) {
            announced.add(eType)
            broadcast(prefix + cmp("${eType.name} >> ${targetType.name}"))
        }
        lootTable.populateLoot(java.util.Random(loc.world.seed), context).forEach { item ->
            loc.world.dropItem(loc, item)
        }
    }
}
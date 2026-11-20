package com.cuac_xd.zencore.api

import com.cuac_xd.zencore.ZenCorePlugin
import com.cuac_xd.zencore.aura.ZenAuraEngine
import com.cuac_xd.zencore.data.ZenPlayerDataService
import com.cuac_xd.zencore.engine.ZenEngine
import com.cuac_xd.zencore.engine.condition.ZenCondition
import com.cuac_xd.zencore.engine.context.ZenExecutionContext
import com.cuac_xd.zencore.engine.context.ZenPDC
import com.cuac_xd.zencore.engine.effect.ZenEffect
import com.cuac_xd.zencore.engine.filter.ZenFilter
import com.cuac_xd.zencore.engine.mutator.ZenMutator
import com.cuac_xd.zencore.loot.ZenLootEngine
import com.cuac_xd.zencore.notify.ZenNotification
import com.cuac_xd.zencore.particle.ZenParticleShapes
import com.cuac_xd.zencore.profiler.ZenProfiler
import com.cuac_xd.zencore.projectile.ZenProjectileEngine
import com.cuac_xd.zencore.protection.ZenProtectionHub
import com.cuac_xd.zencore.scheduler.ZenScheduler
import com.cuac_xd.zencore.trigger.ZenTriggerEngine
import java.util.UUID

object ZenCoreAPI {
    private var plugin: ZenCorePlugin? = null
    private var fallbackEngine: ZenEngine? = null

    @JvmStatic
    fun init(instance: ZenCorePlugin) {
        plugin = instance
        ZenPDC.init(instance)
        ZenScheduler.init(instance)
    }

    @JvmStatic
    fun getPlugin(): ZenCorePlugin? = plugin

    @JvmStatic
    val engine: ZenEngine
        get() {
            val p = plugin
            if (p != null) {
                return p.engine
            }
            if (fallbackEngine == null) {
                fallbackEngine = ZenEngine()
            }
            return fallbackEngine!!
        }

    // Direct access to core subsystem singletons
    @JvmStatic val hooks get() = com.cuac_xd.zencore.hooks.ZenHooksHub
    @JvmStatic val economy get() = com.cuac_xd.zencore.hooks.ZenEconomyHook
    @JvmStatic val cosmetics get() = com.cuac_xd.zencore.hooks.ZenCosmeticsHook
    @JvmStatic val customItems get() = com.cuac_xd.zencore.hooks.ZenCustomItemsHook
    @JvmStatic val enchants get() = com.cuac_xd.zencore.hooks.ZenEnchantsHook
    @JvmStatic val parties get() = com.cuac_xd.zencore.party.ZenPartyHub
    @JvmStatic val partyHook get() = com.cuac_xd.zencore.hooks.ZenPartyHook
    @JvmStatic val boosterHook get() = com.cuac_xd.zencore.hooks.ZenBoosterHook
    @JvmStatic val tags get() = com.cuac_xd.zencore.hooks.ZenTagsHook
    @JvmStatic val discord get() = com.cuac_xd.zencore.hooks.ZenDiscordHook
    @JvmStatic val auctions get() = com.cuac_xd.zencore.hooks.ZenAuctionsHook
    @JvmStatic val jobs get() = com.cuac_xd.zencore.hooks.ZenJobsHook
    @JvmStatic val horses get() = com.cuac_xd.zencore.hooks.ZenHorsesHook
    @JvmStatic val storage get() = com.cuac_xd.zencore.hooks.ZenStorageHook
    @JvmStatic val bans get() = com.cuac_xd.zencore.hooks.ZenBansHook
    @JvmStatic val bank get() = com.cuac_xd.zencore.hooks.ZenBankHook
    @JvmStatic val afkZone get() = com.cuac_xd.zencore.hooks.ZenAfkZoneHook
    @JvmStatic val chat get() = com.cuac_xd.zencore.hooks.ZenChatHook
    @JvmStatic val chatGames get() = com.cuac_xd.zencore.hooks.ZenChatGamesHook
    @JvmStatic val dragonEvent get() = com.cuac_xd.zencore.hooks.ZenDragonEventHook
    @JvmStatic val fairy get() = com.cuac_xd.zencore.hooks.ZenFairyHook
    @JvmStatic val forges get() = com.cuac_xd.zencore.hooks.ZenForgesHook
    @JvmStatic val hub get() = com.cuac_xd.zencore.hooks.ZenHubHook
    @JvmStatic val quests get() = com.cuac_xd.zencore.hooks.ZenQuestsHook
    @JvmStatic val rankups get() = com.cuac_xd.zencore.hooks.ZenRankupsHook
    @JvmStatic val rewards get() = com.cuac_xd.zencore.hooks.ZenRewardsHook
    @JvmStatic val rogueLike get() = com.cuac_xd.zencore.hooks.ZenRogueLikeHook
    @JvmStatic val spawner get() = com.cuac_xd.zencore.hooks.ZenSpawnerHook
    @JvmStatic val blockRegen get() = com.cuac_xd.zencore.hooks.ZenBlockRegenHook
    @JvmStatic val contest get() = com.cuac_xd.zencore.hooks.ZenContestHook
    @JvmStatic val crates get() = com.cuac_xd.zencore.hooks.ZenCratesHook
    @JvmStatic val triggers get() = ZenTriggerEngine
    @JvmStatic val pdc get() = ZenPDC
    @JvmStatic val projectiles get() = ZenProjectileEngine
    @JvmStatic val playerData get() = ZenPlayerDataService
    @JvmStatic val notifications get() = ZenNotification
    @JvmStatic val auras get() = ZenAuraEngine
    @JvmStatic val protection get() = ZenProtectionHub
    @JvmStatic val loot get() = ZenLootEngine
    @JvmStatic val shapes get() = ZenParticleShapes
    @JvmStatic val profiler get() = ZenProfiler
    @JvmStatic val scheduler get() = ZenScheduler
    @JvmStatic val itemParser get() = com.cuac_xd.zencore.item.ItemParser
    @JvmStatic val menuParser get() = com.cuac_xd.zencore.gui.MenuParser
    @JvmStatic val holograms get() = com.cuac_xd.zencore.hologram.ZenHologramEngine
    @JvmStatic val recipes get() = com.cuac_xd.zencore.recipe.ZenRecipeEngine
    @JvmStatic val drops get() = com.cuac_xd.zencore.drop.ZenDropEngine
    @JvmStatic val boosters get() = com.cuac_xd.zencore.booster.ZenBoosterEngine
    @JvmStatic val stats get() = com.cuac_xd.zencore.stat.ZenStatEngine
    @JvmStatic val blockTracker get() = com.cuac_xd.zencore.world.ZenBlockTracker
    @JvmStatic val worldActions get() = com.cuac_xd.zencore.world.ZenWorldActions

    private var defaultCooldowns: com.cuac_xd.zencore.cooldown.ZenCooldownManager? = null
    @JvmStatic val cooldowns: com.cuac_xd.zencore.cooldown.ZenCooldownManager
        get() {
            if (defaultCooldowns == null) {
                defaultCooldowns = com.cuac_xd.zencore.cooldown.ZenCooldownManager(plugin)
            }
            return defaultCooldowns!!
        }

    @JvmStatic
    fun createMessageService(prefix: String = ""): com.cuac_xd.zencore.message.ZenMessageService {
        return com.cuac_xd.zencore.message.ZenMessageService(prefix)
    }

    @JvmStatic
    fun createCommand(
        name: String,
        commandPermission: String? = null,
        playerOnly: Boolean = false,
        commandAliases: List<String> = emptyList()
    ): com.cuac_xd.zencore.command.ZenCommand {
        return com.cuac_xd.zencore.command.ZenCommand(
            commandName = name,
            commandPermission = commandPermission,
            playerOnly = playerOnly,
            commandAliases = commandAliases
        )
    }

    @JvmStatic
    fun createDatabase(
        type: com.cuac_xd.zencore.database.ZenDatabaseEngine.DatabaseType,
        sqliteFile: java.io.File? = null,
        host: String = "127.0.0.1",
        port: Int = 3306,
        database: String = "minecraft",
        user: String = "root",
        pass: String = ""
    ): com.cuac_xd.zencore.database.ZenDatabaseEngine {
        return com.cuac_xd.zencore.database.ZenDatabaseEngine(
            type = type,
            host = host,
            port = port,
            database = database,
            user = user,
            pass = pass,
            sqliteFile = sqliteFile
        )
    }

    @JvmStatic
    fun createConfig(file: java.io.File, streamProvider: (() -> java.io.InputStream?)? = null): com.cuac_xd.zencore.config.ZenConfig {
        return com.cuac_xd.zencore.config.ZenConfig(file, streamProvider)
    }

    @JvmStatic
    fun createItem(material: org.bukkit.Material, amount: Int = 1): com.cuac_xd.zencore.item.ItemBuilder {
        return com.cuac_xd.zencore.item.ItemBuilder(material, amount)
    }

    @JvmStatic
    fun editItem(itemStack: org.bukkit.inventory.ItemStack): com.cuac_xd.zencore.item.ItemBuilder {
        return com.cuac_xd.zencore.item.ItemBuilder(itemStack)
    }

    @JvmStatic
    fun createMenu(rows: Int = 6, title: String): com.cuac_xd.zencore.gui.ZenMenu {
        return com.cuac_xd.zencore.gui.ZenMenu(rows, title)
    }

    @JvmStatic
    fun registerEffect(id: String, effect: ZenEffect) {
        engine.effectRegistry.register(id, effect)
    }

    @JvmStatic
    fun registerCondition(id: String, condition: ZenCondition) {
        engine.conditionRegistry.register(id, condition)
    }

    @JvmStatic
    fun registerFilter(id: String, filter: ZenFilter) {
        engine.filterRegistry.register(id, filter)
    }

    @JvmStatic
    fun registerMutator(id: String, mutator: ZenMutator) {
        engine.mutatorRegistry.register(id, mutator)
    }

    @JvmStatic
    fun executeEffect(id: String, context: ZenExecutionContext, args: Map<String, @JvmSuppressWildcards Any>): Boolean {
        if (!engine.effectRegistry.has(id)) return false
        engine.executeEffect(id, context, args, plugin)
        return true
    }

    @JvmStatic
    fun testCondition(id: String, context: ZenExecutionContext, args: Map<String, @JvmSuppressWildcards Any>): Boolean {
        val condition = engine.conditionRegistry.get(id) ?: return false
        return condition.test(context, args)
    }

    @JvmStatic
    fun clearCooldowns(playerUUID: UUID) {
        engine.clearCooldowns(playerUUID)
    }
}

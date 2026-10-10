package com.cuac_xd.zencore

import com.cuac_xd.zencore.api.ZenCoreAPI
import com.cuac_xd.zencore.aura.ZenAuraEngine
import com.cuac_xd.zencore.engine.ZenEngine
import com.cuac_xd.zencore.gui.ZenGUI
import com.cuac_xd.zencore.integration.IntegrationHub
import com.cuac_xd.zencore.trigger.ZenTriggerEngine
import org.bukkit.plugin.java.JavaPlugin

class ZenCorePlugin : JavaPlugin() {
    lateinit var engine: ZenEngine private set

    override fun onEnable() {
        companionInstance = this
        this.engine = ZenEngine()

        // Initialize API and Subsystems
        ZenCoreAPI.init(this)

        // Initialize Integrations (Vault, PlaceholderAPI)
        IntegrationHub.init()

        // Register Core Event Listeners
        server.pluginManager.registerEvents(ZenTriggerEngine, this)
        server.pluginManager.registerEvents(ZenGUI.Companion, this)

        // Prune cooldowns periodically every 60s
        server.scheduler.runTaskTimer(this, Runnable {
            engine.pruneExpiredCooldowns()
        }, 1200L, 1200L)

        logger.info("ZenCore v${pluginMeta.version} enabled successfully! (Written in Kotlin, Paper 1.21+)")
    }

    override fun onDisable() {
        ZenAuraEngine.stopAll()
        logger.info("ZenCore disabled.")
    }

    companion object {
        private var companionInstance: ZenCorePlugin? = null

        @JvmStatic
        fun getInstance(): ZenCorePlugin? = companionInstance
    }
}

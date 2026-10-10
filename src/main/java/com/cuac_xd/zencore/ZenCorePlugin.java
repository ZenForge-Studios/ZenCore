package com.cuac_xd.zencore;

import com.cuac_xd.zencore.api.ZenCoreAPI;
import com.cuac_xd.zencore.engine.ZenEngine;
import com.cuac_xd.zencore.integration.IntegrationHub;
import org.bukkit.plugin.java.JavaPlugin;

public final class ZenCorePlugin extends JavaPlugin {

    private static ZenCorePlugin instance;
    private ZenEngine engine;

    @Override
    public void onEnable() {
        instance = this;
        this.engine = new ZenEngine();

        // Initialize API
        ZenCoreAPI.init(this);

        // Initialize Integrations (Vault, PlaceholderAPI, etc.)
        IntegrationHub.init();

        // Prune cooldowns periodically every 60s
        getServer().getScheduler().runTaskTimer(this, () -> {
            if (engine != null) {
                engine.pruneExpiredCooldowns();
            }
        }, 1200L, 1200L);

        getLogger().info("ZenCore v" + getPluginMeta().getVersion() + " has been successfully enabled! (Running on Java 25 & Paper 1.21+)");
    }

    @Override
    public void onDisable() {
        getLogger().info("ZenCore has been disabled.");
    }

    public static ZenCorePlugin getInstance() {
        return instance;
    }

    public ZenEngine getEngine() {
        return engine;
    }
}

package com.cuac_xd.zencore.scheduler

import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask

/**
 * Fluent async and sync task dispatcher and chained scheduler.
 */
object ZenScheduler {
    private var plugin: Plugin? = null

    @JvmStatic
    fun init(pluginInstance: Plugin) {
        plugin = pluginInstance
    }

    private fun getPlugin(): Plugin {
        return plugin ?: throw IllegalStateException("ZenScheduler is not initialized with a Plugin instance!")
    }

    @JvmStatic
    fun runSync(action: () -> Unit): BukkitTask {
        return Bukkit.getScheduler().runTask(getPlugin(), Runnable(action))
    }

    @JvmStatic
    fun runAsync(action: () -> Unit): BukkitTask {
        return Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), Runnable(action))
    }

    @JvmStatic
    fun runLater(delayTicks: Long, action: () -> Unit): BukkitTask {
        return Bukkit.getScheduler().runTaskLater(getPlugin(), Runnable(action), delayTicks)
    }

    @JvmStatic
    fun runLaterAsync(delayTicks: Long, action: () -> Unit): BukkitTask {
        return Bukkit.getScheduler().runTaskLaterAsynchronously(getPlugin(), Runnable(action), delayTicks)
    }

    @JvmStatic
    fun runTimer(delayTicks: Long, periodTicks: Long, action: (BukkitTask) -> Unit): BukkitTask {
        lateinit var task: BukkitTask
        task = Bukkit.getScheduler().runTaskTimer(getPlugin(), Runnable {
            action(task)
        }, delayTicks, periodTicks)
        return task
    }

    @JvmStatic
    fun <T> asyncChain(asyncWork: () -> T, syncCallback: (T) -> Unit) {
        runAsync {
            val result = asyncWork()
            runSync {
                syncCallback(result)
            }
        }
    }
}

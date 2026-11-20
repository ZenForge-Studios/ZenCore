package com.cuac_xd.zencore.hooks

import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/**
 * Hook para interactuar con ZenEconomy de forma segura y tipada.
 * Prioriza el [ZenEconomyProvider] registrado nativamente; si no hay proveedor registrado,
 * usa un fallback reflexivo corregido contra ZenEconomyAPI.
 */
object ZenEconomyHook {

    private val providerRef = AtomicReference<ZenEconomyProvider?>(null)

    @JvmStatic
    fun registerProvider(provider: ZenEconomyProvider) {
        providerRef.set(provider)
    }

    @JvmStatic
    @JvmOverloads
    fun unregisterProvider(provider: ZenEconomyProvider? = null) {
        if (provider == null) {
            providerRef.set(null)
        } else {
            providerRef.compareAndSet(provider, null)
        }
    }

    @JvmStatic
    fun getProvider(): ZenEconomyProvider? = providerRef.get()

    @JvmStatic
    fun isInstalled(): Boolean {
        if (providerRef.get() != null) return true
        val plugin = Bukkit.getPluginManager().getPlugin("ZenEconomy")
        return plugin != null && plugin.isEnabled
    }

    @JvmStatic
    fun currencyExists(currencyId: String): Boolean {
        providerRef.get()?.let { return it.currencyExists(currencyId) }
        if (!isInstalled()) return false
        return runCatching {
            val api = getAPI() ?: return false
            val method = api.javaClass.getMethod("currencyExists", String::class.java)
            method.invoke(api, currencyId) as? Boolean ?: false
        }.getOrDefault(false)
    }

    @JvmStatic
    fun getBalance(player: OfflinePlayer, currencyId: String): Double =
        getBalance(player.uniqueId, currencyId)

    @JvmStatic
    fun getBalance(uuid: UUID, currencyId: String): Double {
        providerRef.get()?.let { return it.getBalance(uuid, currencyId) }
        if (!isInstalled()) return 0.0
        return runCatching {
            val api = getAPI() ?: return 0.0
            val method = api.javaClass.getMethod("getBalanceSync", UUID::class.java, String::class.java)
            val result = method.invoke(api, uuid, currencyId)
            (result as? Number)?.toDouble() ?: 0.0
        }.getOrDefault(0.0)
    }

    @JvmStatic
    fun has(player: OfflinePlayer, currencyId: String, amount: Double): Boolean =
        has(player.uniqueId, currencyId, amount)

    @JvmStatic
    fun has(uuid: UUID, currencyId: String, amount: Double): Boolean {
        if (amount.isNaN() || amount.isInfinite() || amount < 0.0) return false
        providerRef.get()?.let { return it.has(uuid, currencyId, amount) }
        if (!isInstalled()) return false
        return runCatching {
            val api = getAPI() ?: return false
            val method = api.javaClass.getMethod("hasSync", UUID::class.java, String::class.java, Double::class.javaPrimitiveType)
            method.invoke(api, uuid, currencyId, amount) as? Boolean ?: false
        }.getOrDefault(false)
    }

    @JvmStatic
    fun deposit(player: OfflinePlayer, currencyId: String, amount: Double): Boolean =
        deposit(player.uniqueId, currencyId, amount)

    @JvmStatic
    fun deposit(uuid: UUID, currencyId: String, amount: Double): Boolean {
        if (!amount.isFinite() || amount <= 0.0) return false
        providerRef.get()?.let { return it.deposit(uuid, currencyId, amount) }
        if (!isInstalled()) return false
        return runCatching {
            val api = getAPI() ?: return false
            val method = api.javaClass.getMethod("deposit", UUID::class.java, String::class.java, Double::class.javaPrimitiveType)
            val future = method.invoke(api, uuid, currencyId, amount) as? CompletableFuture<*>
            resolveBooleanFuture(future)
        }.getOrDefault(false)
    }

    @JvmStatic
    fun withdraw(player: OfflinePlayer, currencyId: String, amount: Double): Boolean =
        withdraw(player.uniqueId, currencyId, amount)

    @JvmStatic
    fun withdraw(uuid: UUID, currencyId: String, amount: Double): Boolean {
        if (!amount.isFinite() || amount <= 0.0) return false
        providerRef.get()?.let { return it.withdraw(uuid, currencyId, amount) }
        if (!isInstalled()) return false
        return runCatching {
            val api = getAPI() ?: return false
            val method = api.javaClass.getMethod("withdraw", UUID::class.java, String::class.java, Double::class.javaPrimitiveType)
            val future = method.invoke(api, uuid, currencyId, amount) as? CompletableFuture<*>
            resolveBooleanFuture(future)
        }.getOrDefault(false)
    }

    @JvmStatic
    fun setBalance(player: OfflinePlayer, currencyId: String, amount: Double): Boolean =
        setBalance(player.uniqueId, currencyId, amount)

    @JvmStatic
    fun setBalance(uuid: UUID, currencyId: String, amount: Double): Boolean {
        if (!amount.isFinite() || amount < 0.0) return false
        providerRef.get()?.let { return it.setBalance(uuid, currencyId, amount) }
        if (!isInstalled()) return false
        return runCatching {
            val api = getAPI() ?: return false
            val method = api.javaClass.getMethod("setBalance", UUID::class.java, String::class.java, Double::class.javaPrimitiveType)
            val future = method.invoke(api, uuid, currencyId, amount) as? CompletableFuture<*>
            resolveBooleanFuture(future)
        }.getOrDefault(false)
    }

    @JvmStatic
    fun depositAsync(uuid: UUID, currencyId: String, amount: Double): CompletableFuture<Boolean> {
        if (!amount.isFinite() || amount <= 0.0) return CompletableFuture.completedFuture(false)
        providerRef.get()?.let { return it.depositAsync(uuid, currencyId, amount) }
        return CompletableFuture.completedFuture(deposit(uuid, currencyId, amount))
    }

    @JvmStatic
    fun withdrawAsync(uuid: UUID, currencyId: String, amount: Double): CompletableFuture<Boolean> {
        if (!amount.isFinite() || amount <= 0.0) return CompletableFuture.completedFuture(false)
        providerRef.get()?.let { return it.withdrawAsync(uuid, currencyId, amount) }
        return CompletableFuture.completedFuture(withdraw(uuid, currencyId, amount))
    }

    @JvmStatic
    fun format(currencyId: String, amount: Double): String {
        providerRef.get()?.let { return it.format(currencyId, amount) }
        if (!isInstalled()) return amount.toString()
        return runCatching {
            val api = getAPI() ?: return amount.toString()
            val method = api.javaClass.getMethod("format", String::class.java, Double::class.javaPrimitiveType)
            method.invoke(api, currencyId, amount)?.toString() ?: amount.toString()
        }.getOrDefault(amount.toString())
    }

    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun getRegisteredCurrencies(): List<String> {
        providerRef.get()?.let { return it.getRegisteredCurrencies() }
        if (!isInstalled()) return emptyList()
        return runCatching {
            val api = getAPI() ?: return emptyList()
            val method = api.javaClass.getMethod("getCurrencyIds")
            (method.invoke(api) as? Collection<String>)?.toList() ?: emptyList()
        }.getOrDefault(emptyList())
    }

    private fun resolveBooleanFuture(future: CompletableFuture<*>?): Boolean {
        if (future == null) return false
        if (future.isDone) {
            return runCatching { future.getNow(null) as? Boolean ?: false }.getOrDefault(false)
        }
        return if (Bukkit.isPrimaryThread()) {
            // Avoid deadlocking main thread if future awaits main thread
            false
        } else {
            runCatching { future.get(3, TimeUnit.SECONDS) as? Boolean ?: false }.getOrDefault(false)
        }
    }

    private fun getAPI(): Any? {
        return runCatching {
            val clazz = Class.forName("com.cuac_xd.zeneconomy.api.ZenEconomyAPI")
            val getMethod = clazz.getMethod("get")
            getMethod.invoke(null)
        }.getOrNull()
    }
}

package com.cuac_xd.zencore.stat

import org.bukkit.Bukkit
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import java.util.Locale
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Defines how a [StatModifier] combines with a player's base stat value.
 */
enum class StatOperation {
    /** Added directly to the base stat before percentages and multipliers. */
    ADDITIVE,

    /** Multiplies the intermediate value (`product(amount)`). */
    MULTIPLICATIVE,

    /** Summed as percentage points and applied as `1.0 + sum(amount) / 100.0`. */
    PERCENTAGE
}

/**
 * Represents a stat modifier applied to a player under a unique [key] and [stat] pair.
 *
 * @property key Identifier of the modifier source (e.g., `"ruby_ring"`, `"rage_buff"`).
 * @property stat Target stat name (matched case-insensitively, e.g., `"strength"`, `"max_mana"`).
 * @property amount Numeric value interpreted according to [operation].
 * @property operation Stacking operation applied during stat resolution.
 * @property expiresAt Epoch millisecond timestamp when this modifier expires, or `0L` if permanent.
 */
data class StatModifier(
    val key: String,
    val stat: String,
    val amount: Double,
    val operation: StatOperation = StatOperation.ADDITIVE,
    val expiresAt: Long = 0L
) {
    /**
     * Returns `true` if this modifier has a finite expiration and has reached or passed [now].
     */
    fun isExpired(now: Long = System.currentTimeMillis()): Boolean = expiresAt > 0L && now >= expiresAt
}

/**
 * Thread-safe player stat, modifier, and resource pool engine (mana, energy, stamina, etc.).
 */
object ZenStatEngine {

    private data class AttributeSpec(
        val resolveAttribute: () -> Attribute,
        val defaultBase: Double,
        val minValue: Double = 0.0
    )

    // Resolved lazily inside syncBukkitAttribute so Attribute.<clinit> (Paper RegistryAccess)
    // is never triggered in unit tests without a running Bukkit server.
    private val attributeMappings = mapOf(
        "max_health" to AttributeSpec({ Attribute.MAX_HEALTH }, 20.0, 1.0),
        "health" to AttributeSpec({ Attribute.MAX_HEALTH }, 20.0, 1.0),
        "movement_speed" to AttributeSpec({ Attribute.MOVEMENT_SPEED }, 0.1, 0.0),
        "speed" to AttributeSpec({ Attribute.MOVEMENT_SPEED }, 0.1, 0.0),
        "attack_damage" to AttributeSpec({ Attribute.ATTACK_DAMAGE }, 1.0, 0.0),
        "damage" to AttributeSpec({ Attribute.ATTACK_DAMAGE }, 1.0, 0.0),
        "armor" to AttributeSpec({ Attribute.ARMOR }, 0.0, 0.0),
        "defense" to AttributeSpec({ Attribute.ARMOR }, 0.0, 0.0),
        "attack_speed" to AttributeSpec({ Attribute.ATTACK_SPEED }, 4.0, 0.0),
        "luck" to AttributeSpec({ Attribute.LUCK }, 0.0, -1024.0),
        "mining_efficiency" to AttributeSpec({ Attribute.MINING_EFFICIENCY }, 0.0, 0.0),
        "block_break_speed" to AttributeSpec({ Attribute.BLOCK_BREAK_SPEED }, 1.0, 0.0)
    )

    private val baseStats = ConcurrentHashMap<UUID, ConcurrentHashMap<String, Double>>()
    private val modifiers = ConcurrentHashMap<UUID, ConcurrentHashMap<String, StatModifier>>()
    private val playerResources = ConcurrentHashMap<UUID, ConcurrentHashMap<String, Double>>()

    /**
     * Sets the base value of [stat] for [playerUuid].
     */
    @JvmStatic
    fun setBaseStat(playerUuid: UUID, stat: String, baseValue: Double) {
        baseStats
            .computeIfAbsent(playerUuid) { ConcurrentHashMap() }[normalize(stat)] = baseValue
    }

    /**
     * Returns the base value of [stat] for [playerUuid], or [default] if unset.
     */
    @JvmStatic
    @JvmOverloads
    fun getBaseStat(playerUuid: UUID, stat: String, default: Double = 0.0): Double {
        return baseStats[playerUuid]?.get(normalize(stat)) ?: default
    }

    /**
     * Adds or replaces a modifier for [playerUuid] identified by [key] and [stat].
     * A [durationTicks] `> 0` converts to milliseconds via `durationTicks * 50L`; `<= 0` is permanent.
     */
    @JvmStatic
    @JvmOverloads
    fun addModifier(
        playerUuid: UUID,
        key: String,
        stat: String,
        amount: Double,
        operation: StatOperation = StatOperation.ADDITIVE,
        durationTicks: Long = 0L
    ) {
        val normalizedStat = normalize(stat)
        val expiresAt = if (durationTicks > 0L) {
            System.currentTimeMillis() + durationTicks * 50L
        } else {
            0L
        }
        val modifier = StatModifier(
            key = key,
            stat = normalizedStat,
            amount = amount,
            operation = operation,
            expiresAt = expiresAt
        )
        modifiers
            .computeIfAbsent(playerUuid) { ConcurrentHashMap() }[compositeKey(key, normalizedStat)] = modifier
    }

    /**
     * Removes modifiers matching [key] for [playerUuid].
     * If [stat] is `null`, removes all modifiers with [key] across every stat.
     */
    @JvmStatic
    @JvmOverloads
    fun removeModifier(playerUuid: UUID, key: String, stat: String? = null) {
        val playerMods = modifiers[playerUuid] ?: return
        if (stat != null) {
            playerMods.remove(compositeKey(key, normalize(stat)))
        } else {
            playerMods.entries.removeIf { it.value.key.equals(key, ignoreCase = true) }
        }
        if (playerMods.isEmpty()) {
            modifiers.remove(playerUuid, playerMods)
        }
    }

    /**
     * Prunes expired modifiers for [playerUuid] and computes the effective stat value:
     * `(base + sum(ADDITIVE)) * (1.0 + sum(PERCENTAGE) / 100.0) * product(MULTIPLICATIVE)`.
     */
    @JvmStatic
    @JvmOverloads
    fun getStat(playerUuid: UUID, stat: String, defaultBase: Double = 0.0): Double {
        val normalizedStat = normalize(stat)
        val base = getBaseStat(playerUuid, normalizedStat, defaultBase)
        val playerMods = modifiers[playerUuid] ?: return base

        val now = System.currentTimeMillis()
        prunePlayerModifiers(playerUuid, playerMods, now)

        var additiveSum = 0.0
        var percentageSum = 0.0
        var multiplicativeProduct = 1.0

        for (mod in playerMods.values) {
            if (!mod.stat.equals(normalizedStat, ignoreCase = true)) continue
            when (mod.operation) {
                StatOperation.ADDITIVE -> additiveSum += mod.amount
                StatOperation.PERCENTAGE -> percentageSum += mod.amount
                StatOperation.MULTIPLICATIVE -> multiplicativeProduct *= mod.amount
            }
        }

        return (base + additiveSum) * (1.0 + percentageSum / 100.0) * multiplicativeProduct
    }

    /**
     * Returns all non-expired modifiers currently active for [playerUuid], optionally filtered by [stat].
     */
    @JvmStatic
    @JvmOverloads
    fun getModifiers(playerUuid: UUID, stat: String? = null): List<StatModifier> {
        val playerMods = modifiers[playerUuid] ?: return emptyList()
        prunePlayerModifiers(playerUuid, playerMods, System.currentTimeMillis())
        return if (stat == null) {
            playerMods.values.toList()
        } else {
            playerMods.values.filter { it.stat.equals(stat, ignoreCase = true) }
        }
    }

    /**
     * Returns the current amount in resource [pool] for [playerUuid], defaulting to [getMaxResource] if unset.
     */
    @JvmStatic
    @JvmOverloads
    fun getResource(playerUuid: UUID, pool: String = "mana"): Double {
        val max = getMaxResource(playerUuid, pool)
        val stored = playerResources[playerUuid]?.get(normalize(pool)) ?: return max
        return stored.coerceIn(0.0, max)
    }

    /**
     * Returns the maximum capacity of resource [pool] for [playerUuid], calculated as
     * `(100.0 + getStat(playerUuid, "max_${pool.lowercase()}", 0.0)).coerceAtLeast(0.0)`.
     */
    @JvmStatic
    @JvmOverloads
    fun getMaxResource(playerUuid: UUID, pool: String = "mana"): Double {
        return (100.0 + getStat(playerUuid, "max_${normalize(pool)}", 0.0)).coerceAtLeast(0.0)
    }

    /**
     * Sets [playerUuid]'s resource [pool] level, clamped to `0.0..getMaxResource(playerUuid, pool)`.
     */
    @JvmStatic
    fun setResource(playerUuid: UUID, pool: String, amount: Double) {
        val poolKey = normalize(pool)
        val poolMap = playerResources.computeIfAbsent(playerUuid) { ConcurrentHashMap() }
        synchronized(poolMap) {
            val max = getMaxResource(playerUuid, poolKey)
            poolMap[poolKey] = amount.coerceIn(0.0, max)
        }
    }

    /**
     * Adds [amount] to [playerUuid]'s resource [pool], clamped to `0.0..max`, and returns the new level.
     */
    @JvmStatic
    fun giveResource(playerUuid: UUID, pool: String, amount: Double): Double {
        val poolKey = normalize(pool)
        val poolMap = playerResources.computeIfAbsent(playerUuid) { ConcurrentHashMap() }
        synchronized(poolMap) {
            val max = getMaxResource(playerUuid, poolKey)
            val current = (poolMap[poolKey] ?: max).coerceIn(0.0, max)
            val updated = (current + amount).coerceIn(0.0, max)
            poolMap[poolKey] = updated
            return updated
        }
    }

    /**
     * Attempts to consume [amount] from [playerUuid]'s resource [pool].
     * Returns `true` and subtracts [amount] if sufficient resource is available; otherwise returns `false`.
     */
    @JvmStatic
    fun consumeResource(playerUuid: UUID, pool: String, amount: Double): Boolean {
        if (amount < 0.0 || !amount.isFinite()) return false
        val poolKey = normalize(pool)
        val poolMap = playerResources.computeIfAbsent(playerUuid) { ConcurrentHashMap() }
        synchronized(poolMap) {
            val max = getMaxResource(playerUuid, poolKey)
            val current = (poolMap[poolKey] ?: max).coerceIn(0.0, max)
            if (current < amount) return false
            poolMap[poolKey] = (current - amount).coerceIn(0.0, max)
            return true
        }
    }

    /**
     * Returns `true` if [playerUuid] has at least [amount] available in resource [pool].
     */
    @JvmStatic
    fun hasResource(playerUuid: UUID, pool: String, amount: Double): Boolean {
        return getResource(playerUuid, pool) >= amount
    }

    private val bukkitGetServerMethod = runCatching { Bukkit::class.java.getMethod("getServer") }.getOrNull()

    private fun isServerAvailable(): Boolean {
        return runCatching { bukkitGetServerMethod?.invoke(null) != null }.getOrDefault(false)
    }

    /**
     * Bridges a ZenCore [stat] (such as `max_health`, `movement_speed`, `attack_damage`, or `armor`)
     * to the matching Paper [Attribute] base value on [player]. Runs safely inside `runCatching`.
     */
    @JvmStatic
    fun syncBukkitAttribute(player: Player, stat: String) {
        if (!isServerAvailable()) return
        val normalizedStat = normalize(stat)
        val spec = attributeMappings[normalizedStat] ?: return
        runCatching {
            val attribute = spec.resolveAttribute()
            val instance = player.getAttribute(attribute) ?: return@runCatching
            val computed = getStat(player.uniqueId, normalizedStat, spec.defaultBase)
                .coerceAtLeast(spec.minValue)
            instance.baseValue = computed
            if (attribute == Attribute.MAX_HEALTH && player.health > computed) {
                player.health = computed
            }
        }
    }

    /**
     * Clears all stored base stats, modifiers, and resource pools for [uuid].
     */
    @JvmStatic
    fun clearPlayer(uuid: UUID) {
        baseStats.remove(uuid)
        modifiers.remove(uuid)
        playerResources.remove(uuid)
    }

    /**
     * Clears all stored stats, modifiers, and resources for all players.
     */
    @JvmStatic
    fun clearAll() {
        baseStats.clear()
        modifiers.clear()
        playerResources.clear()
    }

    /**
     * Removes all expired modifiers across all tracked players.
     */
    @JvmStatic
    fun pruneExpired() {
        val now = System.currentTimeMillis()
        val iterator = modifiers.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            entry.value.entries.removeIf { it.value.isExpired(now) }
            if (entry.value.isEmpty()) {
                iterator.remove()
            }
        }
    }

    private fun prunePlayerModifiers(
        playerUuid: UUID,
        playerMods: ConcurrentHashMap<String, StatModifier>,
        now: Long
    ) {
        playerMods.entries.removeIf { it.value.isExpired(now) }
        if (playerMods.isEmpty()) {
            modifiers.remove(playerUuid, playerMods)
        }
    }

    private fun normalize(value: String): String = value.lowercase(Locale.ROOT)

    private fun compositeKey(key: String, normalizedStat: String): String {
        return "${normalize(key)}:$normalizedStat"
    }
}

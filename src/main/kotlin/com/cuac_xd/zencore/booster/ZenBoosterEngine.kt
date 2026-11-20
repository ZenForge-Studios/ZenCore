package com.cuac_xd.zencore.booster

import com.cuac_xd.zencore.party.ZenPartyHub
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.Locale
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Defines how multiple active boosters in the same category combine into a single multiplier.
 */
enum class BoosterStackMode {
    /** Adds each booster's bonus over 1.0: `1.0 + sum((multiplier - 1.0).coerceAtLeast(0.0))`. */
    SUM,

    /** Multiplies all active booster multipliers together: `product(multiplier)`. */
    MULTIPLY,

    /** Picks the single highest multiplier among all active boosters. */
    HIGHEST
}

/**
 * Represents a personal, global, party-shared, or permission-granted multiplier booster.
 *
 * @property id Unique identifier of this booster within its scope.
 * @property category Booster category (matched case-insensitively, e.g., `"xp"`, `"drops"`, `"damage"`).
 * @property multiplier Multiplier value (e.g., `1.5` for +50% or `2.0` for 2x).
 * @property expiresAt Epoch millisecond timestamp when this booster expires, or `0L` if permanent.
 * @property shareWithParty Whether party members receive this booster while in the same party.
 */
data class ActiveBooster @JvmOverloads constructor(
    val id: String,
    val category: String,
    val multiplier: Double,
    val expiresAt: Long,
    val shareWithParty: Boolean = false,
    val scope: ZenBoosterScope = ZenBoosterScope.PERSONAL,
    val ownerUuid: UUID? = null,
    val ownerName: String? = null,
    val typeId: String? = null
) {
    /**
     * Returns `true` if this booster has a finite expiration and has reached or passed [now].
     */
    fun isExpired(now: Long = System.currentTimeMillis()): Boolean = expiresAt > 0L && now >= expiresAt

    /**
     * Returns remaining whole seconds until expiration, `-1L` if permanent, or `0L` if expired.
     */
    fun remainingSeconds(now: Long = System.currentTimeMillis()): Long {
        if (expiresAt <= 0L) return -1L
        return ((expiresAt - now) / 1000L).coerceAtLeast(0L)
    }
}

/**
 * Thread-safe booster engine supporting personal, global, party-shared, and permission-based boosters.
 */
object ZenBoosterEngine {

    private val providerRef = java.util.concurrent.atomic.AtomicReference<ZenBoosterProvider?>(null)
    private val listeners = java.util.concurrent.CopyOnWriteArrayList<ZenBoosterListener>()

    private val CATEGORY_ALIASES = mapOf(
        "eco" to "economy",
        "money" to "economy",
        "balance" to "economy",
        "currency" to "economy",
        "vault" to "economy",
        "exp" to "xp",
        "experience" to "xp",
        "vanilla_xp" to "xp",
        "skills" to "skills_xp",
        "skill_xp" to "skills_xp",
        "ecoskills" to "skills_xp",
        "jobs" to "jobs_money",
        "jobs_eco" to "jobs_money",
        "mcmmo" to "mcmmo_xp",
        "mobs" to "mob_coins",
        "mobcoins" to "mob_coins",
        "drops" to "drop_rate",
        "loot" to "drop_rate",
        "mining" to "mining_speed",
        "haste" to "mining_speed",
        "speed" to "movement_speed",
        "walk_speed" to "movement_speed",
        "attack" to "attack_speed",
        "health" to "max_health",
        "hp" to "max_health",
        "scale" to "player_scale",
        "size" to "player_scale"
    )

    @JvmStatic
    fun registerProvider(provider: ZenBoosterProvider) {
        providerRef.set(provider)
    }

    @JvmStatic
    @JvmOverloads
    fun unregisterProvider(provider: ZenBoosterProvider? = null) {
        if (provider == null) {
            providerRef.set(null)
        } else {
            providerRef.compareAndSet(provider, null)
        }
    }

    @JvmStatic
    fun getProvider(): ZenBoosterProvider? = providerRef.get()

    @JvmStatic
    fun isAvailable(): Boolean = providerRef.get() != null

    @JvmStatic
    fun addListener(listener: ZenBoosterListener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
    }

    @JvmStatic
    fun removeListener(listener: ZenBoosterListener) {
        listeners.remove(listener)
    }

    @JvmStatic
    fun clearListeners() {
        listeners.clear()
    }

    @JvmStatic
    @JvmOverloads
    fun notifyBoosterActivated(booster: ActiveBooster, activatorUuid: UUID? = null) {
        for (listener in listeners) {
            runCatching { listener.onBoosterActivated(booster, activatorUuid) }
        }
    }

    @JvmStatic
    @JvmOverloads
    fun notifyBoosterExpired(booster: ActiveBooster, reason: String = "EXPIRED") {
        for (listener in listeners) {
            runCatching { listener.onBoosterExpired(booster, reason) }
        }
    }

    @JvmStatic
    fun normalizeCategoryAlias(rawCategory: String): String {
        val cleaned = rawCategory.trim().lowercase(java.util.Locale.ROOT).replace('-', '_')
        return CATEGORY_ALIASES[cleaned] ?: cleaned
    }

    @JvmStatic
    @JvmOverloads
    fun getAllActiveBoosters(playerUuid: UUID? = null, category: String? = null): List<ActiveBooster> {
        val now = System.currentTimeMillis()
        val combined = mutableListOf<ActiveBooster>()
        val provider = providerRef.get()
        if (provider != null) {
            runCatching { provider.getActiveBoosters(playerUuid, category) }
                .getOrNull()
                ?.let { combined.addAll(it) }
        }
        combined.addAll(collectApplicableBoosters(playerUuid, category, now))
        return combined
    }

    @JvmStatic
    fun getTeamBoosters(player: org.bukkit.entity.Player): List<ActiveBooster> {
        val provider = providerRef.get()
        if (provider != null) {
            return runCatching { provider.getTeamBoosters(player) }.getOrDefault(emptyList())
        }
        return emptyList()
    }

    @JvmStatic
    fun getPersonalBoosters(playerUuid: UUID): List<ActiveBooster> {
        val provider = providerRef.get()
        if (provider != null) {
            return runCatching { provider.getPersonalBoosters(playerUuid) }.getOrDefault(emptyList())
        }
        val now = System.currentTimeMillis()
        return collectApplicableBoosters(playerUuid, null, now)
            .filter { it.scope == ZenBoosterScope.PERSONAL }
    }

    @JvmStatic
    fun getPrimaryGlobalBooster(): ActiveBooster? {
        val provider = providerRef.get()
        if (provider != null) {
            val fromProvider = runCatching { provider.getPrimaryGlobalBooster() }.getOrNull()
            if (fromProvider != null) return fromProvider
        }
        val now = System.currentTimeMillis()
        return collectApplicableBoosters(null, null, now)
            .maxByOrNull { it.multiplier }
    }

    @JvmStatic
    fun getBoosterById(boosterId: String): ZenBoosterDefinition? {
        val provider = providerRef.get() ?: return null
        return runCatching { provider.getBoosterById(boosterId) }.getOrNull()
    }

    @JvmStatic
    fun getAllBoosterDefinitions(): Collection<ZenBoosterDefinition> {
        val provider = providerRef.get() ?: return emptyList()
        return runCatching { provider.getAllBoosterDefinitions() }.getOrDefault(emptyList())
    }

    @JvmStatic
    @JvmOverloads
    fun activateBooster(
        boosterId: String,
        activator: org.bukkit.entity.Player,
        bypassCooldown: Boolean = false
    ): Boolean {
        val provider = providerRef.get() ?: return false
        return runCatching { provider.activateBooster(boosterId, activator, bypassCooldown) }.getOrDefault(false)
    }

    @JvmStatic
    @JvmOverloads
    fun giveBooster(targetUuid: UUID, boosterId: String, amount: Int = 1): Boolean {
        val provider = providerRef.get() ?: return false
        return runCatching { provider.giveBooster(targetUuid, boosterId, amount) }.getOrDefault(false)
    }

    @JvmStatic
    @JvmOverloads
    fun stopBooster(
        boosterId: String,
        scope: ZenBoosterScope? = null,
        targetUuid: UUID? = null
    ): Int {
        var count = 0
        val provider = providerRef.get()
        if (provider != null) {
            count += runCatching { provider.stopBooster(boosterId, scope, targetUuid) }.getOrDefault(0)
        }
        val key = normalizeKey(boosterId)
        if (targetUuid == null && (scope == null || scope == ZenBoosterScope.GLOBAL)) {
            val removed = globalBoosters.remove(key)
            if (removed != null) {
                count++
                notifyBoosterExpired(removed, "STOPPED")
            }
        }
        if (targetUuid != null && (scope == null || scope == ZenBoosterScope.PERSONAL)) {
            val map = playerBoosters[targetUuid]
            val removed = map?.remove(key)
            if (removed != null) {
                if (map.isEmpty()) {
                    playerBoosters.remove(targetUuid, map)
                }
                count++
                notifyBoosterExpired(removed, "STOPPED")
            }
        } else if (targetUuid == null && scope == ZenBoosterScope.PERSONAL) {
            for ((uuid, map) in playerBoosters.entries) {
                val removed = map.remove(key)
                if (removed != null) {
                    if (map.isEmpty()) {
                        playerBoosters.remove(uuid, map)
                    }
                    count++
                    notifyBoosterExpired(removed, "STOPPED")
                }
            }
        }
        return count
    }

    @JvmStatic
    fun openMainMenu(player: org.bukkit.entity.Player) {
        providerRef.get()?.let { runCatching { it.openMainMenu(player) } }
    }

    @JvmStatic
    fun openVaultMenu(player: org.bukkit.entity.Player) {
        providerRef.get()?.let { runCatching { it.openVaultMenu(player) } }
    }

    private const val PERMISSION_PREFIX = "zencore.booster."

    private val globalBoosters = ConcurrentHashMap<String, ActiveBooster>()
    private val playerBoosters = ConcurrentHashMap<UUID, ConcurrentHashMap<String, ActiveBooster>>()

    /**
     * Registers or replaces a personal booster for [playerUuid].
     * A [durationSeconds] `<= 0` creates a permanent booster (`expiresAt = 0L`).
     */
    @JvmStatic
    @JvmOverloads
    fun addPlayerBooster(
        playerUuid: UUID,
        id: String,
        category: String,
        multiplier: Double,
        durationSeconds: Long,
        shareWithParty: Boolean = false
    ) {
        val booster = ActiveBooster(
            id = id,
            category = category,
            multiplier = multiplier,
            expiresAt = computeExpiresAt(durationSeconds),
            shareWithParty = shareWithParty
        )
        playerBoosters
            .computeIfAbsent(playerUuid) { ConcurrentHashMap() }[normalizeKey(id)] = booster
    }

    /**
     * Registers or replaces a server-wide global booster.
     * A [durationSeconds] `<= 0` creates a permanent booster (`expiresAt = 0L`).
     */
    @JvmStatic
    fun addGlobalBooster(
        id: String,
        category: String,
        multiplier: Double,
        durationSeconds: Long
    ) {
        val booster = ActiveBooster(
            id = id,
            category = category,
            multiplier = multiplier,
            expiresAt = computeExpiresAt(durationSeconds),
            shareWithParty = false
        )
        globalBoosters[normalizeKey(id)] = booster
    }

    /**
     * Removes a booster by [id]. Pass `null` for [playerUuid] to remove a global booster.
     */
    @JvmStatic
    fun removeBooster(playerUuid: UUID?, id: String) {
        val key = normalizeKey(id)
        if (playerUuid == null) {
            globalBoosters.remove(key)
            return
        }
        val map = playerBoosters[playerUuid] ?: return
        map.remove(key)
        if (map.isEmpty()) {
            playerBoosters.remove(playerUuid, map)
        }
    }

    /**
     * Returns `true` if [playerUuid] currently benefits from at least one non-expired booster
     * matching [category] (personal, global, party-shared, or online permission booster).
     */
    @JvmStatic
    fun hasActiveBooster(playerUuid: UUID, category: String): Boolean {
        val now = System.currentTimeMillis()
        if (collectApplicableBoosters(playerUuid, category, now).isNotEmpty()) {
            return true
        }
        val provider = providerRef.get() ?: return false
        return runCatching { provider.hasActiveBooster(playerUuid, category) }.getOrDefault(false)
    }

    /**
     * Computes the combined multiplier for [playerUuid] (or only global boosters if `null`)
     * in [category] using [mode]. Returns `1.0` when no boosters apply.
     */
    @JvmStatic
    @JvmOverloads
    fun getMultiplier(
        playerUuid: UUID?,
        category: String,
        mode: BoosterStackMode = BoosterStackMode.SUM
    ): Double {
        val now = System.currentTimeMillis()
        val boosters = collectApplicableBoosters(playerUuid, category, now)
        val localMul = if (boosters.isEmpty()) {
            1.0
        } else {
            when (mode) {
                BoosterStackMode.SUM -> 1.0 + boosters.sumOf { (it.multiplier - 1.0).coerceAtLeast(0.0) }
                BoosterStackMode.MULTIPLY -> boosters.fold(1.0) { acc, booster -> acc * booster.multiplier }
                BoosterStackMode.HIGHEST -> boosters.maxOfOrNull { it.multiplier } ?: 1.0
            }
        }

        val provider = providerRef.get() ?: return localMul
        val providerMul = runCatching { provider.getMultiplier(playerUuid, category, mode) }.getOrDefault(1.0)
        if (boosters.isEmpty()) return providerMul
        if (providerMul == 1.0) return localMul

        return when (mode) {
            BoosterStackMode.SUM -> localMul + (providerMul - 1.0)
            BoosterStackMode.MULTIPLY -> localMul * providerMul
            BoosterStackMode.HIGHEST -> maxOf(localMul, providerMul)
        }
    }

    /**
     * Returns all non-expired boosters currently affecting [playerUuid]
     * (provider boosters + personal, global, shared party boosters, and permission boosters if online).
     */
    @JvmStatic
    fun getActiveBoosters(playerUuid: UUID): List<ActiveBooster> {
        return getAllActiveBoosters(playerUuid, null)
    }

    /**
     * Returns all currently non-expired global boosters (including provider global boosters).
     */
    @JvmStatic
    fun getGlobalBoosters(): List<ActiveBooster> {
        val now = System.currentTimeMillis()
        val combined = mutableListOf<ActiveBooster>()
        val provider = providerRef.get()
        if (provider != null) {
            runCatching { provider.getGlobalBoosters() }
                .getOrNull()
                ?.let { combined.addAll(it) }
        }
        combined.addAll(globalBoosters.values.filter { !it.isExpired(now) })
        return combined
    }

    /**
     * Extracts permission-based boosters for an online [player] matching `zencore.booster.<category>.<value>`.
     * Supports decimal multipliers (e.g. `1.5`, `1_5`) or percentages (e.g. `50%` -> `1.5`).
     */
    @JvmStatic
    @JvmOverloads
    fun getPermissionBoosters(player: Player, category: String? = null): List<ActiveBooster> {
        val perms = runCatching { player.effectivePermissions }.getOrNull() ?: return emptyList()
        val result = mutableListOf<ActiveBooster>()
        for (info in perms) {
            if (!info.value) continue
            val perm = info.permission.lowercase(Locale.ROOT)
            if (!perm.startsWith(PERMISSION_PREFIX)) continue

            val rest = perm.substring(PERMISSION_PREFIX.length)
            val dotIndex = rest.indexOf('.')
            if (dotIndex <= 0 || dotIndex >= rest.length - 1) continue

            val permCategory = rest.substring(0, dotIndex)
            if (category != null && !permCategory.equals(category, ignoreCase = true)) continue

            val rawValue = rest.substring(dotIndex + 1)
            val multiplier = parsePermissionMultiplier(rawValue) ?: continue
            result.add(
                ActiveBooster(
                    id = "perm:$permCategory:$rawValue",
                    category = permCategory,
                    multiplier = multiplier,
                    expiresAt = 0L,
                    shareWithParty = false
                )
            )
        }
        return result
    }

    /**
     * Computes the highest permission-granted booster multiplier for [player] in [category], or `1.0` if none.
     */
    @JvmStatic
    fun getPermissionMultiplier(player: Player, category: String): Double {
        return getPermissionBoosters(player, category).maxOfOrNull { it.multiplier } ?: 1.0
    }

    /**
     * Removes all expired global and player boosters.
     */
    @JvmStatic
    fun pruneExpired() {
        val now = System.currentTimeMillis()
        globalBoosters.entries.removeIf { it.value.isExpired(now) }

        val iterator = playerBoosters.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            entry.value.entries.removeIf { it.value.isExpired(now) }
            if (entry.value.isEmpty()) {
                iterator.remove()
            }
        }
    }

    /**
     * Clears all global and player boosters. Intended for test isolation and shutdown cleanup.
     */
    @JvmStatic
    fun clearAll() {
        globalBoosters.clear()
        playerBoosters.clear()
    }

    private fun collectApplicableBoosters(
        playerUuid: UUID?,
        category: String?,
        now: Long
    ): List<ActiveBooster> {
        val collected = mutableListOf<ActiveBooster>()

        for (booster in globalBoosters.values) {
            if (!booster.isExpired(now) && matchesCategory(booster, category)) {
                collected.add(booster)
            }
        }

        if (playerUuid == null) {
            return collected
        }

        playerBoosters[playerUuid]?.values?.forEach { booster ->
            if (!booster.isExpired(now) && matchesCategory(booster, category)) {
                collected.add(booster)
            }
        }

        if (ZenPartyHub.isInParty(playerUuid)) {
            val partyId = ZenPartyHub.getPartyId(playerUuid)
            if (partyId != null) {
                for (memberUuid in ZenPartyHub.getMembers(partyId)) {
                    if (memberUuid == playerUuid) continue
                    playerBoosters[memberUuid]?.values?.forEach { booster ->
                        if (booster.shareWithParty && !booster.isExpired(now) && matchesCategory(booster, category)) {
                            collected.add(booster)
                        }
                    }
                }
            }
        }

        val onlinePlayer = getOnlinePlayerSafely(playerUuid)
        if (onlinePlayer != null) {
            collected.addAll(getPermissionBoosters(onlinePlayer, category))
        }

        return collected
    }

    private fun matchesCategory(booster: ActiveBooster, category: String?): Boolean {
        if (category == null) return true
        if (booster.category.equals(category, ignoreCase = true)) return true
        return normalizeCategoryAlias(booster.category) == normalizeCategoryAlias(category)
    }

    private fun computeExpiresAt(durationSeconds: Long): Long {
        return if (durationSeconds <= 0L) 0L else System.currentTimeMillis() + durationSeconds * 1000L
    }

    private fun normalizeKey(value: String): String = value.lowercase(Locale.ROOT)

    private fun parsePermissionMultiplier(rawValue: String): Double? {
        val parsed = if (rawValue.endsWith("%")) {
            rawValue.dropLast(1).toDoubleOrNull()?.let { 1.0 + it / 100.0 }
        } else {
            rawValue.replace('_', '.').toDoubleOrNull()
        }
        return parsed?.takeIf { it.isFinite() && it > 0.0 }
    }

    private val bukkitGetServerMethod = runCatching { Bukkit::class.java.getMethod("getServer") }.getOrNull()

    private fun getOnlinePlayerSafely(playerUuid: UUID): Player? {
        val hasServer = runCatching { bukkitGetServerMethod?.invoke(null) != null }.getOrDefault(false)
        if (!hasServer) return null
        return runCatching { Bukkit.getPlayer(playerUuid) }.getOrNull()
    }
}

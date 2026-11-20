package com.cuac_xd.zencore.hooks

import com.cuac_xd.zencore.integration.IntegrationHub
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.UUID

/**
 * Hub unificado de ganchos del ecosistema Zen.
 * Proporciona acceso a todos los plugins hermanos y servicios externos de forma segura.
 */
object ZenHooksHub {

    /**
     * Acceso a economías: ZenEconomy (multidivisa) o Vault.
     */
    @JvmStatic
    val economy get() = ZenEconomyHook

    /**
     * Acceso a cosméticos: ZenCosmetics.
     */
    @JvmStatic
    val cosmetics get() = ZenCosmeticsHook

    /**
     * Acceso a items personalizados: ZenCustomItems.
     */
    @JvmStatic
    val customItems get() = ZenCustomItemsHook

    /**
     * Acceso a encantamientos custom: ZenEnchants.
     */
    @JvmStatic
    val enchants get() = ZenEnchantsHook

    /**
     * Acceso al sistema de partys: ZenPartys / ZenPartyHub.
     */
    @JvmStatic
    val parties get() = ZenPartyHook

    /**
     * Acceso al sistema de boosters: ZenBoosters / ZenBoosterEngine.
     */
    @JvmStatic
    val boosters get() = ZenBoosterHook

    /**
     * Acceso al sistema de tags múltiples: ZenTags.
     */
    @JvmStatic
    val tags get() = ZenTagsHook

    /**
     * Acceso al sistema de Discord: ZenDiscord.
     */
    @JvmStatic
    val discord get() = ZenDiscordHook

    /** Acceso a subastas: ZenAuctions */
    @JvmStatic val auctions get() = ZenAuctionsHook

    /** Acceso a trabajos y profesiones: ZenJobs */
    @JvmStatic val jobs get() = ZenJobsHook

    /** Acceso a monturas y establos: ZenHorses */
    @JvmStatic val horses get() = ZenHorsesHook

    /** Acceso a bóvedas virtuales y almacenamiento: ZenStorage */
    @JvmStatic val storage get() = ZenStorageHook

    /** Acceso a sanciones y moderación: ZenBans */
    @JvmStatic val bans get() = ZenBansHook

    /** Acceso a cuentas bancarias e intereses: ZenBank */
    @JvmStatic val bank get() = ZenBankHook

    /** Acceso a zonas AFK y recompensas de tiempo: ZenAfkZone */
    @JvmStatic val afkZone get() = ZenAfkZoneHook

    /** Acceso a canales de chat y chat bubbles: ZenChat */
    @JvmStatic val chat get() = ZenChatHook

    /** Acceso a minijuegos en chat: ZenChatGames */
    @JvmStatic val chatGames get() = ZenChatGamesHook

    /** Acceso a eventos de Dragón: ZenDragonEvent */
    @JvmStatic val dragonEvent get() = ZenDragonEventHook

    /** Acceso a Fairy Souls y perks pasivos: ZenFairy */
    @JvmStatic val fairy get() = ZenFairyHook

    /** Acceso a forjas avanzadas y herrería: ZenForges */
    @JvmStatic val forges get() = ZenForgesHook

    /** Acceso a lobby, parkour y hotbar: ZenHub */
    @JvmStatic val hub get() = ZenHubHook

    /** Acceso a misiones y misiones diarias: ZenQuests */
    @JvmStatic val quests get() = ZenQuestsHook

    /** Acceso a progresión de rangos y prestigios: ZenRankups */
    @JvmStatic val rankups get() = ZenRankupsHook

    /** Acceso a recompensas diarias y calendarios: ZenRewards */
    @JvmStatic val rewards get() = ZenRewardsHook

    /** Acceso a mazmorras y progresión roguelike: ZenRogueLike */
    @JvmStatic val rogueLike get() = ZenRogueLikeHook

    /** Acceso a spawners apilables y cálculo de drops: ZenSpawner */
    @JvmStatic val spawner get() = ZenSpawnerHook

    /** Acceso a regeneración de bloques y minas: ZenBlockRegen */
    @JvmStatic val blockRegen get() = ZenBlockRegenHook

    /** Acceso a concursos agrícolas estilo Jacob: ZenContest */
    @JvmStatic val contest get() = ZenContestHook

    /** Acceso a cajas de recompensa y llaves: ZenCrates */
    @JvmStatic val crates get() = ZenCratesHook

    private fun isVaultCurrency(currency: String): Boolean {
        val normalized = currency.trim()
        return normalized.isEmpty() ||
            normalized.equals("vault", ignoreCase = true) ||
            normalized.equals("money", ignoreCase = true)
    }

    private fun resolveOnlinePlayer(player: OfflinePlayer): Player? =
        (player as? Player) ?: runCatching { player.player }.getOrNull()

    private fun isZenEconomyInstalled(): Boolean =
        runCatching { ZenEconomyHook.isInstalled() }.getOrDefault(false)

    /**
     * Indica si el sistema de partys está disponible vía proveedor o hook reflexivo.
     */
    @JvmStatic
    fun isPartyAvailable(): Boolean = ZenPartyHook.isInstalled()

    /**
     * Indica si ZenBoosters está disponible vía proveedor o hook reflexivo.
     */
    @JvmStatic
    fun isBoostersAvailable(): Boolean = ZenBoosterHook.isInstalled()

    /**
     * Indica si ZenTags está disponible vía hook reflexivo.
     */
    @JvmStatic
    fun isTagsAvailable(): Boolean = ZenTagsHook.isInstalled()

    @JvmStatic fun isAuctionsAvailable(): Boolean = ZenAuctionsHook.isInstalled()
    @JvmStatic fun isJobsAvailable(): Boolean = ZenJobsHook.isInstalled()
    @JvmStatic fun isHorsesAvailable(): Boolean = ZenHorsesHook.isInstalled()
    @JvmStatic fun isStorageAvailable(): Boolean = ZenStorageHook.isInstalled()
    @JvmStatic fun isBansAvailable(): Boolean = ZenBansHook.isInstalled()
    @JvmStatic fun isBankAvailable(): Boolean = ZenBankHook.isInstalled()
    @JvmStatic fun isAfkZoneAvailable(): Boolean = ZenAfkZoneHook.isInstalled()
    @JvmStatic fun isChatAvailable(): Boolean = ZenChatHook.isInstalled()
    @JvmStatic fun isChatGamesAvailable(): Boolean = ZenChatGamesHook.isInstalled()
    @JvmStatic fun isDragonEventAvailable(): Boolean = ZenDragonEventHook.isInstalled()
    @JvmStatic fun isFairyAvailable(): Boolean = ZenFairyHook.isInstalled()
    @JvmStatic fun isForgesAvailable(): Boolean = ZenForgesHook.isInstalled()
    @JvmStatic fun isHubAvailable(): Boolean = ZenHubHook.isInstalled()
    @JvmStatic fun isQuestsAvailable(): Boolean = ZenQuestsHook.isInstalled()
    @JvmStatic fun isRankupsAvailable(): Boolean = ZenRankupsHook.isInstalled()
    @JvmStatic fun isRewardsAvailable(): Boolean = ZenRewardsHook.isInstalled()
    @JvmStatic fun isRogueLikeAvailable(): Boolean = ZenRogueLikeHook.isInstalled()
    @JvmStatic fun isSpawnerAvailable(): Boolean = ZenSpawnerHook.isInstalled()
    @JvmStatic fun isBlockRegenAvailable(): Boolean = ZenBlockRegenHook.isInstalled()
    @JvmStatic fun isContestAvailable(): Boolean = ZenContestHook.isInstalled()
    @JvmStatic fun isCratesAvailable(): Boolean = ZenCratesHook.isInstalled()

    /**
     * Indica si hay un proveedor económico disponible para la moneda indicada.
     */
    @JvmStatic
    @JvmOverloads
    fun isEconomyAvailable(currency: String = "vault"): Boolean {
        return if (isVaultCurrency(currency)) {
            IntegrationHub.isVaultAvailable()
        } else {
            isZenEconomyInstalled()
        }
    }

    /**
     * Devuelve un mapa con el estado de disponibilidad de los hooks del ecosistema.
     */
    @JvmStatic
    fun getAvailableHooks(): Map<String, Boolean> = mapOf(
        "vault" to IntegrationHub.isVaultAvailable(),
        "placeholderapi" to IntegrationHub.isPlaceholderApiAvailable(),
        "economy" to isZenEconomyInstalled(),
        "cosmetics" to runCatching { ZenCosmeticsHook.isInstalled() }.getOrDefault(false),
        "customItems" to runCatching { ZenCustomItemsHook.isInstalled() }.getOrDefault(false),
        "enchants" to runCatching { ZenEnchantsHook.isInstalled() }.getOrDefault(false),
        "parties" to ZenPartyHook.isInstalled(),
        "boosters" to ZenBoosterHook.isInstalled(),
        "tags" to ZenTagsHook.isInstalled(),
        "discord" to ZenDiscordHook.isInstalled(),
        "auctions" to ZenAuctionsHook.isInstalled(),
        "jobs" to ZenJobsHook.isInstalled(),
        "horses" to ZenHorsesHook.isInstalled(),
        "storage" to ZenStorageHook.isInstalled(),
        "bans" to ZenBansHook.isInstalled(),
        "bank" to ZenBankHook.isInstalled(),
        "afkZone" to ZenAfkZoneHook.isInstalled(),
        "chat" to ZenChatHook.isInstalled(),
        "chatGames" to ZenChatGamesHook.isInstalled(),
        "dragonEvent" to ZenDragonEventHook.isInstalled(),
        "fairy" to ZenFairyHook.isInstalled(),
        "forges" to ZenForgesHook.isInstalled(),
        "hub" to ZenHubHook.isInstalled(),
        "quests" to ZenQuestsHook.isInstalled(),
        "rankups" to ZenRankupsHook.isInstalled(),
        "rewards" to ZenRewardsHook.isInstalled(),
        "rogueLike" to ZenRogueLikeHook.isInstalled(),
        "spawner" to ZenSpawnerHook.isInstalled(),
        "blockRegen" to ZenBlockRegenHook.isInstalled(),
        "contest" to ZenContestHook.isInstalled(),
        "crates" to ZenCratesHook.isInstalled()
    )

    /**
     * Comprueba si un plugin específico del ecosistema o Bukkit está presente y activo.
     */
    @JvmStatic
    fun isPluginAvailable(name: String): Boolean {
        val hooks = getAvailableHooks()
        if (hooks[name] == true) return true
        val normalized = name.trim().lowercase().removePrefix("zen")
        for ((key, value) in hooks) {
            if (key.lowercase() == normalized && value) return true
        }
        return runCatching {
            val p = org.bukkit.Bukkit.getPluginManager().getPlugin(name)
            p != null && p.isEnabled
        }.getOrDefault(false)
    }

    /**
     * Consulta el saldo de un jugador en cualquier moneda soportada (Vault o ZenEconomy).
     */
    @JvmStatic
    fun getBalanceAny(player: OfflinePlayer, currency: String): Double {
        val normalized = currency.trim()
        if (isVaultCurrency(normalized)) {
            if (!IntegrationHub.isVaultAvailable()) return 0.0
            return runCatching { IntegrationHub.getEconomy()?.getBalance(player) ?: 0.0 }.getOrDefault(0.0)
        }
        if (isZenEconomyInstalled()) {
            return ZenEconomyHook.getBalance(player, normalized)
        }
        return 0.0
    }

    /**
     * Retira cualquier tipo de moneda soportada:
     * Si currency es "vault", "money" o vacía, usa Vault.
     * Si no, consulta ZenEconomy.
     */
    @JvmStatic
    fun withdrawAny(player: OfflinePlayer, currency: String, amount: Double): Boolean {
        if (amount.isNaN() || amount <= 0.0) return false
        val normalized = currency.trim()
        if (isVaultCurrency(normalized)) {
            if (!IntegrationHub.isVaultAvailable()) return false
            val online = resolveOnlinePlayer(player)
            if (online != null) return IntegrationHub.withdrawMoney(online, amount)
            return runCatching {
                IntegrationHub.getEconomy()?.withdrawPlayer(player, amount)?.transactionSuccess() == true
            }.getOrDefault(false)
        }
        if (isZenEconomyInstalled()) {
            return ZenEconomyHook.withdraw(player, normalized, amount)
        }
        return false
    }

    /**
     * Deposita cualquier tipo de moneda soportada.
     */
    @JvmStatic
    fun depositAny(player: OfflinePlayer, currency: String, amount: Double): Boolean {
        if (amount.isNaN() || amount <= 0.0) return false
        val normalized = currency.trim()
        if (isVaultCurrency(normalized)) {
            if (!IntegrationHub.isVaultAvailable()) return false
            val online = resolveOnlinePlayer(player)
            if (online != null) return IntegrationHub.depositMoney(online, amount)
            return runCatching {
                IntegrationHub.getEconomy()?.depositPlayer(player, amount)?.transactionSuccess() == true
            }.getOrDefault(false)
        }
        if (isZenEconomyInstalled()) {
            return ZenEconomyHook.deposit(player, normalized, amount)
        }
        return false
    }

    /**
     * Deposita moneda a los miembros en línea de una party, con opción de dividir el monto equitativamente.
     * Devuelve la cantidad de miembros que recibieron el depósito.
     */
    @JvmStatic
    @JvmOverloads
    fun depositPartyAny(partyId: UUID, currency: String, amount: Double, split: Boolean = false): Int {
        if (amount.isNaN() || amount <= 0.0) return 0
        val members = ZenPartyHook.getOnlineMembers(partyId)
        if (members.isEmpty()) return 0
        val perMember = if (split) amount / members.size.toDouble() else amount
        if (perMember <= 0.0) return 0
        var succeeded = 0
        for (member in members) {
            if (depositAny(member, currency, perMember)) {
                succeeded++
            }
        }
        return succeeded
    }

    /**
     * Comprueba si el jugador tiene suficiente saldo en la moneda dada.
     */
    @JvmStatic
    fun hasAny(player: OfflinePlayer, currency: String, amount: Double): Boolean {
        if (amount.isNaN() || amount < 0.0) return false
        val normalized = currency.trim()
        if (isVaultCurrency(normalized)) {
            if (!IntegrationHub.isVaultAvailable()) return false
            val online = resolveOnlinePlayer(player)
            if (online != null) return IntegrationHub.hasMoney(online, amount)
            return runCatching {
                IntegrationHub.getEconomy()?.has(player, amount) == true
            }.getOrDefault(false)
        }
        if (isZenEconomyInstalled()) {
            return ZenEconomyHook.has(player, normalized, amount)
        }
        return false
    }
}

package com.cuac_xd.zencore.booster

import org.bukkit.entity.Player
import java.util.UUID

/**
 * Alcance de aplicación de un booster dentro del ecosistema ZenCore.
 */
enum class ZenBoosterScope {
    PERSONAL,
    GLOBAL,
    TEAM,
    PASSIVE
}

/**
 * Definición estática de un booster configurado en un proveedor externo (como ZenBoosters).
 *
 * @property id Identificador único del booster.
 * @property displayName Nombre visible (MiniMessage/legacy).
 * @property category Categoría normalizada en ZenCore (p. ej. "economy", "xp", "skills_xp").
 * @property typeId Subtipo personalizado cuando la categoría es "custom" o específica.
 * @property scope Alcance del booster (PERSONAL, GLOBAL, TEAM, PASSIVE).
 * @property multiplier Factor multiplicador (p. ej. `1.5` para +50%, `2.0` para ×2.0).
 * @property rawModifier Modificador delta (p. ej. `0.5` para +50%, `1.0` para +100%).
 * @property durationSeconds Duración en segundos (`-1` si es permanente).
 * @property cooldownSeconds Cooldown en segundos entre activaciones.
 * @property permission Permiso requerido para activar (vacío si no requiere).
 */
data class ZenBoosterDefinition @JvmOverloads constructor(
    val id: String,
    val displayName: String,
    val category: String,
    val typeId: String? = null,
    val scope: ZenBoosterScope = ZenBoosterScope.PERSONAL,
    val multiplier: Double = 1.5,
    val rawModifier: Double = multiplier - 1.0,
    val durationSeconds: Long = 60L,
    val cooldownSeconds: Long = 0L,
    val permission: String = ""
)

/**
 * Contrato principal del proveedor de Boosters para ZenCore.
 * Un plugin proveedor (como ZenBoosters) implementa esta interfaz y se registra en [ZenBoosterEngine]
 * para ofrecer gestión persistente, GUIs, vouchers, cooldowns y scopes avanzados a todo el ecosistema.
 */
interface ZenBoosterProvider {

    fun hasActiveBooster(playerUuid: UUID, category: String): Boolean

    fun getMultiplier(playerUuid: UUID?, category: String, mode: BoosterStackMode): Double

    fun getActiveBoosters(playerUuid: UUID?, category: String?): List<ActiveBooster>

    fun getGlobalBoosters(): List<ActiveBooster>

    fun getTeamBoosters(player: Player): List<ActiveBooster>

    fun getPersonalBoosters(playerUuid: UUID): List<ActiveBooster>

    fun getPrimaryGlobalBooster(): ActiveBooster?

    fun getBoosterById(boosterId: String): ZenBoosterDefinition?

    fun getAllBoosterDefinitions(): Collection<ZenBoosterDefinition>

    fun activateBooster(boosterId: String, activator: Player, bypassCooldown: Boolean): Boolean

    fun giveBooster(targetUuid: UUID, boosterId: String, amount: Int): Boolean

    fun stopBooster(boosterId: String, scope: ZenBoosterScope?, targetUuid: UUID?): Int

    fun openMainMenu(player: Player)

    fun openVaultMenu(player: Player)
}

/**
 * Listener para que otros plugins basados en ZenCore reaccionen en tiempo real
 * al ciclo de vida de los boosters (activación y expiración/detención).
 */
interface ZenBoosterListener {
    fun onBoosterActivated(booster: ActiveBooster, activatorUuid: UUID?) {}
    fun onBoosterExpired(booster: ActiveBooster, reason: String) {}
}

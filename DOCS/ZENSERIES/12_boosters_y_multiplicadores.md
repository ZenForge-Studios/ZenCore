# 🚀 12. Boosters y Multiplicadores (`ZenBoosterEngine` + `ZenBoosters`)

El módulo de boosters (`com.cuac_xd.zencore.booster`) combina un **motor híbrido**:
1. **Proveedor externo persistente (`ZenBoosterProvider`):** implementado y registrado automáticamente por **`ZenBoosters`** al iniciar mediante `ZenBoostersAPI.init(...)` (con base de datos, scopes `PERSONAL` / `GLOBAL` / `TEAM` / `PASSIVE`, cooldowns, maldiciones, atributos vanilla 1.21+, bóveda/vouchers y menús GUI).
2. **Registro local en memoria (`ZenBoosterEngine`):** boosters temporales, compartidos en party (`shareWithParty = true`) y pasivos por permisos (`zencore.booster.<categoría>.<valor>`), disponibles incluso sin instalar `ZenBoosters`.

Cuando ambos están activos para un mismo jugador y categoría, `ZenBoosterEngine` **delega y combina** automáticamente los multiplicadores del proveedor (`ZenBoosters`) con los boosters locales según el `BoosterStackMode` elegido.

---

## 🔗 Arquitectura Híbrida y Alias de Categorías

- **Conversión automática de factor vs. modificador:** en los YAML de `ZenBoosters`, `multiplier: 0.5` representa `+50%` (modificador delta) y `1.0` representa `×2.0` (`+100%`). Al exponerse en `ZenCore`, `ZenBoosters` normaliza el factor real (`1.0 + modifier` = `1.5`, `2.0`, etc.) tanto en `ZenBoosterEngine.ActiveBooster.multiplier` como en `ZenBoosterDefinition`.
- **Integración automática con Partys (`ZenPartyTeamHook`):** dentro de `ZenBoosters`, `ZenPartyTeamHook` se registra con prioridad en `HookRegistry` consultando `ZenCoreAPI.partyHook.getPartyId(player)` para que cualquier booster de scope `TEAM` aplique automáticamente a todos los miembros de la party de `ZenPartys`.
- **Normalización de alias (`normalizeCategoryAlias`):** evita desajustes de nombres entre plugins:
  - `eco`, `money`, `balance`, `currency`, `vault` → `economy`
  - `exp`, `experience`, `vanilla_xp` → `xp`
  - `skills`, `skill_xp`, `ecoskills` → `skills_xp`
  - `jobs`, `jobs_eco` → `jobs_money`
  - `mcmmo` → `mcmmo_xp`
  - `mobs`, `mobcoins` → `mob_coins`
  - `drops`, `loot` → `drop_rate`
  - `mining`, `haste` → `mining_speed`
  - `speed`, `walk_speed` → `movement_speed`
  - `attack` → `attack_speed`
  - `health`, `hp` → `max_health`
  - `scale`, `size` → `player_scale`
  - Además, las definiciones `CUSTOM` con `type-id` (por ejemplo `type-id: "spawners"`) coinciden de forma aislada al consultar `"spawners"`.

---

## 🧩 Fuentes de Boosters y Modos de Apilamiento

Cuando consultas el multiplicador de un jugador en una categoría (case-insensitive, con alias o `type-id` custom), `ZenBoosterEngine` reúne automáticamente:
1. **Proveedor `ZenBoosters` (si está instalado):** boosters `GLOBAL`, `TEAM` (usando `ZenPartyTeamHook` u otros hooks de clanes), `PERSONAL` y `PASSIVE` (LuckPerms / equipamiento).
2. **Boosters Locales Globales:** activos en todo el servidor dentro de `ZenBoosterEngine`.
3. **Boosters Locales Personales:** asignados al `UUID` del jugador.
4. **Boosters Compartidos en Party:** boosters locales de otros miembros de la misma party (consultando `ZenPartyHub`) con `shareWithParty = true` (sin duplicar los propios).
5. **Boosters por Permisos:** permisos en jugadores conectados con el formato `zencore.booster.<categoría>.<valor>`.

### Modos de Combinación (`BoosterStackMode`)
| Modo | Fórmula | Ejemplo (`1.5x` + `2.0x`) |
| :--- | :--- | :--- |
| **`SUM`** *(por defecto)* | `1.0 + sum((multiplier - 1.0).coerceAtLeast(0.0))` | `1.0 + 0.5 + 1.0 = 2.5x` |
| **`MULTIPLY`** | `product(multiplier)` | `1.5 * 2.0 = 3.0x` |
| **`HIGHEST`** | `max(multiplier)` | `2.0x` |

> [!TIP]
> Si no existe ningún booster activo para la categoría consultada, cualquiera de los tres modos devuelve **`1.0`**.

### Boosters por Permisos (`zencore.booster.<categoría>.<valor>`)
Los rangos VIP o recompensas permanentes pueden otorgar boosters mediante permisos de Bukkit/LuckPerms sin necesidad de timers:
- **Decimal:** `zencore.booster.xp.1.5` o `zencore.booster.xp.1_5` → `1.5x` en `"xp"`.
- **Porcentaje:** `zencore.booster.drops.50%` → `1.5x` en `"drops"`.

---

## 📦 Modelos (`ActiveBooster`, `ZenBoosterScope`, `ZenBoosterDefinition`)

```kotlin
enum class ZenBoosterScope { PERSONAL, GLOBAL, TEAM, PASSIVE }

data class ActiveBooster @JvmOverloads constructor(
    val id: String,
    val category: String,
    val multiplier: Double,
    val expiresAt: Long,          // 0L = permanente
    val shareWithParty: Boolean = false,
    val scope: ZenBoosterScope = ZenBoosterScope.PERSONAL,
    val ownerUuid: UUID? = null,
    val ownerName: String? = null,
    val typeId: String? = null
)

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
```
- `isExpired(now = System.currentTimeMillis()): Boolean`: verdadero si `expiresAt > 0L && now >= expiresAt`.
- `remainingSeconds(now = System.currentTimeMillis()): Long`: segundos restantes, `-1L` si es permanente o `0L` si ya expiró.

> [!NOTE]
> `ZenCorePlugin` ejecuta `ZenBoosterEngine.pruneExpired()` periódicamente cada 60 segundos para limpiar boosters locales expirados de memoria, mientras que `ZenBoosters` gestiona la expiración en base de datos y notifica los eventos a los listeners de ZenCore.

---

## 💻 API en Código (Kotlin y Java)

Acceso recomendado mediante:
- `ZenCoreAPI.boosters` (`ZenBoosterEngine`, motor + hub de proveedor)
- `ZenCoreAPI.boosterHook` / `ZenCoreAPI.hooks.boosters` (`ZenBoosterHook`, con fallback reflexivo seguro sobre `ZenBoostersAPI`)

| Método | Descripción |
| :--- | :--- |
| `isAvailable()` / `ZenBoosterHook.isInstalled()` | Indica si hay un `ZenBoosterProvider` activo o `ZenBoosters` habilitado. |
| `hasActiveBooster(playerUuid, category)` | Comprueba tanto boosters locales como del proveedor `ZenBoosters`. |
| `getMultiplier(playerUuid, category, mode = BoosterStackMode.SUM)` | Combina el proveedor (`ZenBoosters`) y los boosters locales (`playerUuid = null` evalúa globales). |
| `getAllActiveBoosters(playerUuid = null, category = null)` | Lista boosters combinados de proveedor y memoria local (con filtro opcional de categoría). |
| `getTeamBoosters(player)` / `getPersonalBoosters(playerUuid)` | Consulta boosters por scope desde el proveedor (con fallback local para personales). |
| `getPrimaryGlobalBooster()` / `getGlobalBoosters()` | Obtiene el booster global principal o todos los globales vigentes. |
| `getBoosterById(boosterId)` / `getAllBoosterDefinitions()` | Consulta definiciones YAML cargadas en `ZenBoosters`. |
| `activateBooster(boosterId, activator, bypassCooldown = false)` | Activa un booster definido en `ZenBoosters` respetando permiso y cooldown. |
| `giveBooster(targetUuid, boosterId, amount = 1)` | Entrega vouchers virtuales a la bóveda del jugador en `ZenBoosters`. |
| `stopBooster(boosterId, scope = null, targetUuid = null)` | Detiene boosters activos en `ZenBoosters` y/o memoria local (devuelve cantidad detenida). |
| `openMainMenu(player)` / `openVaultMenu(player)` | Abre los menús GUI nativos de `ZenBoosters` para el jugador. |
| `addPlayerBooster(...)` / `addGlobalBooster(...)` / `removeBooster(...)` | Registra o elimina boosters locales en memoria (`durationSeconds <= 0` = permanente). |
| `addListener(listener)` / `removeListener(listener)` | Suscribe un `ZenBoosterListener` a activaciones y expiraciones en tiempo real. |

### Listener de Ciclo de Vida (`ZenBoosterListener`)
```kotlin
ZenCoreAPI.boosterHook.addListener(object : ZenBoosterListener {
    override fun onBoosterActivated(booster: ActiveBooster, activatorUuid: UUID?) {
        // Reaccionar cuando se activa un booster (por ejemplo actualizar hologramas/scoreboards)
    }

    override fun onBoosterExpired(booster: ActiveBooster, reason: String) {
        // Reaccionar cuando expira o es detenido (reason: NATURAL, FORCED, SHUTDOWN, STOPPED)
    }
})
```

### Ejemplo en Kotlin
```kotlin
import com.cuac_xd.zencore.api.ZenCoreAPI
import com.cuac_xd.zencore.booster.BoosterStackMode

// 1. Activar un booster de XP compartido con toda la party durante 30 minutos
ZenCoreAPI.boosters.addPlayerBooster(
    playerUuid = player.uniqueId,
    id = "vip_party_xp",
    category = "xp",
    multiplier = 1.5,
    durationSeconds = 1800L,
    shareWithParty = true
)

// 2. Activar un booster global de evento de 2.0x drops durante 1 hora
ZenCoreAPI.boosters.addGlobalBooster(
    id = "weekend_drops",
    category = "drops",
    multiplier = 2.0,
    durationSeconds = 3600L
)

// 3. Calcular el multiplicador final al otorgar experiencia o dinero
val xpMultiplier = ZenCoreAPI.boosters.getMultiplier(player.uniqueId, "xp", BoosterStackMode.SUM)
val finalXp = (baseXp * xpMultiplier).toInt()
player.giveExp(finalXp)
```

### Ejemplo en Java
```java
import com.cuac_xd.zencore.api.ZenCoreAPI;
import com.cuac_xd.zencore.booster.BoosterStackMode;
import com.cuac_xd.zencore.booster.ZenBoosterEngine;

ZenBoosterEngine.addPlayerBooster(player.getUniqueId(), "relic_damage", "damage", 1.25, 300L, false);

double dmgMultiplier = ZenCoreAPI.getBoosters().getMultiplier(
    player.getUniqueId(),
    "damage",
    BoosterStackMode.MULTIPLY
);
```

---

## ⚡ Efectos, Condiciones, Mutadores y Placeholders YAML

| Tipo | ID | Parámetros | Descripción |
| :--- | :--- | :--- | :--- |
| **Efecto** | `ACTIVATE_BOOSTER` | `id` / `booster` / `booster_id`, `category` (def. `"xp"`), `multiplier`, `duration`, `global`, `share_with_party`, `bypass_cooldown` | Si se pasa `id` sin `multiplier`/`duration` y `ZenBoosters` está activo, delega la activación al proveedor (`ZenBoosters`); en caso contrario registra un booster en memoria. |
| **Efecto** | `GIVE_BOOSTER` *(alias `GIVE_BOOSTER_VOUCHER`)* | `id` / `booster` / `booster_id`, `amount` (def. `1`) | Entrega vouchers virtuales del booster a la bóveda (`vault`) del jugador en `ZenBoosters`. |
| **Efecto** | `STOP_BOOSTER` *(alias `REMOVE_BOOSTER`)* | `id` / `booster` / `booster_id`, `scope` (`PERSONAL` \| `GLOBAL` \| `TEAM` \| `PASSIVE`), `global` (def. `false`) | Detiene el booster especificado en `ZenBoosters` y/o en memoria local. |
| **Efecto** | `OPEN_BOOSTER_MENU` | *(ninguno)* | Abre el menú principal de `ZenBoosters` para `ctx.player`. |
| **Efecto** | `OPEN_BOOSTER_VAULT` | *(ninguno)* | Abre la bóveda de boosters (`vault_menu`) de `ZenBoosters` para `ctx.player`. |
| **Condición** | `HAS_ACTIVE_BOOSTER` | `category` / `value` (opcional) | Comprueba si el jugador posee un booster activo en la categoría (incluyendo `ZenBoosters`). |
| **Mutador** | `APPLY_BOOSTER_MULTIPLIER` | `category` (def. `"damage"`), `mode` (`SUM` \| `MULTIPLY` \| `HIGHEST`, def. `"SUM"`), `variable` (opcional) | Multiplica `ctx.damage` (o `ctx.data[variable]`) por el multiplicador activo del jugador. |
| **Placeholder** | `%booster_multiplier%` / `%booster_multiplier_<cat>%` / `%booster_has_<cat>%` | `<cat>` (ej. `economy`, `xp`, `skills_xp`, `spawners`) | Resuelve en `ContextResolver` el multiplicador combinado o si tiene booster activo en la categoría. |

### Ejemplo YAML
```yaml
recompensa_booster_jefe:
  triggers:
    - kill_entity
  effects:
    # Delega a ZenBoosters si no especificas multiplier/duration:
    - type: GIVE_BOOSTER
      booster: "eco_2x_global"
      amount: 1
    - type: MESSAGE
      message: "<green>¡Recibiste un voucher de booster! Multiplicador Eco actual: %booster_multiplier_economy%x"

totem_furia_grupal:
  triggers:
    - right_click
  effects:
    - type: ACTIVATE_BOOSTER
      id: "party_fury"
      category: "damage"
      multiplier: "1.25 + (0.05 * %party_size%)"
      duration: 300
      share_with_party: true
      cooldown: 600
      mana_cost: 40
    - type: MESSAGE
      message: "<gold>¡Has activado un Booster de Daño compartido con tu party durante 5 minutos!"

escalado_dano_con_booster:
  triggers:
    - melee_attack
  conditions:
    - type: HAS_ACTIVE_BOOSTER
      args:
        category: "damage"
  mutators:
    - type: APPLY_BOOSTER_MULTIPLIER
      args:
        category: "damage"
        mode: "SUM"
```

# 🌐 Guía de Compatibilidad y Ecosistema de Plugins con ZenCore

**ZenCore** funciona como el núcleo central (ecosistema hub al estilo *libreforge/eco*) para toda la suite de plugins de Zen (`ZenSpawner`, `ZenEconomy`, `ZenCustomItems`, `ZenCosmetics`, `ZenEnchants`, `ZenPartys`, `ZenBoosters`, etc.).

Al hacer que tu plugin (por ejemplo, **ZenSpawner**) dependa de **ZenCore**, adquieres automáticamente:
1. **Acceso unificado a otros plugins** mediante `ZenCoreAPI.hooks`, `ZenCoreAPI.parties`, `ZenCoreAPI.partyHook`, `ZenCoreAPI.boosters` y `ZenCoreAPI.boosterHook` sin tener que añadir `depend` o `softdepend` a cada uno ni preocuparte por `ClassNotFoundException`.
2. **Efectos, condiciones, filtros y mutadores YAML listos para usar** para interactuar con monedas de `ZenEconomy`, cosméticos de `ZenCosmetics`, items de `ZenCustomItems`, encantamientos de `ZenEnchants`, partys de `ZenPartys`, boosters de `ZenBoosters`, regiones protegidas y `Vault`.
3. **Cero crash o incompatibilidad**: si el usuario no tiene instalado un plugin hermano (ej. `ZenCosmetics`, `ZenPartys` o `ZenBoosters`), las llamadas fallan de manera silenciosa y segura retornando valores por defecto (`false`, `0`, `1.0` para multiplicadores, `emptyList()`, `null`, etc.).

---

## 📦 1. Cómo Añadir ZenCore como Dependencia

### En tu `build.gradle.kts`:
```kotlin
repositories {
    mavenLocal()
    mavenCentral()
    // Repositorios de PaperMC, Spigot, etc.
}

dependencies {
    // Dependencia al core compilada contra tu plugin
    compileOnly("com.cuac_xd.zencore:zencore:1.0.0")
}
```

### En tu `paper-plugin.yml` o `plugin.yml`:
```yaml
name: ZenSpawner
version: '1.0.0'
main: com.cuac_xd.zenspawner.ZenSpawnerPlugin
api-version: '1.21'
dependencies:
  server:
    ZenCore:
      load: BEFORE
      required: true # ZenCore como dependencia principal
    # Opcionales si no usas ZenCore como puente directo:
    Vault:
      load: BEFORE
      required: false
```

---

## 🧩 2. Uso desde Código (Kotlin / Java)

Cualquier plugin puede invocar directamente `ZenCoreAPI` para interactuar con los plugins del ecosistema:

### 2.1. ZenEconomy (Multidivisa y Vault)
No necesitas importar `ZenEconomyAPI` en tu plugin; `ZenCore` gestiona todo:

```kotlin
import com.cuac_xd.zencore.api.ZenCoreAPI

// Comprobar si el plugin ZenEconomy está activo
if (ZenCoreAPI.economy.isInstalled()) {
    val currencies = ZenCoreAPI.economy.getRegisteredCurrencies() // ["crystals", "gems", "tokens"]
}

// Consultar saldo de una moneda custom
val gems = ZenCoreAPI.economy.getBalance(player, "gems")

// Cobrar al jugador (ej. al comprar un Spawner o Upgrade)
if (ZenCoreAPI.economy.has(player, "crystals", 500.0)) {
    ZenCoreAPI.economy.withdraw(player, "crystals", 500.0)
    player.sendMessage("¡Has mejorado tu spawner usando 500 Cristales!")
}

// También puedes usar el puente unificado (Vault o ZenEconomy según el identificador):
ZenCoreAPI.hooks.withdrawAny(player, "vault", 1000.0) // Usa Vault
ZenCoreAPI.hooks.withdrawAny(player, "souls", 50.0)    // Usa ZenEconomy "souls"
```

### 2.2. ZenCustomItems
Otorga, valida o crea items personalizados directamente:

```kotlin
// Comprobar si un item en mano es un item de ZenCustomItems
val itemInHand = player.inventory.itemInMainHand
if (ZenCoreAPI.customItems.isZenItem(itemInHand)) {
    val itemId = ZenCoreAPI.customItems.getItemId(itemInHand)
    if (ZenCoreAPI.customItems.matches(itemInHand, "SPAWNER_PICKAXE")) {
        // Lógica especial para minar spawners
    }
}

// Crear un item custom y entregarlo al jugador
ZenCoreAPI.customItems.giveItem(player, "LEGENDARY_SPAWNER_CORE", amount = 1)
```

### 2.3. ZenCosmetics
Desbloquea o equipa cosméticos (auras, efectos de victoria, sombreros, etc.):

```kotlin
// Desbloquear un cosmético como recompensa de un hito o spawner nivel maximo
if (!ZenCoreAPI.cosmetics.hasCosmetic(player, "spawner_master_aura")) {
    ZenCoreAPI.cosmetics.unlockCosmetic(player, "spawner_master_aura")
    ZenCoreAPI.cosmetics.equipCosmetic(player, "spawner_master_aura")
}
```

### 2.4. ZenEnchants
Aplica o consulta encantamientos personalizados sobre cualquier arma, armadura o herramienta:

```kotlin
val pickaxe = player.inventory.itemInMainHand

// Comprobar si la herramienta tiene un encantamiento de ZenEnchants
if (ZenCoreAPI.enchants.hasEnchantment(pickaxe, "spawner_magnet")) {
    val level = ZenCoreAPI.enchants.getEnchantmentLevel(pickaxe, "spawner_magnet")
    // Incrementar radio de recolección de drops del spawner según el nivel
}

// Aplicar un encantamiento de ZenEnchants a un item
ZenCoreAPI.enchants.applyEnchantment(pickaxe, "telepathy", level = 1, player = player)
```

### 2.5. Protección de Terrenos (WorldGuard, GriefPrevention, Towny)
Comprueba permisos de construcción y combate sin vincular librerías externas:

```kotlin
val block = targetLocation.block

// Comprueba si el jugador puede colocar/romper el spawner en esa ubicación
if (!ZenCoreAPI.protection.canBreak(player, block)) {
    player.sendMessage("§cNo tienes permiso para interactuar con spawners en esta región protegida.")
    return
}
```

### 2.6. ZenPartys (`ZenPartyHub` / `ZenPartyHook`)
Consulta partys, miembros cercanos, roles y reparte recompensas o teletransporta grupos sin depender directamente de `ZenPartys`:

```kotlin
import com.cuac_xd.zencore.api.ZenCoreAPI

// Comprobar disponibilidad (proveedor ZenPartyHub o fallback reflexivo ZenPartysAPI)
if (ZenCoreAPI.hooks.isPartyAvailable()) {
    val party = ZenCoreAPI.partyHook.getPartyByPlayer(player)
    if (party != null) {
        // Obtener miembros online en el mismo mundo dentro de 25 bloques
        val nearby = ZenCoreAPI.partyHook.getNearbyOnlineMembers(player, 25.0)
        val isModOrLeader = ZenCoreAPI.partyHook.isModeratorOrLeader(player)

        // Difundir mensaje y repartir monedas equitativamente entre miembros online
        ZenCoreAPI.partyHook.broadcastToParty(party.id, "<green>¡Evento completado con ${nearby.size} aliados cerca!")
        ZenCoreAPI.hooks.depositPartyAny(party.id, "gems", 200.0, split = true)
    }
}
```

En **Java** también puedes invocar los métodos estáticos (`@JvmStatic`):
```java
if (ZenHooksHub.isPartyAvailable()) {
    ZenPartyView party = ZenPartyHook.getPartyByPlayer(player);
    if (party != null && ZenPartyHook.isModeratorOrLeader(player)) {
        ZenPartyHook.warpPartyToLeader(party.getId());
    }
}
```
> Consulta la referencia completa de métodos, modelos y `ZenPartyListener` en [`11_partys.md`](file:///C:/Development/Plugins%20MC/ZenCore/DOCS/ZENSERIES/11_partys.md).

### 2.7. ZenBoosters (`ZenBoosterEngine` / `ZenBoosterHook`)
Consulta multiplicadores combinados (globales, de team/party, personales y pasivos), entrega vouchers o abre los menús de `ZenBoosters` sin acoplamiento duro:

```kotlin
import com.cuac_xd.zencore.api.ZenCoreAPI
import com.cuac_xd.zencore.booster.BoosterStackMode
import com.cuac_xd.zencore.booster.ZenBoosterScope

// Multiplicador combinado (ZenBoosters + boosters locales/party-shared + permisos)
// Acepta categorías o alias como "eco", "economy", "xp", "skills_xp", o un type-id CUSTOM como "spawners"
val ecoMult = ZenCoreAPI.boosterHook.getMultiplier(player, "economy", BoosterStackMode.SUM)

// Si ZenBoosters está activo, puedes consultar definiciones, entregar vouchers o abrir su UI
if (ZenCoreAPI.hooks.isBoostersAvailable()) {
    ZenCoreAPI.boosterHook.giveBooster(player, "eco_2x_global", amount = 1)
    ZenCoreAPI.boosterHook.activateBooster("personal_xp", player, bypassCooldown = false)
}
```

> Además, dentro de `ZenBoosters`, el hook `ZenPartyTeamHook` conecta automáticamente los boosters de scope `TEAM` con las partys de `ZenPartys`. Consulta [`12_boosters_y_multiplicadores.md`](file:///C:/Development/Plugins%20MC/ZenCore/DOCS/ZENSERIES/12_boosters_y_multiplicadores.md) para ver `ZenBoosterProvider`, `ZenBoosterListener` y la tabla completa de alias.

## 🌐 2.8. Suite Completa de Plugins Zen (29 Plugins Integrados)

ZenCore integra de forma desacoplada y nativa 29 plugins de la suite Zen. Cualquier plugin que dependa de ZenCore puede acceder a sus hermanos directamente desde `ZenCoreAPI` o `ZenHooksHub` sin agregar dependencias duras en su `build.gradle.kts` o `pom.xml`:

| Plugin Hermano | Acceso en `ZenCoreAPI` | Método de Verificación |
| :--- | :--- | :--- |
| **ZenEconomy** | `ZenCoreAPI.economy` | `ZenHooksHub.isEconomyAvailable(curr)` |
| **ZenCosmetics** | `ZenCoreAPI.cosmetics` | `ZenCoreAPI.cosmetics.isInstalled()` |
| **ZenCustomItems** | `ZenCoreAPI.customItems` | `ZenCoreAPI.customItems.isInstalled()` |
| **ZenEnchants** | `ZenCoreAPI.enchants` | `ZenCoreAPI.enchants.isInstalled()` |
| **ZenPartys** | `ZenCoreAPI.partyHook` / `.parties` | `ZenHooksHub.isPartyAvailable()` |
| **ZenBoosters** | `ZenCoreAPI.boosterHook` / `.boosters` | `ZenHooksHub.isBoostersAvailable()` |
| **ZenTags** | `ZenCoreAPI.tags` | `ZenCoreAPI.tags.isInstalled()` |
| **ZenDiscord** | `ZenCoreAPI.discord` | `ZenCoreAPI.discord.isInstalled()` |
| **ZenAuctions** | `ZenCoreAPI.auctions` | `ZenHooksHub.isAuctionsAvailable()` |
| **ZenJobs** | `ZenCoreAPI.jobs` | `ZenHooksHub.isJobsAvailable()` |
| **ZenHorses** | `ZenCoreAPI.horses` | `ZenHooksHub.isHorsesAvailable()` |
| **ZenStorage** | `ZenCoreAPI.storage` | `ZenHooksHub.isStorageAvailable()` |
| **ZenBans** | `ZenCoreAPI.bans` | `ZenHooksHub.isBansAvailable()` |
| **ZenBank** | `ZenCoreAPI.bank` | `ZenHooksHub.isBankAvailable()` |
| **ZenAfkZone** | `ZenCoreAPI.afkZone` | `ZenHooksHub.isAfkZoneAvailable()` |
| **ZenChat** | `ZenCoreAPI.chat` | `ZenHooksHub.isChatAvailable()` |
| **ZenChatGames** | `ZenCoreAPI.chatGames` | `ZenHooksHub.isChatGamesAvailable()` |
| **ZenDragonEvent** | `ZenCoreAPI.dragonEvent` | `ZenHooksHub.isDragonEventAvailable()` |
| **ZenFairy** | `ZenCoreAPI.fairy` | `ZenHooksHub.isFairyAvailable()` |
| **ZenForges** | `ZenCoreAPI.forges` | `ZenHooksHub.isForgesAvailable()` |
| **ZenHub** | `ZenCoreAPI.hub` | `ZenHooksHub.isHubAvailable()` |
| **ZenQuests** | `ZenCoreAPI.quests` | `ZenHooksHub.isQuestsAvailable()` |
| **ZenRankups** | `ZenCoreAPI.rankups` | `ZenHooksHub.isRankupsAvailable()` |
| **ZenRewards** | `ZenCoreAPI.rewards` | `ZenHooksHub.isRewardsAvailable()` |
| **ZenRogueLike** | `ZenCoreAPI.rogueLike` | `ZenHooksHub.isRogueLikeAvailable()` |
| **ZenSpawner** | `ZenCoreAPI.spawner` | `ZenHooksHub.isSpawnerAvailable()` |
| **ZenBlockRegen** | `ZenCoreAPI.blockRegen` | `ZenHooksHub.isBlockRegenAvailable()` |
| **ZenContest** | `ZenCoreAPI.contest` | `ZenHooksHub.isContestAvailable()` |
| **ZenCrates** | `ZenCoreAPI.crates` | `ZenHooksHub.isCratesAvailable()` |

---

## ⚡ 3. Configuración en Archivos YAML (Efectos y Condiciones)

Los archivos de configuración de tu plugin (ej. `spawners/iron_golem.yml`, menús de compra, misiones, etc.) pueden ejecutar directamente las acciones de los otros plugins mediante la sintaxis nativa de **ZenCore**:

### 3.1. Cobrar o Dar Monedas de ZenEconomy / Vault
```yaml
effects:
  # Retirar 250 gemas de ZenEconomy
  - type: TAKE_CURRENCY
    currency: "gems"
    amount: "250"

  # Entregar 1000 de dinero (Vault)
  - type: GIVE_CURRENCY
    currency: "vault"
    amount: "1000"

  # O establecer saldo exacto
  - type: SET_CURRENCY
    currency: "crystals"
    amount: "5000"
```

### 3.2. Verificar si el Jugador Cumple los Requisitos (Condiciones)
```yaml
conditions:
  # Requiere al menos 50 cristales en ZenEconomy
  - type: HAS_CURRENCY
    currency: "crystals"
    amount: "50"

  # Requiere poseer un cosmético específico
  - type: HAS_COSMETIC
    id: "spawner_flame_particle"

  # Requiere tener al menos 1 item custom en el inventario
  - type: HAS_CUSTOM_ITEM
    id: "SPAWNER_FRAGMENT"
    amount: 1

  # Requiere que el item en la mano tenga el encantamiento 'spawner_touch' nivel 2 o superior
  - type: HAS_ENCHANT
    enchant: "spawner_touch"
    level: 2
```

### 3.3. Entregar Items Custom de ZenCustomItems
```yaml
effects:
  - type: GIVE_CUSTOM_ITEM
    id: "SUPER_SPAWNER_WAND"
    amount: 1
```

### 3.4. Gestionar Cosméticos de ZenCosmetics
```yaml
effects:
  - type: UNLOCK_COSMETIC
    id: "mob_slayer_trail"

  - type: EQUIP_COSMETIC
    id: "mob_slayer_trail"
```

### 3.5. Encantar con ZenEnchants
```yaml
effects:
  - type: APPLY_ENCHANT
    enchant: "decapitation"
    level: 3
```

### 3.6. Partys con ZenPartys (`ZenPartyHub` / `ZenPartyHook`)
```yaml
conditions:
  - type: IN_PARTY
  - type: IS_PARTY_MODERATOR
  - type: PARTY_SIZE_ABOVE
    args:
      size: 2
  - type: NEARBY_PARTY_MEMBERS
    args:
      count: 1
      radius: 20.0

filters:
  - type: NOT_SAME_PARTY

mutators:
  - type: PARTY_SIZE_MULTIPLIER
    args:
      per_member: 0.1

effects:
  - type: PARTY_HEAL
    amount: 4
    radius: 20.0
  - type: PARTY_GIVE_CURRENCY
    currency: "gems"
    amount: "100"
    split: true
  - type: PARTY_BROADCAST
    message: "<green>¡%player% ha otorgado recompensas a la party (%party_size% miembros)!"
```
> Además están disponibles `PARTY_WARP`, `PARTY_CONNECT_SERVER`, `PARTY_POTION_EFFECT`, `PARTY_GIVE_XP`, las condiciones `IS_PARTY_LEADER`, `SAME_PARTY`, `PARTY_SIZE_BELOW`, `PARTY_SIZE_EQUALS`, `PARTY_FRIENDLY_FIRE` y los placeholders `%party_size%`, `%party_leader%` e `%in_party%`. Consulta [`11_partys.md`](file:///C:/Development/Plugins%20MC/ZenCore/DOCS/ZENSERIES/11_partys.md).

### 3.7. Boosters, Vouchers y Multiplicadores de `ZenBoosters`
```yaml
conditions:
  - type: HAS_ACTIVE_BOOSTER
    category: "economy"

mutators:
  - type: APPLY_BOOSTER_MULTIPLIER
    category: "spawners"
    mode: "SUM"

effects:
  # Si ZenBoosters está instalado y no pasas multiplier/duration, delega al proveedor por ID:
  - type: ACTIVATE_BOOSTER
    id: "eco_2x_global"
    bypass_cooldown: true
  - type: GIVE_BOOSTER
    booster: "personal_xp"
    amount: 2
  - type: MESSAGE
    message: "<aqua>Tu multiplicador de economía es %booster_multiplier_economy%x"
```
> También están disponibles `STOP_BOOSTER` (`REMOVE_BOOSTER`), `OPEN_BOOSTER_MENU`, `OPEN_BOOSTER_VAULT` y `%booster_has_<cat>%`. Consulta [`12_boosters_y_multiplicadores.md`](file:///C:/Development/Plugins%20MC/ZenCore/DOCS/ZENSERIES/12_boosters_y_multiplicadores.md).

---

## 🚀 4. Ejemplo Práctico: Configuración de Spawner en `ZenSpawner`

```yaml
spawner:
  id: "iron_golem"
  name: "<gradient:#ffaa00:#ff5500>Spawner de Gólem de Hierro</gradient>"
  
  # Al romper o interactuar
  on_break:
    conditions:
      - type: HAS_ENCHANT
        enchant: "silk_touch"
        level: 1
      - type: HAS_CURRENCY
        currency: "souls"
        amount: "10"
    effects:
      - type: TAKE_CURRENCY
        currency: "souls"
        amount: "10"
      - type: GIVE_CUSTOM_ITEM
        id: "IRON_GOLEM_SPAWNER_ITEM"
        amount: 1
      - type: SOUND
        sound: "BLOCK_BEACON_DEACTIVATE"
        pitch: 1.5
      - type: MESSAGE
        message: "<green>¡Has recuperado el Spawner de Gólem pagando 10 Almas!"
```

---

## 🛡️ 5. Manejo Seguro contra Fallos (Soft Dependencies)
- Si un servidor **NO** tiene instalado `ZenEconomy`, el efecto `GIVE_CURRENCY` simplemente no hará nada y no arrojará errores de consola.
- Si un servidor **NO** tiene instalado `ZenCustomItems`, `HAS_CUSTOM_ITEM` retornará `false`.
- Si un servidor **NO** tiene instalado `ZenPartys`, `ZenCoreAPI.partyHook.isInParty(player)` retornará `false`, `getOnlineMembers(...)` / `getNearbyOnlineMembers(...)` devolverán listas vacías y efectos como `PARTY_BROADCAST`, `PARTY_HEAL` o `PARTY_GIVE_CURRENCY` se omitirán sin errores.
- Si un servidor **NO** tiene instalado `ZenBoosters`, `ZenCoreAPI.boosterHook.getMultiplier(...)` seguirá funcionando con los boosters locales y por permisos de `ZenBoosterEngine` (retornando `1.0` si no hay ninguno activo), mientras `GIVE_BOOSTER`, `OPEN_BOOSTER_MENU` y `OPEN_BOOSTER_VAULT` se omiten de forma segura.
- Puedes consultar en cualquier momento el estado real de todas las integraciones mediante `ZenCoreAPI.hooks.getAvailableHooks()`.
- Todo el ecosistema está completamente desacoplado mediante proveedores registrados (`ZenPartyHub`, `ZenBoosterEngine`), reflexión segura (`ZenPartysAPI.get()` / `INSTANCE`, `ZenBoostersAPI.getInstance()`) y verificaciones de `PluginManager.isPluginEnabled(...)`.

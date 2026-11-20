# 🌐 10. Compatibilidad del Ecosistema ZenCore

**ZenCore** es el núcleo central (al estilo *Libreforge/Eco* de Auxilor) que unifica e interconecta a todos los plugins de la familia Zen. Al depender de ZenCore, cualquier plugin puede invocar funcionalidades de sus plugins hermanos sin necesidad de declararlos como dependencias duras ni arriesgarse a excepciones `ClassNotFoundException` o `NoClassDefFoundError`.

Para la guía detallada de integración en YAML y configuración de dependencias, consulta también:
- 👉 **[`DOCS/compatibilidad_plugins.md`](file:///C:/Development/Plugins%20MC/ZenCore/DOCS/compatibilidad_plugins.md)** (Guía completa de hooks, YAML y arquitectura)
- 👉 **[`11_partys.md`](file:///C:/Development/Plugins%20MC/ZenCore/DOCS/ZENSERIES/11_partys.md)** (Referencia de `ZenPartys`, `ZenPartyHub` y `ZenPartyHook`)
- 👉 **[`12_boosters_y_multiplicadores.md`](file:///C:/Development/Plugins%20MC/ZenCore/DOCS/ZENSERIES/12_boosters_y_multiplicadores.md)** (Referencia de `ZenBoosters`, `ZenBoosterEngine` y `ZenBoosterHook`)

---

## 📋 1. Plugins Integrados en la Familia Zen (29 Plugins)

Todos los plugins a continuación han sido adaptados para depender de **ZenCore** y cuentan con hooks de primera clase expuestos en `ZenCoreAPI` y `ZenHooksHub`:

| Plugin | Acceso Directo (`ZenCoreAPI`) | Hook en `ZenHooksHub` | Funcionalidad Principal | Comportamiento Seguro Si Falta |
| :--- | :--- | :--- | :--- | :--- |
| **ZenEconomy** | `ZenCoreAPI.economy` | `ZenHooksHub.economy` | Multidivisa, balances, depósitos, retiros y Vault | Retorna `0.0`, `false`, `emptyList()` |
| **ZenCosmetics** | `ZenCoreAPI.cosmetics` | `ZenHooksHub.cosmetics` | Auras, trajes, cosméticos de victoria y partículas | Retorna `false`, omite desbloqueo |
| **ZenCustomItems** | `ZenCoreAPI.customItems` | `ZenHooksHub.customItems` | Creación y validación de items custom por ID | Retorna `null`, `false` |
| **ZenEnchants** | `ZenCoreAPI.enchants` | `ZenHooksHub.enchants` | Encantamientos personalizados sobre herramientas/armas | Retorna `0`, `false` |
| **ZenPartys** | `ZenCoreAPI.partyHook` / `.parties` | `ZenHooksHub.parties` | Partys, roles, teletransporte y recompensas en grupo | Retorna `null`, `emptyList()`, `false` |
| **ZenBoosters** | `ZenCoreAPI.boosterHook` / `.boosters` | `ZenHooksHub.boosters` | Multiplicadores globales/personales/team, vouchers | Retorna `1.0`, omite menús |
| **ZenTags** | `ZenCoreAPI.tags` | `ZenHooksHub.tags` | Títulos, prefijos y tags cosméticos de chat | Retorna `false`, `null` |
| **ZenDiscord** | `ZenCoreAPI.discord` | `ZenHooksHub.discord` | Webhooks y sincronización de eventos con Discord | No envía mensajes |
| **ZenAuctions** | `ZenCoreAPI.auctions` | `ZenHooksHub.auctions` | Subastas activas, cancelaciones y verificación | Retorna `0`, `false` |
| **ZenJobs** | `ZenCoreAPI.jobs` | `ZenHooksHub.jobs` | Trabajos, niveles, XP y pertenencia de jugadores | Retorna `0`, `0.0`, `emptyList()` |
| **ZenHorses** | `ZenCoreAPI.horses` | `ZenHooksHub.horses` | Invocación, guardado y renombrado de monturas | Retorna `CompletableFuture(false)` |
| **ZenStorage** | `ZenCoreAPI.storage` | `ZenHooksHub.storage` | Bóvedas virtuales de jugador y almacenamiento compartido | Retorna `0`, límites por defecto |
| **ZenBans** | `ZenCoreAPI.bans` | `ZenHooksHub.bans` | Silencio de chat global, desbaneos y sanciones | Retorna `false` |
| **ZenBank** | `ZenCoreAPI.bank` | `ZenHooksHub.bank` | Cuentas bancarias de jugadores, balances y depósitos | Retorna `0.0`, `false` |
| **ZenAfkZone** | `ZenCoreAPI.afkZone` | `ZenHooksHub.afkZone` | Detección de zonas AFK y nombres de zona | Retorna `false`, `null` |
| **ZenChat** | `ZenCoreAPI.chat` | `ZenHooksHub.chat` | Canales de chat activos y cambio de canal | Retorna `null`, `false` |
| **ZenChatGames** | `ZenCoreAPI.chatGames` | `ZenHooksHub.chatGames` | Detección y ejecución de minijuegos automáticos en chat | Retorna `false` |
| **ZenDragonEvent** | `ZenCoreAPI.dragonEvent` | `ZenHooksHub.dragonEvent` | Control del evento Dragon, spawns forzados y detención | Retorna `false` |
| **ZenFairy** | `ZenCoreAPI.fairy` | `ZenHooksHub.fairy` | Almas de hadas coleccionables y tipos registrados | Retorna `0`, `emptySet()` |
| **ZenForges** | `ZenCoreAPI.forges` | `ZenHooksHub.forges` | Procesos de forja activos y entrega de XP de herrería | Retorna `0`, `false` |
| **ZenHub** | `ZenCoreAPI.hub` | `ZenHooksHub.hub` | Estado de parkour infinito y lobby central | Retorna `false` |
| **ZenQuests** | `ZenCoreAPI.quests` | `ZenHooksHub.quests` | Progreso activo de misiones e hitos | Retorna `false` |
| **ZenRankups** | `ZenCoreAPI.rankups` | `ZenHooksHub.rankups` | Rango actual y nivel de prestigio de jugadores | Retorna `null`, `0` |
| **ZenRewards** | `ZenCoreAPI.rewards` | `ZenHooksHub.rewards` | Racha diaria de recompensas (`streak`) | Retorna `0` |
| **ZenRogueLike** | `ZenCoreAPI.rogueLike` | `ZenHooksHub.rogueLike` | Mazmorras activas y verificación de runs | Retorna `false` |
| **ZenSpawner** | `ZenCoreAPI.spawner` | `ZenHooksHub.spawner` | Conteo de spawners por jugador y totales del servidor | Retorna `0`, `false` |
| **ZenBlockRegen** | `ZenCoreAPI.blockRegen` | `ZenHooksHub.blockRegen` | Regeneración de bloques minados, bypass y regiones de minería | Retorna `false`, `null` |
| **ZenContest** | `ZenCoreAPI.contest` | `ZenHooksHub.contest` | Concursos agrícolas estilo Jacob, puntuaciones y estados | Retorna `false`, `0L`, `emptyList()` |
| **ZenCrates** | `ZenCoreAPI.crates` | `ZenHooksHub.crates` | Apertura de cajas, llaves físicas y virtuales | Retorna `false`, `0`, `emptyList()` |

---

## 🛠️ 2. Ejemplos de Interacción Entre Plugins

### 2.1. ZenJobs & ZenRankups (Requisitos Cruzados)
Comprobar el nivel de trabajo y el rango antes de permitir una acción en otro plugin:
```kotlin
import com.cuac_xd.zencore.api.ZenCoreAPI

val player = event.player

// Verificar si el jugador tiene el trabajo de minero a nivel 10
val minerLevel = ZenCoreAPI.jobs.getPlayerJobLevel(player, "miner")

// Verificar el rango en ZenRankups
val rank = ZenCoreAPI.rankups.getPlayerRank(player.uniqueId)
val prestige = ZenCoreAPI.rankups.getPlayerPrestige(player.uniqueId)

if (minerLevel >= 10 && prestige >= 1) {
    player.sendMessage("§a¡Acceso concedido a la mina de prestigio!")
}
```

### 2.2. ZenBank & ZenEconomy (Depósitos y Bóvedas)
Transferir fondos a la cuenta bancaria de `ZenBank` al completar eventos de otro plugin:
```kotlin
// Depositar intereses o recompensas al banco
if (ZenCoreAPI.bank.isInstalled()) {
    ZenCoreAPI.bank.deposit(player.uniqueId, 1500.0)
    val total = ZenCoreAPI.bank.getBankBalance(player.uniqueId)
    player.sendMessage("§aNuevo saldo bancario: $$total")
}
```

### 2.3. ZenStorage & ZenQuests (Misiones y Almacenamiento)
Otorga bóvedas virtuales o valida el progreso de misiones:
```kotlin
// Consultar bóvedas desbloqueadas en ZenStorage
val vaults = ZenCoreAPI.storage.getUnlockedVaults(player.uniqueId)

// Comprobar si el jugador tiene una misión activa en ZenQuests
if (ZenCoreAPI.quests.hasActiveProgress(player.uniqueId)) {
    player.sendMessage("§eTienes misiones pendientes. ¡Complétalas para ganar más bóvedas!")
}
```

### 2.4. ZenSpawner & ZenBoosters (Multiplicadores de Spawner)
```kotlin
// Conteo de spawners de un jugador
val spawners = ZenCoreAPI.spawner.getSpawnersCount(player.uniqueId)

// Aplicar multiplicador de spawn activo desde ZenBoosters
val mult = ZenCoreAPI.boosterHook.getMultiplier(player, "spawners")
val finalDropRate = baseRate * mult
```

### 2.5. ZenCrates, ZenContest & ZenBlockRegen (Recompensas, Concursos y Minería)
```kotlin
// 1. Entregar llaves de cajas o abrir una caja
if (ZenCoreAPI.crates.isInstalled()) {
    ZenCoreAPI.crates.givePhysicalKey(player, "legendary_key", amount = 1)
    val keyCount = ZenCoreAPI.crates.getPhysicalKeyCount(player, "legendary_key")
    player.sendMessage("§6¡Has recibido una llave legendaria! Tienes: $keyCount")
}

// 2. Comprobar concursos agrícolas activos y puntuación del jugador
if (ZenCoreAPI.contest.isContestActive("wheat_contest")) {
    val score = ZenCoreAPI.contest.getPlayerScore(player, "wheat_contest")
    player.sendMessage("§ePuntuación en concurso de trigo: $score")
}

// 3. Comprobar si un bloque está regenerándose o en región protegida
val blockLoc = block.location
if (ZenCoreAPI.blockRegen.isRegenerating(blockLoc)) {
    val region = ZenCoreAPI.blockRegen.getRegionName(blockLoc)
    player.sendMessage("§cEste bloque se está regenerando en la región $region.")
}
```

### 2.6. Diagnóstico Global del Ecosistema
Puedes comprobar en cualquier momento el estado de todos los plugins conectados:
```kotlin
val statusMap = ZenCoreAPI.hooks.getAvailableHooks()
// Devuelve: {"ZenEconomy": true, "ZenSpawner": true, "ZenBank": false, ...}
```

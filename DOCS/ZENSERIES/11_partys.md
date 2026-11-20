# 👥 11. Sistema Unificado de Partys (`ZenPartys`)

El módulo de Partys de **ZenCore** (`ZenPartyHub` y `ZenPartyHook`) permite que cualquier plugin de la suite (dungeons, eventos, misiones, duelos, economías o encantamientos) consulte y opere sobre partys de **`ZenPartys`** sin acoplamiento duro ni errores si `ZenPartys` no está instalado.

---

## 🏗️ Arquitectura de Integración

1. **`ZenPartyProvider` (`com.cuac_xd.zencore.party`):** Contrato que implementa el plugin proveedor (como `ZenPartys`) al registrarse en `ZenPartyHub.registerProvider(provider)`.
2. **`ZenPartyHub` (`ZenCoreAPI.parties`):** Bus central thread-safe para consultas, acciones y despacho de eventos a listeners (`ZenPartyListener`).
3. **`ZenPartyHook` (`ZenCoreAPI.partyHook` / `ZenCoreAPI.hooks.parties`):** Capa recomendada para plugins consumidores y efectos YAML. Prioriza `ZenPartyHub` y, si no hubiera proveedor registrado, usa reflexión segura sobre `com.cuac_xd.zenpartys.api.ZenPartysAPI` (obteniendo el singleton mediante `get()` o el campo `INSTANCE` e invocando métodos `@JvmStatic` o de instancia).
4. **`ZenHooksHub` (`ZenCoreAPI.hooks`):** Incluye utilidades grupales como `isPartyAvailable()` y `depositPartyAny(partyId, currency, amount, split)`.

---

## 📦 Modelos de Datos (`ZenPartyModels`)

| Modelo | Propiedades y Métodos Principales |
| :--- | :--- |
| **`ZenPartyRole`** | Enum con los roles `LEADER`, `MODERATOR` y `MEMBER`. |
| **`ZenPartyMember`** | `uuid: UUID`, `name: String`, `role: ZenPartyRole`, `joinedAt: Long`, `serverName: String`, `online: Boolean`, `chatModeEnabled: Boolean`. |
| **`ZenPartyInvite`** | `id`, `partyId`, `inviterUuid`, `inviterName`, `targetUuid`, `targetName`, `createdAt`, `expiresAt`, `sourceServer`; métodos `isExpired(now)` y `remainingSeconds(now)`. |
| **`ZenPartyView`** | Vista inmutable con `id`, `leaderUuid`, `leaderName`, `members`, `maxSize` (por defecto `8`), `partyChatEnabled`, `friendlyFire`, `openParty`, `autoConnect`, `createdAt`; propiedades `size`, `isFull`; métodos `isMember(uuid)`, `isLeader(uuid)`, `isModerator(uuid)`, `isModeratorOrLeader(uuid)`, `getMember(uuid)`. |

---

## 🛠️ Referencia de Métodos (`ZenPartyHub` y `ZenPartyHook`)

> [!NOTE]
> Diferencia importante de firma: en `ZenPartyHub`, `getParty(partyId: UUID)` busca una party por su **ID de party**, mientras que en `ZenPartyHook`, `getParty(playerUuid: UUID)` / `getParty(player: Player)` buscan por **jugador** (alias de `getPartyByPlayer`) y `getPartyById(partyId: UUID)` busca por ID de party.

### Consultas
| Método | Descripción |
| :--- | :--- |
| `isAvailable()` / `isInstalled()` | Comprueba si hay un proveedor registrado o si `ZenPartys` está habilitado. |
| `isInParty(player)` / `isInParty(uuid)` | Indica si el jugador pertenece actualmente a una party. |
| `getPartyId(player)` / `getPartyId(uuid)` | Devuelve el `UUID` de la party del jugador o `null`. |
| `getPartyByPlayer(player)` / `getPartyByPlayer(uuid)` | Devuelve la vista inmutable `ZenPartyView?` de la party del jugador. |
| `getParty(partyId)` *(Hub)* / `getPartyById(partyId)` *(Hook)* | Obtiene `ZenPartyView?` a partir del `UUID` de la party. |
| `getAllParties()` | Devuelve `Collection<ZenPartyView>` con todas las partys activas. |
| `getMembers(partyId)` | Devuelve `Set<UUID>` con todos los miembros de la party. |
| `getOnlineMembers(partyId)` | Devuelve `List<Player>` con los miembros conectados en el servidor actual. |
| `getNearbyOnlineMembers(player, radius)` | Devuelve miembros online de la party en el mismo mundo (`distance <= radius` si `radius > 0.0`, o todos en ese mundo si `radius <= 0.0`). |
| `getOnlineMemberCount(partyId)` | Cantidad de miembros online de la party en el servidor actual. |
| `getLeader(partyId)` | Devuelve el `UUID?` del líder de la party. |
| `isLeader(player)` / `isLeader(uuid)` | Comprueba si el jugador es el líder de su party. |
| `isModeratorOrLeader(player)` / `isModeratorOrLeader(uuid)` | Comprueba si el jugador tiene rol `LEADER` o `MODERATOR`. |
| `areInSameParty(p1, p2)` | Sobrecargas con `Player` y `UUID` para comprobar si comparten party. |
| `getMaxSize(partyId)` | Capacidad máxima configurada de la party. |
| `isPartyChatEnabled(partyId)` | Indica si el chat de party está habilitado para esa party. |
| `isPlayerInPartyChatMode(uuid)` | Indica si el jugador tiene activado el modo de chat automático de party. |
| `getPendingInvites(targetNameOrUuid)` | Devuelve `List<ZenPartyInvite>` con las invitaciones pendientes. |

### Acciones
| Método | Descripción |
| :--- | :--- |
| `createParty(leader)` | Crea una party nueva con `leader` como líder y devuelve `ZenPartyView?`. |
| `disbandParty(partyId, actorUuid = null)` | Disuelve la party indicada. |
| `invitePlayer(inviter, targetName)` | Envía una invitación al jugador objetivo. |
| `acceptInvite(player, inviterOrPartyName = null)` | Acepta una invitación pendiente. |
| `denyInvite(player, inviterOrPartyName = null)` | Rechaza una invitación pendiente. |
| `leaveParty(player)` / `leaveParty(uuid)` | Saca al jugador de su party actual. |
| `kickMember(partyId, targetUuid, actorUuid = null)` | Expulsa a un miembro de la party. |
| `transferLeadership(partyId, newLeaderUuid, actorUuid = null)` | Transfiere el liderazgo a otro miembro. |
| `togglePartyChat(partyId, actorUuid = null)` | Alterna el estado del chat grupal de la party. |
| `setPlayerChatMode(playerUuid, enabled)` | Activa o desactiva el modo de chat de party para un jugador. |
| `sendPartyChat(partyId, senderUuid, senderName, message)` | Envía un mensaje de chat de party como un jugador concreto. |
| `broadcastToParty(partyId, miniMessage)` | Difunde un anuncio MiniMessage a toda la party. |
| `connectPartyToServer(partyId, serverName)` | Conecta a toda la party a otro servidor del proxy. |
| `warpPartyToLeader(partyId)` | Teletransporta a los miembros de la party hacia el líder. |
| `openMainMenu(player)` | Abre el menú principal de `ZenPartys` al jugador. |
| `ZenHooksHub.depositPartyAny(partyId, currency, amount, split = false)` | Deposita monedas (`vault` o divisa de `ZenEconomy`) a todos los miembros online; si `split = true`, divide el monto equitativamente. Retorna la cantidad de miembros pagados. |

---

## 💻 Ejemplos de Uso (Kotlin y Java)

### Kotlin
```kotlin
import com.cuac_xd.zencore.api.ZenCoreAPI

if (ZenCoreAPI.hooks.isPartyAvailable()) {
    val party = ZenCoreAPI.partyHook.getPartyByPlayer(player)
    if (party != null && ZenCoreAPI.partyHook.isModeratorOrLeader(player)) {
        // Obtener aliados de party en el mismo mundo dentro de 25 bloques
        val nearbyAllies = ZenCoreAPI.partyHook.getNearbyOnlineMembers(player, 25.0)
        nearbyAllies.forEach { ally ->
            ally.giveExp(50)
        }

        // Anunciar y repartir recompensa económica entre la party
        ZenCoreAPI.partyHook.broadcastToParty(
            party.id,
            "<gradient:#00ff88:#00aaff>¡Dungeon completada por la party de ${party.leaderName}!</gradient>"
        )
        ZenCoreAPI.hooks.depositPartyAny(party.id, "gems", 500.0, split = true)
    }
}
```

### Java
```java
import com.cuac_xd.zencore.api.ZenCoreAPI;
import com.cuac_xd.zencore.hooks.ZenHooksHub;
import com.cuac_xd.zencore.hooks.ZenPartyHook;
import com.cuac_xd.zencore.party.ZenPartyView;
import org.bukkit.entity.Player;

if (ZenHooksHub.isPartyAvailable()) {
    ZenPartyView party = ZenPartyHook.getPartyByPlayer(player);
    if (party != null && ZenPartyHook.isModeratorOrLeader(player)) {
        for (Player member : ZenPartyHook.getNearbyOnlineMembers(player, 30.0)) {
            member.sendRichMessage("<aqua>¡El líder ha activado el portal grupal!");
        }
        ZenCoreAPI.getPartyHook().warpPartyToLeader(party.getId());
        ZenHooksHub.depositPartyAny(party.getId(), "vault", 1000.0, false);
    }
}
```

---

## 🔔 Listeners en Tiempo Real (`ZenPartyListener`)

Otros plugins pueden suscribirse al ciclo de vida de las partys registrando un `ZenPartyListener` en `ZenPartyHub`:

```kotlin
import com.cuac_xd.zencore.api.ZenCoreAPI
import com.cuac_xd.zencore.party.ZenPartyListener
import com.cuac_xd.zencore.party.ZenPartyMember
import com.cuac_xd.zencore.party.ZenPartyView
import java.util.UUID

val dungeonPartyListener = object : ZenPartyListener {
    override fun onPartyCreated(party: ZenPartyView) {
        // Inicializar datos de dungeon para la nueva party
    }

    override fun onPartyDisbanded(party: ZenPartyView, reason: String) {
        // Expulsar o cerrar instancia activa si la party se disuelve
    }

    override fun onMemberJoined(party: ZenPartyView, member: ZenPartyMember) {
        ZenCoreAPI.partyHook.broadcastToParty(party.id, "<green>+ ${member.name} se unió al grupo.")
    }

    override fun onMemberLeft(party: ZenPartyView, memberUuid: UUID, reason: String) {
        // Actualizar marcador o cancelar cola grupal
    }

    override fun onLeaderChanged(party: ZenPartyView, oldLeader: UUID, newLeader: UUID) {
        // Reasignar permisos de anfitrión de sala
    }
}

// Registrar en onEnable() y limpiar en onDisable()
ZenCoreAPI.parties.registerListener(dungeonPartyListener)
// ZenCoreAPI.parties.unregisterListener(dungeonPartyListener)
```

---

## ⚡ Efectos, Condiciones, Filtros, Mutadores y Placeholders YAML

### Efectos (`CoreEffects`)
| ID (y Alias) | Parámetros | Descripción |
| :--- | :--- | :--- |
| `PARTY_BROADCAST` (`PARTY_MESSAGE`) | `message` / `text` | Envía un mensaje MiniMessage (con placeholders resueltos) a toda la party. |
| `PARTY_WARP` | *(ninguno)* | Teletransporta a los miembros de la party hacia el líder. |
| `PARTY_CONNECT_SERVER` | `server` / `target_server` | Conecta a toda la party al servidor indicado (soporta placeholders). |
| `PARTY_HEAL` | `amount` (def. `4`), `radius` (def. `20.0`) | Cura a los miembros online de la party en el mismo mundo y radio. |
| `PARTY_POTION_EFFECT` | `effect` / `potion`, `duration` (ticks, def. `100`), `amplifier` (def. `0`), `radius` (def. `20.0`) | Aplica un efecto de poción a los miembros cercanos de la party. |
| `PARTY_GIVE_CURRENCY` | `currency` / `type` (def. `"vault"`), `amount` (def. `1`), `split` (def. `false`) | Entrega dinero o divisas de `ZenEconomy` a toda la party vía `ZenHooksHub.depositPartyAny`. |
| `PARTY_GIVE_XP` | `amount` (def. `10`), `radius` (def. `20.0`) | Otorga experiencia a los miembros cercanos de la party. |

### Condiciones (`CoreConditions`)
| ID | Parámetros | Descripción |
| :--- | :--- | :--- |
| `IN_PARTY` | *(ninguno)* | Verdadero si el jugador pertenece a una party. |
| `IS_PARTY_LEADER` | *(ninguno)* | Verdadero si el jugador es el líder de su party. |
| `IS_PARTY_MODERATOR` | *(ninguno)* | Verdadero si el jugador es líder o moderador (`isModeratorOrLeader`). |
| `SAME_PARTY` | *(ninguno)* | Verdadero si `ctx.player` y el objetivo (`targetEntity` o `target`) comparten party. |
| `PARTY_SIZE_ABOVE` | `size` / `value` (def. `1`) | Comprueba `party.size >= size`. |
| `PARTY_SIZE_BELOW` | `size` / `value` (def. `2`) | Comprueba `party.size < size`. |
| `PARTY_SIZE_EQUALS` | `size` / `value` (def. `1`) | Comprueba `party.size == size`. |
| `NEARBY_PARTY_MEMBERS` | `count` / `value` (def. `1`), `radius` (def. `20.0`) | Comprueba que haya al menos `count` compañeros de party cercanos (excluyendo al propio jugador). |
| `PARTY_FRIENDLY_FIRE` | *(ninguno)* | Comprueba si la party tiene `friendlyFire` activado. |

### Filtros (`CoreFilters`) y Mutadores (`CoreMutators`)
| Tipo | ID | Parámetros / Comportamiento |
| :--- | :--- | :--- |
| **Filtro** | `IN_PARTY` | Filtra si `ctx.player` está en una party. |
| **Filtro** | `SAME_PARTY` | Filtra si `ctx.player` y la entidad objetivo están en la misma party. |
| **Filtro** | `NOT_SAME_PARTY` | Filtra si NO están en la misma party (útil para evitar dañar aliados en AoE). |
| **Mutador** | `PARTY_SIZE_MULTIPLIER` | `per_member` (def. `0.1`), `variable` (opcional). Multiplica `ctx.damage` (o `ctx.data[variable]`) por `1.0 + (partySize * per_member)`. |

### Placeholders Contextuales (`ContextResolver`)
| Placeholder | Valor devuelto |
| :--- | :--- |
| `%party_size%` | Número de miembros en la party del jugador (`0` si no tiene party). |
| `%party_leader%` | Nombre del líder de la party (`""` si no tiene party). |
| `%in_party%` | `"true"` o `"false"`. |

### Ejemplo Completo YAML
```yaml
habilidad_aura_guerra:
  triggers:
    - melee_attack
  conditions:
    - type: IN_PARTY
    - type: NEARBY_PARTY_MEMBERS
      args:
        count: 1
        radius: 15.0
  filters:
    - type: NOT_SAME_PARTY
  mutators:
    - type: PARTY_SIZE_MULTIPLIER
      args:
        per_member: 0.15
  effects:
    - type: PARTY_POTION_EFFECT
      effect: STRENGTH
      duration: 100
      amplifier: 0
      radius: 15.0
      cooldown: 12
    - type: PARTY_HEAL
      amount: "2 + (%party_size% * 0.5)"
      radius: 15.0
    - type: PARTY_BROADCAST
      message: "<gold>[Party]</gold> <yellow>%player%</yellow> activó Aura de Guerra (x%party_size% miembros)."
```

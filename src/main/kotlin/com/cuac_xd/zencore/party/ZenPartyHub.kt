package com.cuac_xd.zencore.party

import org.bukkit.World
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicReference

/**
 * Proveedor y bus central de Partys en ZenCore.
 * Permite a ZenPartys registrarse como proveedor principal y a cualquier plugin
 * consumidor interactuar con las partys sin depender directamente de ZenPartys.
 */
object ZenPartyHub {
    private val providerRef = AtomicReference<ZenPartyProvider?>(null)
    private val listeners = CopyOnWriteArrayList<ZenPartyListener>()

    @JvmStatic
    fun registerProvider(provider: ZenPartyProvider) {
        providerRef.set(provider)
    }

    @JvmStatic
    fun unregisterProvider(provider: ZenPartyProvider) {
        providerRef.compareAndSet(provider, null)
    }

    @JvmStatic
    fun getProvider(): ZenPartyProvider? = providerRef.get()

    @JvmStatic
    fun isAvailable(): Boolean = providerRef.get() != null

    @JvmStatic
    fun registerListener(listener: ZenPartyListener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
    }

    @JvmStatic
    fun unregisterListener(listener: ZenPartyListener) {
        listeners.remove(listener)
    }

    // ==========================================
    // DELEGADOS DE CONSULTA
    // ==========================================

    @JvmStatic
    fun isInParty(player: Player): Boolean = isInParty(player.uniqueId)

    @JvmStatic
    fun isInParty(playerUuid: UUID): Boolean = providerRef.get()?.isInParty(playerUuid) ?: false

    @JvmStatic
    fun getPartyId(player: Player): UUID? = getPartyId(player.uniqueId)

    @JvmStatic
    fun getPartyId(playerUuid: UUID): UUID? = providerRef.get()?.getPartyId(playerUuid)

    @JvmStatic
    fun getParty(partyId: UUID): ZenPartyView? = providerRef.get()?.getParty(partyId)

    @JvmStatic
    fun getPartyByPlayer(player: Player): ZenPartyView? = getPartyByPlayer(player.uniqueId)

    @JvmStatic
    fun getPartyByPlayer(playerUuid: UUID): ZenPartyView? = providerRef.get()?.getPartyByPlayer(playerUuid)

    @JvmStatic
    fun getAllParties(): Collection<ZenPartyView> = providerRef.get()?.getAllParties() ?: emptyList()

    @JvmStatic
    fun getMembers(partyId: UUID): Set<UUID> = providerRef.get()?.getMembers(partyId) ?: emptySet()

    @JvmStatic
    fun getOnlineMembers(partyId: UUID): List<Player> = providerRef.get()?.getOnlineMembers(partyId) ?: emptyList()

    /**
     * Obtiene los miembros en línea de la party del jugador dentro del mismo mundo.
     * Si [radius] es mayor que 0.0 filtra por distancia <= [radius]; si es <= 0.0 devuelve todos en ese mundo.
     */
    @JvmStatic
    fun getNearbyOnlineMembers(player: Player, radius: Double): List<Player> {
        val party = getPartyByPlayer(player.uniqueId) ?: return emptyList()
        return filterNearbyOnlineMembers(player, getOnlineMembers(party.id), radius)
    }

    internal fun filterNearbyOnlineMembers(
        player: Player,
        onlineMembers: List<Player>,
        radius: Double
    ): List<Player> {
        if (onlineMembers.isEmpty() || radius.isNaN()) return emptyList()
        val playerWorld = runCatching { player.world }.getOrNull() ?: return emptyList()
        if (radius <= 0.0) {
            return onlineMembers.filter { member ->
                if (member.uniqueId == player.uniqueId) return@filter true
                val memberWorld = runCatching { member.world }.getOrNull() ?: return@filter false
                isSameWorld(playerWorld, memberWorld)
            }
        }
        val playerLoc = runCatching { player.location }.getOrNull() ?: return emptyList()
        val maxDistSq = radius * radius
        return onlineMembers.filter { member ->
            if (member.uniqueId == player.uniqueId) return@filter true
            val memberWorld = runCatching { member.world }.getOrNull() ?: return@filter false
            if (!isSameWorld(playerWorld, memberWorld)) return@filter false
            val memberLoc = runCatching { member.location }.getOrNull() ?: return@filter false
            val dx = memberLoc.x - playerLoc.x
            val dy = memberLoc.y - playerLoc.y
            val dz = memberLoc.z - playerLoc.z
            (dx * dx + dy * dy + dz * dz) <= maxDistSq
        }
    }

    private fun isSameWorld(first: World, second: World): Boolean {
        if (first === second) return true
        val firstUid = runCatching { first.uid }.getOrNull()
        val secondUid = runCatching { second.uid }.getOrNull()
        if (firstUid != null && secondUid != null) return firstUid == secondUid
        return first == second
    }

    @JvmStatic
    fun getOnlineMemberCount(partyId: UUID): Int = getOnlineMembers(partyId).size

    @JvmStatic
    fun getLeader(partyId: UUID): UUID? = providerRef.get()?.getLeader(partyId)

    @JvmStatic
    fun isLeader(player: Player): Boolean = isLeader(player.uniqueId)

    @JvmStatic
    fun isLeader(playerUuid: UUID): Boolean = providerRef.get()?.isLeader(playerUuid) ?: false

    @JvmStatic
    fun isModeratorOrLeader(player: Player): Boolean = isModeratorOrLeader(player.uniqueId)

    @JvmStatic
    fun isModeratorOrLeader(playerUuid: UUID): Boolean {
        val party = getPartyByPlayer(playerUuid) ?: return false
        return party.isModeratorOrLeader(playerUuid)
    }

    @JvmStatic
    fun areInSameParty(player1: Player, player2: Player): Boolean =
        areInSameParty(player1.uniqueId, player2.uniqueId)

    @JvmStatic
    fun areInSameParty(player1: UUID, player2: UUID): Boolean =
        providerRef.get()?.areInSameParty(player1, player2) ?: false

    @JvmStatic
    fun getMaxSize(partyId: UUID): Int = providerRef.get()?.getMaxSize(partyId) ?: 0

    @JvmStatic
    fun isPartyChatEnabled(partyId: UUID): Boolean = providerRef.get()?.isPartyChatEnabled(partyId) ?: false

    @JvmStatic
    fun isPlayerInPartyChatMode(playerUuid: UUID): Boolean =
        providerRef.get()?.isPlayerInPartyChatMode(playerUuid) ?: false

    @JvmStatic
    fun getPendingInvites(targetNameOrUuid: String): List<ZenPartyInvite> =
        providerRef.get()?.getPendingInvites(targetNameOrUuid) ?: emptyList()

    // ==========================================
    // DELEGADOS DE ACCIÓN
    // ==========================================

    @JvmStatic
    fun createParty(leader: Player): ZenPartyView? = providerRef.get()?.createParty(leader)

    @JvmStatic
    @JvmOverloads
    fun disbandParty(partyId: UUID, actorUuid: UUID? = null): Boolean =
        providerRef.get()?.disbandParty(partyId, actorUuid) ?: false

    @JvmStatic
    fun invitePlayer(inviter: Player, targetName: String): Boolean =
        providerRef.get()?.invitePlayer(inviter, targetName) ?: false

    @JvmStatic
    @JvmOverloads
    fun acceptInvite(player: Player, inviterOrPartyName: String? = null): Boolean =
        providerRef.get()?.acceptInvite(player, inviterOrPartyName) ?: false

    @JvmStatic
    @JvmOverloads
    fun denyInvite(player: Player, inviterOrPartyName: String? = null): Boolean =
        providerRef.get()?.denyInvite(player, inviterOrPartyName) ?: false

    @JvmStatic
    fun leaveParty(player: Player): Boolean = leaveParty(player.uniqueId)

    @JvmStatic
    fun leaveParty(playerUuid: UUID): Boolean = providerRef.get()?.leaveParty(playerUuid) ?: false

    @JvmStatic
    @JvmOverloads
    fun kickMember(partyId: UUID, targetUuid: UUID, actorUuid: UUID? = null): Boolean =
        providerRef.get()?.kickMember(partyId, targetUuid, actorUuid) ?: false

    @JvmStatic
    @JvmOverloads
    fun transferLeadership(partyId: UUID, newLeaderUuid: UUID, actorUuid: UUID? = null): Boolean =
        providerRef.get()?.transferLeadership(partyId, newLeaderUuid, actorUuid) ?: false

    @JvmStatic
    @JvmOverloads
    fun togglePartyChat(partyId: UUID, actorUuid: UUID? = null): Boolean =
        providerRef.get()?.togglePartyChat(partyId, actorUuid) ?: false

    @JvmStatic
    fun setPlayerChatMode(playerUuid: UUID, enabled: Boolean): Boolean =
        providerRef.get()?.setPlayerChatMode(playerUuid, enabled) ?: false

    @JvmStatic
    fun sendPartyChat(partyId: UUID, senderUuid: UUID, senderName: String, message: String) {
        providerRef.get()?.sendPartyChat(partyId, senderUuid, senderName, message)
    }

    @JvmStatic
    fun broadcastToParty(partyId: UUID, miniMessage: String) {
        providerRef.get()?.broadcastToParty(partyId, miniMessage)
    }

    @JvmStatic
    fun connectPartyToServer(partyId: UUID, serverName: String): Boolean =
        providerRef.get()?.connectPartyToServer(partyId, serverName) ?: false

    @JvmStatic
    fun warpPartyToLeader(partyId: UUID): Boolean =
        providerRef.get()?.warpPartyToLeader(partyId) ?: false

    @JvmStatic
    fun openMainMenu(player: Player) {
        providerRef.get()?.openMainMenu(player)
    }

    // ==========================================
    // DESPACHO DE EVENTOS A LISTENERS
    // ==========================================

    @JvmStatic
    fun notifyPartyCreated(party: ZenPartyView) {
        for (listener in listeners) {
            runCatching { listener.onPartyCreated(party) }
        }
    }

    @JvmStatic
    fun notifyPartyDisbanded(party: ZenPartyView, reason: String) {
        for (listener in listeners) {
            runCatching { listener.onPartyDisbanded(party, reason) }
        }
    }

    @JvmStatic
    fun notifyMemberJoined(party: ZenPartyView, member: ZenPartyMember) {
        for (listener in listeners) {
            runCatching { listener.onMemberJoined(party, member) }
        }
    }

    @JvmStatic
    fun notifyMemberLeft(party: ZenPartyView, memberUuid: UUID, reason: String) {
        for (listener in listeners) {
            runCatching { listener.onMemberLeft(party, memberUuid, reason) }
        }
    }

    @JvmStatic
    fun notifyLeaderChanged(party: ZenPartyView, oldLeader: UUID, newLeader: UUID) {
        for (listener in listeners) {
            runCatching { listener.onLeaderChanged(party, oldLeader, newLeader) }
        }
    }
}

package com.cuac_xd.zencore.party

import org.bukkit.entity.Player
import java.util.UUID

/**
 * Contrato principal del proveedor de Partys para ZenCore.
 * Un plugin proveedor (como ZenPartys) implementa esta interfaz y se registra en [ZenPartyHub]
 * para ofrecer un sistema unificado de partys a todo el ecosistema de plugins.
 */
interface ZenPartyProvider {

    // ==========================================
    // CONSULTAS
    // ==========================================

    fun isInParty(playerUuid: UUID): Boolean

    fun getPartyId(playerUuid: UUID): UUID?

    fun getParty(partyId: UUID): ZenPartyView?

    fun getPartyByPlayer(playerUuid: UUID): ZenPartyView?

    fun getAllParties(): Collection<ZenPartyView>

    fun getMembers(partyId: UUID): Set<UUID>

    fun getOnlineMembers(partyId: UUID): List<Player>

    fun getLeader(partyId: UUID): UUID?

    fun isLeader(playerUuid: UUID): Boolean

    fun areInSameParty(player1: UUID, player2: UUID): Boolean

    fun getMaxSize(partyId: UUID): Int

    fun isPartyChatEnabled(partyId: UUID): Boolean

    fun isPlayerInPartyChatMode(playerUuid: UUID): Boolean

    fun getPendingInvites(targetNameOrUuid: String): List<ZenPartyInvite>

    // ==========================================
    // ACCIONES
    // ==========================================

    fun createParty(leader: Player): ZenPartyView?

    fun disbandParty(partyId: UUID, actorUuid: UUID? = null): Boolean

    fun invitePlayer(inviter: Player, targetName: String): Boolean

    fun acceptInvite(player: Player, inviterOrPartyName: String? = null): Boolean

    fun denyInvite(player: Player, inviterOrPartyName: String? = null): Boolean

    fun leaveParty(playerUuid: UUID): Boolean

    fun kickMember(partyId: UUID, targetUuid: UUID, actorUuid: UUID? = null): Boolean

    fun transferLeadership(partyId: UUID, newLeaderUuid: UUID, actorUuid: UUID? = null): Boolean

    fun togglePartyChat(partyId: UUID, actorUuid: UUID? = null): Boolean

    fun setPlayerChatMode(playerUuid: UUID, enabled: Boolean): Boolean

    fun sendPartyChat(partyId: UUID, senderUuid: UUID, senderName: String, message: String)

    fun broadcastToParty(partyId: UUID, miniMessage: String)

    fun connectPartyToServer(partyId: UUID, serverName: String): Boolean

    fun warpPartyToLeader(partyId: UUID): Boolean

    fun openMainMenu(player: Player)
}

/**
 * Listener para que otros plugins basados en ZenCore reaccionen en tiempo real
 * al ciclo de vida de las partys (creación, disolución, entradas, salidas y cambios de líder).
 */
interface ZenPartyListener {
    fun onPartyCreated(party: ZenPartyView) {}
    fun onPartyDisbanded(party: ZenPartyView, reason: String) {}
    fun onMemberJoined(party: ZenPartyView, member: ZenPartyMember) {}
    fun onMemberLeft(party: ZenPartyView, memberUuid: UUID, reason: String) {}
    fun onLeaderChanged(party: ZenPartyView, oldLeader: UUID, newLeader: UUID) {}
}

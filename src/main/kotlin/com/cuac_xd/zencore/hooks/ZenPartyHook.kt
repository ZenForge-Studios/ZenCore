package com.cuac_xd.zencore.hooks

import com.cuac_xd.zencore.party.ZenPartyHub
import com.cuac_xd.zencore.party.ZenPartyInvite
import com.cuac_xd.zencore.party.ZenPartyView
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.lang.reflect.Modifier
import java.util.UUID

/**
 * Hook unificado para interactuar con el sistema de Partys del ecosistema Zen.
 * Delega prioritariamente en [ZenPartyHub] (proveedor registrado por ZenPartys) y cuenta con
 * fallback reflexivo seguro sobre `com.cuac_xd.zenpartys.api.ZenPartysAPI` para evitar acoplamiento duro.
 */
object ZenPartyHook {

    private const val API_CLASS_NAME = "com.cuac_xd.zenpartys.api.ZenPartysAPI"

    @JvmStatic
    fun isInstalled(): Boolean {
        if (ZenPartyHub.isAvailable()) return true
        return runCatching {
            val server = Bukkit.getServer() as Any? ?: return@runCatching false
            val plugin = server.let { Bukkit.getPluginManager().getPlugin("ZenPartys") }
            plugin != null && plugin.isEnabled
        }.getOrDefault(false)
    }

    // ==========================================
    // CONSULTAS
    // ==========================================

    @JvmStatic
    fun isInParty(player: Player): Boolean = isInParty(player.uniqueId)

    @JvmStatic
    fun isInParty(playerUuid: UUID): Boolean {
        if (ZenPartyHub.isAvailable()) return ZenPartyHub.isInParty(playerUuid)
        return invokeFallback("isInParty", arrayOf(UUID::class.java), arrayOf(playerUuid)) as? Boolean ?: false
    }

    @JvmStatic
    fun getParty(playerUuid: UUID): ZenPartyView? = getPartyByPlayer(playerUuid)

    @JvmStatic
    fun getParty(player: Player): ZenPartyView? = getPartyByPlayer(player.uniqueId)

    @JvmStatic
    fun getPartyById(partyId: UUID): ZenPartyView? {
        if (ZenPartyHub.isAvailable()) return ZenPartyHub.getParty(partyId)
        return invokeFallback("getParty", arrayOf(UUID::class.java), arrayOf(partyId)) as? ZenPartyView
    }

    @JvmStatic
    fun getPartyByPlayer(player: Player): ZenPartyView? = getPartyByPlayer(player.uniqueId)

    @JvmStatic
    fun getPartyByPlayer(playerUuid: UUID): ZenPartyView? {
        if (ZenPartyHub.isAvailable()) return ZenPartyHub.getPartyByPlayer(playerUuid)
        return invokeFallback("getPartyByPlayer", arrayOf(UUID::class.java), arrayOf(playerUuid)) as? ZenPartyView
    }

    @JvmStatic
    fun getPartyId(player: Player): UUID? = getPartyId(player.uniqueId)

    @JvmStatic
    fun getPartyId(playerUuid: UUID): UUID? {
        if (ZenPartyHub.isAvailable()) return ZenPartyHub.getPartyId(playerUuid)
        return invokeFallback("getPartyId", arrayOf(UUID::class.java), arrayOf(playerUuid)) as? UUID
    }

    @JvmStatic
    fun getAllParties(): Collection<ZenPartyView> {
        if (ZenPartyHub.isAvailable()) return ZenPartyHub.getAllParties()
        val res = invokeFallback("getAllParties", emptyArray(), emptyArray()) as? Collection<*>
        return res?.filterIsInstance<ZenPartyView>() ?: emptyList()
    }

    @JvmStatic
    fun getMembers(partyId: UUID): Set<UUID> {
        if (ZenPartyHub.isAvailable()) return ZenPartyHub.getMembers(partyId)
        val res = invokeFallback("getMembers", arrayOf(UUID::class.java), arrayOf(partyId)) as? Collection<*>
        return res?.filterIsInstance<UUID>()?.toSet() ?: emptySet()
    }

    @JvmStatic
    fun getOnlineMembers(partyId: UUID): List<Player> {
        if (ZenPartyHub.isAvailable()) return ZenPartyHub.getOnlineMembers(partyId)
        val res = invokeFallback("getOnlineMembers", arrayOf(UUID::class.java), arrayOf(partyId)) as? Collection<*>
        return res?.filterIsInstance<Player>() ?: emptyList()
    }

    @JvmStatic
    fun getNearbyOnlineMembers(player: Player, radius: Double): List<Player> {
        if (ZenPartyHub.isAvailable()) return ZenPartyHub.getNearbyOnlineMembers(player, radius)
        val direct = invokeFallback(
            "getNearbyOnlineMembers",
            arrayOf(Player::class.java, Double::class.javaPrimitiveType!!),
            arrayOf(player, radius)
        ) as? Collection<*>
        if (direct != null) return direct.filterIsInstance<Player>()
        val party = getPartyByPlayer(player.uniqueId) ?: return emptyList()
        return ZenPartyHub.filterNearbyOnlineMembers(player, getOnlineMembers(party.id), radius)
    }

    @JvmStatic
    fun getOnlineMemberCount(partyId: UUID): Int {
        if (ZenPartyHub.isAvailable()) return ZenPartyHub.getOnlineMemberCount(partyId)
        val direct = invokeFallback("getOnlineMemberCount", arrayOf(UUID::class.java), arrayOf(partyId)) as? Number
        if (direct != null) return direct.toInt()
        return getOnlineMembers(partyId).size
    }

    @JvmStatic
    fun getLeader(partyId: UUID): UUID? {
        if (ZenPartyHub.isAvailable()) return ZenPartyHub.getLeader(partyId)
        return invokeFallback("getLeader", arrayOf(UUID::class.java), arrayOf(partyId)) as? UUID
    }

    @JvmStatic
    fun isLeader(player: Player): Boolean = isLeader(player.uniqueId)

    @JvmStatic
    fun isLeader(playerUuid: UUID): Boolean {
        if (ZenPartyHub.isAvailable()) return ZenPartyHub.isLeader(playerUuid)
        return invokeFallback("isLeader", arrayOf(UUID::class.java), arrayOf(playerUuid)) as? Boolean ?: false
    }

    @JvmStatic
    fun isModeratorOrLeader(player: Player): Boolean = isModeratorOrLeader(player.uniqueId)

    @JvmStatic
    fun isModeratorOrLeader(playerUuid: UUID): Boolean {
        if (ZenPartyHub.isAvailable()) return ZenPartyHub.isModeratorOrLeader(playerUuid)
        val direct = invokeFallback("isModeratorOrLeader", arrayOf(UUID::class.java), arrayOf(playerUuid)) as? Boolean
        if (direct != null) return direct
        return getPartyByPlayer(playerUuid)?.isModeratorOrLeader(playerUuid) ?: isLeader(playerUuid)
    }

    @JvmStatic
    fun areInSameParty(player1: Player, player2: Player): Boolean =
        areInSameParty(player1.uniqueId, player2.uniqueId)

    @JvmStatic
    fun areInSameParty(player1: UUID, player2: UUID): Boolean {
        if (ZenPartyHub.isAvailable()) return ZenPartyHub.areInSameParty(player1, player2)
        return invokeFallback(
            "areInSameParty",
            arrayOf(UUID::class.java, UUID::class.java),
            arrayOf(player1, player2)
        ) as? Boolean ?: false
    }

    @JvmStatic
    fun getMaxSize(partyId: UUID): Int {
        if (ZenPartyHub.isAvailable()) return ZenPartyHub.getMaxSize(partyId)
        return (invokeFallback("getMaxSize", arrayOf(UUID::class.java), arrayOf(partyId)) as? Number)?.toInt() ?: 0
    }

    @JvmStatic
    fun isPartyChatEnabled(partyId: UUID): Boolean {
        if (ZenPartyHub.isAvailable()) return ZenPartyHub.isPartyChatEnabled(partyId)
        return invokeFallback("isPartyChatEnabled", arrayOf(UUID::class.java), arrayOf(partyId)) as? Boolean ?: false
    }

    @JvmStatic
    fun isPlayerInPartyChatMode(playerUuid: UUID): Boolean {
        if (ZenPartyHub.isAvailable()) return ZenPartyHub.isPlayerInPartyChatMode(playerUuid)
        return invokeFallback(
            "isPlayerInPartyChatMode",
            arrayOf(UUID::class.java),
            arrayOf(playerUuid)
        ) as? Boolean ?: false
    }

    @JvmStatic
    fun getPendingInvites(targetNameOrUuid: String): List<ZenPartyInvite> {
        if (ZenPartyHub.isAvailable()) return ZenPartyHub.getPendingInvites(targetNameOrUuid)
        val res = invokeFallback(
            "getPendingInvites",
            arrayOf(String::class.java),
            arrayOf(targetNameOrUuid)
        ) as? Collection<*>
        return res?.filterIsInstance<ZenPartyInvite>() ?: emptyList()
    }

    // ==========================================
    // ACCIONES
    // ==========================================

    @JvmStatic
    fun createParty(leader: Player): ZenPartyView? {
        if (ZenPartyHub.isAvailable()) return ZenPartyHub.createParty(leader)
        return invokeFallback("createParty", arrayOf(Player::class.java), arrayOf(leader)) as? ZenPartyView
    }

    @JvmStatic
    @JvmOverloads
    fun disbandParty(partyId: UUID, actorUuid: UUID? = null): Boolean {
        if (ZenPartyHub.isAvailable()) return ZenPartyHub.disbandParty(partyId, actorUuid)
        return invokeFallback(
            "disbandParty",
            arrayOf(UUID::class.java, UUID::class.java),
            arrayOf(partyId, actorUuid)
        ) as? Boolean ?: false
    }

    @JvmStatic
    fun invitePlayer(inviter: Player, targetName: String): Boolean {
        if (ZenPartyHub.isAvailable()) return ZenPartyHub.invitePlayer(inviter, targetName)
        return invokeFallback(
            "invitePlayer",
            arrayOf(Player::class.java, String::class.java),
            arrayOf(inviter, targetName)
        ) as? Boolean ?: false
    }

    @JvmStatic
    @JvmOverloads
    fun acceptInvite(player: Player, inviterOrPartyName: String? = null): Boolean {
        if (ZenPartyHub.isAvailable()) return ZenPartyHub.acceptInvite(player, inviterOrPartyName)
        return invokeFallback(
            "acceptInvite",
            arrayOf(Player::class.java, String::class.java),
            arrayOf(player, inviterOrPartyName)
        ) as? Boolean ?: false
    }

    @JvmStatic
    @JvmOverloads
    fun denyInvite(player: Player, inviterOrPartyName: String? = null): Boolean {
        if (ZenPartyHub.isAvailable()) return ZenPartyHub.denyInvite(player, inviterOrPartyName)
        return invokeFallback(
            "denyInvite",
            arrayOf(Player::class.java, String::class.java),
            arrayOf(player, inviterOrPartyName)
        ) as? Boolean ?: false
    }

    @JvmStatic
    fun leaveParty(player: Player): Boolean = leaveParty(player.uniqueId)

    @JvmStatic
    fun leaveParty(playerUuid: UUID): Boolean {
        if (ZenPartyHub.isAvailable()) return ZenPartyHub.leaveParty(playerUuid)
        return invokeFallback("leaveParty", arrayOf(UUID::class.java), arrayOf(playerUuid)) as? Boolean ?: false
    }

    @JvmStatic
    @JvmOverloads
    fun kickMember(partyId: UUID, targetUuid: UUID, actorUuid: UUID? = null): Boolean {
        if (ZenPartyHub.isAvailable()) return ZenPartyHub.kickMember(partyId, targetUuid, actorUuid)
        return invokeFallback(
            "kickMember",
            arrayOf(UUID::class.java, UUID::class.java, UUID::class.java),
            arrayOf(partyId, targetUuid, actorUuid)
        ) as? Boolean ?: false
    }

    @JvmStatic
    @JvmOverloads
    fun transferLeadership(partyId: UUID, newLeaderUuid: UUID, actorUuid: UUID? = null): Boolean {
        if (ZenPartyHub.isAvailable()) return ZenPartyHub.transferLeadership(partyId, newLeaderUuid, actorUuid)
        return invokeFallback(
            "transferLeadership",
            arrayOf(UUID::class.java, UUID::class.java, UUID::class.java),
            arrayOf(partyId, newLeaderUuid, actorUuid)
        ) as? Boolean ?: false
    }

    @JvmStatic
    @JvmOverloads
    fun togglePartyChat(partyId: UUID, actorUuid: UUID? = null): Boolean {
        if (ZenPartyHub.isAvailable()) return ZenPartyHub.togglePartyChat(partyId, actorUuid)
        return invokeFallback(
            "togglePartyChat",
            arrayOf(UUID::class.java, UUID::class.java),
            arrayOf(partyId, actorUuid)
        ) as? Boolean ?: false
    }

    @JvmStatic
    fun setPlayerChatMode(playerUuid: UUID, enabled: Boolean): Boolean {
        if (ZenPartyHub.isAvailable()) return ZenPartyHub.setPlayerChatMode(playerUuid, enabled)
        return invokeFallback(
            "setPlayerChatMode",
            arrayOf(UUID::class.java, Boolean::class.javaPrimitiveType!!),
            arrayOf(playerUuid, enabled)
        ) as? Boolean ?: false
    }

    @JvmStatic
    fun sendPartyChat(partyId: UUID, senderUuid: UUID, senderName: String, message: String) {
        if (ZenPartyHub.isAvailable()) {
            ZenPartyHub.sendPartyChat(partyId, senderUuid, senderName, message)
            return
        }
        invokeFallback(
            "sendPartyChat",
            arrayOf(UUID::class.java, UUID::class.java, String::class.java, String::class.java),
            arrayOf(partyId, senderUuid, senderName, message)
        )
    }

    @JvmStatic
    fun broadcastToParty(partyId: UUID, miniMessage: String) {
        if (ZenPartyHub.isAvailable()) {
            ZenPartyHub.broadcastToParty(partyId, miniMessage)
            return
        }
        invokeFallback(
            "broadcastToParty",
            arrayOf(UUID::class.java, String::class.java),
            arrayOf(partyId, miniMessage)
        )
    }

    @JvmStatic
    fun connectPartyToServer(partyId: UUID, serverName: String): Boolean {
        if (ZenPartyHub.isAvailable()) return ZenPartyHub.connectPartyToServer(partyId, serverName)
        return invokeFallback(
            "connectPartyToServer",
            arrayOf(UUID::class.java, String::class.java),
            arrayOf(partyId, serverName)
        ) as? Boolean ?: false
    }

    @JvmStatic
    fun warpPartyToLeader(partyId: UUID): Boolean {
        if (ZenPartyHub.isAvailable()) return ZenPartyHub.warpPartyToLeader(partyId)
        return invokeFallback("warpPartyToLeader", arrayOf(UUID::class.java), arrayOf(partyId)) as? Boolean ?: false
    }

    @JvmStatic
    fun openMainMenu(player: Player) {
        if (ZenPartyHub.isAvailable()) {
            ZenPartyHub.openMainMenu(player)
            return
        }
        invokeFallback("openMainMenu", arrayOf(Player::class.java), arrayOf(player))
    }

    // ==========================================
    // REFLEXIÓN SEGURA SOBRE ZenPartysAPI
    // ==========================================

    private fun getApiInstance(clazz: Class<*>): Any? {
        val fromGet = runCatching {
            val getMethod = clazz.getMethod("get")
            if (Modifier.isStatic(getMethod.modifiers)) getMethod.invoke(null) else null
        }.getOrNull()
        if (fromGet != null) return fromGet

        val fromPublicField = runCatching { clazz.getField("INSTANCE").get(null) }.getOrNull()
        if (fromPublicField != null) return fromPublicField

        return runCatching {
            clazz.getDeclaredField("INSTANCE").apply { isAccessible = true }.get(null)
        }.getOrNull()
    }

    private fun invokeFallback(methodName: String, paramTypes: Array<Class<*>>, args: Array<Any?>): Any? {
        return runCatching {
            val clazz = Class.forName(API_CLASS_NAME)
            val method = runCatching { clazz.getMethod(methodName, *paramTypes) }
                .getOrElse { clazz.getDeclaredMethod(methodName, *paramTypes).apply { isAccessible = true } }
            if (Modifier.isStatic(method.modifiers)) {
                method.invoke(null, *args)
            } else {
                val api = getApiInstance(clazz) ?: return null
                method.invoke(api, *args)
            }
        }.getOrNull()
    }
}

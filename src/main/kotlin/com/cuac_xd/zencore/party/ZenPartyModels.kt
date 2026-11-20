package com.cuac_xd.zencore.party

import java.util.UUID

/**
 * Roles disponibles dentro de una Party del ecosistema Zen.
 */
enum class ZenPartyRole {
    LEADER,
    MODERATOR,
    MEMBER
}

/**
 * Representación inmutable de un miembro dentro de una Party.
 */
data class ZenPartyMember(
    val uuid: UUID,
    val name: String,
    val role: ZenPartyRole,
    val joinedAt: Long,
    val serverName: String = "local",
    val online: Boolean = true,
    val chatModeEnabled: Boolean = false
)

/**
 * Representación inmutable de una invitación de Party con tiempo de expiración.
 */
data class ZenPartyInvite(
    val id: UUID,
    val partyId: UUID,
    val inviterUuid: UUID,
    val inviterName: String,
    val targetUuid: UUID?,
    val targetName: String,
    val createdAt: Long,
    val expiresAt: Long,
    val sourceServer: String = "local"
) {
    fun isExpired(now: Long = System.currentTimeMillis()): Boolean = now >= expiresAt

    fun remainingSeconds(now: Long = System.currentTimeMillis()): Long =
        ((expiresAt - now) / 1000L).coerceAtLeast(0L)
}

/**
 * Vista inmutable de una Party expuesta a través de ZenCore para que cualquier plugin
 * del ecosistema (dungeons, duelos, quests, eventos, etc.) pueda operar con partys conjuntas.
 */
data class ZenPartyView(
    val id: UUID,
    val leaderUuid: UUID,
    val leaderName: String,
    val members: List<ZenPartyMember>,
    val maxSize: Int = 8,
    val partyChatEnabled: Boolean = true,
    val friendlyFire: Boolean = false,
    val openParty: Boolean = false,
    val autoConnect: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    val size: Int get() = members.size

    val isFull: Boolean get() = members.size >= maxSize

    fun isMember(uuid: UUID): Boolean = members.any { it.uuid == uuid }

    fun isLeader(uuid: UUID): Boolean = leaderUuid == uuid

    fun isModerator(uuid: UUID): Boolean = getMember(uuid)?.role == ZenPartyRole.MODERATOR

    fun isModeratorOrLeader(uuid: UUID): Boolean {
        if (leaderUuid == uuid) return true
        val role = getMember(uuid)?.role ?: return false
        return role == ZenPartyRole.LEADER || role == ZenPartyRole.MODERATOR
    }

    fun getMember(uuid: UUID): ZenPartyMember? = members.firstOrNull { it.uuid == uuid }
}

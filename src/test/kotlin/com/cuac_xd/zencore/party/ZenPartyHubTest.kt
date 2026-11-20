package com.cuac_xd.zencore.party

import com.cuac_xd.zencore.api.ZenCoreAPI
import com.cuac_xd.zencore.engine.ZenEngine
import com.cuac_xd.zencore.engine.context.ContextResolver
import com.cuac_xd.zencore.engine.context.ZenExecutionContext
import com.cuac_xd.zencore.hooks.ZenPartyHook
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.lang.reflect.Proxy
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class ZenPartyHubTest {

    private val fakeLeaderId = UUID.randomUUID()
    private val fakeModId = UUID.randomUUID()
    private val fakeMemberId = UUID.randomUUID()
    private val fakeFarMemberId = UUID.randomUUID()
    private val fakeOutsiderId = UUID.randomUUID()

    private class FakePlayerState(
        val uuid: UUID,
        val name: String,
        val world: World,
        var location: Location,
        var health: Double = 20.0,
        var maxHealth: Double = 20.0
    )

    private fun fakeWorld(uid: UUID = UUID.randomUUID(), name: String = "world"): World {
        return Proxy.newProxyInstance(
            World::class.java.classLoader,
            arrayOf(World::class.java)
        ) { proxy, method, args ->
            when (method.name) {
                "getUID", "getUid" -> uid
                "getName" -> name
                "equals" -> {
                    val other = args?.getOrNull(0)
                    other === proxy || (other is World && runCatching { other.uid }.getOrNull() == uid)
                }
                "hashCode" -> uid.hashCode()
                "toString" -> "FakeWorld($name)"
                else -> null
            }
        } as World
    }

    private fun fakePlayer(state: FakePlayerState): Player {
        return Proxy.newProxyInstance(
            Player::class.java.classLoader,
            arrayOf(Player::class.java)
        ) { proxy, method, args ->
            when (method.name) {
                "getUniqueId" -> state.uuid
                "getName" -> state.name
                "getWorld" -> state.world
                "getLocation" -> state.location.clone()
                "isDead" -> false
                "isOnline" -> true
                "getHealth" -> state.health
                "setHealth" -> {
                    state.health = (args?.getOrNull(0) as? Number)?.toDouble() ?: state.health
                    null
                }
                "getMaxHealth" -> state.maxHealth
                "getFoodLevel" -> 20
                "getAttribute" -> null
                "equals" -> {
                    val other = args?.getOrNull(0)
                    other === proxy || (other is Player && other.uniqueId == state.uuid)
                }
                "hashCode" -> state.uuid.hashCode()
                "toString" -> "FakePlayer(${state.name})"
                else -> null
            }
        } as Player
    }

    private fun fakePlayer(uuid: UUID, name: String): Player {
        val world = fakeWorld()
        return fakePlayer(FakePlayerState(uuid, name, world, Location(world, 0.0, 64.0, 0.0)))
    }

    private fun createStubProvider(
        partyStore: ConcurrentHashMap<UUID, ZenPartyView>,
        onlinePlayers: Map<UUID, Player> = emptyMap(),
        broadcastLog: MutableList<Pair<UUID, String>> = mutableListOf()
    ): ZenPartyProvider {
        return object : ZenPartyProvider {
            override fun isInParty(playerUuid: UUID): Boolean = getPartyByPlayer(playerUuid) != null
            override fun getPartyId(playerUuid: UUID): UUID? = getPartyByPlayer(playerUuid)?.id
            override fun getParty(partyId: UUID): ZenPartyView? = partyStore[partyId]
            override fun getPartyByPlayer(playerUuid: UUID): ZenPartyView? =
                partyStore.values.firstOrNull { it.isMember(playerUuid) }
            override fun getAllParties(): Collection<ZenPartyView> = partyStore.values
            override fun getMembers(partyId: UUID): Set<UUID> =
                partyStore[partyId]?.members?.map { it.uuid }?.toSet() ?: emptySet()
            override fun getOnlineMembers(partyId: UUID): List<Player> =
                partyStore[partyId]?.members?.mapNotNull { onlinePlayers[it.uuid] } ?: emptyList()
            override fun getLeader(partyId: UUID): UUID? = partyStore[partyId]?.leaderUuid
            override fun isLeader(playerUuid: UUID): Boolean = getPartyByPlayer(playerUuid)?.isLeader(playerUuid) == true
            override fun areInSameParty(player1: UUID, player2: UUID): Boolean {
                val p1 = getPartyId(player1) ?: return false
                return p1 == getPartyId(player2)
            }
            override fun getMaxSize(partyId: UUID): Int = partyStore[partyId]?.maxSize ?: 8
            override fun isPartyChatEnabled(partyId: UUID): Boolean = partyStore[partyId]?.partyChatEnabled ?: false
            override fun isPlayerInPartyChatMode(playerUuid: UUID): Boolean = false
            override fun getPendingInvites(targetNameOrUuid: String): List<ZenPartyInvite> = emptyList()

            override fun createParty(leader: Player): ZenPartyView {
                val id = UUID.randomUUID()
                val view = ZenPartyView(
                    id = id,
                    leaderUuid = leader.uniqueId,
                    leaderName = leader.name,
                    members = listOf(
                        ZenPartyMember(leader.uniqueId, leader.name, ZenPartyRole.LEADER, System.currentTimeMillis())
                    )
                )
                partyStore[id] = view
                ZenPartyHub.notifyPartyCreated(view)
                return view
            }

            override fun disbandParty(partyId: UUID, actorUuid: UUID?): Boolean {
                val removed = partyStore.remove(partyId) ?: return false
                ZenPartyHub.notifyPartyDisbanded(removed, "DISBANDED")
                return true
            }

            override fun invitePlayer(inviter: Player, targetName: String): Boolean = true
            override fun acceptInvite(player: Player, inviterOrPartyName: String?): Boolean = true
            override fun denyInvite(player: Player, inviterOrPartyName: String?): Boolean = true
            override fun leaveParty(playerUuid: UUID): Boolean = false
            override fun kickMember(partyId: UUID, targetUuid: UUID, actorUuid: UUID?): Boolean = false
            override fun transferLeadership(partyId: UUID, newLeaderUuid: UUID, actorUuid: UUID?): Boolean = false
            override fun togglePartyChat(partyId: UUID, actorUuid: UUID?): Boolean = false
            override fun setPlayerChatMode(playerUuid: UUID, enabled: Boolean): Boolean = false
            override fun sendPartyChat(partyId: UUID, senderUuid: UUID, senderName: String, message: String) {}
            override fun broadcastToParty(partyId: UUID, miniMessage: String) {
                broadcastLog.add(partyId to miniMessage)
            }
            override fun connectPartyToServer(partyId: UUID, serverName: String): Boolean = false
            override fun warpPartyToLeader(partyId: UUID): Boolean = false
            override fun openMainMenu(player: Player) {}
        }
    }

    @AfterEach
    fun cleanup() {
        ZenPartyHub.getProvider()?.let { ZenPartyHub.unregisterProvider(it) }
    }

    @Test
    fun `ZenPartyHub registers provider, notifies listeners, and evaluates engine conditions`() {
        val partyStore = ConcurrentHashMap<UUID, ZenPartyView>()
        val provider = createStubProvider(partyStore)
        ZenCoreAPI.parties.registerProvider(provider)

        var createdPartyId: UUID? = null
        val listener = object : ZenPartyListener {
            override fun onPartyCreated(party: ZenPartyView) {
                createdPartyId = party.id
            }
        }
        ZenPartyHub.registerListener(listener)

        try {
            val leader = fakePlayer(fakeLeaderId, "LeaderOne")
            val member = fakePlayer(fakeMemberId, "MemberTwo")
            val outsider = fakePlayer(fakeOutsiderId, "OutsiderThree")

            val created = ZenCoreAPI.parties.createParty(leader)
            assertNotNull(created)
            assertEquals(created!!.id, createdPartyId)

            // Add second member directly to stub store
            val updated = created.copy(
                members = created.members + ZenPartyMember(
                    fakeMemberId,
                    "MemberTwo",
                    ZenPartyRole.MEMBER,
                    System.currentTimeMillis()
                )
            )
            partyStore[created.id] = updated

            assertTrue(ZenPartyHub.isAvailable())
            assertTrue(ZenPartyHub.isInParty(fakeLeaderId))
            assertTrue(ZenPartyHub.isLeader(fakeLeaderId))
            assertFalse(ZenPartyHub.isLeader(fakeMemberId))
            assertTrue(ZenPartyHub.areInSameParty(fakeLeaderId, fakeMemberId))
            assertFalse(ZenPartyHub.areInSameParty(fakeLeaderId, fakeOutsiderId))

            val engine = ZenEngine()
            val leaderCtx = ZenExecutionContext.builder().player(leader).target(member).build()
            val outsiderCtx = ZenExecutionContext.builder().player(leader).target(outsider).build()

            assertTrue(engine.conditionRegistry.get("IN_PARTY")!!.test(leaderCtx, emptyMap()))
            assertTrue(engine.conditionRegistry.get("IS_PARTY_LEADER")!!.test(leaderCtx, emptyMap()))
            assertTrue(engine.conditionRegistry.get("SAME_PARTY")!!.test(leaderCtx, emptyMap()))
            assertFalse(engine.conditionRegistry.get("SAME_PARTY")!!.test(outsiderCtx, emptyMap()))
            assertTrue(engine.conditionRegistry.get("PARTY_SIZE_ABOVE")!!.test(leaderCtx, mapOf("size" to 2)))
            assertFalse(engine.conditionRegistry.get("PARTY_SIZE_ABOVE")!!.test(leaderCtx, mapOf("size" to 3)))

            assertTrue(engine.filterRegistry.get("SAME_PARTY")!!.test(leaderCtx, emptyMap()))
            assertTrue(engine.filterRegistry.get("NOT_SAME_PARTY")!!.test(outsiderCtx, emptyMap()))
        } finally {
            ZenPartyHub.unregisterListener(listener)
        }
    }

    @Test
    fun `new party conditions, mutators, and ContextResolver placeholders work accurately`() {
        val world = fakeWorld()
        val leaderState = FakePlayerState(fakeLeaderId, "LeaderOne", world, Location(world, 0.0, 64.0, 0.0))
        val modState = FakePlayerState(fakeModId, "ModTwo", world, Location(world, 5.0, 64.0, 0.0))
        val memberState = FakePlayerState(fakeMemberId, "MemberThree", world, Location(world, 8.0, 64.0, 0.0))
        val farMemberState = FakePlayerState(fakeFarMemberId, "FarFour", world, Location(world, 100.0, 64.0, 0.0))
        val outsiderState = FakePlayerState(fakeOutsiderId, "OutsiderFive", world, Location(world, 2.0, 64.0, 0.0))

        val leader = fakePlayer(leaderState)
        val moderator = fakePlayer(modState)
        val member = fakePlayer(memberState)
        val farMember = fakePlayer(farMemberState)
        val outsider = fakePlayer(outsiderState)

        val onlinePlayers = mapOf(
            fakeLeaderId to leader,
            fakeModId to moderator,
            fakeMemberId to member,
            fakeFarMemberId to farMember,
            fakeOutsiderId to outsider
        )
        val partyStore = ConcurrentHashMap<UUID, ZenPartyView>()
        val provider = createStubProvider(partyStore, onlinePlayers)
        ZenCoreAPI.parties.registerProvider(provider)

        val partyId = UUID.randomUUID()
        val now = System.currentTimeMillis()
        partyStore[partyId] = ZenPartyView(
            id = partyId,
            leaderUuid = fakeLeaderId,
            leaderName = "LeaderOne",
            members = listOf(
                ZenPartyMember(fakeLeaderId, "LeaderOne", ZenPartyRole.LEADER, now),
                ZenPartyMember(fakeModId, "ModTwo", ZenPartyRole.MODERATOR, now),
                ZenPartyMember(fakeMemberId, "MemberThree", ZenPartyRole.MEMBER, now),
                ZenPartyMember(fakeFarMemberId, "FarFour", ZenPartyRole.MEMBER, now)
            ),
            friendlyFire = false
        )

        val engine = ZenEngine()
        val leaderCtx = ZenExecutionContext.builder().player(leader).damage(10.0).build()
        val modCtx = ZenExecutionContext.builder().player(moderator).build()
        val memberCtx = ZenExecutionContext.builder().player(member).build()
        val outsiderCtx = ZenExecutionContext.builder().player(outsider).build()

        // PARTY_SIZE_BELOW & PARTY_SIZE_EQUALS (size = 4)
        assertTrue(engine.conditionRegistry.get("PARTY_SIZE_BELOW")!!.test(leaderCtx, mapOf("size" to 5)))
        assertFalse(engine.conditionRegistry.get("PARTY_SIZE_BELOW")!!.test(leaderCtx, mapOf("size" to 4)))
        assertTrue(engine.conditionRegistry.get("PARTY_SIZE_EQUALS")!!.test(leaderCtx, mapOf("size" to 4)))
        assertFalse(engine.conditionRegistry.get("PARTY_SIZE_EQUALS")!!.test(leaderCtx, mapOf("size" to 3)))
        assertFalse(engine.conditionRegistry.get("PARTY_SIZE_BELOW")!!.test(outsiderCtx, mapOf("size" to 5)))

        // IS_PARTY_MODERATOR
        assertTrue(engine.conditionRegistry.get("IS_PARTY_MODERATOR")!!.test(leaderCtx, emptyMap()))
        assertTrue(engine.conditionRegistry.get("IS_PARTY_MODERATOR")!!.test(modCtx, emptyMap()))
        assertFalse(engine.conditionRegistry.get("IS_PARTY_MODERATOR")!!.test(memberCtx, emptyMap()))
        assertFalse(engine.conditionRegistry.get("IS_PARTY_MODERATOR")!!.test(outsiderCtx, emptyMap()))
        assertTrue(ZenPartyHub.isModeratorOrLeader(moderator))
        assertTrue(ZenPartyHook.isModeratorOrLeader(moderator))
        assertFalse(ZenPartyHook.isModeratorOrLeader(member))

        // NEARBY_PARTY_MEMBERS (within 20 blocks of leader: moderator + member = 2 others; farMember is 100 blocks away)
        assertTrue(engine.conditionRegistry.get("NEARBY_PARTY_MEMBERS")!!.test(leaderCtx, mapOf("count" to 2, "radius" to 20.0)))
        assertFalse(engine.conditionRegistry.get("NEARBY_PARTY_MEMBERS")!!.test(leaderCtx, mapOf("count" to 3, "radius" to 20.0)))
        assertTrue(engine.conditionRegistry.get("NEARBY_PARTY_MEMBERS")!!.test(leaderCtx, mapOf("count" to 3, "radius" to 0.0)))

        // PARTY_FRIENDLY_FIRE
        assertFalse(engine.conditionRegistry.get("PARTY_FRIENDLY_FIRE")!!.test(leaderCtx, emptyMap()))
        partyStore[partyId] = partyStore[partyId]!!.copy(friendlyFire = true)
        assertTrue(engine.conditionRegistry.get("PARTY_FRIENDLY_FIRE")!!.test(leaderCtx, emptyMap()))

        // IN_PARTY filter
        assertTrue(engine.filterRegistry.get("IN_PARTY")!!.test(memberCtx, emptyMap()))
        assertFalse(engine.filterRegistry.get("IN_PARTY")!!.test(outsiderCtx, emptyMap()))

        // PARTY_SIZE_MULTIPLIER mutator (damage & variable)
        engine.executeMutators(
            leaderCtx,
            listOf(mapOf("id" to "PARTY_SIZE_MULTIPLIER", "args" to mapOf("per_member" to 0.25)))
        )
        assertEquals(20.0, leaderCtx.damage, 1e-6) // 10.0 * (1.0 + 4 * 0.25)

        engine.executeMutators(
            leaderCtx,
            listOf(mapOf("id" to "PARTY_SIZE_MULTIPLIER", "args" to mapOf("per_member" to 0.1, "variable" to "party_bonus")))
        )
        assertEquals(1.4, (leaderCtx.data["party_bonus"] as Number).toDouble(), 1e-6)

        // ContextResolver party placeholders
        assertEquals(
            "in=true,size=4,leader=LeaderOne",
            ContextResolver.resolvePlaceholders("in=%in_party%,size=%party_size%,leader=%party_leader%", memberCtx)
        )
        assertEquals(
            "in=false,size=0,leader=",
            ContextResolver.resolvePlaceholders("in=%in_party%,size=%party_size%,leader=%party_leader%", outsiderCtx)
        )
    }

    @Test
    fun `new party effects PARTY_BROADCAST, PARTY_HEAL, and PARTY_GIVE_CURRENCY behave safely and accurately`() {
        val world = fakeWorld()
        val leaderState = FakePlayerState(fakeLeaderId, "LeaderOne", world, Location(world, 0.0, 64.0, 0.0), health = 10.0, maxHealth = 20.0)
        val nearState = FakePlayerState(fakeMemberId, "NearTwo", world, Location(world, 10.0, 64.0, 0.0), health = 16.0, maxHealth = 20.0)
        val farState = FakePlayerState(fakeFarMemberId, "FarThree", world, Location(world, 50.0, 64.0, 0.0), health = 10.0, maxHealth = 20.0)
        val outsiderState = FakePlayerState(fakeOutsiderId, "Outsider", world, Location(world, 2.0, 64.0, 0.0), health = 10.0, maxHealth = 20.0)

        val leader = fakePlayer(leaderState)
        val nearMember = fakePlayer(nearState)
        val farMember = fakePlayer(farState)
        val outsider = fakePlayer(outsiderState)

        val onlinePlayers = mapOf(
            fakeLeaderId to leader,
            fakeMemberId to nearMember,
            fakeFarMemberId to farMember,
            fakeOutsiderId to outsider
        )
        val broadcasts = mutableListOf<Pair<UUID, String>>()
        val partyStore = ConcurrentHashMap<UUID, ZenPartyView>()
        val provider = createStubProvider(partyStore, onlinePlayers, broadcasts)
        ZenCoreAPI.parties.registerProvider(provider)

        val created = ZenCoreAPI.parties.createParty(leader)!!
        val now = System.currentTimeMillis()
        partyStore[created.id] = created.copy(
            members = created.members + listOf(
                ZenPartyMember(fakeMemberId, "NearTwo", ZenPartyRole.MEMBER, now),
                ZenPartyMember(fakeFarMemberId, "FarThree", ZenPartyRole.MEMBER, now)
            )
        )

        assertTrue(ZenPartyHook.isInstalled())
        assertEquals(3, ZenPartyHub.getOnlineMemberCount(created.id))
        assertEquals(3, ZenPartyHook.getOnlineMemberCount(created.id))
        assertEquals(2, ZenPartyHook.getNearbyOnlineMembers(leader, 20.0).size)

        val engine = ZenEngine()
        val ctx = ZenExecutionContext.builder().player(leader).build()

        // PARTY_BROADCAST
        engine.executeEffect(
            "PARTY_BROADCAST",
            ctx,
            mapOf("message" to "Boss killed by %party_leader% (%party_size% members)")
        )
        assertEquals(1, broadcasts.size)
        assertEquals(created.id, broadcasts[0].first)
        assertEquals("Boss killed by LeaderOne (3 members)", broadcasts[0].second)

        // PARTY_HEAL (radius 20 heals leader 10 -> 16, nearMember 16 -> 20 capped, skips farMember & outsider)
        engine.executeEffect("PARTY_HEAL", ctx, mapOf("amount" to 6.0, "radius" to 20.0))
        assertEquals(16.0, leaderState.health, 1e-6)
        assertEquals(20.0, nearState.health, 1e-6)
        assertEquals(10.0, farState.health, 1e-6)
        assertEquals(10.0, outsiderState.health, 1e-6)

        // PARTY_GIVE_CURRENCY executes safely even with no Vault/ZenEconomy plugin loaded in unit tests
        engine.executeEffect(
            "PARTY_GIVE_CURRENCY",
            ctx,
            mapOf("currency" to "vault", "amount" to 100.0, "split" to true)
        )
        engine.executeEffect(
            "PARTY_GIVE_CURRENCY",
            ctx,
            mapOf("currency" to "gems", "amount" to 50.0, "split" to false)
        )

        // Disband via ZenPartyHook parity
        assertTrue(ZenPartyHook.disbandParty(created.id, leader.uniqueId))
        assertFalse(ZenPartyHub.isInParty(leader))
    }
}

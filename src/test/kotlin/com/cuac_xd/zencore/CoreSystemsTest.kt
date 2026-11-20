package com.cuac_xd.zencore

import com.cuac_xd.zencore.api.ZenCoreAPI
import com.cuac_xd.zencore.booster.ActiveBooster
import com.cuac_xd.zencore.booster.BoosterStackMode
import com.cuac_xd.zencore.booster.ZenBoosterDefinition
import com.cuac_xd.zencore.booster.ZenBoosterEngine
import com.cuac_xd.zencore.booster.ZenBoosterListener
import com.cuac_xd.zencore.booster.ZenBoosterProvider
import com.cuac_xd.zencore.booster.ZenBoosterScope
import com.cuac_xd.zencore.engine.ZenEngine
import com.cuac_xd.zencore.engine.context.ContextResolver
import com.cuac_xd.zencore.engine.context.ZenExecutionContext
import com.cuac_xd.zencore.engine.effect.ZenEffect
import com.cuac_xd.zencore.hooks.ZenBoosterHook
import com.cuac_xd.zencore.hooks.ZenHooksHub
import com.cuac_xd.zencore.party.ZenPartyHub
import com.cuac_xd.zencore.party.ZenPartyInvite
import com.cuac_xd.zencore.party.ZenPartyMember
import com.cuac_xd.zencore.party.ZenPartyProvider
import com.cuac_xd.zencore.party.ZenPartyRole
import com.cuac_xd.zencore.party.ZenPartyView
import com.cuac_xd.zencore.stat.StatModifier
import com.cuac_xd.zencore.stat.StatOperation
import com.cuac_xd.zencore.stat.ZenStatEngine
import com.cuac_xd.zencore.trigger.ZenTriggerEngine
import com.cuac_xd.zencore.world.ZenBlockTracker
import com.cuac_xd.zencore.world.ZenWorldActions
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.lang.reflect.Proxy
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class CoreSystemsTest {

    private fun fakeWorld(name: String = "world"): World {
        val uid = UUID.randomUUID()
        return Proxy.newProxyInstance(
            World::class.java.classLoader,
            arrayOf(World::class.java)
        ) { proxy, method, args ->
            when (method.name) {
                "getUID", "getUid" -> uid
                "getName" -> name
                "equals" -> args?.getOrNull(0) === proxy
                "hashCode" -> uid.hashCode()
                "toString" -> "FakeWorld($name)"
                else -> null
            }
        } as World
    }

    private fun fakePlayer(uuid: UUID = UUID.randomUUID(), name: String = "Tester"): Player {
        val world = fakeWorld()
        val loc = Location(world, 0.0, 64.0, 0.0)
        return Proxy.newProxyInstance(
            Player::class.java.classLoader,
            arrayOf(Player::class.java)
        ) { proxy, method, args ->
            when (method.name) {
                "getUniqueId" -> uuid
                "getName" -> name
                "getWorld" -> world
                "getLocation" -> loc.clone()
                "isDead" -> false
                "isOnline" -> true
                "getHealth" -> 20.0
                "getMaxHealth" -> 20.0
                "getFoodLevel" -> 20
                "equals" -> {
                    val other = args?.getOrNull(0)
                    other === proxy || (other is Player && other.uniqueId == uuid)
                }
                "hashCode" -> uuid.hashCode()
                "toString" -> "FakePlayer($name)"
                else -> null
            }
        } as Player
    }

    private fun fakeBlock(world: World, x: Int, y: Int, z: Int, material: Material = Material.STONE): Block {
        return Proxy.newProxyInstance(
            Block::class.java.classLoader,
            arrayOf(Block::class.java)
        ) { proxy, method, args ->
            when (method.name) {
                "getWorld" -> world
                "getX" -> x
                "getY" -> y
                "getZ" -> z
                "getType" -> material
                "getLocation" -> Location(world, x.toDouble(), y.toDouble(), z.toDouble())
                "equals" -> args?.getOrNull(0) === proxy
                "hashCode" -> (31 * x + y) * 31 + z
                else -> null
            }
        } as Block
    }

    private fun registerSimplePartyProvider(parties: Map<UUID, ZenPartyView>) {
        val provider = object : ZenPartyProvider {
            override fun isInParty(playerUuid: UUID): Boolean = getPartyByPlayer(playerUuid) != null
            override fun getPartyId(playerUuid: UUID): UUID? = getPartyByPlayer(playerUuid)?.id
            override fun getParty(partyId: UUID): ZenPartyView? = parties[partyId]
            override fun getPartyByPlayer(playerUuid: UUID): ZenPartyView? =
                parties.values.firstOrNull { it.isMember(playerUuid) }
            override fun getAllParties(): Collection<ZenPartyView> = parties.values
            override fun getMembers(partyId: UUID): Set<UUID> =
                parties[partyId]?.members?.map { it.uuid }?.toSet() ?: emptySet()
            override fun getOnlineMembers(partyId: UUID): List<Player> = emptyList()
            override fun getLeader(partyId: UUID): UUID? = parties[partyId]?.leaderUuid
            override fun isLeader(playerUuid: UUID): Boolean = getPartyByPlayer(playerUuid)?.isLeader(playerUuid) == true
            override fun areInSameParty(player1: UUID, player2: UUID): Boolean {
                val p1 = getPartyId(player1) ?: return false
                return p1 == getPartyId(player2)
            }
            override fun getMaxSize(partyId: UUID): Int = parties[partyId]?.maxSize ?: 8
            override fun isPartyChatEnabled(partyId: UUID): Boolean = false
            override fun isPlayerInPartyChatMode(playerUuid: UUID): Boolean = false
            override fun getPendingInvites(targetNameOrUuid: String): List<ZenPartyInvite> = emptyList()
            override fun createParty(leader: Player): ZenPartyView? = null
            override fun disbandParty(partyId: UUID, actorUuid: UUID?): Boolean = false
            override fun invitePlayer(inviter: Player, targetName: String): Boolean = false
            override fun acceptInvite(player: Player, inviterOrPartyName: String?): Boolean = false
            override fun denyInvite(player: Player, inviterOrPartyName: String?): Boolean = false
            override fun leaveParty(playerUuid: UUID): Boolean = false
            override fun kickMember(partyId: UUID, targetUuid: UUID, actorUuid: UUID?): Boolean = false
            override fun transferLeadership(partyId: UUID, newLeaderUuid: UUID, actorUuid: UUID?): Boolean = false
            override fun togglePartyChat(partyId: UUID, actorUuid: UUID?): Boolean = false
            override fun setPlayerChatMode(playerUuid: UUID, enabled: Boolean): Boolean = false
            override fun sendPartyChat(partyId: UUID, senderUuid: UUID, senderName: String, message: String) {}
            override fun broadcastToParty(partyId: UUID, miniMessage: String) {}
            override fun connectPartyToServer(partyId: UUID, serverName: String): Boolean = false
            override fun warpPartyToLeader(partyId: UUID): Boolean = false
            override fun openMainMenu(player: Player) {}
        }
        ZenPartyHub.registerProvider(provider)
    }

    @AfterEach
    fun cleanup() {
        ZenBoosterEngine.clearAll()
        ZenBoosterEngine.unregisterProvider()
        ZenBoosterEngine.clearListeners()
        ZenStatEngine.clearAll()
        ZenBlockTracker.clearAll()
        ZenPartyHub.getProvider()?.let { ZenPartyHub.unregisterProvider(it) }
    }

    @Test
    fun `ZenCoreAPI exposes new subsystems`() {
        assertSame(ZenBoosterEngine, ZenCoreAPI.boosters)
        assertSame(ZenStatEngine, ZenCoreAPI.stats)
        assertSame(ZenBlockTracker, ZenCoreAPI.blockTracker)
        assertSame(ZenWorldActions, ZenCoreAPI.worldActions)
    }

    @Test
    fun `ZenBoosterEngine handles SUM, MULTIPLY, HIGHEST, expiration, and party-shared boosters`() {
        val playerId = UUID.randomUUID()
        val partyMateId = UUID.randomUUID()
        val outsiderId = UUID.randomUUID()

        // Empty defaults to 1.0 across all stack modes
        assertFalse(ZenBoosterEngine.hasActiveBooster(playerId, "xp"))
        assertEquals(1.0, ZenBoosterEngine.getMultiplier(playerId, "xp", BoosterStackMode.SUM), 1e-6)
        assertEquals(1.0, ZenBoosterEngine.getMultiplier(playerId, "xp", BoosterStackMode.MULTIPLY), 1e-6)
        assertEquals(1.0, ZenBoosterEngine.getMultiplier(playerId, "xp", BoosterStackMode.HIGHEST), 1e-6)

        // Personal + Global stacking (case-insensitive category)
        ZenBoosterEngine.addPlayerBooster(playerId, "personal_xp", "XP", 1.5, 0L)
        ZenBoosterEngine.addGlobalBooster("global_xp", "xp", 2.0, 120L)
        ZenBoosterEngine.addPlayerBooster(playerId, "drops_only", "drops", 3.0, 0L)

        assertTrue(ZenBoosterEngine.hasActiveBooster(playerId, "xp"))
        // SUM: 1.0 + (1.5 - 1.0) + (2.0 - 1.0) = 2.5
        assertEquals(2.5, ZenBoosterEngine.getMultiplier(playerId, "xp", BoosterStackMode.SUM), 1e-6)
        // MULTIPLY: 1.5 * 2.0 = 3.0
        assertEquals(3.0, ZenBoosterEngine.getMultiplier(playerId, "xp", BoosterStackMode.MULTIPLY), 1e-6)
        // HIGHEST: max(1.5, 2.0) = 2.0
        assertEquals(2.0, ZenBoosterEngine.getMultiplier(playerId, "xp", BoosterStackMode.HIGHEST), 1e-6)
        // Category isolation
        assertEquals(3.0, ZenBoosterEngine.getMultiplier(playerId, "drops", BoosterStackMode.SUM), 1e-6)

        // Party-shared boosters
        val partyId = UUID.randomUUID()
        val now = System.currentTimeMillis()
        registerSimplePartyProvider(
            mapOf(
                partyId to ZenPartyView(
                    id = partyId,
                    leaderUuid = playerId,
                    leaderName = "Leader",
                    members = listOf(
                        ZenPartyMember(playerId, "Leader", ZenPartyRole.LEADER, now),
                        ZenPartyMember(partyMateId, "Mate", ZenPartyRole.MEMBER, now)
                    )
                )
            )
        )

        ZenBoosterEngine.addPlayerBooster(partyMateId, "mate_shared", "xp", 1.4, 60L, shareWithParty = true)
        ZenBoosterEngine.addPlayerBooster(partyMateId, "mate_private", "xp", 4.0, 60L, shareWithParty = false)

        // Leader gets personal (1.5) + global (2.0) + mate_shared (1.4); does NOT get mate_private (4.0)
        // SUM: 1.0 + 0.5 + 1.0 + 0.4 = 2.9
        assertEquals(2.9, ZenBoosterEngine.getMultiplier(playerId, "xp", BoosterStackMode.SUM), 1e-6)
        // MULTIPLY: 1.5 * 2.0 * 1.4 = 4.2
        assertEquals(4.2, ZenBoosterEngine.getMultiplier(playerId, "xp", BoosterStackMode.MULTIPLY), 1e-6)
        // Party mate does not double-count their own shared booster (gets global 2.0 + mate_shared 1.4 + mate_private 4.0)
        assertEquals(1.0 + 1.0 + 0.4 + 3.0, ZenBoosterEngine.getMultiplier(partyMateId, "xp", BoosterStackMode.SUM), 1e-6)
        // Outsider only receives global booster (2.0)
        assertEquals(2.0, ZenBoosterEngine.getMultiplier(outsiderId, "xp", BoosterStackMode.SUM), 1e-6)

        // Removal
        ZenBoosterEngine.removeBooster(partyMateId, "mate_shared")
        ZenBoosterEngine.removeBooster(null, "global_xp")
        assertEquals(1.5, ZenBoosterEngine.getMultiplier(playerId, "xp", BoosterStackMode.SUM), 1e-6)

        // ActiveBooster expiration semantics
        val permanent = ActiveBooster("perm", "xp", 1.5, 0L)
        assertFalse(permanent.isExpired(now))
        assertEquals(-1L, permanent.remainingSeconds(now))

        val finite = ActiveBooster("temp", "xp", 1.5, now + 5000L)
        assertFalse(finite.isExpired(now))
        assertEquals(5L, finite.remainingSeconds(now))
        assertTrue(finite.isExpired(now + 5000L))
        assertEquals(0L, finite.remainingSeconds(now + 6000L))

        // Real expiration & pruneExpired()
        ZenBoosterEngine.addPlayerBooster(outsiderId, "short_lived", "coins", 2.0, 1L)
        assertTrue(ZenBoosterEngine.hasActiveBooster(outsiderId, "coins"))
        Thread.sleep(1100L)
        assertFalse(ZenBoosterEngine.hasActiveBooster(outsiderId, "coins"))
        ZenBoosterEngine.pruneExpired()
        assertTrue(ZenBoosterEngine.getActiveBoosters(outsiderId).isEmpty())
    }

    @Test
    fun `ZenBoosterEngine integrates with ZenEngine effects, conditions, and mutators`() {
        val player = fakePlayer(name = "BoosterUser")
        val engine = ZenEngine()
        val ctx = ZenExecutionContext.builder().player(player).damage(10.0).build()

        assertFalse(engine.conditionRegistry.get("HAS_ACTIVE_BOOSTER")!!.test(ctx, mapOf("category" to "damage")))

        engine.executeEffect(
            "ACTIVATE_BOOSTER",
            ctx,
            mapOf("id" to "rage_boost", "category" to "damage", "multiplier" to 1.5, "duration" to 60)
        )
        assertTrue(engine.conditionRegistry.get("HAS_ACTIVE_BOOSTER")!!.test(ctx, mapOf("category" to "damage")))

        engine.executeMutators(
            ctx,
            listOf(mapOf("id" to "APPLY_BOOSTER_MULTIPLIER", "args" to mapOf("category" to "damage", "mode" to "SUM")))
        )
        assertEquals(15.0, ctx.damage, 1e-6)

        engine.executeMutators(
            ctx,
            listOf(mapOf("id" to "APPLY_BOOSTER_MULTIPLIER", "args" to mapOf("category" to "damage", "variable" to "boost_var")))
        )
        assertEquals(1.5, (ctx.data["boost_var"] as Number).toDouble(), 1e-6)
    }

    @Test
    fun `ZenStatEngine computes ADDITIVE, PERCENTAGE, MULTIPLICATIVE, expiration, and resource pools`() {
        val uuid = UUID.randomUUID()

        // Base + combined operations:
        // (base + sum(ADDITIVE)) * (1.0 + sum(PERCENTAGE)/100.0) * product(MULTIPLICATIVE)
        ZenStatEngine.setBaseStat(uuid, "strength", 10.0)
        assertEquals(10.0, ZenStatEngine.getBaseStat(uuid, "STRENGTH"), 1e-6)

        ZenStatEngine.addModifier(uuid, "ring", "strength", 5.0, StatOperation.ADDITIVE)
        ZenStatEngine.addModifier(uuid, "amulet", "strength", 5.0, StatOperation.ADDITIVE)
        ZenStatEngine.addModifier(uuid, "aura", "strength", 25.0, StatOperation.PERCENTAGE)
        ZenStatEngine.addModifier(uuid, "passive", "strength", 25.0, StatOperation.PERCENTAGE)
        ZenStatEngine.addModifier(uuid, "crit", "strength", 2.0, StatOperation.MULTIPLICATIVE)

        // (10 + 5 + 5) * (1 + (25 + 25)/100) * 2 = 20 * 1.5 * 2 = 60.0
        assertEquals(60.0, ZenStatEngine.getStat(uuid, "strength"), 1e-6)

        // Replacing same key+stat overwrites prior modifier
        ZenStatEngine.addModifier(uuid, "ring", "strength", 15.0, StatOperation.ADDITIVE)
        // (10 + 15 + 5) * 1.5 * 2 = 90.0
        assertEquals(90.0, ZenStatEngine.getStat(uuid, "strength"), 1e-6)

        // Remove single stat modifier vs remove key across all stats
        ZenStatEngine.addModifier(uuid, "ring", "defense", 10.0, StatOperation.ADDITIVE)
        ZenStatEngine.removeModifier(uuid, "ring", "strength")
        assertEquals(10.0, ZenStatEngine.getStat(uuid, "defense"), 1e-6)
        ZenStatEngine.removeModifier(uuid, "ring")
        assertEquals(0.0, ZenStatEngine.getStat(uuid, "defense"), 1e-6)

        // Expiring stat modifier (1 tick = 50ms) & StatModifier.isExpired
        val expiredCheck = StatModifier("temp", "speed", 5.0, StatOperation.ADDITIVE, expiresAt = 1000L)
        assertTrue(expiredCheck.isExpired(1000L))
        ZenStatEngine.addModifier(uuid, "burst", "speed", 20.0, StatOperation.ADDITIVE, durationTicks = 1L)
        assertEquals(20.0, ZenStatEngine.getStat(uuid, "speed"), 1e-6)
        Thread.sleep(80L)
        ZenStatEngine.pruneExpired()
        assertEquals(0.0, ZenStatEngine.getStat(uuid, "speed"), 1e-6)
        assertTrue(ZenStatEngine.getModifiers(uuid, "speed").isEmpty())

        // Resource pool (mana & max_mana stat scaling)
        assertEquals(100.0, ZenStatEngine.getMaxResource(uuid, "mana"), 1e-6)
        assertEquals(100.0, ZenStatEngine.getResource(uuid, "mana"), 1e-6)

        ZenStatEngine.addModifier(uuid, "staff", "max_mana", 50.0, StatOperation.ADDITIVE)
        assertEquals(150.0, ZenStatEngine.getMaxResource(uuid, "mana"), 1e-6)

        ZenStatEngine.setResource(uuid, "mana", 200.0) // clamped to 150
        assertEquals(150.0, ZenStatEngine.getResource(uuid, "mana"), 1e-6)
        ZenStatEngine.setResource(uuid, "mana", -10.0) // clamped to 0
        assertEquals(0.0, ZenStatEngine.getResource(uuid, "mana"), 1e-6)

        assertEquals(40.0, ZenStatEngine.giveResource(uuid, "mana", 40.0), 1e-6)
        assertTrue(ZenStatEngine.hasResource(uuid, "mana", 30.0))
        assertTrue(ZenStatEngine.consumeResource(uuid, "mana", 30.0))
        assertEquals(10.0, ZenStatEngine.getResource(uuid, "mana"), 1e-6)

        // Insufficient / invalid consumption fails cleanly without mutating balance
        assertFalse(ZenStatEngine.consumeResource(uuid, "mana", 25.0))
        assertFalse(ZenStatEngine.consumeResource(uuid, "mana", -5.0))
        assertFalse(ZenStatEngine.consumeResource(uuid, "mana", Double.NaN))
        assertEquals(10.0, ZenStatEngine.getResource(uuid, "mana"), 1e-6)
    }

    @Test
    fun `ZenStatEngine integrates with ZenEngine effects, conditions, and ContextResolver placeholders`() {
        val player = fakePlayer(name = "Mage")
        val engine = ZenEngine()
        val ctx = ZenExecutionContext.builder().player(player).level(3).build()

        engine.executeEffect(
            "ADD_STAT_MODIFIER",
            ctx,
            mapOf("key" to "wisdom", "stat" to "max_mana", "amount" to "20 * %level%", "operation" to "ADDITIVE")
        )
        assertEquals(160.0, ZenStatEngine.getMaxResource(player.uniqueId, "mana"), 1e-6)
        assertTrue(engine.conditionRegistry.get("STAT_ABOVE")!!.test(ctx, mapOf("stat" to "max_mana", "value" to 50)))
        assertTrue(engine.conditionRegistry.get("STAT_BELOW")!!.test(ctx, mapOf("stat" to "max_mana", "value" to 100)))

        ZenStatEngine.setResource(player.uniqueId, "mana", 50.0)
        assertEquals("50.0/160.0", ContextResolver.resolvePlaceholders("%mana%/%max_mana%", ctx))

        engine.executeEffect("GIVE_MANA", ctx, mapOf("amount" to "30"))
        assertEquals(80.0, ZenStatEngine.getResource(player.uniqueId, "mana"), 1e-6)
        assertTrue(engine.conditionRegistry.get("HAS_MANA")!!.test(ctx, mapOf("amount" to 80)))

        engine.executeEffect("TAKE_MANA", ctx, mapOf("amount" to "25"))
        assertEquals(55.0, ZenStatEngine.getResource(player.uniqueId, "mana"), 1e-6)

        engine.executeEffect("REMOVE_STAT_MODIFIER", ctx, mapOf("key" to "wisdom", "stat" to "max_mana"))
        assertEquals(100.0, ZenStatEngine.getMaxResource(player.uniqueId, "mana"), 1e-6)
    }

    @Test
    fun `ZenEngine evaluates dynamic math formulas for require, cooldown, mana_cost, and every`() {
        val player = fakePlayer(name = "FormulaCaster")
        val engine = ZenEngine()
        val executions = AtomicInteger(0)

        engine.effectRegistry.register("TEST_CAST", ZenEffect { _, _ ->
            executions.incrementAndGet()
        })

        // 1. require formula: "%level% * 2 > 5"
        val lowLevelCtx = ZenExecutionContext.builder().player(player).level(2).build()
        val highLevelCtx = ZenExecutionContext.builder().player(player).level(3).build()

        engine.executeEffect("TEST_CAST", lowLevelCtx, mapOf("require" to "%level% * 2 > 5"))
        assertEquals(0, executions.get())
        engine.executeEffect("TEST_CAST", highLevelCtx, mapOf("require" to "%level% * 2 > 5"))
        assertEquals(1, executions.get())

        // 2. mana_cost formula pre-check runs BEFORE cooldown commit
        ZenStatEngine.setResource(player.uniqueId, "mana", 15.0)
        // level=3 => mana_cost = 10 + 3*5 = 25 (insufficient), cooldown = max(1, 5 - 3) = 2s
        val castArgs = mapOf(
            "mana_cost" to "10 + %level% * 5",
            "cooldown" to "max(1, 5 - %level%)",
            "cooldown_group" to "fireball",
            "send_mana_message" to false,
            "send_cooldown_message" to false
        )
        engine.executeEffect("TEST_CAST", highLevelCtx, castArgs)
        assertEquals(1, executions.get()) // blocked by insufficient mana
        assertEquals(15.0, ZenStatEngine.getResource(player.uniqueId, "mana"), 1e-6)
        // Ensure cooldown was NOT committed on mana failure
        ZenStatEngine.setResource(player.uniqueId, "mana", 60.0)
        engine.executeEffect("TEST_CAST", highLevelCtx, castArgs)
        assertEquals(2, executions.get()) // succeeds immediately
        assertEquals(35.0, ZenStatEngine.getResource(player.uniqueId, "mana"), 1e-6)

        // 3. Cooldown formula blocks immediate recast and protects mana
        engine.executeEffect("TEST_CAST", highLevelCtx, castArgs)
        assertEquals(2, executions.get())
        assertEquals(35.0, ZenStatEngine.getResource(player.uniqueId, "mana"), 1e-6)

        // Clearing cooldowns allows next cast
        engine.clearCooldowns(player.uniqueId)
        engine.executeEffect("TEST_CAST", highLevelCtx, castArgs)
        assertEquals(3, executions.get())
        assertEquals(10.0, ZenStatEngine.getResource(player.uniqueId, "mana"), 1e-6)

        // 4. Custom resource_pool + resource_cost formula
        ZenStatEngine.setResource(player.uniqueId, "stamina", 40.0)
        engine.executeEffect(
            "TEST_CAST",
            highLevelCtx,
            mapOf("resource_pool" to "stamina", "resource_cost" to "15 + %level% * 5", "send_resource_message" to false)
        )
        assertEquals(4, executions.get())
        assertEquals(10.0, ZenStatEngine.getResource(player.uniqueId, "stamina"), 1e-6)

        // 5. every formula ("1 + %level%" with level=1 -> every 2 calls)
        val everyCtx = ZenExecutionContext.builder().player(player).level(1).build()
        val everyArgs = mapOf("every" to "1 + %level%")
        engine.executeEffect("TEST_CAST", everyCtx, everyArgs)
        assertEquals(4, executions.get())
        engine.executeEffect("TEST_CAST", everyCtx, everyArgs)
        assertEquals(5, executions.get())
    }

    @Test
    fun `ZenTriggerEngine trigger dispatches custom trigger contexts to registered handlers`() {
        val player = fakePlayer(name = "TriggerHero")
        val uniqueTrigger = "custom_skill_cast_${UUID.randomUUID()}"
        val capturedTriggers = mutableListOf<String>()
        var capturedPower = 0.0

        ZenTriggerEngine.registerHandler { triggerId, ctx ->
            if (triggerId == uniqueTrigger) {
                capturedTriggers.add(triggerId)
                capturedPower = (ctx.getVariable("power") as? Number)?.toDouble() ?: 0.0
            }
        }

        val ctx = ZenExecutionContext.builder()
            .player(player)
            .level(5)
            .damage(12.0)
            .build()
        ctx.setVariable("power", 42.5)

        ZenTriggerEngine.trigger(uniqueTrigger, ctx)

        assertEquals(uniqueTrigger, ctx.trigger)
        assertEquals(listOf(uniqueTrigger), capturedTriggers)
        assertEquals(42.5, capturedPower, 1e-6)
    }

    @Test
    fun `ZenBlockTracker and ZenWorldActions operate safely in unit tests`() {
        val world = fakeWorld()
        val block = fakeBlock(world, 10, 64, -20, Material.DIAMOND_ORE)
        val engine = ZenEngine()
        val ctx = ZenExecutionContext.builder().block(block).build()

        assertFalse(ZenBlockTracker.isPlayerPlaced(block))
        assertTrue(engine.conditionRegistry.get("NOT_PLAYER_PLACED")!!.test(ctx, emptyMap()))
        assertFalse(engine.conditionRegistry.get("IS_PLAYER_PLACED")!!.test(ctx, emptyMap()))
        assertTrue(engine.filterRegistry.get("NOT_PLAYER_PLACED")!!.test(ctx, emptyMap()))

        ZenBlockTracker.markPlaced(block)
        assertTrue(ZenBlockTracker.isPlayerPlaced(block))
        assertTrue(engine.conditionRegistry.get("IS_PLAYER_PLACED")!!.test(ctx, emptyMap()))
        assertTrue(engine.filterRegistry.get("IS_PLAYER_PLACED")!!.test(ctx, emptyMap()))

        ZenBlockTracker.markBroken(block)
        assertFalse(ZenBlockTracker.isPlayerPlaced(block))

        assertEquals(Material.IRON_INGOT, ZenWorldActions.getSmeltedMaterial(Material.IRON_ORE))
        assertEquals(Material.IRON_INGOT, ZenWorldActions.getSmeltedMaterial(Material.RAW_IRON))
        assertEquals(Material.CHARCOAL, ZenWorldActions.getSmeltedMaterial(Material.OAK_LOG))
        assertEquals(Material.GLASS, ZenWorldActions.getSmeltedMaterial(Material.SAND))
        assertNull(ZenWorldActions.getSmeltedMaterial(Material.DIAMOND))
    }

    @Test
    fun `ZenBoosterProvider, ZenBoosterListener, and ZenBoosterHook integrate with ZenBoosterEngine and ZenHooksHub`() {
        val player = fakePlayer(name = "BoosterHero")
        val playerId = player.uniqueId

        assertSame(ZenBoosterHook, ZenCoreAPI.boosterHook)
        assertSame(ZenBoosterHook, ZenHooksHub.boosters)
        assertFalse(ZenBoosterEngine.isAvailable())
        assertEquals("economy", ZenBoosterEngine.normalizeCategoryAlias("eco"))
        assertEquals("economy", ZenBoosterEngine.normalizeCategoryAlias("Money"))
        assertEquals("xp", ZenBoosterEngine.normalizeCategoryAlias("exp"))
        assertEquals("skills_xp", ZenBoosterEngine.normalizeCategoryAlias("skills"))

        val definition = ZenBoosterDefinition(
            id = "eco_2x_global",
            displayName = "Eco 2x Global",
            category = "economy",
            scope = ZenBoosterScope.GLOBAL,
            multiplier = 2.0,
            rawModifier = 1.0,
            durationSeconds = 300L
        )
        val providerBooster = ActiveBooster(
            id = "eco_2x_global",
            category = "economy",
            multiplier = 2.0,
            expiresAt = System.currentTimeMillis() + 300_000L,
            shareWithParty = false,
            scope = ZenBoosterScope.GLOBAL,
            ownerUuid = playerId,
            ownerName = player.name
        )
        val teamBooster = ActiveBooster(
            id = "party_xp_boost",
            category = "xp",
            multiplier = 1.5,
            expiresAt = 0L,
            shareWithParty = true,
            scope = ZenBoosterScope.TEAM,
            ownerUuid = playerId
        )

        var menuOpened = false
        var vaultOpened = false
        val provider = object : ZenBoosterProvider {
            override fun hasActiveBooster(playerUuid: UUID, category: String): Boolean =
                ZenBoosterEngine.normalizeCategoryAlias(category) == "economy"

            override fun getMultiplier(playerUuid: UUID?, category: String, mode: BoosterStackMode): Double =
                if (ZenBoosterEngine.normalizeCategoryAlias(category) == "economy") 2.0 else 1.0

            override fun getActiveBoosters(playerUuid: UUID?, category: String?): List<ActiveBooster> =
                listOf(providerBooster, teamBooster).filter {
                    category == null || ZenBoosterEngine.normalizeCategoryAlias(it.category) ==
                        ZenBoosterEngine.normalizeCategoryAlias(category)
                }

            override fun getGlobalBoosters(): List<ActiveBooster> = listOf(providerBooster)
            override fun getTeamBoosters(player: Player): List<ActiveBooster> = listOf(teamBooster)
            override fun getPersonalBoosters(playerUuid: UUID): List<ActiveBooster> = emptyList()
            override fun getPrimaryGlobalBooster(): ActiveBooster = providerBooster
            override fun getBoosterById(boosterId: String): ZenBoosterDefinition? =
                if (boosterId == definition.id) definition else null
            override fun getAllBoosterDefinitions(): Collection<ZenBoosterDefinition> = listOf(definition)
            override fun activateBooster(boosterId: String, activator: Player, bypassCooldown: Boolean): Boolean = false
            override fun giveBooster(targetUuid: UUID, boosterId: String, amount: Int): Boolean = false
            override fun stopBooster(boosterId: String, scope: ZenBoosterScope?, targetUuid: UUID?): Int = 0
            override fun openMainMenu(player: Player) { menuOpened = true }
            override fun openVaultMenu(player: Player) { vaultOpened = true }
        }

        ZenBoosterEngine.registerProvider(provider)

        assertTrue(ZenBoosterEngine.isAvailable())
        assertTrue(ZenBoosterHook.isInstalled())
        assertTrue(ZenBoosterHook.isProviderRegistered())
        assertTrue(ZenHooksHub.isBoostersAvailable())
        assertEquals(true, ZenHooksHub.getAvailableHooks()["boosters"])

        // Provider-only queries via hook & alias
        assertTrue(ZenBoosterHook.hasActiveBooster(player, "eco"))
        assertEquals(2.0, ZenBoosterHook.getMultiplier(player, "economy"), 1e-6)
        assertEquals(2.0, ZenBoosterHook.getMultiplier(playerId, "eco"), 1e-6)
        assertEquals(listOf(teamBooster), ZenBoosterHook.getTeamBoosters(player))
        assertEquals(providerBooster, ZenBoosterHook.getPrimaryGlobalBooster())
        assertEquals(definition, ZenBoosterHook.getBoosterById("eco_2x_global"))
        assertEquals(1, ZenBoosterHook.getAllBoosterDefinitions().size)

        // Combine local ZenCore booster (1.5x on "eco" alias) with provider (2.0x on "economy")
        ZenBoosterEngine.addPlayerBooster(playerId, "local_eco", "eco", 1.5, 60L)
        assertEquals(2.5, ZenBoosterHook.getMultiplier(player, "economy", BoosterStackMode.SUM), 1e-6)
        assertEquals(3.0, ZenBoosterHook.getMultiplier(player, "economy", BoosterStackMode.MULTIPLY), 1e-6)
        assertEquals(2.0, ZenBoosterHook.getMultiplier(player, "economy", BoosterStackMode.HIGHEST), 1e-6)
        assertEquals(2, ZenBoosterHook.getActiveBoosters(player, "economy").size)

        ZenBoosterHook.openMainMenu(player)
        ZenBoosterHook.openVaultMenu(player)
        assertTrue(menuOpened)
        assertTrue(vaultOpened)

        // Listener lifecycle notifications
        val events = mutableListOf<String>()
        val listener = object : ZenBoosterListener {
            override fun onBoosterActivated(booster: ActiveBooster, activatorUuid: UUID?) {
                events.add("activated:${booster.id}:$activatorUuid")
            }

            override fun onBoosterExpired(booster: ActiveBooster, reason: String) {
                events.add("expired:${booster.id}:$reason")
            }
        }
        ZenBoosterHook.addListener(listener)
        ZenBoosterEngine.notifyBoosterActivated(providerBooster, playerId)
        ZenBoosterEngine.notifyBoosterExpired(providerBooster, "FORCED")
        assertEquals(listOf("activated:eco_2x_global:$playerId", "expired:eco_2x_global:FORCED"), events)

        ZenBoosterHook.removeListener(listener)
        ZenBoosterEngine.unregisterProvider(provider)
        assertFalse(ZenBoosterEngine.isAvailable())
    }

    @Test
    fun `CoreEffects GIVE_BOOSTER, ACTIVATE_BOOSTER, STOP_BOOSTER and ContextResolver booster placeholders work`() {
        val player = fakePlayer(name = "EffectHero")
        val playerId = player.uniqueId
        val engine = ZenEngine()

        val activatedIds = mutableListOf<String>()
        val givenVouchers = mutableMapOf<String, Int>()
        val stoppedIds = mutableListOf<String>()

        val provider = object : ZenBoosterProvider {
            override fun hasActiveBooster(playerUuid: UUID, category: String): Boolean =
                category.equals("economy", ignoreCase = true) || category.equals("skills_xp", ignoreCase = true)

            override fun getMultiplier(playerUuid: UUID?, category: String, mode: BoosterStackMode): Double =
                when (ZenBoosterEngine.normalizeCategoryAlias(category)) {
                    "economy" -> 2.0
                    "skills_xp" -> 1.75
                    else -> 1.0
                }

            override fun getActiveBoosters(playerUuid: UUID?, category: String?): List<ActiveBooster> = emptyList()
            override fun getGlobalBoosters(): List<ActiveBooster> = emptyList()
            override fun getTeamBoosters(player: Player): List<ActiveBooster> = emptyList()
            override fun getPersonalBoosters(playerUuid: UUID): List<ActiveBooster> = emptyList()
            override fun getPrimaryGlobalBooster(): ActiveBooster? = null
            override fun getBoosterById(boosterId: String): ZenBoosterDefinition? = null
            override fun getAllBoosterDefinitions(): Collection<ZenBoosterDefinition> = emptyList()
            override fun activateBooster(boosterId: String, activator: Player, bypassCooldown: Boolean): Boolean {
                activatedIds.add("$boosterId:$bypassCooldown")
                return true
            }
            override fun giveBooster(targetUuid: UUID, boosterId: String, amount: Int): Boolean {
                if (targetUuid != playerId) return false
                givenVouchers[boosterId] = (givenVouchers[boosterId] ?: 0) + amount
                return true
            }
            override fun stopBooster(boosterId: String, scope: ZenBoosterScope?, targetUuid: UUID?): Int {
                stoppedIds.add("$boosterId:${scope?.name}:${targetUuid != null}")
                return 1
            }
            override fun openMainMenu(player: Player) {}
            override fun openVaultMenu(player: Player) {}
        }
        ZenBoosterEngine.registerProvider(provider)

        val ctx = ZenExecutionContext.builder().player(player).build()
        ctx.data["booster_category"] = "economy"

        // ACTIVATE_BOOSTER delegates by ID when no inline multiplier/duration is given
        engine.effectRegistry.get("ACTIVATE_BOOSTER")!!.execute(
            ctx,
            mapOf("id" to "eco_2x_global", "bypass_cooldown" to true)
        )
        assertEquals(listOf("eco_2x_global:true"), activatedIds)

        // GIVE_BOOSTER & GIVE_BOOSTER_VOUCHER
        engine.effectRegistry.get("GIVE_BOOSTER")!!.execute(
            ctx,
            mapOf("booster" to "personal_xp", "amount" to 3)
        )
        engine.effectRegistry.get("GIVE_BOOSTER_VOUCHER")!!.execute(
            ctx,
            mapOf("id" to "personal_xp", "amount" to 2)
        )
        assertEquals(5, givenVouchers["personal_xp"])

        // Inline local booster activation + STOP_BOOSTER removal
        engine.effectRegistry.get("ACTIVATE_BOOSTER")!!.execute(
            ctx,
            mapOf("id" to "spawner_boost", "category" to "spawners", "multiplier" to 1.5, "duration" to 60)
        )
        assertEquals(1.5, ZenBoosterEngine.getMultiplier(playerId, "spawners"), 1e-6)

        engine.effectRegistry.get("STOP_BOOSTER")!!.execute(
            ctx,
            mapOf("id" to "spawner_boost", "scope" to "PERSONAL")
        )
        assertEquals(1.0, ZenBoosterEngine.getMultiplier(playerId, "spawners"), 1e-6)
        assertTrue(stoppedIds.contains("spawner_boost:PERSONAL:true"))

        // ContextResolver booster placeholders
        val resolved = ContextResolver.resolvePlaceholders(
            "eco=%booster_multiplier_economy% skills=%booster_multiplier_skills_xp% has=%booster_has_economy% def=%booster_multiplier%",
            ctx
        )
        assertEquals("eco=2.0 skills=1.75 has=true def=2.0", resolved)
    }

    @Test
    fun testZenCoreFamilyEcosystemHooks() {
        val hooksMap = ZenHooksHub.getAvailableHooks()
        assertTrue(hooksMap.size >= 31, "Expected at least 31 hooks registered in ZenHooksHub, found: ${hooksMap.size}")

        val expectedHookKeys = listOf(
            "economy", "cosmetics", "customItems", "enchants", "parties", "boosters", "tags", "discord",
            "auctions", "jobs", "horses", "storage", "bans", "bank", "afkZone", "chat", "chatGames",
            "dragonEvent", "fairy", "forges", "hub", "quests", "rankups", "rewards", "rogueLike", "spawner",
            "blockRegen", "contest", "crates"
        )

        for (hookKey in expectedHookKeys) {
            assertTrue(hooksMap.containsKey(hookKey), "ZenHooksHub must contain key for $hookKey")
            assertFalse(ZenHooksHub.isPluginAvailable(hookKey), "$hookKey should not be active in unit test environment")
        }

        // Test safe fallbacks on all hooks from ZenCoreAPI and ZenHooksHub
        val testUuid = UUID.randomUUID()
        val player = fakePlayer(testUuid, "ZenTester")

        // ZenAuctions
        assertFalse(ZenCoreAPI.auctions.isInstalled())
        assertEquals(0, ZenCoreAPI.auctions.getActiveAuctionCount())
        assertSame(ZenCoreAPI.auctions, ZenHooksHub.auctions)

        // ZenJobs
        assertFalse(ZenCoreAPI.jobs.isInstalled())
        assertEquals(emptyList<String>(), ZenCoreAPI.jobs.getAllJobs())
        assertSame(ZenCoreAPI.jobs, ZenHooksHub.jobs)

        // ZenHorses
        assertFalse(ZenCoreAPI.horses.isInstalled())
        assertSame(ZenCoreAPI.horses, ZenHooksHub.horses)

        // ZenStorage
        assertFalse(ZenCoreAPI.storage.isInstalled())
        assertEquals(0, ZenCoreAPI.storage.getUnlockedVaults(testUuid))
        assertSame(ZenCoreAPI.storage, ZenHooksHub.storage)

        // ZenBans
        assertFalse(ZenCoreAPI.bans.isInstalled())
        assertFalse(ZenCoreAPI.bans.isChatMuted())
        assertSame(ZenCoreAPI.bans, ZenHooksHub.bans)

        // ZenBank
        assertFalse(ZenCoreAPI.bank.isInstalled())
        assertEquals(0.0, ZenCoreAPI.bank.getBankBalance(testUuid), 1e-6)
        assertSame(ZenCoreAPI.bank, ZenHooksHub.bank)

        // ZenAfkZone
        assertFalse(ZenCoreAPI.afkZone.isInstalled())
        assertFalse(ZenCoreAPI.afkZone.isInAfkZone(testUuid))
        assertSame(ZenCoreAPI.afkZone, ZenHooksHub.afkZone)

        // ZenChat & ZenChatGames
        assertFalse(ZenCoreAPI.chat.isInstalled())
        assertSame(ZenCoreAPI.chat, ZenHooksHub.chat)
        assertFalse(ZenCoreAPI.chatGames.isInstalled())
        assertFalse(ZenCoreAPI.chatGames.hasActiveGame())
        assertSame(ZenCoreAPI.chatGames, ZenHooksHub.chatGames)

        // ZenDragonEvent
        assertFalse(ZenCoreAPI.dragonEvent.isInstalled())
        assertSame(ZenCoreAPI.dragonEvent, ZenHooksHub.dragonEvent)

        // ZenFairy
        assertFalse(ZenCoreAPI.fairy.isInstalled())
        assertEquals(0, ZenCoreAPI.fairy.getTotalSouls())
        assertEquals(emptySet<String>(), ZenCoreAPI.fairy.getSoulTypeIds())
        assertSame(ZenCoreAPI.fairy, ZenHooksHub.fairy)

        // ZenForges
        assertFalse(ZenCoreAPI.forges.isInstalled())
        assertEquals(0, ZenCoreAPI.forges.getActiveProcessesCount(testUuid))
        assertSame(ZenCoreAPI.forges, ZenHooksHub.forges)

        // ZenHub
        assertFalse(ZenCoreAPI.hub.isInstalled())
        assertFalse(ZenCoreAPI.hub.isParkourActive(testUuid))
        assertSame(ZenCoreAPI.hub, ZenHooksHub.hub)

        // ZenQuests
        assertFalse(ZenCoreAPI.quests.isInstalled())
        assertFalse(ZenCoreAPI.quests.hasActiveProgress(testUuid))
        assertSame(ZenCoreAPI.quests, ZenHooksHub.quests)

        // ZenRankups
        assertFalse(ZenCoreAPI.rankups.isInstalled())
        assertNull(ZenCoreAPI.rankups.getPlayerRank(testUuid))
        assertNull(ZenCoreAPI.rankups.getPlayerPrestige(testUuid))
        assertSame(ZenCoreAPI.rankups, ZenHooksHub.rankups)

        // ZenRewards
        assertFalse(ZenCoreAPI.rewards.isInstalled())
        assertEquals(0, ZenCoreAPI.rewards.getStreak(testUuid))
        assertSame(ZenCoreAPI.rewards, ZenHooksHub.rewards)

        // ZenRogueLike
        assertFalse(ZenCoreAPI.rogueLike.isInstalled())
        assertFalse(ZenCoreAPI.rogueLike.isInRun(testUuid))
        assertSame(ZenCoreAPI.rogueLike, ZenHooksHub.rogueLike)

        // ZenSpawner
        assertFalse(ZenCoreAPI.spawner.isInstalled())
        assertEquals(0, ZenCoreAPI.spawner.getSpawnersCount(testUuid))
        assertEquals(0, ZenCoreAPI.spawner.getTotalSpawnersCount())
        assertSame(ZenCoreAPI.spawner, ZenHooksHub.spawner)

        // ZenBlockRegen
        assertFalse(ZenCoreAPI.blockRegen.isInstalled())
        assertFalse(ZenCoreAPI.blockRegen.isPlayerInBypass(player))
        assertSame(ZenCoreAPI.blockRegen, ZenHooksHub.blockRegen)

        // ZenContest
        assertFalse(ZenCoreAPI.contest.isInstalled())
        assertFalse(ZenCoreAPI.contest.hasActiveContests())
        assertEquals(0L, ZenCoreAPI.contest.getPlayerScore(testUuid, "wheat_contest"))
        assertSame(ZenCoreAPI.contest, ZenHooksHub.contest)

        // ZenCrates
        assertFalse(ZenCoreAPI.crates.isInstalled())
        assertEquals(emptyList<String>(), ZenCoreAPI.crates.getCrateIds())
        assertEquals(emptyList<String>(), ZenCoreAPI.crates.getKeyIds())
        assertEquals(0, ZenCoreAPI.crates.getPhysicalKeyCount(player, "legendary_key"))
        assertSame(ZenCoreAPI.crates, ZenHooksHub.crates)
    }
}

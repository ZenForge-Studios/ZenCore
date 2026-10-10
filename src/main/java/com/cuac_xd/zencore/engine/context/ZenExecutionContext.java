package com.cuac_xd.zencore.engine.context;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Universal execution context for triggers, conditions, mutators, filters, and effects across all Zen plugins.
 */
public class ZenExecutionContext {

    private String trigger;
    private Player player;
    private LivingEntity target;
    private Entity targetEntity;
    private Block block;
    private ItemStack item;
    private Location location;
    private int level = 1;
    private double damage = 0.0;
    private boolean cancelled = false;
    private Event event;
    private final Map<String, Object> data = new HashMap<>();

    public ZenExecutionContext() {}

    public ZenExecutionContext(@Nullable Player player, @Nullable LivingEntity target, @Nullable Block block, @Nullable ItemStack item) {
        this.player = player;
        this.target = target;
        this.targetEntity = target;
        this.block = block;
        this.item = item;
        this.location = (player != null) ? player.getLocation() : (target != null ? target.getLocation() : (block != null ? block.getLocation() : null));
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getTrigger() {
        return trigger;
    }

    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }

    @Nullable
    public Player getPlayer() {
        return player;
    }

    public void setPlayer(@Nullable Player player) {
        this.player = player;
    }

    @Nullable
    public LivingEntity getTarget() {
        return target;
    }

    public void setTarget(@Nullable LivingEntity target) {
        this.target = target;
        if (target != null) {
            this.targetEntity = target;
        }
    }

    @Nullable
    public Entity getTargetEntity() {
        return targetEntity != null ? targetEntity : target;
    }

    public void setTargetEntity(@Nullable Entity targetEntity) {
        this.targetEntity = targetEntity;
        if (targetEntity instanceof LivingEntity living) {
            this.target = living;
        }
    }

    @Nullable
    public Block getBlock() {
        return block;
    }

    public void setBlock(@Nullable Block block) {
        this.block = block;
    }

    @Nullable
    public ItemStack getItem() {
        return item;
    }

    public void setItem(@Nullable ItemStack item) {
        this.item = item;
    }

    @Nullable
    public Location getLocation() {
        if (location != null) return location;
        if (player != null) return player.getLocation();
        if (targetEntity != null) return targetEntity.getLocation();
        if (block != null) return block.getLocation().add(0.5, 0.5, 0.5);
        return null;
    }

    public void setLocation(@Nullable Location location) {
        this.location = location;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public double getDamage() {
        return damage;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
        if (event instanceof Cancellable cancellable) {
            cancellable.setCancelled(cancelled);
        }
    }

    public void cancelEvent() {
        setCancelled(true);
    }

    @Nullable
    public Event getEvent() {
        return event;
    }

    public void setEvent(@Nullable Event event) {
        this.event = event;
    }

    @NotNull
    public Map<String, Object> getData() {
        return data;
    }

    public Object getCustomData(String key) {
        return data.get(key);
    }

    public void setCustomData(String key, Object value) {
        data.put(key, value);
    }

    public static class Builder {
        private final ZenExecutionContext ctx = new ZenExecutionContext();

        public Builder trigger(String trigger) { ctx.trigger = trigger; return this; }
        public Builder player(Player player) { ctx.player = player; return this; }
        public Builder target(LivingEntity target) { ctx.setTarget(target); return this; }
        public Builder targetEntity(Entity targetEntity) { ctx.setTargetEntity(targetEntity); return this; }
        public Builder block(Block block) { ctx.block = block; return this; }
        public Builder item(ItemStack item) { ctx.item = item; return this; }
        public Builder location(Location location) { ctx.location = location; return this; }
        public Builder level(int level) { ctx.level = level; return this; }
        public Builder damage(double damage) { ctx.damage = damage; return this; }
        public Builder event(Event event) { ctx.setEvent(event); return this; }
        public Builder data(String key, Object value) { ctx.data.put(key, value); return this; }

        public ZenExecutionContext build() {
            return ctx;
        }
    }
}

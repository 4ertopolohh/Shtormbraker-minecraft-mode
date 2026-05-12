package com.shtormbraker.shtormbraker.events;

import com.shtormbraker.shtormbraker.ShtormbrakerConfigValues;
import com.shtormbraker.shtormbraker.ShtormbrakerMod;
import com.shtormbraker.shtormbraker.util.ModUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = ShtormbrakerMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PlayerInventoryBuffHandler {
    private static final UUID HEALTH_MODIFIER_UUID = UUID.fromString("fde14e87-d241-439d-bd0b-fc6408de5ca3");
    private static final UUID SPEED_MODIFIER_UUID = UUID.fromString("d30f38a7-d3ef-4598-8f07-f608313ac2f5");
    private static final String HEALTH_MODIFIER_NAME = "shtormbraker_inventory_health_bonus";
    private static final String SPEED_MODIFIER_NAME = "shtormbraker_inventory_speed_bonus";

    private PlayerInventoryBuffHandler() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide || !(event.player instanceof ServerPlayer player)) {
            return;
        }

        if (ModUtil.hasShtormbrakerInInventory(player)) {
            applyBuffs(player);
        } else {
            clearBuffs(player);
        }
    }

    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (!ModUtil.hasShtormbrakerInInventory(player)) {
            return;
        }

        player.setDeltaMovement(player.getDeltaMovement().multiply(1.0D, ShtormbrakerConfigValues.PLAYER_JUMP_MULTIPLIER, 1.0D));
        player.hurtMarked = true;
    }

    public static void clearBuffs(ServerPlayer player) {
        removeModifier(player, Attributes.MAX_HEALTH, HEALTH_MODIFIER_UUID);
        removeModifier(player, Attributes.MOVEMENT_SPEED, SPEED_MODIFIER_UUID);

        float maxHealth = player.getMaxHealth();
        if (player.getHealth() > maxHealth) {
            player.setHealth(maxHealth);
        }
    }

    private static void applyBuffs(ServerPlayer player) {
        boolean hadHealthModifier = hasModifier(player, Attributes.MAX_HEALTH, HEALTH_MODIFIER_UUID);
        applyModifier(player, Attributes.MAX_HEALTH, HEALTH_MODIFIER_UUID, HEALTH_MODIFIER_NAME,
                ShtormbrakerConfigValues.PLAYER_MAX_HEALTH_MULTIPLIER - 1.0D);
        applyModifier(player, Attributes.MOVEMENT_SPEED, SPEED_MODIFIER_UUID, SPEED_MODIFIER_NAME,
                ShtormbrakerConfigValues.PLAYER_MOVE_SPEED_MULTIPLIER - 1.0D);

        if (!hadHealthModifier) {
            player.setHealth(player.getMaxHealth());
        }

        refreshRegeneration(player);
    }

    private static void refreshRegeneration(ServerPlayer player) {
        MobEffectInstance active = player.getEffect(MobEffects.REGENERATION);
        int duration = ShtormbrakerConfigValues.PLAYER_REGEN_EFFECT_DURATION_TICKS;
        int amplifier = Math.max(
                ShtormbrakerConfigValues.PLAYER_REGEN_EFFECT_AMPLIFIER,
                (int) Math.floor(ShtormbrakerConfigValues.PLAYER_REGEN_MULTIPLIER) - 1
        );
        if (active != null && active.getAmplifier() == amplifier && active.getDuration() > duration / 2) {
            return;
        }

        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, duration, amplifier, true, false, false));
    }

    private static void applyModifier(ServerPlayer player, Attribute attribute,
                                      UUID uuid, String name, double amount) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) {
            return;
        }

        AttributeModifier current = instance.getModifier(uuid);
        if (current != null && current.getAmount() == amount && current.getOperation() == AttributeModifier.Operation.MULTIPLY_TOTAL) {
            return;
        }

        if (current != null) {
            instance.removeModifier(uuid);
        }
        instance.addPermanentModifier(new AttributeModifier(uuid, name, amount, AttributeModifier.Operation.MULTIPLY_TOTAL));
    }

    private static boolean hasModifier(ServerPlayer player, Attribute attribute, UUID uuid) {
        AttributeInstance instance = player.getAttribute(attribute);
        return instance != null && instance.getModifier(uuid) != null;
    }

    private static void removeModifier(ServerPlayer player, Attribute attribute, UUID uuid) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance != null && instance.getModifier(uuid) != null) {
            instance.removeModifier(uuid);
        }
    }
}

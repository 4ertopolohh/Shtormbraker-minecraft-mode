package com.shtormbraker.shtormbraker.util;

import com.shtormbraker.shtormbraker.registry.ModItems;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class ModUtil {
    private ModUtil() {
    }

    public static boolean isHoldingShtormbraker(Player player) {
        return isShtormbraker(player.getMainHandItem()) || isShtormbraker(player.getOffhandItem());
    }

    public static boolean hasShtormbrakerInInventory(Player player) {
        if (isHoldingShtormbraker(player)) {
            return true;
        }

        for (ItemStack stack : player.getInventory().items) {
            if (isShtormbraker(stack)) {
                return true;
            }
        }

        for (ItemStack stack : player.getInventory().offhand) {
            if (isShtormbraker(stack)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isShtormbraker(ItemStack stack) {
        return !stack.isEmpty() && stack.is(ModItems.SHTORMBRAKER.get());
    }

    public static boolean removeOneShtormbraker(Player player) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (isShtormbraker(stack)) {
                stack.shrink(1);
                return true;
            }
        }

        for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack stack = player.getInventory().items.get(i);
            if (isShtormbraker(stack)) {
                stack.shrink(1);
                if (stack.isEmpty()) {
                    player.getInventory().items.set(i, ItemStack.EMPTY);
                }
                return true;
            }
        }

        return false;
    }

    public static void giveOrDropShtormbraker(Player player) {
        ItemStack stack = new ItemStack(ModItems.SHTORMBRAKER.get());
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }
}

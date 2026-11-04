package com.Cna4ik.maceswap.client;

import com.Cna4ik.maceswap.config.MaceSwapConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.lang.reflect.Method;

public class ItemHelper {

    public static boolean isWeaponOrTool(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        return stack.is(ItemTags.SWORDS)
                || stack.is(ItemTags.AXES)
                || stack.is(ItemTags.PICKAXES)
                || stack.is(ItemTags.SHOVELS)
                || stack.is(ItemTags.HOES)
                || stack.is(Items.TRIDENT)
                || stack.is(Items.MACE)
                || stack.is(Items.BOW)
                || stack.is(Items.CROSSBOW)
                || stack.is(Items.SHIELD);
    }

    public static boolean isMeleeWeaponOrEmptyOrWindCharge(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return true;
        }
        return stack.is(ItemTags.SWORDS)
                || stack.is(ItemTags.AXES)
                || stack.is(Items.MACE)
                || stack.is(Items.WIND_CHARGE);
    }

    /**
     * Moves the first matching stack from the main inventory into the hotbar by invoking the
     * game mode's (int, int, int, enum[, Player]) click method reflectively with the SWAP click type.
     * Reflection is kept from the original so the method is resolved at runtime rather than by name.
     */
    public static void refillItem(Minecraft client, Item item) {
        if (client.player == null || client.gameMode == null) {
            return;
        }
        Inventory inv = client.player.getInventory();
        if (findInHotbar(inv, item) != -1) {
            return;
        }
        for (int i = 9; i < 36; i++) {
            if (!inv.getItem(i).is(item)) {
                continue;
            }
            int hotbarSlot = 8;
            for (int j = 0; j < 9; j++) {
                if (inv.getItem(j).isEmpty()) {
                    hotbarSlot = j;
                    break;
                }
            }
            try {
                for (Method m : client.gameMode.getClass().getMethods()) {
                    Class<?>[] params = m.getParameterTypes();
                    if (!((params.length == 5 && Player.class.isAssignableFrom(params[4])) || params.length == 4)) {
                        continue;
                    }
                    if (params[0] != Integer.TYPE || params[1] != Integer.TYPE || params[2] != Integer.TYPE
                            || !params[3].isEnum()) {
                        continue;
                    }
                    Object[] enumConstants = params[3].getEnumConstants();
                    Object swapType = null;
                    if (enumConstants == null || enumConstants.length <= 2) {
                        continue;
                    }
                    for (Object constant : enumConstants) {
                        if (constant.toString().equals("SWAP")) {
                            swapType = constant;
                            break;
                        }
                    }
                    if (swapType == null) {
                        swapType = enumConstants[2];
                    }
                    if (params.length == 5) {
                        m.invoke(client.gameMode, Integer.valueOf(0), Integer.valueOf(i),
                                Integer.valueOf(hotbarSlot), swapType, client.player);
                    } else {
                        m.invoke(client.gameMode, Integer.valueOf(0), Integer.valueOf(i),
                                Integer.valueOf(hotbarSlot), swapType);
                    }
                    return;
                }
            } catch (Exception ignored) {
            }
            return;
        }
    }

    public static int findInHotbar(Inventory inv, Item item) {
        for (int i = 0; i < 9; i++) {
            if (inv.getItem(i).is(item)) {
                return i;
            }
        }
        return -1;
    }

    public static int findChestplateInHotbar(Inventory inv) {
        for (int i = 0; i < 9; i++) {
            Item item = inv.getItem(i).getItem();
            if (item == Items.NETHERITE_CHESTPLATE
                    || item == Items.DIAMOND_CHESTPLATE
                    || item == Items.IRON_CHESTPLATE
                    || item == Items.GOLDEN_CHESTPLATE
                    || item == Items.CHAINMAIL_CHESTPLATE
                    || item == Items.LEATHER_CHESTPLATE) {
                return i;
            }
        }
        return -1;
    }

    public static int findBestMace(Inventory inv, float fallDistance) {
        MaceSwapConfig config = AutoConfig.getConfigHolder(MaceSwapConfig.class).getConfig();
        MaceSwapConfig.MaceEnchantPreference pref = config.maceEnchantPreference;
        if (pref == MaceSwapConfig.MaceEnchantPreference.SMART) {
            pref = fallDistance >= (float) config.smartDensityFallThreshold
                    ? MaceSwapConfig.MaceEnchantPreference.DENSITY
                    : MaceSwapConfig.MaceEnchantPreference.BREACH;
        }
        int defaultMaceSlot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.is(Items.MACE)) {
                continue;
            }
            if (defaultMaceSlot == -1) {
                defaultMaceSlot = i;
            }
            String components = stack.getComponents().toString().toLowerCase();
            if (pref == MaceSwapConfig.MaceEnchantPreference.DENSITY && components.contains("density")) {
                return i;
            }
            if (pref == MaceSwapConfig.MaceEnchantPreference.BREACH && components.contains("breach")) {
                return i;
            }
        }
        return defaultMaceSlot;
    }
}

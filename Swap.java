package com.Cna4ik.maceswap.client;

import com.Cna4ik.maceswap.config.MaceSwapConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;

import java.lang.reflect.Field;

public class Swap {
    public static boolean isPerformingInternalAttack = false;
    public static int swapBackTimer = 0;
    public static int queuedOriginalSlot = -1;

    public static int getSlot(Inventory inv) {
        try {
            Field f = Inventory.class.getDeclaredField("selected");
            f.setAccessible(true);
            return f.getInt(inv);
        } catch (Exception e) {
            return 0;
        }
    }

    public static void setSlot(Inventory inv, int slot) {
        try {
            Field f = Inventory.class.getDeclaredField("selected");
            f.setAccessible(true);
            f.setInt(inv, slot);
        } catch (Exception e) {
        }
    }

    public static void tick(Minecraft client) {
        if (swapBackTimer > 0) {
            swapBackTimer--;
            if (swapBackTimer == 0 && queuedOriginalSlot != -1) {
                if (client.gameMode != null && client.player != null) {
                    setSlot(client.player.getInventory(), queuedOriginalSlot);
                    if (client.getConnection() != null) {
                        client.getConnection().send(new ServerboundSetCarriedItemPacket(queuedOriginalSlot));
                    }
                    queuedOriginalSlot = -1;
                }
            }
        }
    }

    public static boolean handleGhostSwapAttack(Player player, Entity target) {
        if (isPerformingInternalAttack) {
            return false;
        }
        if (ElytraMace.state > 0 || TridentMace.state > 0) {
            return false;
        }
        if (player.getMainHandItem().is(Items.MACE)) {
            return false;
        }

        MaceSwapConfig config = AutoConfig.getConfigHolder(MaceSwapConfig.class).getConfig();

        boolean isHoldingSpear = BuiltInRegistries.ITEM.getKey(player.getMainHandItem().getItem())
                .getPath().contains("spear");
        if (isHoldingSpear) {
            if (MaceSwapClient.isAutoMaceEnabled && SpearSwap.pendingSpearMaceDelay <= 0) {
                SpearSwap.pendingSpearTarget = target;
                SpearSwap.pendingSpearMaceDelay = 2;
            }
            return false;
        }

        if (!MaceSwapClient.isAutoMaceEnabled) {
            return false;
        }
        if (config.swapItemsMode == MaceSwapConfig.SwapItemsMode.ONLY_WEAPONS
                && !ItemHelper.isWeaponOrTool(player.getMainHandItem())) {
            return false;
        }
        if (AxeSwap.handleAxeAttack(player, target, config)) {
            return false;
        }

        Inventory inv = player.getInventory();
        Minecraft client = Minecraft.getInstance();
        int maceSlot = ItemHelper.findBestMace(inv, (float) player.fallDistance);
        if (maceSlot != -1 && swapBackTimer == 0 && client.gameMode != null) {
            queuedOriginalSlot = getSlot(inv);
            setSlot(inv, maceSlot);
            isPerformingInternalAttack = true;
            try {
                client.gameMode.attack(player, target);
            } finally {
                isPerformingInternalAttack = false;
            }
            player.swing(InteractionHand.MAIN_HAND);
            swapBackTimer = 1;
            return true;
        }
        return false;
    }
}

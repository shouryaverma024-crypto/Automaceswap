package com.Cna4ik.maceswap.client;

import com.Cna4ik.maceswap.config.MaceSwapConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;

import java.lang.reflect.Field;

public class AxeSwap {
    public static int axeSwapState = 0;
    public static int axeOriginalSlot = -1;
    public static int axeMaceSlot = -1;
    public static Entity queuedAxeTarget = null;

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
        if (axeSwapState == 2) {
            if (axeOriginalSlot != -1 && client.gameMode != null && client.player != null) {
                setSlot(client.player.getInventory(), axeOriginalSlot);
                if (client.getConnection() != null) {
                    client.getConnection().send(new ServerboundSetCarriedItemPacket(axeOriginalSlot));
                }
            }
            axeSwapState = 0;
            axeOriginalSlot = -1;
            axeMaceSlot = -1;
            queuedAxeTarget = null;
        } else if (axeSwapState == 1) {
            if (queuedAxeTarget != null && queuedAxeTarget.isAlive()
                    && client.gameMode != null && client.player != null) {
                int maceSlot = ItemHelper.findBestMace(client.player.getInventory(),
                        (float) client.player.fallDistance);
                if (maceSlot != -1) {
                    if (axeOriginalSlot == -1) {
                        axeOriginalSlot = getSlot(client.player.getInventory());
                    }
                    axeMaceSlot = maceSlot;
                    axeSwapState = 2;
                    setSlot(client.player.getInventory(), axeMaceSlot);
                    if (client.getConnection() != null) {
                        client.getConnection().send(new ServerboundSetCarriedItemPacket(axeMaceSlot));
                    }
                    Swap.isPerformingInternalAttack = true;
                    try {
                        client.gameMode.attack(client.player, queuedAxeTarget);
                    } finally {
                        Swap.isPerformingInternalAttack = false;
                    }
                    client.player.swing(InteractionHand.MAIN_HAND);
                } else {
                    axeSwapState = 0;
                    queuedAxeTarget = null;
                }
            } else {
                axeSwapState = 0;
                queuedAxeTarget = null;
            }
        }
    }

    public static boolean handleAxeAttack(Player player, Entity target, MaceSwapConfig config) {
        if (config.axeSwapEnabled && player.getMainHandItem().getItem() instanceof AxeItem) {
            boolean isBlocking = false;
            if (target instanceof LivingEntity livingTarget) {
                isBlocking = livingTarget.isBlocking();
            }
            if (isBlocking) {
                if (axeSwapState == 0) {
                    queuedAxeTarget = target;
                    axeSwapState = 1;
                }
                return true;
            }
        }
        return false;
    }
}

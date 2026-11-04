package com.Cna4ik.maceswap.client;

import com.Cna4ik.maceswap.config.MaceSwapConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import java.lang.reflect.Field;

public class SpearSwap {
    public static int pendingSpearMaceDelay = -1;
    public static Entity pendingSpearTarget = null;
    public static int spearSwapTimer = 0;
    public static int previousSlot = -1;
    private static boolean wasUsePressed = false;
    private static final int MACE_DELAY_TICKS = 2;

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

    public static void queueMaceAttack(Entity target) {
        pendingSpearMaceDelay = MACE_DELAY_TICKS;
        pendingSpearTarget = target;
    }

    public static void tick(Minecraft client, MaceSwapConfig config, boolean attackJustPressed, boolean inMenu) {
        if (client.player == null || client.level == null) {
            return;
        }

        if (spearSwapTimer > 0) {
            spearSwapTimer--;
            if (spearSwapTimer == 0 && previousSlot != -1) {
                setSlot(client.player.getInventory(), previousSlot);
                if (client.getConnection() != null) {
                    client.getConnection().send(new ServerboundSetCarriedItemPacket(previousSlot));
                }
                previousSlot = -1;
            }
        }

        if (pendingSpearMaceDelay > 0) {
            pendingSpearMaceDelay--;
        } else if (pendingSpearMaceDelay == 0 && pendingSpearTarget != null) {
            performSpearMaceAttack(client, pendingSpearTarget);
            pendingSpearMaceDelay = -1;
            pendingSpearTarget = null;
        }

        if (MaceSwapClient.isAutoMaceEnabled) {
            boolean isUsePressed = client.options.keyUse.isDown();
            boolean useReleased = !isUsePressed && wasUsePressed;
            String itemName = BuiltInRegistries.ITEM.getKey(client.player.getMainHandItem().getItem()).getPath();
            if (itemName.contains("spear")
                    && client.hitResult != null
                    && client.hitResult.getType() == HitResult.Type.ENTITY) {
                Entity target = ((EntityHitResult) client.hitResult).getEntity();
                if (!client.player.onGround() && client.player.fallDistance >= 3.0) {
                    if (isUsePressed && target instanceof LivingEntity livingTarget) {
                        if (livingTarget.hurtTime == 10) {
                            client.player.stopUsingItem();
                            if (client.gameMode != null) {
                                client.gameMode.releaseUsingItem(client.player);
                            }
                            while (client.options.keyUse.consumeClick()) {
                            }
                            queueMaceAttack(target);
                        }
                    } else if (useReleased) {
                        queueMaceAttack(target);
                    }
                }
            }
            wasUsePressed = isUsePressed;
        } else {
            wasUsePressed = client.options.keyUse.isDown();
        }

        if (!inMenu && attackJustPressed && config.spearSwapEnabled) {
            boolean isHoldingSpear = BuiltInRegistries.ITEM.getKey(client.player.getMainHandItem().getItem())
                    .getPath().contains("spear");
            if (!isHoldingSpear && !ItemHelper.isWeaponOrTool(client.player.getMainHandItem())) {
                boolean allowSpear = !(config.spearSwapIgnoreBlocks
                        && client.hitResult != null
                        && client.hitResult.getType() == HitResult.Type.BLOCK);
                if (allowSpear) {
                    int spearSlot = -1;
                    for (int i = 0; i < 9; i++) {
                        if (BuiltInRegistries.ITEM.getKey(client.player.getInventory().getItem(i).getItem())
                                .getPath().contains("spear")) {
                            spearSlot = i;
                            break;
                        }
                    }
                    if (spearSlot != -1 && client.getConnection() != null) {
                        previousSlot = getSlot(client.player.getInventory());
                        setSlot(client.player.getInventory(), spearSlot);
                        client.getConnection().send(new ServerboundSetCarriedItemPacket(spearSlot));
                        client.player.swing(InteractionHand.MAIN_HAND);
                        spearSwapTimer = 1;
                    }
                }
            }
        }
    }

    private static void performSpearMaceAttack(Minecraft client, Entity target) {
        Player player = client.player;
        if (player == null || client.gameMode == null) {
            return;
        }
        Inventory inv = player.getInventory();
        int maceSlot = -1;
        for (int i = 0; i < 9; i++) {
            if (inv.getItem(i).is(Items.MACE)) {
                maceSlot = i;
                break;
            }
        }
        if (maceSlot == -1) {
            return;
        }
        previousSlot = getSlot(inv);
        setSlot(inv, maceSlot);
        if (client.getConnection() != null) {
            client.getConnection().send(new ServerboundSetCarriedItemPacket(maceSlot));
        }
        Swap.isPerformingInternalAttack = true;
        try {
            client.gameMode.attack(player, target);
        } finally {
            Swap.isPerformingInternalAttack = false;
        }
        player.swing(InteractionHand.MAIN_HAND);
        spearSwapTimer = 1;
    }
}

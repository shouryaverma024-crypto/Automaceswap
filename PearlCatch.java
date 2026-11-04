package com.Cna4ik.maceswap.client;

import com.Cna4ik.maceswap.config.MaceSwapConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Items;

import java.lang.reflect.Field;

public class PearlCatch {
    public static int state = 0;
    public static int originalSlot = -1;
    public static float originalPitch = 0f;
    public static float originalYaw = 0f;

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

    public static void tick(Minecraft client, MaceSwapConfig config, boolean useJustPressed, boolean isHoldingPearl) {
        if (client.player == null) {
            return;
        }
        if (!config.pearlCatchingEnabled) {
            state = 0;
            return;
        }

        if (state > 0) {
            client.options.keyUse.setDown(false);
            while (client.options.keyUse.consumeClick()) {
            }
            client.player.setXRot(-90.0f);
        }

        if (state == 0) {
            if (MaceSwapClient.isAutoMaceEnabled
                    && config.pearlCatchingEnabled
                    && useJustPressed
                    && isHoldingPearl
                    && Wind.state == 0
                    && TridentMace.state == 0
                    && client.player.getXRot() <= (float) (-config.pearlCatchAngle)) {
                originalPitch = client.player.getXRot();
                originalYaw = client.player.getYRot();
                originalSlot = getSlot(client.player.getInventory());
                client.player.setXRot(-90.0f);
                if (client.getConnection() != null) {
                    client.getConnection().send(new ServerboundMovePlayerPacket.Rot(
                            originalYaw, -90.0f, client.player.onGround(), client.player.horizontalCollision));
                }
                InteractionHand hand = client.player.getMainHandItem().is(Items.ENDER_PEARL)
                        ? InteractionHand.MAIN_HAND
                        : InteractionHand.OFF_HAND;
                if (client.gameMode != null) {
                    client.gameMode.useItem(client.player, hand);
                    client.player.swing(hand);
                }
                state = 1;
            }
        } else if (state == 1) {
            int wSlot = ItemHelper.findInHotbar(client.player.getInventory(), Items.WIND_CHARGE);
            if (wSlot != -1 && client.getConnection() != null) {
                setSlot(client.player.getInventory(), wSlot);
                client.getConnection().send(new ServerboundSetCarriedItemPacket(wSlot));
            }
            state = 2;
        } else if (state == 2) {
            int wSlot = ItemHelper.findInHotbar(client.player.getInventory(), Items.WIND_CHARGE);
            if (wSlot != -1 && getSlot(client.player.getInventory()) == wSlot && client.gameMode != null) {
                if (client.getConnection() != null) {
                    client.getConnection().send(new ServerboundMovePlayerPacket.Rot(
                            originalYaw, -90.0f, client.player.onGround(), client.player.horizontalCollision));
                }
                client.gameMode.useItem(client.player, InteractionHand.MAIN_HAND);
                client.player.swing(InteractionHand.MAIN_HAND);
            }
            state = 3;
        } else if (state >= 3 && state < 6) {
            state++;
        } else if (state == 6) {
            setSlot(client.player.getInventory(), originalSlot);
            client.player.setXRot(originalPitch);
            if (client.getConnection() != null) {
                client.getConnection().send(new ServerboundSetCarriedItemPacket(originalSlot));
                client.getConnection().send(new ServerboundMovePlayerPacket.Rot(
                        originalYaw, originalPitch, client.player.onGround(), client.player.horizontalCollision));
            }
            state = 0;
        }
    }
}

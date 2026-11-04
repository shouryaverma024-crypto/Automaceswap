package com.Cna4ik.maceswap.client;

import com.Cna4ik.maceswap.config.MaceSwapConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Items;

import java.lang.reflect.Field;

public class Wind {
    public static int state = 0;
    public static int originalSlot = -1;
    public static float originalPitch = 0f;

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

    public static void tickSafe(Minecraft client) {
        if (client.player == null) {
            return;
        }
        if (state == 1) {
            if (client.player.getXRot() >= 88.5f) {
                state = 2;
            }
        } else if (state == 2) {
            int currentWSlot = ItemHelper.findInHotbar(client.player.getInventory(), Items.WIND_CHARGE);
            if (currentWSlot != -1 && client.getConnection() != null) {
                setSlot(client.player.getInventory(), currentWSlot);
                client.getConnection().send(new ServerboundSetCarriedItemPacket(currentWSlot));
            }
            state = 3;
        } else if (state == 3) {
            int currentWSlot = ItemHelper.findInHotbar(client.player.getInventory(), Items.WIND_CHARGE);
            if (currentWSlot != -1
                    && getSlot(client.player.getInventory()) == currentWSlot
                    && client.gameMode != null) {
                client.gameMode.useItem(client.player, InteractionHand.MAIN_HAND);
                client.player.swing(InteractionHand.MAIN_HAND);
            }
            setSlot(client.player.getInventory(), originalSlot);
            if (client.getConnection() != null) {
                client.getConnection().send(new ServerboundSetCarriedItemPacket(originalSlot));
            }
            state = 4;
        } else if (state == 4) {
            float pitchDiff = Math.abs(Mth.wrapDegrees(originalPitch - client.player.getXRot()));
            if (pitchDiff <= 2.0f) {
                client.player.setXRot(originalPitch);
                state = 0;
            }
        }
    }

    public static void tickAuto(Minecraft client, MaceSwapConfig config, boolean useJustPressed) {
        // 26.2: Minecraft#screen moved to Gui#screen()
        if (client.player == null || client.gui.screen() != null) {
            return;
        }

        boolean isWindAllowed = false;
        if (config.autoWindChargeMode == MaceSwapConfig.AutoWindChargeMode.ALWAYS) {
            isWindAllowed = MaceSwapClient.isAutoMaceEnabled;
        } else if (config.autoWindChargeMode == MaceSwapConfig.AutoWindChargeMode.ON_LOCK) {
            if (TargetAssist.isActive) {
                isWindAllowed = true;
            }
        }

        if (config.autoWindChargeEnabled && isWindAllowed && useJustPressed
                && state == 0 && PearlCatch.state == 0) {
            int wSlot = ItemHelper.findInHotbar(client.player.getInventory(), Items.WIND_CHARGE);
            if (wSlot != -1 && client.gameMode != null && client.getConnection() != null) {
                boolean holdingValidItem = ItemHelper.isMeleeWeaponOrEmptyOrWindCharge(client.player.getMainHandItem());
                if (holdingValidItem) {
                    client.options.keyUse.setDown(false);
                    if (config.safeWindChargeEnabled) {
                        state = 1;
                        originalSlot = getSlot(client.player.getInventory());
                        originalPitch = client.player.getXRot();
                    } else {
                        int oldSlot = getSlot(client.player.getInventory());
                        float oldPitch = client.player.getXRot();
                        setSlot(client.player.getInventory(), wSlot);
                        client.getConnection().send(new ServerboundSetCarriedItemPacket(wSlot));
                        client.player.setXRot(90.0f);
                        client.getConnection().send(new ServerboundMovePlayerPacket.Rot(
                                client.player.getYRot(), 90.0f,
                                client.player.onGround(), client.player.horizontalCollision));
                        client.gameMode.useItem(client.player, InteractionHand.MAIN_HAND);
                        client.player.swing(InteractionHand.MAIN_HAND);
                        client.player.setXRot(oldPitch);
                        client.getConnection().send(new ServerboundMovePlayerPacket.Rot(
                                client.player.getYRot(), oldPitch,
                                client.player.onGround(), client.player.horizontalCollision));
                        setSlot(client.player.getInventory(), oldSlot);
                        client.getConnection().send(new ServerboundSetCarriedItemPacket(oldSlot));
                    }
                }
            }
        }
    }
}

package com.Cna4ik.maceswap.client;

import com.Cna4ik.maceswap.config.MaceSwapConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Items;

import java.lang.reflect.Field;

public class TridentMace {
    public static int state = 0;
    public static int delayTimer = 0;

    public static void setSlot(Inventory inv, int slot) {
        try {
            Field f = Inventory.class.getDeclaredField("selected");
            f.setAccessible(true);
            f.setInt(inv, slot);
        } catch (Exception e) {
        }
    }

    public static void finish(Minecraft client) {
        state = 0;
        client.options.keyUse.setDown(false);
        if (client.gameMode != null && client.player != null) {
            client.gameMode.releaseUsingItem(client.player);
        }
    }

    private static void aimAtTarget(Minecraft client, Entity target) {
        if (target == null || !target.isAlive() || target.isRemoved() || client.player == null) {
            return;
        }
        double dx = target.getX() - client.player.getX();
        double dy = target.getEyeY() - client.player.getEyeY();
        double dz = target.getZ() - client.player.getZ();
        double diffXZ = Math.sqrt(dx * dx + dz * dz);
        float targetYaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
        float targetPitch = (float) (-Math.toDegrees(Math.atan2(dy, diffXZ)));
        client.player.setYRot(targetYaw);
        client.player.setXRot(targetPitch);
        if (client.getConnection() != null) {
            client.getConnection().send(new ServerboundMovePlayerPacket.Rot(
                    targetYaw, targetPitch, client.player.onGround(), client.player.horizontalCollision));
        }
    }

    public static void tick(Minecraft client, MaceSwapConfig config) {
        if (client.player == null || client.gameMode == null) {
            return;
        }

        if (config.tridentMaceComboEnabled
                && TargetAssist.isActive
                && client.player.getMainHandItem().is(Items.TRIDENT)
                && state == 0) {
            state = 1;
        }

        if (state > 0 && client.getConnection() != null) {
            if (TargetAssist.lockedTarget == null
                    || !TargetAssist.lockedTarget.isAlive()
                    || TargetAssist.lockedTarget.isRemoved()) {
                finish(client);
            } else if (state == 1) {
                client.player.setXRot(-90.0f);
                client.options.keyUse.setDown(true);
                delayTimer = 15;
                state = 2;
            } else if (state == 2) {
                client.player.setXRot(-90.0f);
                if (delayTimer > 0) {
                    delayTimer--;
                } else {
                    client.options.keyUse.setDown(false);
                    client.gameMode.releaseUsingItem(client.player);
                    delayTimer = 10;
                    state = 3;
                }
            } else if (state == 3) {
                aimAtTarget(client, TargetAssist.lockedTarget);
                if (delayTimer > 0) {
                    delayTimer--;
                } else if (client.player.getDeltaMovement().y <= 0.1 && !client.player.onGround()) {
                    int maceSlot = ItemHelper.findInHotbar(client.player.getInventory(), Items.MACE);
                    if (maceSlot != -1) {
                        setSlot(client.player.getInventory(), maceSlot);
                        client.getConnection().send(new ServerboundSetCarriedItemPacket(maceSlot));
                    }
                    state = 4;
                } else if (client.player.onGround()) {
                    state = 0;
                }
            } else if (state == 4) {
                aimAtTarget(client, TargetAssist.lockedTarget);
                if (client.player.onGround()) {
                    state = 0;
                } else if (config.tridentAutoHitEnabled
                        && (double) client.player.distanceTo(TargetAssist.lockedTarget) <= 3.5) {
                    client.gameMode.attack(client.player, TargetAssist.lockedTarget);
                    client.player.swing(InteractionHand.MAIN_HAND);
                    if (config.tridentWindBurstChain) {
                        delayTimer = 5;
                        state = 5;
                    } else {
                        state = 0;
                    }
                }
            } else if (state == 5) {
                if (delayTimer > 0) {
                    delayTimer--;
                } else if (client.player.getDeltaMovement().y > 0.3) {
                    int tridentSlot = ItemHelper.findInHotbar(client.player.getInventory(), Items.TRIDENT);
                    if (tridentSlot != -1) {
                        setSlot(client.player.getInventory(), tridentSlot);
                        client.getConnection().send(new ServerboundSetCarriedItemPacket(tridentSlot));
                        state = 1;
                    } else {
                        state = 0;
                    }
                } else if (client.player.onGround()) {
                    state = 0;
                }
            }
        }
    }
}

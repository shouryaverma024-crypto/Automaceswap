package com.Cna4ik.maceswap.client;

import com.Cna4ik.maceswap.config.MaceSwapConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Field;

public class ElytraMace {
    public static int state = 0;
    public static int originalSlot = -1;
    public static int delayTimer = 0;
    public static Entity target = null;

    public static void setSlot(Inventory inv, int slot) {
        try {
            Field f = Inventory.class.getDeclaredField("selected");
            f.setAccessible(true);
            f.setInt(inv, slot);
        } catch (Exception e) {
        }
    }

    public static void finish(Minecraft client) {
        if (client != null && client.player != null && originalSlot != -1) {
            setSlot(client.player.getInventory(), originalSlot);
            if (client.getConnection() != null) {
                client.getConnection().send(new ServerboundSetCarriedItemPacket(originalSlot));
            }
        }
        state = 0;
        target = null;
        originalSlot = -1;
    }

    private static int findSafeFirework(Inventory inv) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.is(Items.FIREWORK_ROCKET)) {
                Fireworks fw = stack.get(DataComponents.FIREWORKS);
                if (fw == null || fw.explosions().isEmpty()) {
                    return i;
                }
            }
        }
        return ItemHelper.findInHotbar(inv, Items.FIREWORK_ROCKET);
    }

    public static void tick(Minecraft client, MaceSwapConfig config, boolean isOnGround) {
        if (state == 0) {
            return;
        }
        if (client.gameMode == null || client.player == null || client.level == null) {
            finish(client);
            return;
        }
        if (!TargetAssist.isActive || !MaceSwapClient.isAutoMaceEnabled) {
            finish(client);
            return;
        }
        if (target != null && (!target.isAlive() || target.isRemoved())) {
            finish(client);
            return;
        }

        Inventory inv = client.player.getInventory();

        if (isOnGround && state > 3) {
            if (!(state == 7 && delayTimer > 0)) {
                if (state == 6) {
                    finish(client);
                    return;
                }
                if (state != 5 && state != 11 && state != 12) {
                    finish(client);
                    return;
                }
            }
        }

        if (state == 1) {
            if (!client.player.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA)) {
                int elytraSlot = ItemHelper.findInHotbar(inv, Items.ELYTRA);
                if (elytraSlot != -1) {
                    setSlot(inv, elytraSlot);
                    if (client.getConnection() != null) {
                        client.getConnection().send(new ServerboundSetCarriedItemPacket(elytraSlot));
                    }
                    client.gameMode.useItem(client.player, InteractionHand.MAIN_HAND);
                } else {
                    finish(client);
                    return;
                }
            }
            if (client.player.onGround()) {
                client.player.jumpFromGround();
            }
            delayTimer = 3;
            state = 2;
        } else if (state == 2) {
            if (delayTimer > 0) {
                if (client.getConnection() != null) {
                    client.getConnection().send(new ServerboundPlayerCommandPacket(
                            client.player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
                }
                delayTimer--;
            } else {
                state = 3;
            }
        } else if (state == 3) {
            int fwSlot = findSafeFirework(inv);
            if (fwSlot != -1) {
                setSlot(inv, fwSlot);
                if (client.getConnection() != null) {
                    client.getConnection().send(new ServerboundSetCarriedItemPacket(fwSlot));
                }
                client.player.setXRot(-90.0f);
                if (client.getConnection() != null) {
                    client.getConnection().send(new ServerboundMovePlayerPacket.Rot(
                            client.player.getYRot(), -90.0f,
                            client.player.onGround(), client.player.horizontalCollision));
                }
                client.gameMode.useItem(client.player, InteractionHand.MAIN_HAND);
                client.player.swing(InteractionHand.MAIN_HAND);
                delayTimer = 15;
                state = 4;
            } else {
                finish(client);
            }
        } else if (state == 4) {
            if (delayTimer > 0) {
                client.player.setXRot(-90.0f);
                if (client.getConnection() != null) {
                    client.getConnection().send(new ServerboundMovePlayerPacket.Rot(
                            client.player.getYRot(), -90.0f,
                            client.player.onGround(), client.player.horizontalCollision));
                }
                delayTimer--;
            } else {
                state = 5;
            }
        } else if (state == 5) {
            boolean shouldEquip = false;
            if (client.player.distanceToSqr(target) <= 36.0) {
                shouldEquip = true;
            }
            Vec3 pos = new Vec3(client.player.getX(), client.player.getY(), client.player.getZ());
            Vec3 vel = client.player.getDeltaMovement();
            Vec3 down = pos.add(vel.x * 5.0, -5.0, vel.z * 5.0);
            HitResult hit = client.level.clip(new ClipContext(
                    pos, down, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, client.player));
            if (hit.getType() == HitResult.Type.BLOCK && pos.y - hit.getLocation().y <= 5.0) {
                shouldEquip = true;
            }
            if (shouldEquip) {
                int chestSlot = ItemHelper.findChestplateInHotbar(inv);
                if (chestSlot != -1) {
                    setSlot(inv, chestSlot);
                    if (client.getConnection() != null) {
                        client.getConnection().send(new ServerboundSetCarriedItemPacket(chestSlot));
                    }
                    client.gameMode.useItem(client.player, InteractionHand.MAIN_HAND);
                }
                state = 11;
            }
        } else if (state == 11) {
            setSlot(inv, originalSlot);
            if (client.getConnection() != null) {
                client.getConnection().send(new ServerboundSetCarriedItemPacket(originalSlot));
            }
            if (config.elytraAutoHitEnabled) {
                state = 6;
            } else {
                finish(client);
            }
        } else if (state == 6) {
            if (client.player.distanceToSqr(target) <= 25.0
                    || client.player.getBoundingBox().inflate(2.0).intersects(target.getBoundingBox())) {
                if (!client.player.getMainHandItem().is(Items.MACE)) {
                    int maceSlot = ItemHelper.findBestMace(client.player.getInventory(),
                            (float) client.player.fallDistance);
                    if (maceSlot != -1) {
                        setSlot(client.player.getInventory(), maceSlot);
                        if (client.getConnection() != null) {
                            client.getConnection().send(new ServerboundSetCarriedItemPacket(maceSlot));
                        }
                    }
                }
                Swap.isPerformingInternalAttack = true;
                try {
                    client.gameMode.attack(client.player, target);
                } finally {
                    Swap.isPerformingInternalAttack = false;
                }
                client.player.swing(InteractionHand.MAIN_HAND);
                if (config.elytraWindBurstChain) {
                    delayTimer = 5;
                    state = 7;
                } else {
                    finish(client);
                }
            } else if (client.player.onGround()) {
                finish(client);
            }
        } else if (state == 7) {
            if (delayTimer > 0) {
                delayTimer--;
            } else if (client.player.getDeltaMovement().y <= 0.05) {
                state = 8;
            }
        } else if (state == 8) {
            if (!client.player.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA)) {
                int elytraSlot = ItemHelper.findInHotbar(inv, Items.ELYTRA);
                if (elytraSlot != -1) {
                    setSlot(inv, elytraSlot);
                    if (client.getConnection() != null) {
                        client.getConnection().send(new ServerboundSetCarriedItemPacket(elytraSlot));
                    }
                    client.gameMode.useItem(client.player, InteractionHand.MAIN_HAND);
                } else {
                    finish(client);
                    return;
                }
            }
            delayTimer = 3;
            state = 9;
        } else if (state == 9) {
            if (delayTimer > 0) {
                if (client.getConnection() != null) {
                    client.getConnection().send(new ServerboundPlayerCommandPacket(
                            client.player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
                }
                delayTimer--;
            } else {
                state = 10;
            }
        } else if (state == 10) {
            int fwSlot = findSafeFirework(inv);
            if (fwSlot != -1) {
                setSlot(inv, fwSlot);
                if (client.getConnection() != null) {
                    client.getConnection().send(new ServerboundSetCarriedItemPacket(fwSlot));
                }
                double dx = target.getX() - client.player.getX();
                double dy = target.getEyeY() - client.player.getEyeY();
                double dz = target.getZ() - client.player.getZ();
                double diffXZ = Math.sqrt(dx * dx + dz * dz);
                float targetYaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
                float targetPitch = (float) (-Math.toDegrees(Math.atan2(dy, diffXZ)));
                if (client.getConnection() != null) {
                    client.getConnection().send(new ServerboundMovePlayerPacket.Rot(
                            targetYaw, targetPitch,
                            client.player.onGround(), client.player.horizontalCollision));
                }
                client.gameMode.useItem(client.player, InteractionHand.MAIN_HAND);
                delayTimer = 3;
                state = 12;
            } else {
                finish(client);
            }
        } else if (state == 12) {
            if (delayTimer > 0) {
                delayTimer--;
            } else {
                float originalPitch = client.player.getXRot();
                float originalYaw = client.player.getYRot();
                if (client.getConnection() != null) {
                    client.getConnection().send(new ServerboundMovePlayerPacket.Rot(
                            originalYaw, originalPitch,
                            client.player.onGround(), client.player.horizontalCollision));
                }
                setSlot(inv, originalSlot);
                if (client.getConnection() != null) {
                    client.getConnection().send(new ServerboundSetCarriedItemPacket(originalSlot));
                }
                state = 5;
            }
        }
    }
}

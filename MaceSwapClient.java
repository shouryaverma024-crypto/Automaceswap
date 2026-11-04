package com.Cna4ik.maceswap.client;

import com.Cna4ik.maceswap.config.MaceSwapConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Field;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class MaceSwapClient implements ClientModInitializer {
    private static boolean promptChecked = false;

    public static KeyMapping toggleKey;
    public static KeyMapping configWheelKey;
    public static KeyMapping elytraLaunchKey;

    public static boolean isAutoMaceEnabled = false;
    public static boolean wasWhiteModeBeforeAim = false;

    private boolean wasUsePressed = false;
    private boolean wasAttackPressed = false;

    @Override
    public void onInitializeClient() {
        AutoConfig.register(MaceSwapConfig.class, GsonConfigSerializer::new);

        // GLFW key codes: 82 = R, 90 = Z, 86 = V
        toggleKey = KeyMappingHelper.registerKeyMapping(
                new KeyMapping("key.maceswap.swap", 82, KeyMapping.Category.MISC));
        configWheelKey = KeyMappingHelper.registerKeyMapping(
                new KeyMapping("key.maceswap.wheel", 90, KeyMapping.Category.MISC));
        elytraLaunchKey = KeyMappingHelper.registerKeyMapping(
                new KeyMapping("key.maceswap.elytra", 86, KeyMapping.Category.MISC));

        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("maceswap", "hud"),
                MaceSwapClient::renderHud);

        ClientTickEvents.START_CLIENT_TICK.register(this::onStartTick);
    }

    public static int getSelectedSlot(Inventory inventory) {
        try {
            Field field = Inventory.class.getDeclaredField("selected");
            field.setAccessible(true);
            return field.getInt(inventory);
        } catch (Exception e) {
            return 0;
        }
    }

    public static Entity getLookingAtEntity(Minecraft client, MaceSwapConfig config) {
        if (client.player == null || client.level == null) {
            return null;
        }
        double maxDistance = (double) config.aimDistance;
        boolean playerOnly = config.playerOnlyAim;
        boolean smartLock = config.smartLockEnabled;
        Vec3 cameraPos = client.player.getEyePosition();
        Vec3 rotation = client.player.getViewVector(1.0f);
        Vec3 endPos = cameraPos.add(rotation.x * maxDistance, rotation.y * maxDistance, rotation.z * maxDistance);
        AABB box = client.player.getBoundingBox().expandTowards(rotation.scale(maxDistance)).inflate(1.0, 1.0, 1.0);

        Entity closestEntity = null;
        double bestScore = Double.MAX_VALUE;
        for (Entity entity : client.level.getEntities(client.player, box,
                e -> e instanceof LivingEntity && e.isAlive() && (!playerOnly || e instanceof Player))) {
            AABB entityBox = entity.getBoundingBox().inflate(0.1);
            Optional<Vec3> hit = entityBox.clip(cameraPos, endPos);
            if (smartLock) {
                Vec3 dir = new Vec3(entity.getX(), entity.getY(), entity.getZ()).subtract(cameraPos).normalize();
                if (rotation.dot(dir) > 0.8 && hasLineOfSight(client, entity)) {
                    LivingEntity le = (LivingEntity) entity;
                    double score = (double) (le.getHealth() + (float) le.getArmorValue());
                    if (score < bestScore) {
                        bestScore = score;
                        closestEntity = entity;
                    }
                }
            } else if (hit.isPresent() && hasLineOfSight(client, entity)) {
                double dist = cameraPos.distanceToSqr(hit.get());
                if (dist < bestScore) {
                    bestScore = dist;
                    closestEntity = entity;
                }
            }
        }
        return closestEntity;
    }

    public static boolean hasLineOfSight(Minecraft client, Entity target) {
        if (client.player == null || client.level == null) {
            return false;
        }
        Vec3 start = client.player.getEyePosition();
        Vec3 end = new Vec3(target.getX(), target.getEyeY(), target.getZ());
        ClipContext context = new ClipContext(start, end, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, client.player);
        return client.level.clip(context).getType() == HitResult.Type.MISS;
    }

    public static boolean handleGhostSwapAttack(Player player, Entity target) {
        return Swap.handleGhostSwapAttack(player, target);
    }

    private void onStartTick(Minecraft client) {
        // 26.2: Minecraft#screen / #setScreen moved to Gui#screen() / Gui#setScreen
        if (!promptChecked && client.gui.screen() instanceof TitleScreen) {
            MaceSwapConfig conf = AutoConfig.getConfigHolder(MaceSwapConfig.class).getConfig();
            if (!conf.hasShownDiscordPrompt) {
                client.gui.setScreen(new DiscordPromptScreen(client.gui.screen()));
            }
            promptChecked = true;
        }

        if (client.player == null || client.level == null) {
            return;
        }

        boolean inMenu = client.gui.screen() != null;
        MaceSwapConfig config = AutoConfig.getConfigHolder(MaceSwapConfig.class).getConfig();

        boolean currentUsePressed = client.options.keyUse.isDown();
        boolean useJustPressed = currentUsePressed && !this.wasUsePressed;
        boolean currentAttackPressed = client.options.keyAttack.isDown();
        boolean attackJustPressed = currentAttackPressed && !this.wasAttackPressed;
        boolean isOnGround = client.player.onGround();
        boolean isHoldingPearl = client.player.getMainHandItem().is(Items.ENDER_PEARL)
                || client.player.getOffhandItem().is(Items.ENDER_PEARL);

        if (!inMenu) {
            PearlCatch.tick(client, config, useJustPressed, isHoldingPearl);
            Wind.tickSafe(client);
            TridentMace.tick(client, config);
            ElytraMace.tick(client, config, isOnGround);
            Wind.tickAuto(client, config, useJustPressed);
        }
        SpearSwap.tick(client, config, attackJustPressed, inMenu);
        AxeSwap.tick(client);
        Swap.tick(client);
        AutoRefill.tick(client, config);

        if (!inMenu && TargetAssist.isActive && TargetAssist.lockedTarget != null
                && (!TargetAssist.lockedTarget.isAlive() || TargetAssist.lockedTarget.isRemoved())) {
            TargetAssist.isActive = false;
            TargetAssist.lockedTarget = null;
            isAutoMaceEnabled = wasWhiteModeBeforeAim;
            if (config.displayMode == MaceSwapConfig.DisplayMode.ACTIONBAR) {
                // 26.2: Gui#setOverlayMessage moved to Hud#setOverlayMessage
                client.gui.hud.setOverlayMessage(Component.translatable("message.maceswap.aim_lost"), false);
            }
        }

        while (configWheelKey.consumeClick()) {
            if (client.gui.screen() == null) {
                client.gui.setScreen(new ConfigWheelScreen());
            }
        }

        while (!inMenu && elytraLaunchKey.consumeClick()) {
            if (!config.elytraLaunchEnabled || ElytraMace.state != 0) {
                continue;
            }
            Entity target = getLookingAtEntity(client, config);
            if (target != null) {
                ElytraMace.target = target;
                ElytraMace.state = 1;
                ElytraMace.originalSlot = getSelectedSlot(client.player.getInventory());
                TargetAssist.isActive = true;
                TargetAssist.lockedTarget = target;
                isAutoMaceEnabled = true;
            }
        }

        while (!inMenu && toggleKey.consumeClick()) {
            if (TargetAssist.isActive) {
                TargetAssist.isActive = false;
                TargetAssist.lockedTarget = null;
                isAutoMaceEnabled = wasWhiteModeBeforeAim;
            } else {
                boolean aimActivated = false;
                Entity target = getLookingAtEntity(client, config);
                if (target != null) {
                    wasWhiteModeBeforeAim = isAutoMaceEnabled;
                    isAutoMaceEnabled = true;
                    TargetAssist.isActive = true;
                    TargetAssist.lockedTarget = target;
                    aimActivated = true;
                }
                if (!aimActivated) {
                    isAutoMaceEnabled = !isAutoMaceEnabled;
                }
            }
        }

        this.wasUsePressed = currentUsePressed;
        this.wasAttackPressed = currentAttackPressed;
    }

    private static void renderHud(GuiGraphicsExtractor guiGraphics, DeltaTracker tickDelta) {
        Minecraft client = Minecraft.getInstance();
        // 26.2: Minecraft#screen moved to Gui#screen()
        if (client.gui.screen() != null) {
            return;
        }
        MaceSwapConfig config = AutoConfig.getConfigHolder(MaceSwapConfig.class).getConfig();

        if (client.player != null && client.level != null) {
            if (ElytraMace.state == 3 || ElytraMace.state == 4
                    || TridentMace.state == 1 || TridentMace.state == 2) {
                client.player.setXRot(-90.0f);
            } else if (ElytraMace.state >= 5 || TridentMace.state >= 3) {
                Entity target = TargetAssist.lockedTarget;
                if (target == null && ElytraMace.target != null) {
                    target = ElytraMace.target;
                }
                if (target != null && target.isAlive() && !target.isRemoved()) {
                    double dx = target.getX() - client.player.getX();
                    double dy = target.getEyeY() - client.player.getEyeY();
                    double dz = target.getZ() - client.player.getZ();
                    double diffXZ = Math.sqrt(dx * dx + dz * dz);
                    float targetYaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
                    float targetPitch = (float) (-Math.toDegrees(Math.atan2(dy, diffXZ)));
                    float yawDiff = Mth.wrapDegrees(targetYaw - client.player.getYRot());
                    float pitchDiff = Mth.wrapDegrees(targetPitch - client.player.getXRot());
                    float velSpeed = (float) client.player.getDeltaMovement().length();
                    float speedMult = 1.0f + velSpeed * velSpeed * 15.0f;
                    float maxStep = (float) config.elytraAimSpeed * speedMult / 20.0f;
                    client.player.setYRot(client.player.getYRot() + Mth.clamp(yawDiff * 0.4f, -maxStep, maxStep));
                    client.player.setXRot(client.player.getXRot() + Mth.clamp(pitchDiff * 0.4f, -maxStep, maxStep));
                }
            } else if (Wind.state == 1) {
                float targetPitch = 90.0f;
                float currentPitch = client.player.getXRot();
                float pitchDiff = Mth.wrapDegrees(targetPitch - currentPitch);
                float pitchStep = Mth.clamp(pitchDiff * 0.4f, -15.0f, 15.0f);
                client.player.setXRot(currentPitch + pitchStep);
            } else if (Wind.state == 4) {
                float targetPitch = Wind.originalPitch;
                float currentPitch = client.player.getXRot();
                float pitchDiff = Mth.wrapDegrees(targetPitch - currentPitch);
                float pitchStep = Mth.clamp(pitchDiff * 0.4f, -15.0f, 15.0f);
                client.player.setXRot(currentPitch + pitchStep);
            } else if (ElytraMace.state == 0 && TridentMace.state == 0
                    && Wind.state == 0 && PearlCatch.state == 0) {
                TargetAssist.tick(client, config);
            }
        }

        if (config.displayMode == MaceSwapConfig.DisplayMode.EXCLAMATION_MARK) {
            if (TargetAssist.isActive) {
                guiGraphics.text(client.font, Component.literal("!"), 10,
                        client.getWindow().getGuiScaledHeight() - 15, 0xFF000000, false);
            } else if (isAutoMaceEnabled) {
                guiGraphics.text(client.font, Component.literal("!"), 10,
                        client.getWindow().getGuiScaledHeight() - 15, -1, true);
            }
        }
    }
}

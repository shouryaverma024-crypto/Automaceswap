package com.Cna4ik.maceswap.client;

import com.Cna4ik.maceswap.config.MaceSwapConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class TargetAssist {
    public static boolean isActive = false;
    public static Entity lockedTarget = null;

    public static void tick(Minecraft client, MaceSwapConfig config) {
        if (!isActive || lockedTarget == null || client.player == null) {
            return;
        }
        // 26.2: Minecraft#screen moved to Gui#screen()
        if (client.gui.screen() != null) {
            return;
        }
        if (config.playerOnlyAim && !(lockedTarget instanceof Player)) {
            isActive = false;
            lockedTarget = null;
            return;
        }
        if (!lockedTarget.isAlive()
                || lockedTarget.isRemoved()
                || client.player.distanceTo(lockedTarget) > (float) config.aimDistance
                || !MaceSwapClient.hasLineOfSight(client, lockedTarget)) {
            return;
        }
        if (config.aimOnlyOnFall && (client.player.onGround() || client.player.fallDistance < 1.2f)) {
            return;
        }

        double dx = lockedTarget.getX() - client.player.getX();
        double dy = lockedTarget.getEyeY() - client.player.getEyeY();
        double dz = lockedTarget.getZ() - client.player.getZ();
        double diffXZ = Math.sqrt(dx * dx + dz * dz);
        float targetYaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
        float targetPitch = (float) (-Math.toDegrees(Math.atan2(dy, diffXZ)));
        float currentYaw = client.player.getYRot();
        float currentPitch = client.player.getXRot();
        float yawDiff = Mth.wrapDegrees(targetYaw - currentYaw);
        float pitchDiff = Mth.wrapDegrees(targetPitch - currentPitch);
        if (diffXZ < 2.0 && dy < -1.0) {
            yawDiff *= (float) (diffXZ / 2.0);
        }
        float velSpeed = (float) client.player.getDeltaMovement().length();
        float speedMult = 1.0f + velSpeed * velSpeed * 10.0f;
        float maxStep = (float) config.aimSpeed * speedMult;
        float yawStep = Mth.clamp(yawDiff * 0.15f, -maxStep, maxStep);
        float pitchStep = Mth.clamp(pitchDiff * 0.15f, -maxStep, maxStep);
        client.player.turn((double) yawStep / 0.15, (double) pitchStep / 0.15);
    }
}

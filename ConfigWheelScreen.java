package com.Cna4ik.maceswap.client;

import com.Cna4ik.maceswap.config.MaceSwapConfig;
import com.mojang.blaze3d.platform.Window;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ConfigWheelScreen extends Screen {
    private int lastHoveredIndex = -1;
    private boolean wasMouseDown = false;
    private boolean keyWasReleased = false;

    public ConfigWheelScreen() {
        super(Component.literal("Config Wheel"));
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void tick() {
        if (MaceSwapClient.configWheelKey.isDown()) {
            if (this.keyWasReleased) {
                this.onClose();
                return;
            }
        } else {
            this.keyWasReleased = true;
        }

        if (this.minecraft != null) {
            long handle = 0L;
            try {
                Window win = this.minecraft.getWindow();
                for (Field f : win.getClass().getDeclaredFields()) {
                    if (f.getType() == Long.TYPE) {
                        f.setAccessible(true);
                        handle = f.getLong(win);
                        if (handle != 0L) {
                            break;
                        }
                    }
                }
            } catch (Exception ignored) {
            }

            boolean isMouseDown = false;
            if (handle != 0L) {
                isMouseDown = GLFW.glfwGetMouseButton(handle, 0) == 1;
            }

            if (isMouseDown && !this.wasMouseDown && this.lastHoveredIndex != -1) {
                MaceSwapConfig config = AutoConfig.getConfigHolder(MaceSwapConfig.class).getConfig();
                List<MaceSwapConfig.WheelOption> activeOptions = this.getActiveWheelOptions(config);
                if (this.lastHoveredIndex < activeOptions.size()) {
                    this.cycleOption(config, activeOptions.get(this.lastHoveredIndex));
                    AutoConfig.getConfigHolder(MaceSwapConfig.class).save();
                    if (!config.keepWheelOpen) {
                        this.onClose();
                        return;
                    }
                }
            }
            this.wasMouseDown = isMouseDown;
        }
        super.tick();
    }

    private List<MaceSwapConfig.WheelOption> getActiveWheelOptions(MaceSwapConfig config) {
        List<MaceSwapConfig.WheelOption> list = new ArrayList<>();
        if (config.wheelMaceEnchantPref) list.add(MaceSwapConfig.WheelOption.MACE_ENCHANT_PREF);
        if (config.wheelAxeSwap) list.add(MaceSwapConfig.WheelOption.AXE_SWAP);
        if (config.wheelElytraLaunch) list.add(MaceSwapConfig.WheelOption.ELYTRA_LAUNCH);
        if (config.wheelElytraAutoHit) list.add(MaceSwapConfig.WheelOption.ELYTRA_AUTO_HIT);
        if (config.wheelElytraBurstChain) list.add(MaceSwapConfig.WheelOption.ELYTRA_BURST_CHAIN);
        if (config.wheelTridentCombo) list.add(MaceSwapConfig.WheelOption.TRIDENT_COMBO);
        if (config.wheelTridentAutoHit) list.add(MaceSwapConfig.WheelOption.TRIDENT_AUTO_HIT);
        if (config.wheelTridentBurstChain) list.add(MaceSwapConfig.WheelOption.TRIDENT_BURST_CHAIN);
        if (config.wheelSpearSwap) list.add(MaceSwapConfig.WheelOption.SPEAR_SWAP);
        if (config.wheelAutoWindCharge) list.add(MaceSwapConfig.WheelOption.AUTO_WIND_CHARGE);
        if (config.wheelSafeWindCharge) list.add(MaceSwapConfig.WheelOption.SAFE_WIND_CHARGE);
        if (config.wheelPearlCatching) list.add(MaceSwapConfig.WheelOption.PEARL_CATCHING);
        if (config.wheelSmartLock) list.add(MaceSwapConfig.WheelOption.SMART_LOCK);
        if (config.wheelAimOnlyOnFall) list.add(MaceSwapConfig.WheelOption.AIM_ONLY_ON_FALL);
        if (config.wheelAutoRefill) list.add(MaceSwapConfig.WheelOption.AUTO_REFILL);
        if (list.isEmpty()) {
            list.add(MaceSwapConfig.WheelOption.MACE_ENCHANT_PREF);
            list.add(MaceSwapConfig.WheelOption.AXE_SWAP);
            list.add(MaceSwapConfig.WheelOption.ELYTRA_LAUNCH);
        }
        return list;
    }

    /** Position of wheel slot {@code i} of {@code count}; returns {x, y}. */
    private int[] slotPosition(int i, int count, int centerX, int centerY, int radiusX, int radiusY) {
        if (count == 1) {
            return new int[]{centerX, centerY};
        }
        double angle;
        if (count == 2) {
            angle = (i == 0) ? Math.PI : 0.0;
        } else {
            angle = (Math.PI * 2.0 * (double) i) / (double) count - (Math.PI / 2.0);
        }
        int x = centerX + (int) (Math.cos(angle) * (double) radiusX);
        int y = centerY + (int) (Math.sin(angle) * (double) radiusY);
        return new int[]{x, y};
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0xAA000000);

        MaceSwapConfig config = AutoConfig.getConfigHolder(MaceSwapConfig.class).getConfig();
        List<MaceSwapConfig.WheelOption> activeOptions = this.getActiveWheelOptions(config);

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int radiusX = Math.max(140, activeOptions.size() * 20);
        int radiusY = Math.max(110, activeOptions.size() * 16);

        this.lastHoveredIndex = -1;

        int globalMaxBoxWidth = 0;
        for (MaceSwapConfig.WheelOption option : activeOptions) {
            int titleWidth = this.font.width(Component.translatable(option.translationKey));
            int statusWidth = this.font.width("\u00a7b[DENSITY]");
            globalMaxBoxWidth = Math.max(globalMaxBoxWidth, Math.max(titleWidth, statusWidth) + 12);
        }

        // Hit-testing runs from the last slot to the first so the top-most box wins.
        for (int i = activeOptions.size() - 1; i >= 0; i--) {
            int[] p = slotPosition(i, activeOptions.size(), centerX, centerY, radiusX, radiusY);
            int boxX = p[0] - globalMaxBoxWidth / 2;
            int boxY = p[1] - 10;
            int boxHeight = 24;
            if (mouseX >= boxX && mouseX <= boxX + globalMaxBoxWidth
                    && mouseY >= boxY && mouseY <= boxY + boxHeight) {
                this.lastHoveredIndex = i;
                break;
            }
        }

        for (int i = 0; i < activeOptions.size(); i++) {
            int[] p = slotPosition(i, activeOptions.size(), centerX, centerY, radiusX, radiusY);
            int x = p[0];
            int y = p[1];
            MaceSwapConfig.WheelOption option = activeOptions.get(i);
            boolean isHovered = (i == this.lastHoveredIndex);
            int bgColor = isHovered ? 0x88FFFFFF : 0x55000000;
            int textColor = isHovered ? 0xFF55FF55 : -1;
            String status = this.getStatusText(config, option);
            if (option == MaceSwapConfig.WheelOption.MACE_ENCHANT_PREF) {
                textColor = -1;
            }
            Component titleText = Component.translatable(option.translationKey);

            int boxX = x - globalMaxBoxWidth / 2;
            int boxY = y - 10;
            int boxHeight = 24;
            context.fill(boxX, boxY, boxX + globalMaxBoxWidth, boxY + boxHeight, bgColor);
            int borderColor = isHovered ? -1 : 0xFF888888;
            context.fill(boxX, boxY, boxX + globalMaxBoxWidth, boxY + 1, borderColor);
            context.fill(boxX, boxY + boxHeight - 1, boxX + globalMaxBoxWidth, boxY + boxHeight, borderColor);
            context.fill(boxX, boxY + 1, boxX + 1, boxY + boxHeight - 1, borderColor);
            context.fill(boxX + globalMaxBoxWidth - 1, boxY + 1, boxX + globalMaxBoxWidth, boxY + boxHeight - 1,
                    borderColor);

            int titleWidth = this.font.width(titleText);
            context.text(this.font, titleText, x - titleWidth / 2, y - 6, textColor);
            Component statusComp = Component.literal(status);
            int statusWidth = this.font.width(statusComp);
            context.text(this.font, statusComp, x - statusWidth / 2, y + 5, textColor);
        }

        super.extractRenderState(context, mouseX, mouseY, delta);
    }

    private String getStatusText(MaceSwapConfig config, MaceSwapConfig.WheelOption option) {
        if (option == MaceSwapConfig.WheelOption.MACE_ENCHANT_PREF) {
            return switch (config.maceEnchantPreference) {
                case SMART -> "\u00a7e[SMART]";
                case DENSITY -> "\u00a7b[DENSITY]";
                case BREACH -> "\u00a7c[BREACH]";
            };
        }
        boolean enabled = switch (option) {
            case AXE_SWAP -> config.axeSwapEnabled;
            case ELYTRA_LAUNCH -> config.elytraLaunchEnabled;
            case ELYTRA_AUTO_HIT -> config.elytraAutoHitEnabled;
            case ELYTRA_BURST_CHAIN -> config.elytraWindBurstChain;
            case TRIDENT_COMBO -> config.tridentMaceComboEnabled;
            case TRIDENT_AUTO_HIT -> config.tridentAutoHitEnabled;
            case TRIDENT_BURST_CHAIN -> config.tridentWindBurstChain;
            case SPEAR_SWAP -> config.spearSwapEnabled;
            case AUTO_WIND_CHARGE -> config.autoWindChargeEnabled;
            case SAFE_WIND_CHARGE -> config.safeWindChargeEnabled;
            case PEARL_CATCHING -> config.pearlCatchingEnabled;
            case SMART_LOCK -> config.smartLockEnabled;
            case AIM_ONLY_ON_FALL -> config.aimOnlyOnFall;
            case AUTO_REFILL -> config.autoRefillEnabled;
            default -> false;
        };
        return enabled ? "\u00a7a[ON]" : "\u00a7c[OFF]";
    }

    private void cycleOption(MaceSwapConfig config, MaceSwapConfig.WheelOption option) {
        switch (option) {
            case MACE_ENCHANT_PREF -> config.maceEnchantPreference = switch (config.maceEnchantPreference) {
                case SMART -> MaceSwapConfig.MaceEnchantPreference.DENSITY;
                case DENSITY -> MaceSwapConfig.MaceEnchantPreference.BREACH;
                case BREACH -> MaceSwapConfig.MaceEnchantPreference.SMART;
            };
            case AXE_SWAP -> config.axeSwapEnabled = !config.axeSwapEnabled;
            case ELYTRA_LAUNCH -> config.elytraLaunchEnabled = !config.elytraLaunchEnabled;
            case ELYTRA_AUTO_HIT -> config.elytraAutoHitEnabled = !config.elytraAutoHitEnabled;
            case ELYTRA_BURST_CHAIN -> config.elytraWindBurstChain = !config.elytraWindBurstChain;
            case TRIDENT_COMBO -> config.tridentMaceComboEnabled = !config.tridentMaceComboEnabled;
            case TRIDENT_AUTO_HIT -> config.tridentAutoHitEnabled = !config.tridentAutoHitEnabled;
            case TRIDENT_BURST_CHAIN -> config.tridentWindBurstChain = !config.tridentWindBurstChain;
            case SPEAR_SWAP -> config.spearSwapEnabled = !config.spearSwapEnabled;
            case AUTO_WIND_CHARGE -> config.autoWindChargeEnabled = !config.autoWindChargeEnabled;
            case SAFE_WIND_CHARGE -> config.safeWindChargeEnabled = !config.safeWindChargeEnabled;
            case PEARL_CATCHING -> config.pearlCatchingEnabled = !config.pearlCatchingEnabled;
            case SMART_LOCK -> config.smartLockEnabled = !config.smartLockEnabled;
            case AIM_ONLY_ON_FALL -> config.aimOnlyOnFall = !config.aimOnlyOnFall;
            case AUTO_REFILL -> config.autoRefillEnabled = !config.autoRefillEnabled;
        }
    }
}

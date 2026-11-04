package com.Cna4ik.maceswap.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "maceswap")
public class MaceSwapConfig implements ConfigData {

    // ---------------------------------------------------------------- general
    @ConfigEntry.Category("general")
    @ConfigEntry.Gui.Excluded
    public boolean hasShownDiscordPrompt = false;

    @ConfigEntry.Category("general")
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public DisplayMode displayMode = DisplayMode.ACTIONBAR;

    @ConfigEntry.Category("general")
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public SwapItemsMode swapItemsMode = SwapItemsMode.EVERYTHING;

    @ConfigEntry.Category("general")
    @ConfigEntry.Gui.Tooltip
    public boolean autoRefillEnabled = false;

    // ------------------------------------------------------------------ wheel
    @ConfigEntry.Category("wheel")
    @ConfigEntry.Gui.Tooltip
    public boolean keepWheelOpen = false;

    @ConfigEntry.Category("wheel")
    @ConfigEntry.Gui.Tooltip
    public boolean wheelMaceEnchantPref = true;

    @ConfigEntry.Category("wheel")
    @ConfigEntry.Gui.Tooltip
    public boolean wheelAxeSwap = true;

    @ConfigEntry.Category("wheel")
    @ConfigEntry.Gui.Tooltip
    public boolean wheelElytraLaunch = true;

    @ConfigEntry.Category("wheel")
    @ConfigEntry.Gui.Tooltip
    public boolean wheelElytraAutoHit = false;

    @ConfigEntry.Category("wheel")
    @ConfigEntry.Gui.Tooltip
    public boolean wheelElytraBurstChain = false;

    @ConfigEntry.Category("wheel")
    @ConfigEntry.Gui.Tooltip
    public boolean wheelTridentCombo = false;

    @ConfigEntry.Category("wheel")
    @ConfigEntry.Gui.Tooltip
    public boolean wheelTridentAutoHit = false;

    @ConfigEntry.Category("wheel")
    @ConfigEntry.Gui.Tooltip
    public boolean wheelTridentBurstChain = false;

    @ConfigEntry.Category("wheel")
    @ConfigEntry.Gui.Tooltip
    public boolean wheelSpearSwap = false;

    @ConfigEntry.Category("wheel")
    @ConfigEntry.Gui.Tooltip
    public boolean wheelAutoWindCharge = true;

    @ConfigEntry.Category("wheel")
    @ConfigEntry.Gui.Tooltip
    public boolean wheelSafeWindCharge = true;

    @ConfigEntry.Category("wheel")
    @ConfigEntry.Gui.Tooltip
    public boolean wheelPearlCatching = true;

    @ConfigEntry.Category("wheel")
    @ConfigEntry.Gui.Tooltip
    public boolean wheelSmartLock = false;

    @ConfigEntry.Category("wheel")
    @ConfigEntry.Gui.Tooltip
    public boolean wheelAimOnlyOnFall = true;

    @ConfigEntry.Category("wheel")
    @ConfigEntry.Gui.Tooltip
    public boolean wheelAutoRefill = false;

    // -------------------------------------------------------------------- aim
    @ConfigEntry.Category("aim")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 1, max = 50)
    public int aimSpeed = 15;

    @ConfigEntry.Category("aim")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 1, max = 100)
    public int elytraAimSpeed = 40;

    @ConfigEntry.Category("aim")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 1, max = 50)
    public int aimDistance = 5;

    @ConfigEntry.Category("aim")
    @ConfigEntry.Gui.Tooltip
    public boolean playerOnlyAim = true;

    @ConfigEntry.Category("aim")
    @ConfigEntry.Gui.Tooltip
    public boolean smartLockEnabled = false;

    @ConfigEntry.Category("aim")
    @ConfigEntry.Gui.Tooltip
    public boolean aimOnlyOnFall = false;

    // ----------------------------------------------------------------- combos
    @ConfigEntry.Category("combos")
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public MaceEnchantPreference maceEnchantPreference = MaceEnchantPreference.SMART;

    @ConfigEntry.Category("combos")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 1, max = 20)
    public int smartDensityFallThreshold = 3;

    @ConfigEntry.Category("combos")
    @ConfigEntry.Gui.Tooltip
    public boolean axeSwapEnabled = false;

    @ConfigEntry.Category("combos")
    @ConfigEntry.Gui.Tooltip
    public boolean elytraLaunchEnabled = false;

    @ConfigEntry.Category("combos")
    @ConfigEntry.Gui.Tooltip
    public boolean elytraAutoHitEnabled = false;

    @ConfigEntry.Category("combos")
    @ConfigEntry.Gui.Tooltip
    public boolean elytraWindBurstChain = false;

    @ConfigEntry.Category("combos")
    @ConfigEntry.Gui.Tooltip
    public boolean tridentMaceComboEnabled = false;

    @ConfigEntry.Category("combos")
    @ConfigEntry.Gui.Tooltip
    public boolean tridentAutoHitEnabled = false;

    @ConfigEntry.Category("combos")
    @ConfigEntry.Gui.Tooltip
    public boolean tridentWindBurstChain = false;

    // ------------------------------------------------------------------- misc
    @ConfigEntry.Category("misc")
    @ConfigEntry.Gui.Tooltip
    public boolean spearSwapEnabled = true;

    @ConfigEntry.Category("misc")
    @ConfigEntry.Gui.Tooltip
    public boolean spearSwapIgnoreBlocks = true;

    @ConfigEntry.Category("misc")
    @ConfigEntry.Gui.Tooltip
    public boolean autoWindChargeEnabled = false;

    @ConfigEntry.Category("misc")
    @ConfigEntry.Gui.Tooltip
    public boolean safeWindChargeEnabled = false;

    @ConfigEntry.Category("misc")
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public AutoWindChargeMode autoWindChargeMode = AutoWindChargeMode.ON_LOCK;

    @ConfigEntry.Category("misc")
    @ConfigEntry.Gui.Tooltip
    public boolean pearlCatchingEnabled = false;

    @ConfigEntry.Category("misc")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 0, max = 90)
    public int pearlCatchAngle = 75;

    // ------------------------------------------------------------------ enums
    public enum DisplayMode {
        ACTIONBAR("text.autoconfig.maceswap.option.displayMode.ACTIONBAR"),
        EXCLAMATION_MARK("text.autoconfig.maceswap.option.displayMode.EXCLAMATION_MARK");

        private final String key;

        DisplayMode(String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return key;
        }
    }

    public enum SwapItemsMode {
        ONLY_WEAPONS("text.autoconfig.maceswap.option.swapItemsMode.ONLY_WEAPONS"),
        EVERYTHING("text.autoconfig.maceswap.option.swapItemsMode.EVERYTHING");

        private final String key;

        SwapItemsMode(String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return key;
        }
    }

    public enum MaceEnchantPreference {
        SMART("text.autoconfig.maceswap.option.maceEnchantPreference.SMART"),
        DENSITY("text.autoconfig.maceswap.option.maceEnchantPreference.DENSITY"),
        BREACH("text.autoconfig.maceswap.option.maceEnchantPreference.BREACH");

        private final String key;

        MaceEnchantPreference(String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return key;
        }
    }

    public enum AutoWindChargeMode {
        ON_LOCK("text.autoconfig.maceswap.option.autoWindChargeMode.ON_LOCK"),
        ALWAYS("text.autoconfig.maceswap.option.autoWindChargeMode.ALWAYS");

        private final String key;

        AutoWindChargeMode(String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return key;
        }
    }

    public enum WheelOption {
        MACE_ENCHANT_PREF("text.autoconfig.maceswap.option.maceEnchantPreference"),
        AXE_SWAP("text.autoconfig.maceswap.option.axeSwapEnabled"),
        ELYTRA_LAUNCH("text.autoconfig.maceswap.option.elytraLaunchEnabled"),
        ELYTRA_AUTO_HIT("text.autoconfig.maceswap.option.elytraAutoHitEnabled"),
        ELYTRA_BURST_CHAIN("text.autoconfig.maceswap.option.elytraWindBurstChain"),
        TRIDENT_COMBO("text.autoconfig.maceswap.option.tridentMaceComboEnabled"),
        TRIDENT_AUTO_HIT("text.autoconfig.maceswap.option.tridentAutoHitEnabled"),
        TRIDENT_BURST_CHAIN("text.autoconfig.maceswap.option.tridentWindBurstChain"),
        SPEAR_SWAP("text.autoconfig.maceswap.option.spearSwapEnabled"),
        AUTO_WIND_CHARGE("text.autoconfig.maceswap.option.autoWindChargeEnabled"),
        SAFE_WIND_CHARGE("text.autoconfig.maceswap.option.safeWindChargeEnabled"),
        PEARL_CATCHING("text.autoconfig.maceswap.option.pearlCatchingEnabled"),
        SMART_LOCK("text.autoconfig.maceswap.option.smartLockEnabled"),
        AIM_ONLY_ON_FALL("text.autoconfig.maceswap.option.aimOnlyOnFall"),
        AUTO_REFILL("text.autoconfig.maceswap.option.autoRefillEnabled");

        public final String translationKey;

        WheelOption(String translationKey) {
            this.translationKey = translationKey;
        }

        @Override
        public String toString() {
            return translationKey;
        }
    }
}

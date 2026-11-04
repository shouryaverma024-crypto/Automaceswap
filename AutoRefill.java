package com.Cna4ik.maceswap.client;

import com.Cna4ik.maceswap.config.MaceSwapConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Items;

public class AutoRefill {

    public static void tick(Minecraft client, MaceSwapConfig config) {
        // 26.2: Minecraft#screen moved to Gui#screen()
        if (config.autoRefillEnabled && MaceSwapClient.isAutoMaceEnabled
                && client.gui.screen() == null && client.player.tickCount % 10 == 0) {
            ItemHelper.refillItem(client, Items.WIND_CHARGE);
            ItemHelper.refillItem(client, Items.FIREWORK_ROCKET);
        }
    }
}

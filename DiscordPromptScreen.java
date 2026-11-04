package com.Cna4ik.maceswap.client;

import com.Cna4ik.maceswap.config.MaceSwapConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.awt.Desktop;
import java.net.URI;

public class DiscordPromptScreen extends Screen {
    private static final String URL = "https://modrinth.com/mod/auto-mace-swap";

    private final Screen parent;

    public DiscordPromptScreen(Screen parent) {
        super(Component.translatable("maceswap.discord.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int buttonWidth = 260;
        int buttonHeight = 20;
        int x = (this.width - buttonWidth) / 2;
        int y = this.height - 60;

        this.addRenderableWidget(Button.builder(Component.translatable("maceswap.discord.ok"), button -> {
            new Thread(DiscordPromptScreen::openLink).start();
            MaceSwapConfig config = AutoConfig.getConfigHolder(MaceSwapConfig.class).getConfig();
            config.hasShownDiscordPrompt = true;
            AutoConfig.getConfigHolder(MaceSwapConfig.class).save();
            if (this.minecraft != null) {
                // 26.2: Minecraft#setScreen moved to Gui#setScreen
                this.minecraft.gui.setScreen(this.parent);
            }
        }).bounds(x, y, buttonWidth, buttonHeight).build());

        this.addRenderableWidget(Button.builder(Component.translatable("maceswap.discord.cancel"), button -> {
            if (this.minecraft != null) {
                // 26.2: Minecraft#setScreen moved to Gui#setScreen
                this.minecraft.gui.setScreen(this.parent);
            }
        }).bounds(x, y + 25, buttonWidth, buttonHeight).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0xAA000000);
        int titleWidth = this.font.width(this.title);
        context.text(this.font, this.title, this.width / 2 - titleWidth / 2, this.height - 105, -1, false);
        Component msg = Component.translatable("maceswap.discord.message");
        int msgWidth = this.font.width(msg);
        context.text(this.font, msg, this.width / 2 - msgWidth / 2, this.height - 85, 0xFFAAAAAA, false);
        super.extractRenderState(context, mouseX, mouseY, delta);
    }

    private static void openLink() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + URL);
            } else if (os.contains("mac")) {
                Runtime.getRuntime().exec("open " + URL);
            } else if (os.contains("nix") || os.contains("nux")) {
                Runtime.getRuntime().exec("xdg-open " + URL);
            } else if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(URL));
            }
        } catch (Exception ignored) {
        }
    }
}

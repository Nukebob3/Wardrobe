package net.nukebob.wardrobe.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.nukebob.wardrobe.Wardrobe;
import net.nukebob.wardrobe.config.Config;

import java.io.File;
import java.net.URI;
import java.util.Arrays;

public class WardrobeScreen extends Screen {
    double scroll = 0;
    public static File skinsDirectory;
    public static Identifier selectedSkin = null;

    public WardrobeScreen() {
        super(Text.translatable("screen."+Wardrobe.MOD_ID+".wardrobe"));

        String skinsDirectory = Config.loadConfig().skinsDirectory;
        File skins = new File(skinsDirectory);
        if (!skins.isAbsolute()) {
            skins = new File(MinecraftClient.getInstance().runDirectory, skinsDirectory);
        }
        WardrobeScreen.skinsDirectory = skins;
    }

    @Override
    protected void init() {
        super.init();
        float scale = width/640f;

        File[] files = skinsDirectory.listFiles();
        if (files==null) return;
        Arrays.sort(files, (a, b) -> Boolean.compare(a.isFile(), b.isFile()));

        try {
            int i = 0;
            for (File file : files) {
                if (file.isFile()) {
                    if (file.getName().endsWith(".png")) {
                        URI skinPath = skinsDirectory.toURI().relativize(file.toURI());
                        addDrawableChild(new SkinWidget(skinPath, i, 50 + (int) ((i % 4 * 110)* scale), (int) (50 + (Math.floor(i / 4f) * 130)*scale), (int) (100 * scale), (int) (120 * scale), true));
                        i++;
                    }
                } else if (file.isDirectory()) {
                    for (File subFile : file.listFiles()) {
                        if (subFile.getName().endsWith(".png")) {
                            URI skinPath = skinsDirectory.toURI().relativize(subFile.toURI());
                            addDrawableChild(new SkinWidget(skinPath, i, 50 + (int) ((i % 4 * 110)* scale), (int) (50 + (Math.floor(i / 4f) * 130)*scale), (int) (100 * scale), (int) (120 * scale), true));
                            i++;
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);

    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scroll+=verticalAmount;
        for (Element child : children()) {
            if (child instanceof SkinWidget widget) {
                widget.setPosition(widget.getX(), (int) (widget.getY()+verticalAmount*10));
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
}

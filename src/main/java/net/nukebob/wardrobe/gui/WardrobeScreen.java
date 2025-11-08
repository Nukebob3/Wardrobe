package net.nukebob.wardrobe.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.nukebob.wardrobe.Wardrobe;
import net.nukebob.wardrobe.config.Config;
import net.nukebob.wardrobe.config.SkinFolder;
import net.nukebob.wardrobe.config.SkinSettings;

import java.io.File;
import java.net.URI;

public class WardrobeScreen extends Screen {
    double scroll = 0;
    private final File skinsDirectory;
    public static Identifier selectedSkin = null;

    public WardrobeScreen(File skinsDirectory) {
        super(Text.translatable("screen."+Wardrobe.MOD_ID+".wardrobe"));

        this.skinsDirectory = skinsDirectory;
    }
    public WardrobeScreen(String skinDirectory) {
        this(new File(Config.loadConfig().getSkinsDirectory(),skinDirectory));
    }
    public WardrobeScreen() {
        this("");
    }

    @Override
    protected void init() {
        super.init();
        float scale = width/640f;

        if (skinsDirectory.listFiles()==null) return;

        try {
            SkinSettings skinSettings = SkinSettings.loadSettings();

            boolean root = false;
            SkinFolder currentFolder = SkinFolder.findFolderForDirectory(skinSettings.root, skinsDirectory, Config.loadConfig().getSkinsDirectory());
            if (currentFolder == null) {
                currentFolder = skinSettings.root;
                root = true;
            }
            SkinFolder.mergeFolderWithDirectory(currentFolder, skinsDirectory);
            SkinFolder.removeDuplicates(currentFolder);


            SkinSettings.saveSettings();

            int i = 0;
            int id = 0;
            int j = 0;

            for (SkinFolder folder : currentFolder.folders) {
                if (!folder.type.equals("section")) continue;

                addDrawableChild(new TextWidget(50, (int) (50 + (Math.floor(i / 4f) * 130+j*20+scroll*10)*scale), textRenderer.getWidth(folder.name), textRenderer.fontHeight, Text.literal(folder.name), textRenderer));
                ++j;
                File[] skins = new File(skinsDirectory, folder.name).listFiles();
                if (skins==null) continue;
                for (File file : skins) {
                    URI skinPath = skinsDirectory.toURI().relativize(file.toURI());
                    addDrawableChild(new SkinWidget(skinPath, skinsDirectory, id, 50 + (int) ((i % 4 * 110)* scale), (int) (50 + (Math.floor(i / 4f) * 130+j*20)*scale), (int) (100 * scale), (int) (120 * scale), true));
                    i++;
                    id++;
                }
                i+=4-skins.length%4;
            }
            addDrawableChild(new TextWidget(50, (int) (50 + (Math.floor(i / 4f) * 130+j*20+scroll*10)*scale), textRenderer.getWidth(root?"skins":skinsDirectory.getName()), textRenderer.fontHeight, Text.literal(root?"skins":skinsDirectory.getName()), textRenderer));
            ++j;
            for (String skin : currentFolder.skins) {
                File file = new File(skinsDirectory,skin);
                if (!file.exists()) continue;

                URI skinPath = skinsDirectory.toURI().relativize(file.toURI());
                addDrawableChild(new SkinWidget(skinPath, skinsDirectory, id, 50 + (int) ((i % 4 * 110)* scale), (int) (50 + (Math.floor(i / 4f) * 130+j*20)*scale), (int) (100 * scale), (int) (120 * scale), true));
                i++;
                id++;
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
            if (child instanceof TextWidget widget) {
                widget.setPosition(widget.getX(), (int) (widget.getY()+verticalAmount*10));
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void close() {
        File root = Config.loadConfig().getSkinsDirectory();
        File parentFolder = skinsDirectory.getParentFile();

        if (parentFolder != null && parentFolder.exists() && parentFolder.toPath().startsWith(root.toPath())) {
            client.setScreen(new WardrobeScreen(parentFolder));
        } else {
            client.setScreen(null);
        }
    }

}

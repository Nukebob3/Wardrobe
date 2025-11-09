package net.nukebob.wardrobe.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.nukebob.wardrobe.Wardrobe;
import net.nukebob.wardrobe.config.*;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;

public class WardrobeScreen extends Screen {
    double scroll = 0;
    private final File skinsDirectory;
    public static SkinPropertiesSettings skinPropertiesSettings;
    public static Config config;
    public static SkinLayoutSettings skinLayoutSettings;
    public static Identifier selectedSkin = null;
    public static boolean selectedSkinSlim = true;

    public WardrobeScreen(File skinsDirectory) {
        super(Text.translatable("screen." + Wardrobe.MOD_ID + ".wardrobe"));

        this.skinsDirectory = skinsDirectory;
        skinPropertiesSettings = SkinPropertiesSettings.loadSettings();
        skinLayoutSettings = SkinLayoutSettings.loadSettings();
        config = Config.loadConfig();
    }

    public WardrobeScreen(String skinDirectory) {
        this(new File(Config.loadConfig().getSkinsDirectory(), skinDirectory));
    }

    public WardrobeScreen() {
        this("");
    }

    @Override
    protected void init() {
        super.init();
        float scale = width / 640f;

        if (skinsDirectory.listFiles() == null) return;



        try {
            skinLayoutSettings = SkinLayoutSettings.loadSettings();

            boolean root = false;
            SkinFolder currentFolder = SkinFolder.findFolderForDirectory(skinLayoutSettings.root, skinsDirectory, config.getSkinsDirectory());
            if (currentFolder == null) {
                currentFolder = skinLayoutSettings.root;
                root = true;
            }
            SkinFolder.mergeFolderWithDirectory(currentFolder, skinsDirectory);
            SkinFolder.removeDuplicates(currentFolder);

            SkinLayoutSettings.saveSettings();

            int i = 0,id=0,j=0;

            //Add sections
            for (SkinFolder folder : currentFolder.folders) {
                if (!"section".equals(folder.type)) continue;
                if (folder.skins == null) continue;
                File f = new File(skinsDirectory, folder.name);
                String relativeToRoot = config.getSkinsDirectory().toPath().relativize(f.toPath()).toString().replace("\\", "/");
                SkinData data = skinPropertiesSettings.properties.get(relativeToRoot);
                String title = data!=null?data.name:folder.name;

                addDrawableChild(new TextWidget(50, (int) (50 + (Math.floor(i / 4f) * 130 + j * 20 + scroll * 10) * scale), textRenderer.getWidth(title), textRenderer.fontHeight, Text.literal(title), textRenderer));
                ++j;
                for (String skin : folder.skins) {
                    File file = new File(new File(skinsDirectory, folder.name), skin);
                    if (!file.exists()) continue;

                    if (addSkin(id, i, scale, j, folder, skinPropertiesSettings, file)) {
                        i++;
                        id++;
                    }
                }
                i += (4 - folder.skins.size() % 4) % 4;
            }
            String relativeToRoot = config.getSkinsDirectory().toPath().relativize(skinsDirectory.toPath()).toString().replace("\\", "/");
            SkinData data = skinPropertiesSettings.properties.get(relativeToRoot);
            String title = data!=null?data.name:currentFolder.name;
            addDrawableChild(new TextWidget(50, (int) (50 + (Math.floor(i / 4f) * 130 + j * 20 + scroll * 10) * scale), textRenderer.getWidth(root ? "Skins" : title), textRenderer.fontHeight, Text.literal(root ? "Skins" : title), textRenderer));
            ++j;
            //Add skins
            for (String skin : currentFolder.skins) {
                File file = new File(skinsDirectory, skin);
                if (!file.exists()) continue;

                if (addSkin(id, i, scale, j, currentFolder, skinPropertiesSettings, file)) {
                    i++;
                    id++;
                }
            }
        } catch (Exception e) {
            Wardrobe.LOGGER.error(e.getMessage());
        }
    }

    private boolean addSkin(int id, int i, float scale, int j, SkinFolder currentFolder, SkinPropertiesSettings skinPropertiesSettings, File file) {
        URI skinPath = skinsDirectory.toURI().relativize(file.toURI());
        String relativeToRoot = config.getSkinsDirectory().toPath().relativize(file.toPath()).toString().replace("\\", "/");
        SkinData data = skinPropertiesSettings.properties.get(relativeToRoot);
        if (file.isDirectory()) {
            SkinFolder folder = null;
            for (SkinFolder f : currentFolder.folders) {
                if (f.name.equals(file.getName())) folder = f;
            }
            if (folder!=null&&!(folder.skins.isEmpty()&&folder.folders.isEmpty())) {
                folder.folders = new ArrayList<>();
                folder.skins.removeIf(s -> !(new File(file,s).isFile() && new File(file, s).getName().endsWith(".png")));
                if ("section".equals(folder.type)&&!"section".equals(currentFolder.type)) return false;
                if ("variants".equals(folder.type)&& folder.skins.isEmpty()) folder = null;
            }
            addDrawableChild(new SkinWidget(skinPath, skinsDirectory, id, 50 + (int) ((i % 4 * 110) * scale), (int) (50 + (Math.floor(i / 4f) * 130 + j * 20) * scale), (int) (100 * scale), (int) (120 * scale), folder, data));
        } else {
            addDrawableChild(new SkinWidget(skinPath, skinsDirectory, id, 50 + (int) ((i % 4 * 110) * scale), (int) (50 + (Math.floor(i / 4f) * 130 + j * 20) * scale), (int) (100 * scale), (int) (120 * scale), null, data));
        }
        return true;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scroll += verticalAmount;
        for (Element child : children()) {
            if (child instanceof SkinWidget widget) {
                widget.setPosition(widget.getX(), (int) (widget.getY() + verticalAmount * 10));
            }
            if (child instanceof TextWidget widget) {
                widget.setPosition(widget.getX(), (int) (widget.getY() + verticalAmount * 10));
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void close() {
        File root = config.getSkinsDirectory();
        File parentFolder = skinsDirectory.getParentFile();

        if (parentFolder != null && parentFolder.exists() && parentFolder.toPath().startsWith(root.toPath())) {
            client.setScreen(new WardrobeScreen(parentFolder));
        } else {
            client.setScreen(null);
        }
    }

}

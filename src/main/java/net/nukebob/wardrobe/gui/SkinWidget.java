package net.nukebob.wardrobe.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.nukebob.wardrobe.Wardrobe;
import net.nukebob.wardrobe.api.MojangSkin;
import net.nukebob.wardrobe.config.Config;
import net.nukebob.wardrobe.config.SkinFolder;
import net.nukebob.wardrobe.util.GuiColours;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;

public class SkinWidget extends PressableWidget {
    private final File skinsDirectory;
    private final SkinFolder folder;
    private final URI skinPath;
    private final String name;
    private final boolean slim;
    private int selected = 0;
    private final int id;

    private RenderLayer skinLayer = null;

    public SkinWidget(URI skinPath, File skinsDirectory, int id, int x, int y, int width, int height, boolean slim) {
        this(skinPath, skinsDirectory, id, x, y, width, height, slim, null);
    }
    public SkinWidget(URI skinPath, File skinsDirectory, int id, int x, int y, int width, int height, boolean slim, @Nullable SkinFolder folder) {
        super(x, y, width, height, Text.empty());
        this.skinPath = skinPath;
        this.skinsDirectory = skinsDirectory;
        this.slim = slim;
        this.id = id;

        this.folder = folder;
        skinLayer = getSkinLayer(skinPath);
        name = getName(skinPath);
    }

    public RenderLayer getSkinLayer(URI skinPath) {
        try {
            File file = new File(skinsDirectory.toURI().resolve(skinPath));
            if (file.isDirectory()) {
                if (file.listFiles()==null) return null;
                if (folder!=null&&"variants".equals(folder.type)) {
                    Identifier skinId = null;
                    for (int i = 0; i < Math.min(folder.skins.size(), 5); i++) {
                        File skin = new File(file, folder.skins.get(i));
                        skinId = registerTexture(skin);
                    }
                    return skinId!=null?getSkinLayer(0):null;
                }
                return null;
            }

            return RenderLayer.getEntityTranslucent(registerTexture(file));

        } catch (Exception exception) {
            Wardrobe.LOGGER.error("Failed to load skin {} {}", skinPath, exception.getLocalizedMessage());
            return null;
        }
    }
    public RenderLayer getSkinLayer(int i) {
        try {
            File file = new File(skinsDirectory.toURI().resolve(skinPath));
            File skin = new File(file, folder.skins.get(i));
            File rootDir = Config.loadConfig().getSkinsDirectory();
            String relativePath = rootDir.toURI().relativize(skin.toURI()).getPath();
            Identifier skinId = Identifier.of(Wardrobe.MOD_ID, relativePath);

            return RenderLayer.getEntityTranslucent(skinId);
        } catch (Exception exception) {
            Wardrobe.LOGGER.error("Failed to load skin {} {}", skinPath, exception.getLocalizedMessage());
            return null;
        }
    }

    private Identifier registerTexture(File file) throws IOException {
        NativeImage image = NativeImage.read(new FileInputStream(file));
        NativeImageBackedTexture texture = new NativeImageBackedTexture(() -> "poop", image);

        File rootDir = Config.loadConfig().getSkinsDirectory();
        String relativePath = rootDir.toURI().relativize(file.toURI()).getPath();

        Identifier skinId = Identifier.of(Wardrobe.MOD_ID, relativePath);
        MinecraftClient.getInstance().getTextureManager().registerTexture(skinId, texture);

        return skinId;
    }


    public String getName(URI skinPath) {
        return new File(skinsDirectory.toURI().resolve(skinPath)).getName();
    }

    @Override
    public void onPress() {
        URI absolute = skinsDirectory.toURI().resolve(skinPath);
        File file = new File(absolute);

        if (folder!=null&&("folder".equals(folder.type)||"variants".equals(folder.type))) {
            if ("folder".equals(folder.type)) {
                Screen newScreen = new WardrobeScreen(file);
                MinecraftClient.getInstance().setScreen(newScreen);
            } else {
                File skin = new File(file, folder.skins.get(selected));

                File rootDir = Config.loadConfig().getSkinsDirectory();
                String relativePath = rootDir.toURI().relativize(skin.toURI()).getPath();

                MojangSkin.uploadSkin(skin, "slim", MinecraftClient.getInstance().getSession().getAccessToken());
                WardrobeScreen.selectedSkin = Identifier.of(Wardrobe.MOD_ID, relativePath);
            }
        } else if (file.exists()) {
            File rootDir = Config.loadConfig().getSkinsDirectory();
            String relativePath = rootDir.toURI().relativize(file.toURI()).getPath();

            MojangSkin.uploadSkin(file, "slim", MinecraftClient.getInstance().getSession().getAccessToken());
            WardrobeScreen.selectedSkin = Identifier.of(Wardrobe.MOD_ID, relativePath);
        } else {
            Wardrobe.LOGGER.error("File no longer exists - {}", skinPath);
        }
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        context.fill(getX(), getY(), getX()+width, getY()+height, GuiColours.FRAME_BACKGROUND);
        int color = GuiColours.FRAME_OUTLINE;
        context.drawBorder(getX()+4, getY()+4,width-8,height-8, color);

        /*if (id==5) {
            context.fill(getX(),getY()-2, getX()+width, getY(), 0xFFffe90f);
            context.fillGradient(getX()+width,getY(), getX()+width+2, getY()+height, 0xFFffe90f, 0xFFfcab17);
            context.fill(getX(),getY()+height, getX()+width, getY()+height+2, 0xFFfcab17);
            context.fillGradient(getX()-2,getY(), getX(), getY()+height, 0xFFffe90f, 0xFFfcab17);
        }*/

        PlayerEntityRenderState state = getRenderState();
        renderPlayerEntityState(context, getX(), getY(), state);

        if (folder!=null) {
            if ("folder".equals(folder.type)) {
                Identifier folderIcon = Identifier.of(Wardrobe.MOD_ID, "icon/folder");

                int iconSize = (int) (height * 0.5f);
                int iconX = getX() + (width - iconSize) / 2;
                int iconY = (int) (getY() + (height - iconSize / 1.5f) / 2 - 10);

                context.drawGuiTexture(RenderLayer::getGuiTextured, folderIcon, iconX, iconY, iconSize, iconSize);
            } else if ("variants".equals(folder.type)) {
                if (true) { //TODO decide whether it's on hover or not
                    int maxVariants = Math.min(folder.skins.size(), 5);
                    int totalHeight = (int)(height * 0.7f);
                    int gap = 4;
                    int boxSize = (totalHeight - gap * (5 - 1)) / 5;
                    int startY = getY() + 6;

                    for (int i = 0; i < maxVariants; i++) {
                        int boxX = getX() + width - boxSize - 6;
                        int boxY = startY + i * (boxSize + gap);
                        context.drawBorder(boxX, boxY, boxSize, boxSize, Colors.WHITE);

                        if (mouseX >= boxX && mouseX <= boxX + boxSize &&
                                mouseY >= boxY && mouseY <= boxY + boxSize) {
                            selected = i;
                            skinLayer = getSkinLayer(selected);
                        }
                    }
                }
            }
        }

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        float scale = height / 120f;
        int textWidth = textRenderer.getWidth(name);

        float textX = getX() + width / 2f - (textWidth * scale) / 2f;
        float textY = getY() + height - ((folder!=null&&"folder".equals(folder.type)) ? 50 : 0) - 10;

        context.getMatrices().push();
        context.getMatrices().translate(textX, textY, 0);
        context.getMatrices().scale(scale, scale, 1f);
        context.drawText(textRenderer, name, 0, 0, Colors.WHITE, true);
        context.getMatrices().pop();
    }

    private PlayerEntityRenderState getRenderState () {
        PlayerEntityRenderState state = new PlayerEntityRenderState();
        state.age = Wardrobe.TICKS;

        float animationTime = state.age * 0.067F;
        state.limbSwingAnimationProgress = MathHelper.sin(animationTime) * 0.05F;
        state.limbSwingAmplitude = 0.1F;

        state.limbAmplitudeInverse = Float.POSITIVE_INFINITY;

        state.hatVisible = true;
        state.jacketVisible = true;
        state.leftPantsLegVisible = true;
        state.rightPantsLegVisible = true;
        state.leftSleeveVisible = true;
        state.rightSleeveVisible = true;

        return state;
    }

    private void renderPlayerEntityState(DrawContext context, int x, int y, PlayerEntityRenderState state) {
        if (skinLayer==null) return;

        MinecraftClient client = MinecraftClient.getInstance();
        MatrixStack matrices = context.getMatrices();

        matrices.push();

        matrices.translate((x + (width) / 2f), (y + (height-(height/3f))/2f), 50);
        matrices.scale(-width/2.5f, height/3f, 40f);

        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180f));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(10f));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-30f));

        PlayerEntityModel model = new PlayerEntityModel(getModelPart(slim), slim);

        model.resetTransforms();
        model.setAngles(state);
        model.rightArm.originX+=0.07f;
        model.setVisible(true);

        try {
            RenderSystem.setShaderLights(new Vector3f(-0.8f,-0.5f,1), new Vector3f(0,0f,0.3f));

            //SKIN
            model.render(
                    matrices,
                    client.getBufferBuilders().getEntityVertexConsumers().getBuffer(skinLayer),
                    LightmapTextureManager.MAX_LIGHT_COORDINATE,
                    OverlayTexture.DEFAULT_UV
            );
        } catch (Exception ignored) {}

        matrices.pop();
    }

    private ModelPart getModelPart(boolean slim) {
        ModelData data = PlayerEntityModel.getTexturedModelData(Dilation.NONE, slim);
        return TexturedModelData.of(data, 64, 64).createModel();
    }
}
package net.nukebob.wardrobe.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
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
import net.nukebob.wardrobe.util.GuiColours;
import org.joml.Vector3f;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;

public class SkinWidget extends PressableWidget {
    private final URI skinPath;
    private final String name;
    private final boolean slim;
    private final int id;

    private RenderLayer skinLayer = null;

    public SkinWidget(URI skinPath, int id, int x, int y, int width, int height, boolean slim) {
        super(x, y, width, height, Text.empty());
        this.skinPath = skinPath;
        this.slim = slim;
        this.id = id;

        skinLayer = getSkinLayer(skinPath);
        name = getName(skinPath);
    }

    public RenderLayer getSkinLayer(URI skinPath) {
        try {
            NativeImage image = NativeImage.read(new FileInputStream(WardrobeScreen.skinsDirectory.toURI().resolve(skinPath).getPath()));
            NativeImageBackedTexture texture = new NativeImageBackedTexture(() -> "poop", image);

            Identifier skinId = Identifier.of(Wardrobe.MOD_ID, String.valueOf(id));
            MinecraftClient.getInstance().getTextureManager().registerTexture(skinId, texture);

            return RenderLayer.getEntityTranslucent(skinId);
        } catch (Exception exception) {
            Wardrobe.LOGGER.error("Failed to load skin {} {}", skinPath, exception.getLocalizedMessage());
            return null;
        }
    }

    public String getName(URI skinPath) {
        return new File(WardrobeScreen.skinsDirectory.toURI().resolve(skinPath)).getName();
    }

    @Override
    public void onPress() {
        File file = new File(WardrobeScreen.skinsDirectory.toURI().resolve(skinPath));
        if (file.exists()) {
            MojangSkin.uploadSkin(file, "slim", MinecraftClient.getInstance().getSession().getAccessToken());
        } else {
            Wardrobe.LOGGER.error("File no longer exists - {}", skinPath);
        }
        WardrobeScreen.selectedSkin = Identifier.of(Wardrobe.MOD_ID, String.valueOf(id));
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

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        int textWidth = textRenderer.getWidth(name);
        float scale = height/120f;
        float textX = getX() + (width - textWidth * scale) / 2f;
        float textY = getY() + height - 10;

        context.getMatrices().push();
        context.getMatrices().translate(textX, textY, 0);
        context.getMatrices().scale(scale, scale, scale);
        context.drawText(textRenderer, name, 0,0, Colors.WHITE, true);
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
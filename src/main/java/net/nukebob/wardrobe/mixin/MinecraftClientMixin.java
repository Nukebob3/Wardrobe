package net.nukebob.wardrobe.mixin;

import net.minecraft.client.MinecraftClient;
import net.nukebob.wardrobe.Wardrobe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void wardrobe$render (boolean tick, CallbackInfo ci) {
        Wardrobe.TICKS+=MinecraftClient.getInstance().getRenderTickCounter().getTickProgress(true);
    }
}

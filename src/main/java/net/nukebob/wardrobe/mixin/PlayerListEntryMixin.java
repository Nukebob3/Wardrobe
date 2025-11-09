package net.nukebob.wardrobe.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.SkinTextures;
import net.nukebob.wardrobe.gui.WardrobeScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListEntry.class)
public class PlayerListEntryMixin {
    @Shadow @Final private GameProfile profile;

    @Inject(at = @At("HEAD"), method = "getSkinTextures", cancellable = true)
    private void wardrobe$skinTextures(CallbackInfoReturnable<SkinTextures> cir) {
        if (profile.getId().equals(MinecraftClient.getInstance().getSession().getUuidOrNull())) {
            if (WardrobeScreen.selectedSkin!=null)
                cir.setReturnValue(new SkinTextures(WardrobeScreen.selectedSkin, null, null, null, WardrobeScreen.selectedSkinSlim?SkinTextures.Model.SLIM: SkinTextures.Model.WIDE, true));
        }
    }
}

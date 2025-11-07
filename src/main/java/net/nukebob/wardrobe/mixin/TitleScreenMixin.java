package net.nukebob.wardrobe.mixin;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.nukebob.wardrobe.Wardrobe;
import net.nukebob.wardrobe.gui.WardrobeScreen;
import net.nukebob.wardrobe.util.MenuButtonWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {
	protected TitleScreenMixin() {
		super(Text.empty());
	}

	@Inject(at = @At("TAIL"), method = "addNormalWidgets")
	private void wardrobe$init(int y, int spacingY, CallbackInfoReturnable<Integer> cir) {
		ButtonWidget wardrobeButton = MenuButtonWidget.createWardrobeButton(client);
		wardrobeButton.setPosition(this.width / 2 - 124, y);
		this.addDrawableChild(wardrobeButton);
	}
}
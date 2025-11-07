package net.nukebob.wardrobe.mixin;

import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.nukebob.wardrobe.util.MenuButtonWidget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public class GameMenuScreenMixin extends Screen {
    @Shadow @Final private static Text RETURN_TO_GAME_TEXT;

    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "initWidgets", at = @At(value = "TAIL"))
    private void wardrobe$addButton(CallbackInfo ci) {
        Integer x = null,y = null;
        for (Element child : this.children()) {
            if (child instanceof ButtonWidget button && button.getMessage().equals(RETURN_TO_GAME_TEXT)) {
                x = button.getX()-24;
                y = button.getY();
            }
        }

        if (x!=null) {
            ButtonWidget buttonWidget = MenuButtonWidget.createWardrobeButton(client);
            buttonWidget.setPosition(x, y);
            this.addDrawableChild(buttonWidget);
        }
    }
}

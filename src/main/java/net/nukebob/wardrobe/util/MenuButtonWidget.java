package net.nukebob.wardrobe.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.nukebob.wardrobe.Wardrobe;
import net.nukebob.wardrobe.gui.WardrobeScreen;

public class MenuButtonWidget {
    public static ButtonWidget createWardrobeButton(MinecraftClient client) {
        Text text = Text.translatable("options.wardrobe.wardrobe_button");
        ButtonWidget.PressAction onPress = button -> {client.setScreen(new WardrobeScreen());};
        return TextIconButtonWidget.builder(text, onPress, true).width(20).texture(Identifier.of(Wardrobe.MOD_ID, "icon/wardrobe"), 16, 16).build();
    }
}

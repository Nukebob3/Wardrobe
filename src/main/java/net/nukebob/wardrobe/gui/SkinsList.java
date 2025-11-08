package net.nukebob.wardrobe.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Colors;

import java.io.File;
import java.util.ArrayList;

public class SkinsList {
    private final ArrayList<File> sections;
    private final ArrayList<File> skins;

    public SkinsList() {
        sections = new ArrayList<>();
        skins = new ArrayList<>();
    }

    public void addSection(File file) {
        sections.add(file);
    }

    public void addSkin(File file) {
        skins.add(file);
    }

    public ArrayList<File> getSections() {
        return sections;
    }

    public ArrayList<File> getSkins() {
        return skins;
    }

    public void renderTitles(DrawContext context, float scale, double scroll) {
        int i = 0;
        int j = 0;
        for (File section : sections) {
            context.drawText(MinecraftClient.getInstance().textRenderer,section.getName(),50,(int) (50 + (Math.floor(i / 4f) * 130+j*20+scroll*10)*scale), Colors.WHITE,true);
            ++j;
            File[] skins = section.listFiles();
            for (File file : skins) {
                if (file.getName().endsWith(".png")) {
                    i++;
                }
            }
            i+=4-skins.length%4;
        }
        context.drawText(MinecraftClient.getInstance().textRenderer,"skins",50,(int) (50 + (Math.floor(i / 4f) * 130+j*20+scroll*10)*scale), Colors.WHITE,true);
        ++j;
        for (File file : skins) {
            if (file.getName().endsWith(".png")) {
                i++;
            }
        }
    }
}

package net.nukebob.wardrobe.config;

import net.nukebob.wardrobe.Wardrobe;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class SkinFolder {
    public String name;
    public String type;
    public List<String> skins = new ArrayList<>();
    public List<SkinFolder> folders = new ArrayList<>();

    public SkinFolder() {}

    public SkinFolder(String name) {
        this.name = name;
        this.type = "folder";
    }

    public static SkinFolder buildFromDirectory(File directory) {
        SkinFolder folder = new SkinFolder();
        folder.name = directory.getName(); // optional for root if you want blank

        File[] files = directory.listFiles();
        if (files == null) return folder;

        for (File f : files) {
            if (f.isDirectory()) {
                folder.folders.add(buildFromDirectory(f));
            } else if (f.isFile() && f.getName().endsWith(".png")) {
                folder.skins.add(f.getName());
            }
        }

        return folder;
    }

    /**
     * Recursively searches a SkinFolder tree to find the folder corresponding to the given directory.
     */
    public static SkinFolder findFolderForDirectory(SkinFolder folder, File targetDir, File baseDir) {
        if (folder == null) return null;

        try {
            File targetCanonical = targetDir.getCanonicalFile();
            File currentCanonical;

            if (folder.name == null || folder.name.equals("root")) {
                currentCanonical = baseDir.getCanonicalFile();
            } else {
                currentCanonical = new File(baseDir, folder.name).getCanonicalFile();
            }

            if (!currentCanonical.equals(baseDir) && currentCanonical.equals(targetCanonical)) {
                return folder;
            }

            for (SkinFolder sub : folder.folders) {
                SkinFolder result = findFolderForDirectory(sub, targetDir, currentCanonical);
                if (result != null) return result;
            }
        } catch (Exception e) {
            Wardrobe.LOGGER.error(e.getMessage());
        }

        return null;
    }

    public static void mergeFolderWithDirectory(SkinFolder folder, File dir) {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".png")) {
                if (!folder.skins.contains(file.getName())) {
                    folder.skins.add(file.getName());
                }
            } else if (file.isDirectory()) {
                SkinFolder subFolder = folder.folders.stream()
                        .filter(f -> f.name.equals(file.getName()))
                        .findFirst()
                        .orElse(null);

                if (subFolder == null) {
                    subFolder = new SkinFolder(file.getName());
                    subFolder.type = "folder";
                    folder.folders.add(subFolder);
                }

                if ((subFolder.type.equals("folder") || subFolder.type.equals("variants"))
                        && !folder.skins.contains(subFolder.name)) {
                    folder.skins.add(subFolder.name);
                }

                mergeFolderWithDirectory(subFolder, file);
            }
        }
    }

    public static void removeDuplicates(SkinFolder folder) {
        if (folder == null) return;

        Set<String> uniqueSkins = new LinkedHashSet<>(folder.skins);
        folder.skins.clear();
        folder.skins.addAll(uniqueSkins);

        Set<String> seenFolders = new LinkedHashSet<>();
        List<SkinFolder> toRemove = new java.util.ArrayList<>();
        for (SkinFolder sub : folder.folders) {
            if (seenFolders.contains(sub.name)) {
                toRemove.add(sub);
            } else {
                seenFolders.add(sub.name);
            }
            removeDuplicates(sub);
        }
        folder.folders.removeAll(toRemove);
    }
}

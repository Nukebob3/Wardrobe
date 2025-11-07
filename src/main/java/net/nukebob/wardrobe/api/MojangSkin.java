package net.nukebob.wardrobe.api;

import net.nukebob.wardrobe.Wardrobe;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class MojangSkin {
    /**
     * Uploads a Minecraft skin to the Mojang API.
     *
     * @param skinFile    The local PNG file to upload (64x64 or 64x32).
     * @param variant     "classic" or "slim".
     * @param accessToken The Microsoft/Mojang access token from the client session.
     */
    public static void uploadSkin(File skinFile, String variant, String accessToken) {
        String boundary = "----SkinBoundary" + System.currentTimeMillis();

        try {
            URL url = new URL("https://api.minecraftservices.com/minecraft/profile/skins");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            try (DataOutputStream out = new DataOutputStream(conn.getOutputStream())) {
                // Part 1: variant (text)
                out.writeBytes("--" + boundary + "\r\n");
                out.writeBytes("Content-Disposition: form-data; name=\"variant\"\r\n\r\n");
                out.writeBytes(variant + "\r\n");

                // Part 2: file (binary)
                out.writeBytes("--" + boundary + "\r\n");
                out.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"skin.png\"\r\n");
                out.writeBytes("Content-Type: image/png\r\n\r\n");

                try (FileInputStream fis = new FileInputStream(skinFile)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }

                out.writeBytes("\r\n--" + boundary + "--\r\n");
                out.flush();
            }

            int status = conn.getResponseCode();
            if (status >= 200 && status < 300) {
                Wardrobe.LOGGER.info("Skin uploaded!");
            } else {
                Wardrobe.LOGGER.error("Upload failed: {} {}", status, conn.getResponseMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

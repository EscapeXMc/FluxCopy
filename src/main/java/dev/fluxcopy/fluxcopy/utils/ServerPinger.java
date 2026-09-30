package dev.fluxcopy.fluxcopy.utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Base64;

public class ServerPinger {

    public interface PingCallback {
        void onSuccess(String motd, BufferedImage icon);
        void onFailure(String error);
    }

    public void ping(String ip, int port, PingCallback callback) {
        new Thread(() -> {
            Socket socket = null;
            DataInputStream in = null;

            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(ip, port), 5000);
                socket.setSoTimeout(5000);

                java.io.DataOutputStream out = new java.io.DataOutputStream(socket.getOutputStream());

                byte[] ipBytes = ip.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                out.writeByte(0x00);
                out.writeShort(ipBytes.length + 3);
                out.write(ipBytes);
                out.write(255);
                out.writeShort(port);

                out.writeByte(0x00);
                out.writeShort(8);
                out.writeByte(0x00);
                out.writeShort(754);
                out.writeByte(0x00);

                byte[] statusRequest = {0x00, 0x00};
                out.write(statusRequest);
                out.flush();

                in = new DataInputStream(socket.getInputStream());

                int packetLength = readVarInt(in);
                int packetId = readVarInt(in);

                if (packetId != 0x00) {
                    callback.onFailure("Unexpected packet ID: " + packetId);
                    return;
                }

                int jsonLength = readVarInt(in);
                byte[] jsonBytes = new byte[jsonLength];
                in.readFully(jsonBytes);
                String jsonResponse = new String(jsonBytes, java.nio.charset.StandardCharsets.UTF_8);

                String motd = extractMotd(jsonResponse);
                BufferedImage icon = extractIcon(jsonResponse);

                callback.onSuccess(motd, icon);

            } catch (Exception e) {
                callback.onFailure(e.getMessage());
            } finally {
                try {
                    if (in != null) in.close();
                    if (socket != null) socket.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }).start();
    }

    private int readVarInt(DataInputStream in) throws IOException {
        int value = 0;
        int shift = 0;
        int b;

        do {
            b = in.readUnsignedByte();
            value |= (b & 0x7F) << shift;
            shift += 7;
        } while ((b & 0x80) != 0);

        return value;
    }

    private String extractMotd(String json) {
        try {
            com.google.gson.JsonObject obj = com.google.gson.JsonParser.parseString(json).getAsJsonObject();
            if (obj.has("description")) {
                com.google.gson.JsonElement desc = obj.get("description");
                if (desc.isJsonPrimitive() && desc.getAsJsonPrimitive().isString()) {
                    return desc.getAsString();
                } else if (desc.isJsonObject()) {
                    return desc.toString();
                }
            }
        } catch (Exception e) {
            // ignore, return raw
        }
        return json;
    }

    private BufferedImage extractIcon(String json) {
        try {
            com.google.gson.JsonObject obj = com.google.gson.JsonParser.parseString(json).getAsJsonObject();
            if (obj.has("favicon")) {
                String favicon = obj.get("favicon").getAsString();
                if (favicon.startsWith("data:image/png;base64,")) {
                    String base64 = favicon.substring("data:image/png;base64,".length());
                    byte[] imageBytes = Base64.getDecoder().decode(base64);
                    return ImageIO.read(new ByteArrayInputStream(imageBytes));
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }
}
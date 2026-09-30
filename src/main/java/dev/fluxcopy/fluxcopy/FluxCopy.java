package dev.fluxcopy.fluxcopy;

import dev.fluxcopy.fluxcopy.commands.CopyCommand;
import dev.fluxcopy.fluxcopy.gui.CopyConfirmGUI;
import dev.fluxcopy.fluxcopy.utils.ServerPinger;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.image.BufferedImage;
import java.io.File;

public class FluxCopy extends JavaPlugin {

    private ServerPinger serverPinger;

    @Override
    public void onEnable() {
        serverPinger = new ServerPinger();
        getCommand("copy").setExecutor(new CopyCommand(this, serverPinger));
        getServer().getPluginManager().registerEvents(new CopyConfirmGUI(this, null, "", 0, "", null), this);
        getLogger().info("FluxCopy enabled - Just do copy while Flexing, Only with Flux");
    }

    @Override
    public void onDisable() {
        getLogger().info("FluxCopy disabled");
    }

    public void openConfirmGUI(Player player, String targetIp, int targetPort, String motd, BufferedImage icon) {
        new CopyConfirmGUI(this, player, targetIp, targetPort, motd, icon).open();
    }

    public void applyMotd(String motd) {
        File serverProperties = new File(getDataFolder().getParentFile().getParentFile(), "server.properties");
        if (!serverProperties.exists()) {
            getLogger().warning("server.properties not found at " + serverProperties.getAbsolutePath());
            return;
        }

        try {
            java.util.Properties props = new java.util.Properties();
            try (java.io.FileInputStream fis = new java.io.FileInputStream(serverProperties)) {
                props.load(fis);
            }

            props.setProperty("motd", motd);

            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(serverProperties)) {
                props.store(fos, "Updated by FluxCopy");
            }

            getLogger().info("MOTD updated successfully");
        } catch (Exception e) {
            getLogger().severe("Failed to update MOTD: " + e.getMessage());
        }
    }

    public void applyIcon(BufferedImage icon) {
        if (icon == null) {
            getLogger().warning("No icon provided");
            return;
        }

        try {
            File serverIcon = new File(getDataFolder().getParentFile().getParentFile(), "server-icon.png");
            javax.imageio.ImageIO.write(icon, "png", serverIcon);
            getLogger().info("Server icon saved to " + serverIcon.getAbsolutePath());
        } catch (Exception e) {
            getLogger().severe("Failed to save server icon: " + e.getMessage());
        }
    }

    public void sendMessage(CommandSender sender, String message) {
        sender.sendMessage("§8[§bFluxCopy§8] §r" + message);
    }
}
package dev.fluxcopy.fluxcopy.commands;

import dev.fluxcopy.fluxcopy.FluxCopy;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CopyCommand implements CommandExecutor {

    private final FluxCopy plugin;
    private final dev.fluxcopy.fluxcopy.utils.ServerPinger serverPinger;
    private final Map<UUID, CopySession> sessions = new HashMap<>();

    public CopyCommand(FluxCopy plugin, dev.fluxcopy.fluxcopy.utils.ServerPinger serverPinger) {
        this.plugin = plugin;
        this.serverPinger = serverPinger;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.sendMessage(sender, "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            plugin.sendMessage(player, "§cUsage: /copy <server-ip> [port]");
            plugin.sendMessage(player, "§7Example: /copy play.applemc 25565");
            return true;
        }

        String ip = args[0];
        int port = 25565;

        if (args.length >= 2) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                plugin.sendMessage(player, "§cInvalid port number.");
                return true;
            }
        }

        plugin.sendMessage(player, "§7Pinging §f" + ip + ":" + port + "§7...");

        sessions.remove(player.getUniqueId());

        serverPinger.ping(ip, port, new ServerPingCallback(player, ip, port));

        return true;
    }

    private class ServerPingCallback implements dev.fluxcopy.fluxcopy.utils.ServerPinger.PingCallback {
        private final Player player;
        private final String ip;
        private final int port;

        public ServerPingCallback(Player player, String ip, int port) {
            this.player = player;
            this.ip = ip;
            this.port = port;
        }

        @Override
        public void onSuccess(String motd, BufferedImage icon) {
            if (!player.isOnline()) {
                return;
            }

            sessions.put(player.getUniqueId(), new CopySession(ip, port, motd, icon));
            plugin.openConfirmGUI(player, ip, port, motd, icon);
        }

        @Override
        public void onFailure(String error) {
            if (!player.isOnline()) {
                return;
            }
            plugin.sendMessage(player, "§cFailed to ping server: " + error);
        }
    }

    public static class CopySession {
        public final String ip;
        public final int port;
        public final String motd;
        public final BufferedImage icon;
        public boolean copyMotd;
        public boolean copyIcon;

        public CopySession(String ip, int port, String motd, BufferedImage icon) {
            this.ip = ip;
            this.port = port;
            this.motd = motd;
            this.icon = icon;
            this.copyMotd = false;
            this.copyIcon = false;
        }
    }

    public CopySession getSession(Player player) {
        return sessions.get(player.getUniqueId());
    }

    public void removeSession(Player player) {
        sessions.remove(player.getUniqueId());
    }
}
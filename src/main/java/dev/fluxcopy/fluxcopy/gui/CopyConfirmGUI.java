package dev.fluxcopy.fluxcopy.gui;

import dev.fluxcopy.fluxcopy.FluxCopy;
import dev.fluxcopy.fluxcopy.commands.CopyCommand;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CopyConfirmGUI implements Listener {

    private final FluxCopy plugin;
    private final Player player;
    private final String targetIp;
    private final int targetPort;
    private final String motd;
    private final BufferedImage icon;

    private static final Map<UUID, CopyConfirmGUI> openGuis = new ConcurrentHashMap<>();

    public CopyConfirmGUI(FluxCopy plugin, Player player, String targetIp, int targetPort, String motd, BufferedImage icon) {
        this.plugin = plugin;
        this.player = player;
        this.targetIp = targetIp;
        this.targetPort = targetPort;
        this.motd = motd;
        this.icon = icon;
    }

    public void open() {
        openGuis.put(player.getUniqueId(), this);

        Inventory inv = Bukkit.createInventory(null, 9, "§8FluxCopy - Confirm");

        inv.setItem(2, createButton(Material.LIME_DYE, "§aYes - Copy MOTD", "§7Copy MOTD from " + targetIp));
        inv.setItem(3, createButton(Material.GRAY_DYE, "§8No - Skip MOTD", "§7Do not copy MOTD"));
        inv.setItem(5, createButton(Material.LIME_DYE, "§aYes - Copy Icon", "§7Copy server icon"));
        inv.setItem(6, createButton(Material.GRAY_DYE, "§8No - Skip Icon", "§7Do not copy icon"));

        inv.setItem(4, createInfoItem());

        player.openInventory(inv);
    }

    private ItemStack createButton(Material material, String name, String lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createInfoItem() {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6Server: §f" + targetIp + ":" + targetPort);
            String displayMotd = motd != null && motd.length() > 30 ? motd.substring(0, 30) + "..." : (motd != null ? motd : "N/A");
            meta.setLore(Arrays.asList(
                "§7MOTD: §f" + displayMotd,
                "§7Icon: §f" + (icon != null ? "Available" : "Not available"),
                "",
                "§eClick buttons to confirm"
            ));
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (event.getView().getTitle() == null || !event.getView().getTitle().equals("§8FluxCopy - Confirm")) return;

        event.setCancelled(true);

        Player clicker = (Player) event.getWhoClicked();
        CopyConfirmGUI gui = openGuis.get(clicker.getUniqueId());
        if (gui == null) return;

        int slot = event.getSlot();

        if (slot == 2 || slot == 3 || slot == 5 || slot == 6) {
            CopyCommand.CopySession session = ((CopyCommand) plugin.getCommand("copy").getExecutor()).getSession(clicker);

            if (session == null) {
                plugin.sendMessage(clicker, "§cSession expired. Please run /copy again.");
                clicker.closeInventory();
                return;
            }

            if (slot == 2) {
                session.copyMotd = true;
                plugin.sendMessage(clicker, "§aWill copy MOTD from §f" + targetIp);
            } else if (slot == 3) {
                session.copyMotd = false;
                plugin.sendMessage(clicker, "§8Skipped MOTD copy");
            } else if (slot == 5) {
                session.copyIcon = true;
                plugin.sendMessage(clicker, "§aWill copy icon from §f" + targetIp);
            } else if (slot == 6) {
                session.copyIcon = false;
                plugin.sendMessage(clicker, "§8Skipped icon copy");
            }

            boolean apply = session.copyMotd || session.copyIcon;

            if (apply) {
                if (session.copyMotd) {
                    plugin.applyMotd(session.motd);
                }
                if (session.copyIcon) {
                    plugin.applyIcon(session.icon);
                }
                plugin.sendMessage(clicker, "§aSuccessfully applied changes from §f" + targetIp);
            } else {
                plugin.sendMessage(clicker, "§8No changes applied.");
            }

            ((CopyCommand) plugin.getCommand("copy").getExecutor()).removeSession(clicker);
            clicker.closeInventory();
            openGuis.remove(clicker.getUniqueId());
        }
    }
}
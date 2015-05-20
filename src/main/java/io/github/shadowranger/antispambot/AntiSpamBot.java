package io.github.shadowranger.antispambot;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class AntiSpamBot extends JavaPlugin implements Listener {

    private final Set<UUID> hasMoved = new HashSet<>();

    private String moveToChatMessage;

    //-------------------------------------------//
    // onEnable
    //-------------------------------------------//

    @Override
    public void onEnable() {
        // Save a copy of the default config.yml if it doesn't already exist
        saveDefaultConfig();

        // Load the move to chat message from the config
        moveToChatMessage = getConfig().getString("messages.move-to-chat", "&7You must move before you can chat!");

        // Register the plugin's events
        getServer().getPluginManager().registerEvents(this, this);
    }

    //-------------------------------------------//
    // Utilities
    //-------------------------------------------//

    /**
     * Returns a colorized version of a String.
     *
     * @param string the String to colorize
     * @return the translated colored String
     */
    private String colorize(String string) {
        if (string == null) return "";

        return ChatColor.translateAlternateColorCodes('&', string);
    }

    //-------------------------------------------//
    // Events
    //-------------------------------------------//

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        // Don't continue if the player hasn't moved blocks
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        // Get the player involved in this event
        Player player = event.getPlayer();

        // Allow players with a permission to bypass this restriction
        if (player.hasPermission("antispambot.bypass")) return;

        // Add the player to the hasMoved HashSet
        hasMoved.add(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerKick(PlayerKickEvent event) {
        // Get the player involved in this event
        Player player = event.getPlayer();

        // Allow players with a permission to bypass this restriction
        if (player.hasPermission("antispambot.bypass")) return;

        // If the player is in the hasMoved HashSet, remove them
        hasMoved.remove(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Get the player involved in this event
        Player player = event.getPlayer();

        // Allow players with a permission to bypass this restriction
        if (player.hasPermission("antispambot.bypass")) return;

        // If the player is in the hasMoved HashSet, remove them
        hasMoved.remove(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        // Get the player involved in this event
        Player player = event.getPlayer();

        // Allow players with a permission to bypass this restriction
        if (player.hasPermission("antispambot.bypass")) return;

        // If the player hasn't moved, stop them from using chat
        if (!hasMoved.contains(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(colorize(moveToChatMessage));
        }
    }
}

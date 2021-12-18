package eu.endermite.censura.filter;

import eu.endermite.censura.Censura;
import eu.endermite.censura.config.CachedConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import javax.annotation.Nullable;
import java.text.Normalizer;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class Filter {
    private static final Pattern diacreticMarks = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    public static String preprocessString(String string) {
        String message = string.toLowerCase();
        message = Normalizer.normalize(message, Normalizer.Form.NFD);
        message = ChatColor.stripColor(message);
        message = diacreticMarks.matcher(message).replaceAll("");
        message = Censura.getCachedConfig().getReplacementMap().process(message);
        return message;
    }

    public static boolean detect(String message, CachedConfig.FilterCategory filter) {
        message = preprocessString(message);
        List<MatchType> matches = filter.getMatches();

        FilterCache cache = new FilterCache();
        for (MatchType match : matches) {
            if (match.match(message, cache)) {
                if (Censura.getCachedConfig().isLogDetections())
                    Censura.getPlugin().getLogger().info(String.format("Detected \"%s\" in phrase \"%s\" (type: %s)", match.getSnippet(), message, match.getType()));
                return true;
            }
        }
        return false;
    }

    public static boolean filter(String message, Player player, @Nullable Event event) {
        if (player.isOp() && Censura.getCachedConfig().getOpBypass())
            return false;

        if (player.hasPermission("censura.bypass"))
            return false;

        for (CachedConfig.FilterCategory filter : Censura.getCachedConfig().getCategories()) {
            if (detect(message, filter)) {
                doActions(filter.getPunishments(), player, event);
                return true;
            }
        }
        return false;
    }

    public static boolean filterNoActions(String message) {

        for (CachedConfig.FilterCategory filter : Censura.getCachedConfig().getCategories()) {
            if (detect(message, filter))
                return true;
        }
        return false;
    }

    public static void doActions(List<String> actions, Player player, Event event) {
        for (String a : actions) {
            if (a.startsWith("command:")) {
                CommandSender sender = Censura.getPlugin().getServer().getConsoleSender();
                String command = a.replaceFirst("command: ", "");
                String cmd = command.replaceAll("%player%", player.getName());
                Censura.getPlugin().getServer().getScheduler().runTask(Censura.getPlugin(), () -> Bukkit.dispatchCommand(sender, cmd));
            } else if (a.startsWith("message:")) {
                String message = a.replaceFirst("message: ", "");
                String msg = message.replaceAll("%player%", player.getName());
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
            } else if (a.equals("ghost")) {
                if (event instanceof AsyncPlayerChatEvent) {
                    // Make message invisible to everyone except sender.
                    AsyncPlayerChatEvent chatEvent = (AsyncPlayerChatEvent) event;
                    Set<Player> recipients = chatEvent.getRecipients();
                    recipients.removeIf(recipient -> recipient != chatEvent.getPlayer());

//                  Notify admins about filter
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        if (onlinePlayer.hasPermission("censura.filteredMessages")) {
                            String messageForAdmins = ChatColor.translateAlternateColorCodes('&', String.format("&cCensura - %s: %s", onlinePlayer.getName(), chatEvent.getMessage()));
                            onlinePlayer.sendMessage(messageForAdmins);
                            Censura.getPlugin().getLogger().info("Sent to admins.");
                        }
                    }
                }
            }
        }
    }
}

package eu.endermite.censura.listener;

import eu.endermite.censura.Censura;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandColonListener implements Listener {
    @EventHandler
    public void onChatEven(PlayerCommandPreprocessEvent event) {
        event.setMessage(getMessageWithoutPrefix(event));
    }

    /** /minecraft:xp -> /xp */
    private String getMessageWithoutPrefix(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage();
        String[] messageAsArray = message.split(" ");
        String commandName = messageAsArray[0];

        if (!isInvalidCommand(message) && commandName.contains(":")) {
            String commandNameWithoutPrefix = commandName.split(":")[1];
            messageAsArray[0] = commandNameWithoutPrefix;

            String messageWithoutPrefix = messageAsArray.toString();
            Censura.getPlugin().getLogger().info(String.format("Censura: Prefix removed: %s", commandName));

            return messageWithoutPrefix;
        }

        return message;
    }

    private boolean isInvalidCommand(String command) {
//      only syntax like minecraft:xp should work
        if (colonCount(command) > 1) {
            return true;
        }

        return false;
    }

    private long colonCount(String message) {
        return message.chars().filter(i -> i == ':').count();
    }
}

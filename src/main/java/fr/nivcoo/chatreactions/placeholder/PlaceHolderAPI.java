package fr.nivcoo.chatreactions.placeholder;

import fr.nivcoo.chatreactions.ChatReactions;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

public class PlaceHolderAPI extends PlaceholderExpansion {

    private ChatReactions chatReactions;

    public PlaceHolderAPI() {
        chatReactions = ChatReactions.get();
    }

    @Override
    public String getAuthor() {
        return "nivcoo";
    }

    @Override
    public String getIdentifier() {
        return "chatreactions";
    }

    @Override
    public String getVersion() {
        return "0.0.1";
    }

    @Override
    public String onRequest(OfflinePlayer player, String identifier) {
        if (identifier.equals("get_score")) {
            return String.valueOf(chatReactions.getCacheManager().getPlayerCount(player.getUniqueId()));
        }
        return null;
    }

}

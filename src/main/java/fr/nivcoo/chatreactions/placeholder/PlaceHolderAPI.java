package fr.nivcoo.chatreactions.placeholder;

import fr.nivcoo.chatreactions.ChatReactions;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class PlaceHolderAPI extends PlaceholderExpansion {

    private ChatReactions chatReactions;

    public PlaceHolderAPI() {
        chatReactions = ChatReactions.get();
    }

    @Override
    public @NotNull String getAuthor() {
        return chatReactions.getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "chatreactions";
    }

    @Override
    public @NotNull String getVersion() {
        return chatReactions.getDescription().getVersion();
    }

    @Override
    public String onRequest(OfflinePlayer player, String identifier) {
        if (identifier.equals("get_score")) {
            return String.valueOf(chatReactions.getCacheManager().getPlayerCount(player.getUniqueId()));
        }
        return null;
    }

}

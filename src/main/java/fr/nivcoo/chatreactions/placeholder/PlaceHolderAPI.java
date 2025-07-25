package fr.nivcoo.chatreactions.placeholder;

import fr.nivcoo.chatreactions.ChatReactions;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PlaceHolderAPI extends PlaceholderExpansion {

    private final ChatReactions plugin;

    public PlaceHolderAPI() {
        this.plugin = ChatReactions.get();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "chatreactions";
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String identifier) {
        if (player == null) return "";

        // %chatreactions_get_score%
        if (identifier.equalsIgnoreCase("get_score")) {
            return String.valueOf(plugin.getCacheManager().getPlayerScore(player.getUniqueId()));
        }

        // %chatreactions_top_name_1%
        if (identifier.startsWith("top_name_")) {
            try {
                int rank = Integer.parseInt(identifier.substring("top_name_".length()));
                return getTopPlayerName(rank);
            } catch (NumberFormatException ignored) {
            }
        }

        // %chatreactions_top_score_1%
        if (identifier.startsWith("top_score_")) {
            try {
                int rank = Integer.parseInt(identifier.substring("top_score_".length()));
                return String.valueOf(getTopPlayerScore(rank));
            } catch (NumberFormatException ignored) {
            }
        }

        return null;
    }


    private String getTopPlayerName(int rank) {
        Map<UUID, Integer> scores = plugin.getCacheManager().getSortedRanking();

        int index = 1;
        for (Map.Entry<UUID, Integer> entry : scores.entrySet()) {
            if (index == rank) {
                return Optional.ofNullable(plugin.getServer().getOfflinePlayer(entry.getKey()).getName())
                        .orElse("Unknown");
            }
            index++;
        }

        return "";
    }

    private int getTopPlayerScore(int rank) {
        Map<UUID, Integer> scores = plugin.getCacheManager().getSortedRanking();

        int index = 1;
        for (Map.Entry<UUID, Integer> entry : scores.entrySet()) {
            if (index == rank) {
                return entry.getValue();
            }
            index++;
        }

        return 0;
    }
}

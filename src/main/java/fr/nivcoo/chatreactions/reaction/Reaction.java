package fr.nivcoo.chatreactions.reaction;

import fr.nivcoo.chatreactions.ChatReactions;
import fr.nivcoo.utilsz.config.Config;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class Reaction {

    private final ChatReactions plugin;
    private final ReactionManager manager;
    private final Config config;

    private final String word;
    private final long startMillis;
    private boolean stopped;

    private final LinkedHashMap<UUID, Double> winners;

    private final int onlinePlayersAtStart;

    public Reaction() {
        this.plugin = ChatReactions.get();
        this.manager = plugin.getReactionManager();
        this.config = plugin.getConfiguration();
        this.word = selectRandomWord();
        this.startMillis = System.currentTimeMillis();
        this.stopped = false;
        this.winners = new LinkedHashMap<>();
        this.onlinePlayersAtStart = Bukkit.getOnlinePlayers().size();
    }

    public String getWord() {
        return word;
    }

    public boolean alreadyPlayer(Player player) {
        return winners.containsKey(player.getUniqueId());
    }

    public boolean addPlayerToTop(Player player) {
        if (alreadyPlayer(player)) return false;

        int position = winners.size();
        if (position < manager.getRewardTopSize()) {
            UUID uuid = player.getUniqueId();
            double seconds = Math.round(((System.currentTimeMillis() - startMillis) / 1000.0) * 100.0) / 100.0;
            winners.put(uuid, seconds);


            Component component = LegacyComponentSerializer.legacySection().deserialize(getTopLineOfPlayer(uuid));
            Bukkit.getServer().sendMessage(component);

            String winSound = config.getString("sounds.win");
            player.playSound(player.getLocation(), Sound.valueOf(winSound), 0.4f, 1.7f);
        }

        if (winners.size() >= manager.getRewardTopSize()) {
            manager.stopCurrentReaction();
        } else if (winners.size() >= onlinePlayersAtStart) {
            manager.stopCurrentReaction();
        }

        return true;
    }

    public String getTopLineOfPlayer(UUID uuid) {
        int place = getPlaceOfPlayer(uuid);
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        int rewardTop = manager.getRewardTopSize();
        int earnedPoints = rewardTop - place + 1;

        String type = earnedPoints > 1
                ? config.getString("messages.chat.top.template_points.points")
                : config.getString("messages.chat.top.template_points.point");

        String pointMessage = config.getString("messages.chat.top.template_points.display",
                String.valueOf(earnedPoints), type);

        return config.getString("messages.chat.top.template",
                String.valueOf(place), player.getName(), String.valueOf(winners.get(uuid)),
                getRewardMessageForPlace(place), pointMessage);
    }

    public int getPlaceOfPlayer(UUID uuid) {
        int place = 1;
        for (UUID id : winners.keySet()) {
            if (id.equals(uuid)) break;
            place++;
        }
        return place;
    }

    public String getRewardMessageForPlace(int place) {
        return config.getString("rewards.top." + place + ".message", "");
    }

    private String selectRandomWord() {
        List<String> wordList = manager.getWords();
        return wordList.get(new Random().nextInt(wordList.size()));
    }

    public void start() {
        this.stopped = false;
        winners.clear();

        List<String> startMessages = config.getStringList("messages.chat.start_messages.messages");
        List<String> hoverMessages = config.getStringList("messages.chat.start_messages.hover");
        String startSound = config.getString("sounds.start");

        String messageText = manager.formatMultiline(startMessages);
        String hoverTextFormatted = manager.formatMultiline(hoverMessages).replace("{0}", word);

        Component hoverComponent = LegacyComponentSerializer.legacySection().deserialize(hoverTextFormatted);
        Component messageComponent = LegacyComponentSerializer.legacySection()
                .deserialize(messageText)
                .hoverEvent(HoverEvent.showText(hoverComponent));

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(messageComponent);
            player.playSound(player.getLocation(), Sound.valueOf(startSound), 0.4f, 1.7f);
        }
    }

    public void stop() {
        if (stopped) return;
        stopped = true;

        String finalMessage;
        boolean sendTotal = config.getBoolean("messages.chat.top.send_total_message");
        int topSize = manager.getRewardTopSize();

        if (winners.isEmpty()) {
            finalMessage = config.getString("messages.chat.no_player");

        } else {
            // Récompenses
            int position = 0;
            for (UUID uuid : winners.keySet()) {
                if (position >= topSize) break;
                position++;

                OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
                List<String> commands = config.getStringList("rewards.top." + position + ".commands");

                for (String command : commands) {
                    manager.sendConsoleCommand(command, player);
                }

                int earnedPoints = topSize - position + 1;
                plugin.getCacheManager().updatePlayerScore(uuid, earnedPoints);
            }

            StringBuilder topLines = new StringBuilder();
            int i = 0;
            for (UUID uuid : winners.keySet()) {
                if (i++ > 0) topLines.append("\n");
                topLines.append(getTopLineOfPlayer(uuid));
            }

            if (sendTotal) {
                List<String> summary = config.getStringList("messages.chat.top.message");
                finalMessage = manager.formatMultiline(summary)
                        .replace("{0}", word)
                        .replace("{1}", topLines.toString());
            } else {
                String statusMessage = winners.size() < onlinePlayersAtStart
                        ? config.getString("messages.chat.top.no_all_player")
                        : config.getString("messages.chat.top.all_rewards");

                finalMessage = topLines + "\n" + statusMessage;
            }
        }

        if (finalMessage != null && !finalMessage.isBlank()) {
            Component component = LegacyComponentSerializer.legacySection().deserialize(finalMessage);
            Bukkit.getServer().sendMessage(component);
        }
    }
}

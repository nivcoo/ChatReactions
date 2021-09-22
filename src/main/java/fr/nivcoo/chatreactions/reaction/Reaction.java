package fr.nivcoo.chatreactions.reaction;

import fr.nivcoo.chatreactions.ChatReactions;
import fr.nivcoo.chatreactions.utils.Config;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

public class Reaction {
    private ChatReactions chatReactions;
    private Config config;
    private String word;
    private LinkedHashMap<UUID, Double> players;
    private Long startMillis;

    public Reaction() {
        chatReactions = ChatReactions.get();
        config = chatReactions.getConfiguration();
        selectionRandomWord();
        startMillis = System.currentTimeMillis();
    }

    public String getWord() {
        return word;
    }

    public boolean addPlayerToTop(Player p) {
        if (alreadyPlayer(p))
            return false;

        double second = Math.round(((System.currentTimeMillis() - startMillis) / 1000.0) * 100.0) / 100.0;
        players.put(p.getUniqueId(), second);

        Bukkit.broadcastMessage(getTopLineOfPlayer(p.getUniqueId()));
        String startSound = config.getString("sounds.win");
        p.playSound(p.getLocation(), Sound.valueOf(startSound), .4f, 1.7f);
        if (players.size() >= chatReactions.getReactionManager().getRewardTopSize()) {
            chatReactions.getReactionManager().stopCurrentReaction();
        }
        return true;
    }

    public String getTopLineOfPlayer(UUID uuid) {
        int place = getPlaceOfPlayer(uuid);
        OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
        return config.getString("messages.chat.top.template", String.valueOf(place), p.getName(), String.valueOf(players.get(uuid)), getRewardMessageForPlace(place));
    }

    public int getPlaceOfPlayer(UUID u) {
        int place = 0;
        for (UUID uuid : players.keySet()) {
            place++;
            if (uuid.equals(u))
                break;

        }
        return place;
    }

    public String getRewardMessageForPlace(int place) {
        String message = config.getString("rewards.top." + place + ".message");
        if (message == null)
            message = "";
        return message;
    }

    public void selectionRandomWord() {
        Random rand = new Random();
        List<String> words = chatReactions.getReactionManager().getWords();
        word = words.get(rand.nextInt(words.size()));
    }

    public void start() {

        players = new LinkedHashMap<>();
        String startMessagePath = "messages.chat.start_messages.";

        List<String> startMessages = config.getStringList(startMessagePath + "messages");
        List<String> hoverMessage = config.getStringList(startMessagePath + "hover");
        String startSound = config.getString("sounds.start");

        for (Player p : Bukkit.getOnlinePlayers()) {
            TextComponent message = new TextComponent(multipleLineStringFromList(startMessages));
            message.setHoverEvent(
                    new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(multipleLineStringFromList(hoverMessage).replace("{0}", word)).create()));
            p.spigot().sendMessage(message);
            p.playSound(p.getLocation(), Sound.valueOf(startSound), .4f, 1.7f);
        }

    }

    public String multipleLineStringFromList(List<String> list) {
        return list.stream()
                .map(String::valueOf)
                .collect(Collectors.joining("\n", "", ""));
    }

    public void stop() {

        String message = null;
        boolean sendTotalMessage = config.getBoolean("messages.chat.top.send_total_message");
        if (players.size() == 0)
            message = config.getString("messages.chat.no_player");
        else if (sendTotalMessage) {
            List<String> messages = config.getStringList("messages.chat.top.message");
            StringBuilder topMessage = new StringBuilder();
            int number = 0;
            for (UUID uuid : players.keySet()) {

                int earnedPoints = chatReactions.getReactionManager().getRewardTopSize() - players.size() + 1;

                chatReactions.getCacheManager().updatePlayerCount(uuid, earnedPoints);
                number++;
                topMessage.append(getTopLineOfPlayer(uuid));
                if (number < players.size())
                    topMessage.append("\n");

            }

            message = multipleLineStringFromList(messages).replace("{0}", word).replace("{1}", topMessage);

        }
        if (message != null)
            Bukkit.getServer().broadcastMessage(message);
    }

    public boolean alreadyPlayer(Player p) {
        return players.get(p.getUniqueId()) != null;
    }
}
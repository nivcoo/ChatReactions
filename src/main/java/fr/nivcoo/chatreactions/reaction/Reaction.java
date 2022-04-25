package fr.nivcoo.chatreactions.reaction;

import fr.nivcoo.chatreactions.ChatReactions;
import fr.nivcoo.utilsz.config.Config;
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
    private ReactionManager reactionManager;
    private Config config;
    private String word;
    private LinkedHashMap<UUID, Double> players;
    private Long startMillis;

    private boolean stopped;

    public Reaction() {
        stopped = false;
        chatReactions = ChatReactions.get();
        reactionManager = chatReactions.getReactionManager();
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


        int position = players.size();

        if (position < reactionManager.getRewardTopSize()) {
            UUID uuid = p.getUniqueId();
            double second = Math.round(((System.currentTimeMillis() - startMillis) / 1000.0) * 100.0) / 100.0;
            players.put(uuid, second);
            Bukkit.broadcastMessage(getTopLineOfPlayer(uuid));
            String startSound = config.getString("sounds.win");
            p.playSound(p.getLocation(), Sound.valueOf(startSound), .4f, 1.7f);
        }

        if (players.size() >= reactionManager.getRewardTopSize())
            reactionManager.stopCurrentReaction();


        return true;
    }

    public String getTopLineOfPlayer(UUID uuid) {
        int place = getPlaceOfPlayer(uuid);
        OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
        String templatePointPath = "messages.chat.top.template_points.";
        String point = config.getString(templatePointPath + "point");
        String points = config.getString(templatePointPath + "points");
        String type = point;
        int numberOfWinner = reactionManager.getRewardTopSize();
        int earnedPoints = numberOfWinner - place + 1;
        if (earnedPoints > 1)
            type = points;
        String pointMessage = config.getString(templatePointPath + "display", String.valueOf(earnedPoints), type);
        return config.getString("messages.chat.top.template", String.valueOf(place), p.getName(), String.valueOf(players.get(uuid)), getRewardMessageForPlace(place), pointMessage);
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
        List<String> words = reactionManager.getWords();
        word = words.get(rand.nextInt(words.size()));
    }

    public void start() {
        stopped = false;
        players = new LinkedHashMap<>();
        String startMessagePath = "messages.chat.start_messages.";

        List<String> startMessages = config.getStringList(startMessagePath + "messages");
        List<String> hoverMessage = config.getStringList(startMessagePath + "hover");
        String startSound = config.getString("sounds.start");
        for (Player p : Bukkit.getOnlinePlayers()) {
            TextComponent message = new TextComponent(TextComponent.fromLegacyText(multipleLineStringFromList(startMessages)));
            message.setHoverEvent(
                    new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(multipleLineStringFromList(hoverMessage).replace("{0}", word))));
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
        if(stopped)
            return;
        stopped = true;
        String message;
        boolean sendTotalMessage = config.getBoolean("messages.chat.top.send_total_message");
        int numberOfWinner = reactionManager.getRewardTopSize();
        if (players.size() == 0)
            message = config.getString("messages.chat.no_player");
        else {

            int position = 0;
            for (UUID uuid : players.keySet()) {
                if (position >= numberOfWinner)
                    break;
                position++;

                List<String> commands = config.getStringList("rewards.top." + position + ".commands");
                for (String command : commands)
                    reactionManager.sendConsoleCommand(command, Bukkit.getOfflinePlayer(uuid));

                int earnedPoints = numberOfWinner - position + 1;
                chatReactions.getCacheManager().updatePlayerCount(uuid, earnedPoints);
            }

            if (!sendTotalMessage) {
                if (players.size() < numberOfWinner)
                    message = config.getString("messages.chat.top.no_all_player");
                else
                    message = config.getString("messages.chat.top.all_rewards");
            } else {
                List<String> messages = config.getStringList("messages.chat.top.message");
                StringBuilder topMessage = new StringBuilder();
                int number = 0;
                for (UUID uuid : players.keySet()) {
                    number++;
                    topMessage.append(getTopLineOfPlayer(uuid));
                    if (number < players.size())
                        topMessage.append("\n");
                }

                message = multipleLineStringFromList(messages).replace("{0}", word).replace("{1}", topMessage);

            }
        }

        if (message != null)
            Bukkit.getServer().broadcastMessage(message);
    }

    public boolean alreadyPlayer(Player p) {
        return players.get(p.getUniqueId()) != null;
    }
}

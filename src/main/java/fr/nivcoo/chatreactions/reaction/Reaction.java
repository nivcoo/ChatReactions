package fr.nivcoo.chatreactions.reaction;

import fr.nivcoo.chatreactions.ChatReactions;
import fr.nivcoo.chatreactions.actions.ReactionStartAction;
import fr.nivcoo.chatreactions.actions.ReactionStopAction;
import fr.nivcoo.chatreactions.actions.ReactionTopLineAction;
import fr.nivcoo.chatreactions.actions.rpc.CheckAnswerRes;
import fr.nivcoo.chatreactions.reaction.types.ReactionTypeEntry;
import fr.nivcoo.utilsz.config.Config;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Reaction implements ReactionLike {

    private final ChatReactions plugin;
    private final ReactionManager manager;
    private final Config config;

    private final ReactionTypeEntry type;
    private final String displayToken;
    private final String expectedAnswer;
    private final long startMillis;
    private final int onlinePlayersAtStart;

    private volatile boolean stopped = false;

    private final ConcurrentHashMap<UUID, Double> times = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<UUID> order = new ConcurrentLinkedQueue<>();

    public Reaction() {
        this.plugin = ChatReactions.get();
        this.manager = plugin.getReactionManager();
        this.config = plugin.getConfiguration();
        this.type = manager.selectRandomType();
        this.displayToken = type.generateWord();
        this.expectedAnswer = type.expectedAnswer();
        this.startMillis = System.currentTimeMillis();
        this.onlinePlayersAtStart = Bukkit.getOnlinePlayers().size();
    }

    @Override
    public void start() {
        stopped = false;
        times.clear();
        order.clear();

        List<String> typeLines = type.getDescriptionMessages();
        String startSound = config.getString("sounds.start");

        if (plugin.isRedisEnabled()) {
            plugin.getBus().publish(new ReactionStartAction(displayToken, expectedAnswer, typeLines, startSound));
        }

        plugin.getDisplay().showStart(displayToken, typeLines, startSound);
    }

    @Override
    public void stop() {
        if (stopped) return;
        stopped = true;

        int topSize = manager.getRewardTopSize();
        int targetWinners = Math.min(topSize, onlinePlayersAtStart);

        List<UUID> winnersList = new ArrayList<>(topSize);
        for (UUID u : order) {
            if (winnersList.size() >= topSize) break;
            winnersList.add(u);
        }

        String finalMessage;
        if (winnersList.isEmpty()) {
            finalMessage = config.getString("messages.chat.no_player");
        } else {
            int position = 0;
            for (UUID uuid : winnersList) {
                position++;
                String playerName = plugin.getCacheManager().resolvePlayerName(uuid);

                List<String> commands = config.getStringList("rewards.top." + position + ".commands");
                for (String command : commands) {
                    manager.sendConsoleCommand(command, playerName);
                }
                int earnedPoints = topSize - position + 1;
                plugin.getCacheManager().updatePlayerScore(uuid, earnedPoints);
            }

            boolean sendTotal = config.getBoolean("messages.chat.top.send_total_message");
            if (sendTotal) {
                StringBuilder topLines = new StringBuilder();
                int i = 0;
                for (UUID uuid : winnersList) {
                    if (i++ > 0) topLines.append("\n");
                    topLines.append(getTopLineOfPlayer(uuid));
                }
                List<String> summary = config.getStringList("messages.chat.top.message");
                finalMessage = manager.formatMultiline(summary)
                        .replace("{0}", expectedAnswer)
                        .replace("{1}", topLines.toString());
            } else {
                boolean allGiven = winnersList.size() >= targetWinners;
                finalMessage = allGiven
                        ? config.getString("messages.chat.top.all_rewards")
                        : config.getString("messages.chat.top.no_all_player");
            }
        }

        if (plugin.isRedisEnabled()) {
            plugin.getBus().publish(new ReactionStopAction(finalMessage));
        }

        plugin.getDisplay().showStop(finalMessage);
    }

    @Override
    public CheckAnswerRes tryAccept(UUID player, String input, long atMillis) {
        if (stopped || player == null || input == null) {
            return CheckAnswerRes.empty();
        }
        if (!type.isCorrect(input)) {
            return CheckAnswerRes.empty();
        }

        double seconds = Math.round(((atMillis - startMillis) / 1000.0) * 100.0) / 100.0;

        Double prev = times.putIfAbsent(player, seconds);
        if (prev != null) {
            return CheckAnswerRes.empty();
        }

        order.add(player);
        int place = computePlace(player);
        int cap = manager.getRewardTopSize();
        if (place > cap) {
            return CheckAnswerRes.empty();
        }

        String topLine = getTopLineOfPlayer(player);
        String winSound = config.getString("sounds.win");

        if (plugin.isRedisEnabled()) {
            plugin.getBus().publish(new ReactionTopLineAction(place, player, seconds, topLine, winSound));
        }

        plugin.getDisplay().showTopLine(topLine, player, winSound);

        if (place >= cap) {
            Bukkit.getScheduler().runTask(plugin, manager::stopCurrentReaction);
        }

        return CheckAnswerRes.success(topLine, place, seconds, winSound);
    }

    @Override
    public boolean tryAcceptLocal(UUID player, String input) {
        var res = tryAccept(player, input, System.currentTimeMillis());
        return res.accepted();
    }

    private String getTopLineOfPlayer(UUID uuid) {
        int place = computePlace(uuid);
        String playerName = plugin.getCacheManager().resolvePlayerName(uuid);
        int rewardTop = manager.getRewardTopSize();
        int earnedPoints = rewardTop - place + 1;

        String unit = earnedPoints > 1
                ? config.getString("messages.chat.top.template_points.points")
                : config.getString("messages.chat.top.template_points.point");

        String pointsMsg = config.getString("messages.chat.top.template_points.display",
                String.valueOf(earnedPoints), unit);

        double sec = times.getOrDefault(uuid, 0.0);

        return config.getString("messages.chat.top.template",
                String.valueOf(place),
                playerName,
                String.valueOf(sec),
                getRewardMessageForPlace(place),
                pointsMsg);
    }

    private int computePlace(UUID uuid) {
        int i = 1;
        for (UUID u : order) {
            if (u.equals(uuid)) return i;
            i++;
        }
        return i;
    }

    private String getRewardMessageForPlace(int place) {
        return config.getString("rewards.top." + place + ".message", "");
    }

    @Override
    public boolean isCorrect(String input) {
        return type.isCorrect(input);
    }
}

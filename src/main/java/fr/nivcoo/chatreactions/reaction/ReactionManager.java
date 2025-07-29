package fr.nivcoo.chatreactions.reaction;

import fr.nivcoo.chatreactions.ChatReactions;
import fr.nivcoo.chatreactions.reaction.types.*;
import fr.nivcoo.utilsz.config.Config;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class ReactionManager {

    private final ChatReactions plugin;
    private final Config config;

    private final List<String> topConfig;
    private List<String> words;

    private Reaction currentReaction;
    private Thread reactionThread;
    private Timer reactionTimeout;

    private final List<ReactionTypeEntry> availableTypes = new ArrayList<>();

    public ReactionManager() {
        this.plugin = ChatReactions.get();
        this.config = plugin.getConfiguration();
        this.topConfig = config.getKeys("rewards.top");

        loadWords();
        loadReactionTypes();
        startReactionTask(false);
    }

    public void loadWords() {
        words = new ArrayList<>();
        try {
            File file = new File(plugin.getDataFolder(), config.getString("reaction_types.word.file_name"));
            Scanner scanner = new Scanner(file);
            while (scanner.hasNext()) {
                words.add(scanner.next());
            }
            scanner.close();
        } catch (FileNotFoundException ignored) {
        }
    }

    public List<String> getWords() {
        return words;
    }

    public int getRewardTopSize() {
        return topConfig.size();
    }

    public Reaction getCurrentReaction() {
        return currentReaction;
    }

    public void startReactionTask() {
        startReactionTask(true);
    }

    public void startReactionTask(boolean forceMode) {
        stopReactionTask();

        String threadName = "Reactions Start Thread";
        int timeLimit = config.getInt("time_limit");

        reactionThread = new Thread(() -> {
            boolean waitBeforeStart = !forceMode;

            while (!Thread.interrupted()) {
                try {
                    if (waitBeforeStart) {
                        int min = config.getInt("interval.min");
                        int max = config.getInt("interval.max");
                        int interval = new Random().nextInt(max - min) + min;
                        Thread.sleep(interval * 1000L);
                    }

                    if (currentReaction != null) continue;
                    if (config.getInt("player_needed") > Bukkit.getOnlinePlayers().size() && !forceMode) continue;

                    currentReaction = new Reaction();
                    currentReaction.start();
                    scheduleReactionEnd(threadName, timeLimit);
                    waitBeforeStart = true;

                } catch (InterruptedException ignored) {
                    break;
                }
            }
        }, threadName);

        reactionThread.start();
    }

    public void stopCurrentReaction() {
        if (currentReaction != null)
            currentReaction.stop();
        currentReaction = null;
    }

    public void stopReactionTask() {
        if (reactionThread != null)
            reactionThread.interrupt();
        stopCurrentReaction();
    }

    public void disablePlugin() {
        if (reactionTimeout != null)
            reactionTimeout.cancel();
        stopReactionTask();
    }

    public void sendConsoleCommand(String command, OfflinePlayer player) {
        Bukkit.getScheduler().runTask(plugin, () ->
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName())));
    }

    public String formatMultiline(List<String> list) {
        return String.join("\n", list);
    }

    public void scheduleReactionEnd(String threadName, int delaySeconds) {
        reactionTimeout = new Timer(threadName);
        reactionTimeout.schedule(new TimerTask() {
            @Override
            public void run() {
                stopCurrentReaction();
            }
        }, delaySeconds * 1000L);
    }

    private void loadReactionTypes() {
        availableTypes.clear();

        addIfEnabled(new WordReactionType());
        addIfEnabled(new ReversedWordReactionType());
        addIfEnabled(new MathReactionType());
        addIfEnabled(new RandomStringReactionType());

        plugin.getLogger().info("[ChatReactions] Loaded " + availableTypes.size() + " reaction types.");
    }

    private void addIfEnabled(ReactionTypeEntry type) {
        if (type.getWeight() > 0) {
            availableTypes.add(type);
        }
    }

    public ReactionTypeEntry selectRandomType() {
        if (availableTypes.isEmpty()) {
            return new WordReactionType();
        }

        int totalWeight = availableTypes.stream().mapToInt(ReactionTypeEntry::getWeight).sum();
        if (totalWeight != 100) {
            plugin.getLogger().warning("[ChatReactions] The total weight of reaction types is not 100%: " + totalWeight + "% Normalizing to 100%.");
        }

        int random = new Random().nextInt(100) + 1;
        int cumulative = 0;

        for (ReactionTypeEntry type : availableTypes) {
            cumulative += type.getWeight();
            if (random <= cumulative) {
                return type;
            }
        }
        return availableTypes.get(0);
    }
}

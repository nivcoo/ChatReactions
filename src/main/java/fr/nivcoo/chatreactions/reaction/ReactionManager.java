package fr.nivcoo.chatreactions.reaction;

import fr.nivcoo.chatreactions.ChatReactions;
import fr.nivcoo.chatreactions.reaction.types.*;
import fr.nivcoo.utilsz.config.Config;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class ReactionManager {

    private final ChatReactions plugin;
    private final Config config;

    private final List<String> topConfig;
    private List<String> words;

    private volatile Reaction currentReaction;
    private Thread reactionThread;
    private Timer reactionTimeout;

    private final List<ReactionTypeEntry> availableTypes = new ArrayList<>();

    public ReactionManager() {
        this.plugin = ChatReactions.get();
        this.config = plugin.getConfiguration();
        this.topConfig = config.getKeys("rewards.top");

        loadWords();
        loadReactionTypes();

    }

    public void installShadow(String answer) {
        if (plugin.isManager()) return;
        this.currentReaction = new ShadowReaction(answer);
    }

    public void clearShadow() {
        if (plugin.isManager()) return;
        this.currentReaction = null;
    }

    private static final class ShadowReaction extends Reaction {
        private final String answer;
        ShadowReaction(String answer) {
            super();
            this.answer = answer;
        }
        @Override public void start() { /* no-op shadow */ }
        @Override public void stop()  { /* no-op shadow */ }

        @Override
        public boolean isCorrect(String input) {
            return input != null && input.trim().equalsIgnoreCase(answer);
        }

        @Override
        public boolean tryAcceptLocal(java.util.UUID player, String input) {
            return false;
        }
    }

    public void loadWords() {
        words = new ArrayList<>();
        try {
            File file = new File(plugin.getDataFolder(), config.getString("reaction_types.word.file_name"));
            Scanner scanner = new Scanner(file);
            while (scanner.hasNext()) words.add(scanner.next());
            scanner.close();
        } catch (FileNotFoundException ignored) {}
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

        String threadName = "ChatReactions-Manager-Scheduler";
        int timeLimit = config.getInt("time_limit");

        reactionThread = new Thread(() -> {
            boolean waitBeforeStart = !forceMode;

            while (!Thread.interrupted()) {
                try {
                    if (waitBeforeStart) {
                        int min = Math.max(1, config.getInt("interval.min"));
                        int max = Math.max(min + 1, config.getInt("interval.max"));
                        int interval = new Random().nextInt(max - min) + min;
                        Thread.sleep(interval * 1000L);
                    }

                    if (!plugin.isManager()) continue;

                    if (currentReaction != null) continue;

                    currentReaction = new Reaction();
                    currentReaction.start();
                    scheduleReactionEnd(threadName, timeLimit);
                    waitBeforeStart = true;

                } catch (InterruptedException ignored) {
                    break;
                } catch (Throwable t) {
                    plugin.getLogger().warning("[ChatReactions] Scheduler error: " + t.getMessage());
                }
            }
        }, threadName);

        reactionThread.setDaemon(true);
        reactionThread.start();
    }

    public void stopCurrentReaction() {
        Reaction r = currentReaction;
        if (r != null) r.stop();
        currentReaction = null;

        if (reactionTimeout != null) {
            reactionTimeout.cancel();
            reactionTimeout = null;
        }
    }

    public void stopReactionTask() {
        if (reactionThread != null) {
            reactionThread.interrupt();
            reactionThread = null;
        }
        stopCurrentReaction();
    }

    public void disablePlugin() {
        if (reactionTimeout != null) reactionTimeout.cancel();
        stopReactionTask();
    }

    public void sendConsoleCommand(String command, String username) {
        Bukkit.getScheduler().runTask(plugin, () ->
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", username)));
    }

    public String formatMultiline(List<String> list) {
        return String.join("\n", list);
    }

    public String formatStartMessage(List<String> typeLines) {
        List<String> start = config.getStringList("messages.chat.start_messages.messages");
        List<String> out = new ArrayList<>();
        for (String line : start) {
            if (line.contains("{type_lines}")) out.addAll(typeLines);
            else out.add(line);
        }
        return String.join("\n", out);
    }

    public void scheduleReactionEnd(String threadName, int delaySeconds) {
        reactionTimeout = new Timer(threadName);
        reactionTimeout.schedule(new TimerTask() {
            @Override public void run() {
                stopCurrentReaction();
            }
        }, Math.max(1, delaySeconds) * 1000L);
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
        if (type.getWeight() > 0) availableTypes.add(type);
    }

    public ReactionTypeEntry selectRandomType() {
        if (availableTypes.isEmpty()) return new WordReactionType();

        int totalWeight = availableTypes.stream().mapToInt(ReactionTypeEntry::getWeight).sum();
        if (totalWeight != 100) {
            plugin.getLogger().warning("[ChatReactions] Reaction types weight != 100%: " + totalWeight + "%. Normalizing.");
        }

        int roll = new Random().nextInt(100) + 1;
        int cum = 0;
        for (ReactionTypeEntry t : availableTypes) {
            cum += t.getWeight();
            if (roll <= cum) return t;
        }
        return availableTypes.get(0);
    }
}

package fr.nivcoo.chatreactions.reaction;

import fr.nivcoo.chatreactions.ChatReactions;
import fr.nivcoo.chatreactions.reaction.types.*;
import fr.nivcoo.utilsz.config.Config;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class ReactionManager {

    private final ChatReactions plugin;
    private final Config config;

    private final List<String> topConfig;
    private List<String> words;

    private volatile ReactionLike currentReaction;
    private BukkitTask schedulerTask;
    private BukkitTask reactionTimeoutTask;

    private final List<ReactionTypeEntry> availableTypes = new ArrayList<>();

    public ReactionManager() {
        this.plugin = ChatReactions.get();
        this.config = plugin.getConfiguration();
        this.topConfig = config.getKeys("rewards.top");

        loadWords();
        loadReactionTypes();

        if (plugin.isManager()) startReactionTask(false);
    }

    public void installShadow(String answer) {
        if (plugin.isManager()) return;
        ShadowReaction sh = new ShadowReaction(answer);
        sh.start();
        this.currentReaction = sh;
    }

    public void clearShadow() {
        if (plugin.isManager()) return;
        ReactionLike r = this.currentReaction;
        if (r != null) r.stop();
        this.currentReaction = null;
    }

    public void loadWords() {
        words = new ArrayList<>();
        try {
            File file = new File(plugin.getDataFolder(), config.getString("reaction_types.word.file_name"));
            try (Scanner sc = new Scanner(file)) {
                while (sc.hasNext()) words.add(sc.next());
            }
        } catch (FileNotFoundException ignored) {}
    }

    public List<String> getWords() { return words; }
    public int getRewardTopSize() { return topConfig.size(); }
    public ReactionLike getCurrentReaction() { return currentReaction; }

    public void startReactionTask() { startReactionTask(true); }

    public void startReactionTask(boolean forceMode) {
        stopReactionTask();
        if (!plugin.isManager()) return;

        if (forceMode) {
            attemptStart();
        } else {
            scheduleNextStart();
        }
    }

    private void scheduleNextStart() {
        int min = Math.max(1, config.getInt("interval.min"));
        int max = Math.max(min + 1, config.getInt("interval.max"));
        int delaySec = ThreadLocalRandom.current().nextInt(min, max);
        schedulerTask = Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, this::attemptStart, delaySec * 20L);
    }

    private void attemptStart() {
        if (!plugin.isManager()) return;

        scheduleNextStart();

        if (currentReaction != null) return;

        Reaction real = new Reaction();
        currentReaction = real;
        real.start();

        int timeLimit = Math.max(1, config.getInt("time_limit"));
        scheduleReactionEnd(timeLimit);
    }

    public void stopCurrentReaction() {
        ReactionLike r = currentReaction;
        if (r != null) {
            try { r.stop(); } catch (Throwable ignored) {}
        }
        currentReaction = null;

        if (reactionTimeoutTask != null) {
            try { reactionTimeoutTask.cancel(); } catch (Throwable ignored) {}
            reactionTimeoutTask = null;
        }
    }

    public void stopReactionTask() {
        if (schedulerTask != null) {
            try { schedulerTask.cancel(); } catch (Throwable ignored) {}
            schedulerTask = null;
        }
        stopCurrentReaction();
    }

    public void disablePlugin() {
        stopReactionTask();
    }

    private void scheduleReactionEnd(int delaySeconds) {
        if (reactionTimeoutTask != null) {
            try { reactionTimeoutTask.cancel(); } catch (Throwable ignored) {}
            reactionTimeoutTask = null;
        }
        reactionTimeoutTask = Bukkit.getScheduler().runTaskLaterAsynchronously(
                plugin, this::stopCurrentReaction, Math.max(1, delaySeconds) * 20L
        );
    }

    public void sendConsoleCommand(String command, String username) {
        Bukkit.getScheduler().runTask(plugin, () ->
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", username)));
    }

    public String formatMultiline(List<String> list) { return String.join("\n", list); }

    public String formatStartMessage(List<String> typeLines) {
        List<String> start = config.getStringList("messages.chat.start_messages.messages");
        List<String> out = new ArrayList<>(start.size() + typeLines.size());
        for (String line : start) {
            if (line.contains("{type_lines}")) out.addAll(typeLines);
            else out.add(line);
        }
        return String.join("\n", out);
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
        try {
            if (type.getWeight() > 0) availableTypes.add(type);
        } catch (Throwable ex) {
            plugin.getLogger().warning("[ChatReactions] Ignored reaction type " +
                    type.getClass().getSimpleName() + ": " + ex.getMessage());
        }
    }

    public ReactionTypeEntry selectRandomType() {
        if (availableTypes.isEmpty()) return new WordReactionType();
        int total = availableTypes.stream().mapToInt(ReactionTypeEntry::getWeight).sum();
        if (total <= 0) return availableTypes.get(0);

        int roll = ThreadLocalRandom.current().nextInt(total) + 1;
        int acc = 0;
        for (ReactionTypeEntry t : availableTypes) {
            acc += t.getWeight();
            if (roll <= acc) return t;
        }
        return availableTypes.get(availableTypes.size() - 1);
    }
}

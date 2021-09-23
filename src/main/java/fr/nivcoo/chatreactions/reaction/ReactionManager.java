package fr.nivcoo.chatreactions.reaction;

import fr.nivcoo.chatreactions.ChatReactions;
import fr.nivcoo.utilsz.config.Config;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class ReactionManager implements Listener {

    private ChatReactions chatReactions;
    private Config config;
    private List<String> words;

    private Reaction currentReaction;

    private Thread reactionsThread;
    private Timer delayedReactionEndTimer;

    public ReactionManager() {
        chatReactions = ChatReactions.get();
        config = chatReactions.getConfiguration();
        loadWords();
        runReactionTasks(true);
    }

    public List<String> getWords() {
        return words;
    }

    public void runReactionTasks() {
        runReactionTasks(false);
    }

    public void runReactionTasks(boolean useInterval) {
        stopReactionTasks();
        String threadName = "Reactions Start Thread";

        int time_limit = config.getInt("time_limit");
        reactionsThread = new Thread(() -> {
            boolean useInter = useInterval;
            while (!Thread.interrupted()) {
                try {
                    if (useInter) {
                        int intervalMin = config.getInt("interval.min");
                        int intervalMax = config.getInt("interval.max");
                        Random r = new Random();
                        int interval = r.nextInt(intervalMax - intervalMin) + intervalMin;
                        Thread.sleep(interval * 1000L);
                    }
                    if (currentReaction != null)
                        continue;
                    currentReaction = new Reaction();
                    currentReaction.start();
                    startReactionEndTimer(threadName, time_limit);
                    useInter = true;
                } catch (InterruptedException ex) {
                    break;
                }
            }
        }, threadName);
        reactionsThread.start();

    }

    public int getRewardTopSize() {
        return config.getKeys("rewards.top").size();
    }

    public void stopCurrentReaction() {
        if (currentReaction != null)
            currentReaction.stop();
        currentReaction = null;
    }

    public void stopReactionTasks() {
        if (reactionsThread != null)
            reactionsThread.interrupt();
        stopCurrentReaction();
    }

    public void loadWords() {
        try {
            Scanner s = new Scanner(new File(chatReactions.getDataFolder() + "/" + config.getString("words_file_name")));
            words = new ArrayList<>();
            while (s.hasNext()) {
                words.add(s.next());
            }
            s.close();
        } catch (FileNotFoundException ignored) {
        }
    }

    public void sendConsoleCommand(String command, OfflinePlayer player) {
        Bukkit.getScheduler().runTask(chatReactions, () -> Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),
                command.replaceAll("%player%", player.getName())));

    }

    public void startReactionEndTimer(String threadName, int timeout) {
        delayedReactionEndTimer = new Timer(threadName);
        TimerTask task = new TimerTask() {
            public void run() {
                stopCurrentReaction();
            }
        };
        delayedReactionEndTimer.schedule(task, 1000L * timeout);
    }

    public void cancelDelayedReactionTaskTimer() {
        if (delayedReactionEndTimer != null)
            delayedReactionEndTimer.cancel();
    }

    public void disablePlugin() {
        cancelDelayedReactionTaskTimer();
        stopReactionTasks();
    }


    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent e) {
        if (currentReaction == null)
            return;
        Player p = e.getPlayer();
        if (!e.getMessage().equals(currentReaction.getWord()))
            return;
        e.setCancelled(currentReaction.addPlayerToTop(p));


    }
}

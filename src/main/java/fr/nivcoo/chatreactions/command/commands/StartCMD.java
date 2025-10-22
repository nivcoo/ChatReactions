package fr.nivcoo.chatreactions.command.commands;

import fr.nivcoo.chatreactions.ChatReactions;
import fr.nivcoo.chatreactions.command.CCommand;
import fr.nivcoo.utilsz.config.Config;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StartCMD implements CCommand {

    private final boolean managerRole;

    public StartCMD(boolean managerRole) {
        this.managerRole = managerRole;
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("start");
    }

    @Override
    public String getPermission() {
        return "chatreactions.command.start";
    }

    @Override
    public String getUsage() {
        return "start";
    }

    @Override
    public String getDescription() {
        return "Force le démarrage manuel d'une réaction.";
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public int getMaxArgs() {
        return 1;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public void execute(ChatReactions plugin, CommandSender sender, String[] args) {
        Config config = plugin.getConfiguration();

        String msgNoManager = config.getString("messages.commands.start.no_manager");
        String msgAlreadyRunning = config.getString("messages.commands.start.already_running");
        String msgSuccess = config.getString("messages.commands.start.success");

        if (!managerRole) {
            sender.sendMessage(msgNoManager);
            return;
        }

        if (plugin.getReactionManager().getCurrentReaction() != null) {
            sender.sendMessage(msgAlreadyRunning);
            return;
        }

        plugin.getReactionManager().startReactionTask(true);
        sender.sendMessage(msgSuccess);
    }

    @Override
    public List<String> tabComplete(ChatReactions plugin, CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}

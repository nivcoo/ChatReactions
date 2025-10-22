package fr.nivcoo.chatreactions.command.commands;

import fr.nivcoo.chatreactions.ChatReactions;
import fr.nivcoo.chatreactions.command.CCommand;
import fr.nivcoo.utilsz.config.Config;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StopCMD implements CCommand {

    private final boolean managerRole;

    public StopCMD(boolean managerRole) {
        this.managerRole = managerRole;
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("stop");
    }

    @Override
    public String getPermission() {
        return "chatreactions.command.stop";
    }

    @Override
    public String getUsage() {
        return "stop";
    }

    @Override
    public String getDescription() {
        return "Force l'arrêt immédiat de la réaction en cours.";
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

        String msgNoManager = config.getString("messages.commands.stop.no_manager");
        String msgNoReaction = config.getString("messages.commands.stop.no_reaction");
        String msgSuccess = config.getString("messages.commands.stop.success");

        if (!managerRole) {
            sender.sendMessage(msgNoManager);
            return;
        }

        if (plugin.getReactionManager().getCurrentReaction() == null) {
            sender.sendMessage(msgNoReaction);
            return;
        }

        plugin.getReactionManager().stopCurrentReaction();
        sender.sendMessage(msgSuccess);
    }

    @Override
    public List<String> tabComplete(ChatReactions plugin, CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}

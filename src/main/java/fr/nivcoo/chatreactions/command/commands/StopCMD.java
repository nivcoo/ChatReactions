package fr.nivcoo.chatreactions.command.commands;

import fr.nivcoo.chatreactions.ChatReactions;
import fr.nivcoo.chatreactions.command.CCommand;
import fr.nivcoo.utilsz.config.Config;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StopCMD implements CCommand {

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
        return null;
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
        return false;
    }

    public void execute(ChatReactions plugin, CommandSender sender, String[] args) {
        Config config = plugin.getConfiguration();
        plugin.getReactionManager().stopCurrentReaction();
        sender.sendMessage(config.getString("messages.commands.stop.success"));
    }

    @Override
    public List<String> tabComplete(ChatReactions plugin, CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

}

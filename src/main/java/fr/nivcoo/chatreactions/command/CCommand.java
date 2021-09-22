package fr.nivcoo.chatreactions.command;

import fr.nivcoo.chatreactions.ChatReactions;
import fr.nivcoo.chatreactions.utils.commands.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public interface CCommand extends Command {

    default void execute(JavaPlugin plugin, CommandSender sender, String[] args) {
        execute((ChatReactions) plugin, sender, args);
    }

    default List<String> tabComplete(JavaPlugin plugin, CommandSender sender, String[] args) {
        return tabComplete((ChatReactions) plugin, sender, args);
    }

    void execute(ChatReactions plugin, CommandSender sender, String[] args);

    List<String> tabComplete(ChatReactions plugin, CommandSender sender, String[] args);
}

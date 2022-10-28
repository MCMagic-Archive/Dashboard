package us.mcmagic.dashboard.commands;

import us.mcmagic.dashboard.Dashboard;
import us.mcmagic.dashboard.handlers.ChatColor;
import us.mcmagic.dashboard.handlers.MagicCommand;
import us.mcmagic.dashboard.handlers.Player;

public class Commandoc extends MagicCommand {

    @Override
    public void execute(Player player, String label, String[] args) {
        player.sendMessage(ChatColor.GREEN + "\nTotal Players Online: " + Dashboard.getOnlinePlayers().size() + "\n");
    }
}
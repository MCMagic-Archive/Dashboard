package us.mcmagic.dashboard.commands;

import us.mcmagic.dashboard.handlers.ChatColor;
import us.mcmagic.dashboard.handlers.MagicCommand;
import us.mcmagic.dashboard.handlers.Player;

public class Commandwhereami extends MagicCommand {

    @Override
    public void execute(Player player, String label, String[] args) {
        player.sendMessage(ChatColor.BLUE + "You are on the server " + ChatColor.GOLD + player.getServer());
    }
}
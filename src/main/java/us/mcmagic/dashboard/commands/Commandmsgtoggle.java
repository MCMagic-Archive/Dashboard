package us.mcmagic.dashboard.commands;

import us.mcmagic.dashboard.handlers.ChatColor;
import us.mcmagic.dashboard.handlers.MagicCommand;
import us.mcmagic.dashboard.handlers.Player;
import us.mcmagic.dashboard.handlers.Rank;

public class Commandmsgtoggle extends MagicCommand {

    public Commandmsgtoggle() {
        super(Rank.DEVELOPER);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        player.setRecieveMessages(!player.canRecieveMessages());
        if (player.canRecieveMessages()) {
            player.sendMessage(ChatColor.YELLOW + "You have " + ChatColor.GREEN + "enabled " + ChatColor.YELLOW +
                    "receiving private messages!");
        } else {
            player.sendMessage(ChatColor.YELLOW + "You have " + ChatColor.RED + "disabled " + ChatColor.YELLOW +
                    "receiving private messages!");
        }
    }
}
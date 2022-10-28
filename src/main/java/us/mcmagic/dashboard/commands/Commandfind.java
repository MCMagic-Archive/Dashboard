package us.mcmagic.dashboard.commands;

import us.mcmagic.dashboard.Dashboard;
import us.mcmagic.dashboard.handlers.ChatColor;
import us.mcmagic.dashboard.handlers.MagicCommand;
import us.mcmagic.dashboard.handlers.Player;
import us.mcmagic.dashboard.handlers.Rank;

public class Commandfind extends MagicCommand {

    public Commandfind() {
        super(Rank.EARNINGMYEARS);
        tabCompletePlayers = true;
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "/find [Player]");
            return;
        }
        Player tp = Dashboard.getPlayer(args[0]);
        if (tp == null) {
            player.sendMessage(ChatColor.RED + args[0] + " is not online!");
            return;
        }
        player.sendMessage(ChatColor.BLUE + tp.getName() + " is on the server " + ChatColor.GOLD + tp.getServer());
    }
}
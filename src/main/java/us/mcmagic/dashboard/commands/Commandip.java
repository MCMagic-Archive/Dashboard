package us.mcmagic.dashboard.commands;

import us.mcmagic.dashboard.Dashboard;
import us.mcmagic.dashboard.handlers.ChatColor;
import us.mcmagic.dashboard.handlers.MagicCommand;
import us.mcmagic.dashboard.handlers.Player;
import us.mcmagic.dashboard.handlers.Rank;

public class Commandip extends MagicCommand {

    public Commandip() {
        super(Rank.EARNINGMYEARS);
        tabCompletePlayers = true;
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "/ip [Player]");
            return;
        }
        Player tp = Dashboard.getPlayer(args[0]);
        if (tp == null) {
            player.sendMessage(ChatColor.RED + "That player wasn't found!");
            return;
        }
        player.sendMessage(ChatColor.GREEN + "IP of " + tp.getName() + " is " + tp.getAddress());
    }
}
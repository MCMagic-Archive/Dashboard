package us.mcmagic.dashboard.commands;

import us.mcmagic.dashboard.Dashboard;
import us.mcmagic.dashboard.handlers.ChatColor;
import us.mcmagic.dashboard.handlers.MagicCommand;
import us.mcmagic.dashboard.handlers.Player;
import us.mcmagic.dashboard.handlers.Rank;

public class Commandcc extends MagicCommand {
    public String clearMessage = "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n";

    public Commandcc() {
        super(Rank.EARNINGMYEARS);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        String server = player.getServer();
        boolean park = Dashboard.getServer(server).isPark();
        for (Player tp : Dashboard.getOnlinePlayers()) {
            if (tp.getServer().equals(server) || (park && Dashboard.getServer(tp.getServer()).isPark())) {
                if (tp.getRank().getRankId() < Rank.EARNINGMYEARS.getRankId()) {
                    tp.sendMessage(clearMessage + ChatColor.DARK_AQUA + "Chat has been cleared");
                } else {
                    tp.sendMessage("\n" + ChatColor.DARK_AQUA + "Chat has been cleared by " + player.getName());
                }
            }
        }
    }
}
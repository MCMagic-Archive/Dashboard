package us.mcmagic.dashboard.commands;

import us.mcmagic.dashboard.Dashboard;
import us.mcmagic.dashboard.handlers.ChatColor;
import us.mcmagic.dashboard.handlers.MagicCommand;
import us.mcmagic.dashboard.handlers.Player;
import us.mcmagic.dashboard.handlers.Rank;

public class Commandho extends MagicCommand {

    public Commandho() {
        super(Rank.DEVELOPER);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        if (args.length > 0) {
            String message = "";
            for (String arg : args) {
                message += arg + " ";
            }
            for (Player tp : Dashboard.getOnlinePlayers()) {
                if (tp.getRank().getRankId() >= Rank.DEVELOPER.getRankId()) {
                    tp.sendMessage(ChatColor.RED + "[ADMIN CHAT] " + ChatColor.GRAY + player.getName() + ": " +
                            ChatColor.WHITE + ChatColor.translateAlternateColorCodes('&', message));
                }
            }
            return;
        }
        player.sendMessage(ChatColor.RED + "/ho [Message]");
    }
}
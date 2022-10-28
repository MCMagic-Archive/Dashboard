package us.mcmagic.dashboard.commands;

import us.mcmagic.dashboard.Dashboard;
import us.mcmagic.dashboard.handlers.ChatColor;
import us.mcmagic.dashboard.handlers.MagicCommand;
import us.mcmagic.dashboard.handlers.Player;
import us.mcmagic.dashboard.handlers.Rank;

public class Commandb extends MagicCommand {

    public Commandb() {
        super(Rank.CASTMEMBER);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        if (args.length > 0) {
            String message = "";
            for (String arg : args) {
                message += arg + " ";
            }
            String sname = player.getName();
            String msg = ChatColor.WHITE + "[" + ChatColor.AQUA + "Information" +
                    ChatColor.WHITE + "] " + ChatColor.GREEN + ChatColor.translateAlternateColorCodes('&', message);
            String staff = ChatColor.WHITE + "[" + ChatColor.AQUA +
                    sname + ChatColor.WHITE + "] " + ChatColor.GREEN +
                    ChatColor.translateAlternateColorCodes('&', message);
            for (Player tp : Dashboard.getOnlinePlayers()) {
                if (Dashboard.getPlayer(tp.getUniqueId()).getRank().getRankId() >= Rank.CASTMEMBER.getRankId()) {
                    tp.sendMessage(staff);
                } else {
                    tp.sendMessage(msg);
                }
            }
            return;
        }
        player.sendMessage(ChatColor.RED + "/b [Message]");
    }
}
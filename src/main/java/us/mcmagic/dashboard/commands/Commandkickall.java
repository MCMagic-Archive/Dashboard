package us.mcmagic.dashboard.commands;

import us.mcmagic.dashboard.Dashboard;
import us.mcmagic.dashboard.handlers.ChatColor;
import us.mcmagic.dashboard.handlers.MagicCommand;
import us.mcmagic.dashboard.handlers.Player;
import us.mcmagic.dashboard.handlers.Rank;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Marc on 10/3/16
 */
public class Commandkickall extends MagicCommand {

    public Commandkickall() {
        super(Rank.DEVELOPER);
    }

    @Override
    public void execute(final Player player, String label, String[] args) {
        if (args.length <= 0) {
            player.sendMessage(ChatColor.RED + "/kickall [Message]");
            return;
        }
        String r = "";
        for (String arg : args) {
            r += arg + " ";
        }
        r = ChatColor.translateAlternateColorCodes('&', r.trim());
        player.sendMessage(ChatColor.GREEN + "Disconnecting all players for " + r);
        for (Player tp : Dashboard.getOnlinePlayers()) {
            if (tp.getRank().getRankId() >= Rank.DEVELOPER.getRankId()) {
                continue;
            }
            tp.kickPlayer(r);
        }
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                boolean empty = true;
                for (Player tp : Dashboard.getOnlinePlayers()) {
                    if (tp.getRank().getRankId() < Rank.DEVELOPER.getRankId()) {
                        empty = false;
                        break;
                    }
                }
                if (empty) {
                    player.sendMessage(ChatColor.GREEN + "All players have been disconnected!");
                    cancel();
                }
            }
        }, 0, 1000);
    }
}
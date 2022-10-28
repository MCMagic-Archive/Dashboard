package us.mcmagic.dashboard.commands;

import us.mcmagic.dashboard.Dashboard;
import us.mcmagic.dashboard.handlers.ChatColor;
import us.mcmagic.dashboard.handlers.MagicCommand;
import us.mcmagic.dashboard.handlers.Player;
import us.mcmagic.dashboard.handlers.Rank;
import us.mcmagic.dashboard.utils.NameUtil;

import java.util.List;

public class Commandnamecheck extends MagicCommand {

    public Commandnamecheck() {
        super(Rank.EARNINGMYEARS);
        tabCompletePlayers = true;
    }

    @Override
    public void execute(final Player player, String label, final String[] args) {
        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "/namecheck [username]");
            return;
        }
        player.sendMessage(ChatColor.GREEN + "Requesting name history for " + ChatColor.AQUA + args[0] +
                ChatColor.GREEN + " from Mojang...");
        Dashboard.schedulerManager.runAsync(new Runnable() {
            @Override
            public void run() {
                List<String> names = NameUtil.getNames(args[0]);
                if (names.size() < 2 || names.isEmpty()) {
                    player.sendMessage(ChatColor.RED + "That user could not be found!");
                    return;
                }
                String msg = ChatColor.GREEN + "Previous usernames of " + args[0] + " are:";
                for (int i = 1; i < names.size(); i++) {
                    msg += ChatColor.AQUA + "\n- " + ChatColor.GREEN + names.get(i);
                }
                player.sendMessage(msg);
            }
        });
    }
}
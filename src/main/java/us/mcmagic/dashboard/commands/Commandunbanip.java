package us.mcmagic.dashboard.commands;

import us.mcmagic.dashboard.Dashboard;
import us.mcmagic.dashboard.handlers.ChatColor;
import us.mcmagic.dashboard.handlers.MagicCommand;
import us.mcmagic.dashboard.handlers.Player;
import us.mcmagic.dashboard.handlers.Rank;

import java.util.Arrays;

public class Commandunbanip extends MagicCommand {

    public Commandunbanip() {
        super(Rank.CASTMEMBER);
        aliases = Arrays.asList("pardonip", "pardon-ip");
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "/unbanip [IP Address]");
            return;
        }
        String address = args[0];
        Dashboard.sqlUtil.unbanIP(address);
        Dashboard.moderationUtil.announceUnban("IP " + address, player.getName());
    }
}
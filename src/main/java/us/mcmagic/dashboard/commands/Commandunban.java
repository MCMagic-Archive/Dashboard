package us.mcmagic.dashboard.commands;

import us.mcmagic.dashboard.Dashboard;
import us.mcmagic.dashboard.handlers.ChatColor;
import us.mcmagic.dashboard.handlers.MagicCommand;
import us.mcmagic.dashboard.handlers.Player;
import us.mcmagic.dashboard.handlers.Rank;

import java.util.Arrays;
import java.util.UUID;

public class Commandunban extends MagicCommand {

    public Commandunban() {
        super(Rank.CASTMEMBER);
        aliases = Arrays.asList("pardon");
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "/unban [Username]");
            return;
        }
        String username = args[0];
        UUID uuid = Dashboard.sqlUtil.uuidFromUsername(username);
        if (uuid == null) {
            player.sendMessage(ChatColor.RED + "Player not found!");
            return;
        }
        Dashboard.sqlUtil.unbanPlayer(uuid);
        Dashboard.moderationUtil.announceUnban(username, player.getName());
    }
}
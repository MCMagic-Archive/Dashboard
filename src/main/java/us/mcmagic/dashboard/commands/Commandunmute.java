package us.mcmagic.dashboard.commands;

import us.mcmagic.dashboard.Dashboard;
import us.mcmagic.dashboard.handlers.ChatColor;
import us.mcmagic.dashboard.handlers.MagicCommand;
import us.mcmagic.dashboard.handlers.Player;
import us.mcmagic.dashboard.handlers.Rank;

import java.util.UUID;

public class Commandunmute extends MagicCommand {

    public Commandunmute() {
        super(Rank.CASTMEMBER);
        tabCompletePlayers = true;
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "/unmute [Username]");
            return;
        }
        String username = args[0];
        Player tp = Dashboard.getPlayer(username);
        UUID uuid;
        if (tp == null) {
            uuid = Dashboard.sqlUtil.uuidFromUsername(username);
        } else {
            uuid = tp.getUniqueId();
            username = tp.getName();
        }
        if (uuid == null) {
            player.sendMessage(ChatColor.RED + "Player not found!");
            return;
        }
        Dashboard.sqlUtil.unmutePlayer(uuid);
        tp.getMute().setMuted(false);
        Dashboard.moderationUtil.announceUnmute(username, player.getName());
    }
}
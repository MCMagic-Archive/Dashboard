package us.mcmagic.dashboard.commands;

import us.mcmagic.dashboard.Dashboard;
import us.mcmagic.dashboard.handlers.ChatColor;
import us.mcmagic.dashboard.handlers.MagicCommand;
import us.mcmagic.dashboard.handlers.Player;
import us.mcmagic.dashboard.handlers.Rank;

/**
 * Created by Marc on 9/24/16
 */
public class Commandchatreload extends MagicCommand {

    public Commandchatreload() {
        super(Rank.CASTMEMBER);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        player.sendMessage(ChatColor.GREEN + "Reloading chat settings...");
        Dashboard.chatUtil.reload();
        Dashboard.schedulerManager.getBroadcastClock().reload();
        player.sendMessage(ChatColor.GREEN + "Chat settings reloaded!");
    }
}
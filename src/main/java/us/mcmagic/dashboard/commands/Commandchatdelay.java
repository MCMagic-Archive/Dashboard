package us.mcmagic.dashboard.commands;

import us.mcmagic.dashboard.Dashboard;
import us.mcmagic.dashboard.handlers.ChatColor;
import us.mcmagic.dashboard.handlers.MagicCommand;
import us.mcmagic.dashboard.handlers.Player;
import us.mcmagic.dashboard.handlers.Rank;

public class Commandchatdelay extends MagicCommand {

    public Commandchatdelay() {
        super(Rank.CASTMEMBER);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        if (args.length == 0) {
            player.sendMessage(ChatColor.GREEN + "The chat delay is currently " +
                    (Dashboard.chatUtil.getChatDelay() / 1000) + " seconds!");
            player.sendMessage(ChatColor.GREEN + "Change delay: /chatdelay [Time]");
            return;
        }
        try {
            int time = Integer.parseInt(args[0]);
            Dashboard.chatUtil.setChatDelay(time * 1000);
            Dashboard.moderationUtil.changeChatDelay(time, player.getName());
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Please use a whole number :)");
        }
    }
}
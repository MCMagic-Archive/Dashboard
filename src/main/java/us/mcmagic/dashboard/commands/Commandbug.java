package us.mcmagic.dashboard.commands;

import us.mcmagic.dashboard.handlers.ChatColor;
import us.mcmagic.dashboard.handlers.MagicCommand;
import us.mcmagic.dashboard.handlers.Player;
import us.mcmagic.dashboard.packets.dashboard.PacketLink;

/**
 * Created by Marc on 9/27/16
 */
public class Commandbug extends MagicCommand {

    @Override
    public void execute(Player player, String label, String[] args) {
        PacketLink packet = new PacketLink(player.getUniqueId(), "https://goo.gl/sMMiYZ", "Click to report a bug",
                ChatColor.YELLOW, true);
        player.send(packet);
    }
}
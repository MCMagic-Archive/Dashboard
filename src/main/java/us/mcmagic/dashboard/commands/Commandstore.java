package us.mcmagic.dashboard.commands;

import us.mcmagic.dashboard.handlers.ChatColor;
import us.mcmagic.dashboard.handlers.MagicCommand;
import us.mcmagic.dashboard.handlers.Player;
import us.mcmagic.dashboard.packets.dashboard.PacketLink;

/**
 * Created by Marc on 9/22/16
 */
public class Commandstore extends MagicCommand {

    @Override
    public void execute(Player player, String label, String[] args) {
        PacketLink packet = new PacketLink(player.getUniqueId(), "https://store.mcmagic.us", "Click to visit our store",
                ChatColor.YELLOW, true);
        player.send(packet);
    }
}
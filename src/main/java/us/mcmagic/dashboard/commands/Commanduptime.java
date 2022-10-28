package us.mcmagic.dashboard.commands;

import us.mcmagic.dashboard.Dashboard;
import us.mcmagic.dashboard.handlers.MagicCommand;
import us.mcmagic.dashboard.handlers.Player;
import us.mcmagic.dashboard.packets.dashboard.PacketUptimeCommand;

/**
 * Created by Marc on 8/26/16
 */
public class Commanduptime extends MagicCommand {

    @Override
    public void execute(Player player, String label, String[] args) {
        PacketUptimeCommand packet = new PacketUptimeCommand(player.getUniqueId(), Dashboard.getStartTime());
        player.send(packet);
    }
}
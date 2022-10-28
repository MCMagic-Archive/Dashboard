package us.mcmagic.dashboard.commands;

import us.mcmagic.dashboard.Dashboard;
import us.mcmagic.dashboard.handlers.ChatColor;
import us.mcmagic.dashboard.handlers.MagicCommand;
import us.mcmagic.dashboard.handlers.Player;
import us.mcmagic.dashboard.handlers.Rank;
import us.mcmagic.dashboard.packets.dashboard.PacketConnectionType;
import us.mcmagic.dashboard.packets.dashboard.PacketUpdateMOTD;
import us.mcmagic.dashboard.server.DashboardSocketChannel;
import us.mcmagic.dashboard.server.WebSocketServerHandler;

/**
 * Created by Marc on 8/26/16
 */
public class Commandmotdrl extends MagicCommand {

    public Commandmotdrl() {
        super(Rank.CASTMEMBER);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        player.sendMessage(ChatColor.GREEN + "Loading MOTD from file...");
        Dashboard.loadMOTD();
        player.sendMessage(ChatColor.GREEN + "MOTD Loaded! Notifying Bungees...");
        PacketUpdateMOTD packet = new PacketUpdateMOTD(Dashboard.getMOTD(), Dashboard.getMOTDMaintenance(),
                Dashboard.getInfo());
        for (Object o : WebSocketServerHandler.getGroup()) {
            DashboardSocketChannel dash = (DashboardSocketChannel) o;
            if (!dash.getType().equals(PacketConnectionType.ConnectionType.BUNGEECORD)) {
                continue;
            }
            dash.send(packet);
        }
        player.sendMessage(ChatColor.GREEN + "All Bungees have been notified!");
    }
}

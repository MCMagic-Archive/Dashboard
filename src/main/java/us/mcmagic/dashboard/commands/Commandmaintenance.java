package us.mcmagic.dashboard.commands;

import us.mcmagic.dashboard.Dashboard;
import us.mcmagic.dashboard.handlers.ChatColor;
import us.mcmagic.dashboard.handlers.MagicCommand;
import us.mcmagic.dashboard.handlers.Player;
import us.mcmagic.dashboard.handlers.Rank;
import us.mcmagic.dashboard.packets.dashboard.PacketConnectionType;
import us.mcmagic.dashboard.packets.dashboard.PacketMaintenance;
import us.mcmagic.dashboard.packets.dashboard.PacketMaintenanceWhitelist;
import us.mcmagic.dashboard.server.DashboardSocketChannel;
import us.mcmagic.dashboard.server.WebSocketServerHandler;

import java.util.*;

/**
 * Created by Marc on 9/12/16
 */
public class Commandmaintenance extends MagicCommand {

    @Override
    public void execute(final Player player, String label, String[] args) {
        Dashboard.setMaintenance(!Dashboard.isMaintenance());
        PacketMaintenance packet = new PacketMaintenance(Dashboard.isMaintenance());
        if (Dashboard.isMaintenance()) {
            HashMap<Rank, List<UUID>> staff = Dashboard.sqlUtil.getPlayersByRanks(Rank.EARNINGMYEARS, Rank.CASTMEMBER,
                    Rank.COORDINATOR, Rank.DEVELOPER, Rank.MANAGER, Rank.MAYOR, Rank.OWNER);
            List<UUID> list = new ArrayList<>();
            for (Map.Entry<Rank, List<UUID>> entry : staff.entrySet()) {
                for (UUID uuid : entry.getValue()) {
                    list.add(uuid);
                }
            }
            Dashboard.setMaintenanceWhitelist(list);
            PacketMaintenanceWhitelist whitelist = new PacketMaintenanceWhitelist(list);
            player.sendMessage(ChatColor.GREEN + "Maintenance Mode enabled! Notifying Bungees...");
            for (Object o : WebSocketServerHandler.getGroup()) {
                DashboardSocketChannel bungee = (DashboardSocketChannel) o;
                if (!bungee.getType().equals(PacketConnectionType.ConnectionType.BUNGEECORD)) {
                    continue;
                }
                bungee.send(packet);
                bungee.send(whitelist);
            }
            player.sendMessage(ChatColor.GREEN + "Bungees notified! Disconnecting all Guests...");
            for (Player tp : Dashboard.getOnlinePlayers()) {
                if (tp.getRank().getRankId() >= Rank.EARNINGMYEARS.getRankId()) {
                    continue;
                }
                tp.kickPlayer(ChatColor.AQUA + "Maintenance Mode has been enabled!\nFollow " + ChatColor.BLUE +
                        "@MCMagicDev " + ChatColor.AQUA + "on Twitter for updates.");
            }
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    boolean guests = false;
                    for (Player tp : Dashboard.getOnlinePlayers()) {
                        if (tp.getRank().getRankId() < Rank.EARNINGMYEARS.getRankId()) {
                            guests = true;
                            return;
                        }
                    }
                    player.sendMessage(ChatColor.GREEN + "All Guests have been disconnected!");
                    cancel();
                }
            }, 0L, 1000L);
        } else {
            player.sendMessage(ChatColor.GREEN + "Maintenance Mode " + ChatColor.RED + "disabled! " +
                    ChatColor.GREEN + "Notifying Bungees...");
            for (Object o : WebSocketServerHandler.getGroup()) {
                DashboardSocketChannel bungee = (DashboardSocketChannel) o;
                if (!bungee.getType().equals(PacketConnectionType.ConnectionType.BUNGEECORD)) {
                    continue;
                }
                bungee.send(packet);
            }
            player.sendMessage(ChatColor.GREEN + "Bungees notified!");
        }
    }
}
package us.mcmagic.dashboard.commands;

import us.mcmagic.dashboard.Dashboard;
import us.mcmagic.dashboard.handlers.ChatColor;
import us.mcmagic.dashboard.handlers.MagicCommand;
import us.mcmagic.dashboard.handlers.Player;
import us.mcmagic.dashboard.handlers.Rank;
import us.mcmagic.dashboard.packets.dashboard.PacketStartReboot;
import us.mcmagic.dashboard.server.DashboardSocketChannel;
import us.mcmagic.dashboard.server.WebSocketServerHandler;

import java.util.Timer;
import java.util.TimerTask;

public class Commandreboot extends MagicCommand {

    public Commandreboot() {
        super(Rank.DEVELOPER);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        player.sendMessage(ChatColor.GREEN + "Starting shutdown of Dashboard...");
        PacketStartReboot packet = new PacketStartReboot();
        for (Object o : WebSocketServerHandler.getGroup()) {
            DashboardSocketChannel bungee = (DashboardSocketChannel) o;
            bungee.send(packet);
        }
        player.sendMessage(ChatColor.GREEN + "Bungees notified, disconnecting " + Dashboard.getOnlinePlayers().size()
                + " players...");
        for (Player tp : Dashboard.getOnlinePlayers()) {
            tp.kickPlayer(ChatColor.AQUA + "Please Pardon our Pixie Dust! We are restarting our servers right now.");
        }
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (Dashboard.getOnlinePlayers().isEmpty()) {
                    cancel();
                    System.exit(0);
                }
            }
        }, 1000);
    }
}
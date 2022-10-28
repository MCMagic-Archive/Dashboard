package us.mcmagic.dashboard.server;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.concurrent.GlobalEventExecutor;
import us.mcmagic.dashboard.Dashboard;
import us.mcmagic.dashboard.handlers.*;
import us.mcmagic.dashboard.packets.audio.PacketContainer;
import us.mcmagic.dashboard.packets.audio.PacketGetPlayer;
import us.mcmagic.dashboard.packets.audio.PacketPlayerInfo;
import us.mcmagic.dashboard.packets.dashboard.*;
import us.mcmagic.dashboard.packets.park.*;
import us.mcmagic.dashboard.slack.SlackAttachment;
import us.mcmagic.dashboard.slack.SlackMessage;

import java.util.*;

/**
 * Created by Marc on 6/15/15
 */
public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {

    private static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private WebSocketServerHandshaker handshaker;

    public static ChannelGroup getGroup() {
        return channels;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        channels.add(ctx.channel());
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(null, null, true);
        handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            handshaker.handshake(ctx.channel(), req);
            final DashboardSocketChannel channel = (DashboardSocketChannel) ctx.channel();
        }
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        if (frame instanceof PongWebSocketFrame) {
            return;
        }
        if (!(frame instanceof TextWebSocketFrame)) {
            throw new UnsupportedOperationException(String.format("%s frame types not supported",
                    frame.getClass().getName()));
        }
        String request = ((TextWebSocketFrame) frame).text();
        JsonObject object = (JsonObject) new JsonParser().parse(request);
        if (!object.has("id")) {
            return;
        }
        int id = object.get("id").getAsInt();
        Dashboard.getLogger().info(object.toString());
        DashboardSocketChannel channel = (DashboardSocketChannel) ctx.channel();
        switch (id) {
            /**
             * GetPlayer (Audio)
             */
            case 13: {
                PacketGetPlayer packet = new PacketGetPlayer().fromJSON(object);
                String username = packet.getPlayerName();
                Player tp = Dashboard.getPlayer(username);
                PacketPlayerInfo info;
                if (tp == null || tp.getAudioAuth() == -1) {
                    info = new PacketPlayerInfo(null, username, 0, "");
                } else {
                    info = new PacketPlayerInfo(tp.getUniqueId(), username, tp.getAudioAuth(), tp.getServer());
                    tp.resetAudioAuth();
                    try {
                        PacketAudioConnect connect = new PacketAudioConnect(tp.getUniqueId());
                        Dashboard.getInstance(tp.getServer()).send(connect);
                    } catch (Exception ignored) {
                    }
                }
                channel.send(info);
                break;
            }
            /**
             * AudioServer Packet (Container)
             */
            case 17: {
                PacketContainer packet = new PacketContainer().fromJSON(object);
                for (Object o : WebSocketServerHandler.getGroup()) {
                    DashboardSocketChannel dash = (DashboardSocketChannel) o;
                    if (!dash.getType().equals(PacketConnectionType.ConnectionType.AUDIOSERVER)) {
                        continue;
                    }
                    dash.send(packet);
                }
                break;
            }
            /**
             * ConnectionType
             */
            case 22: {
                PacketConnectionType packet = new PacketConnectionType().fromJSON(object);
                PacketConnectionType.ConnectionType type = packet.getType();
                channel.setType(type);
                switch (type) {
                    case BUNGEECORD: {
                        Dashboard.moderationUtil.sendMessage(ChatColor.GREEN +
                                "A new BungeeCord instance has connected to Dashboard.");
                        SlackMessage m = new SlackMessage("");
                        SlackAttachment a = new SlackAttachment("A new BungeeCord instance has connected to Dashboard from the IP Address " +
                                channel.remoteAddress().getAddress().toString());
                        a.color("good");
                        Dashboard.slackUtil.sendDashboardMessage(m, Arrays.asList(a));
                        break;
                    }
                    case DAEMON: {
                        Dashboard.moderationUtil.sendMessage(ChatColor.GREEN +
                                "A new daemon has connected to Dashboard.");
                        SlackMessage m = new SlackMessage("");
                        SlackAttachment a = new SlackAttachment("A new daemon has connected to Dashboard from the IP Address " +
                                channel.remoteAddress().getAddress().toString());
                        a.color("good");
                        Dashboard.slackUtil.sendDashboardMessage(m, Arrays.asList(a));
                        break;
                    }
                    case WEBCLIENT: {
                        break;
                    }
                    case INSTANCE: {
                        break;
                    }
                    case AUDIOSERVER: {
                        Dashboard.moderationUtil.sendMessage(ChatColor.GREEN +
                                "The Audio Server has connected to Dashboard.");
                        SlackMessage m = new SlackMessage("");
                        SlackAttachment a = new SlackAttachment("The Audio Server has connected to Dashboard from the IP Address " +
                                channel.remoteAddress().getAddress().toString());
                        a.color("good");
                        Dashboard.slackUtil.sendDashboardMessage(m, Arrays.asList(a));
                        break;
                    }
                }
                Dashboard.getLogger().info("New " + type.name().toLowerCase() + " connection");
                if (type.equals(PacketConnectionType.ConnectionType.BUNGEECORD)) {
                    PacketUpdateMOTD motd = new PacketUpdateMOTD(Dashboard.getMOTD(), Dashboard.getMOTDMaintenance(),
                            Dashboard.getInfo());
                    PacketOnlineCount count = new PacketOnlineCount(Dashboard.getOnlinePlayers().size());
                    List<String> servers = new ArrayList<>();
                    for (Server s : Dashboard.getServers()) {
                        servers.add(s.getName() + ":" + s.getAddress() + ":" + s.getPort());
                    }
                    PacketServerList server = new PacketServerList(servers);
                    PacketTargetLobby lobby = new PacketTargetLobby(Dashboard.getTargetServer());
                    PacketCommandList commands = new PacketCommandList(new ArrayList<>(Dashboard.commandUtil.getCommandsAndAliases()));
                    PacketMaintenance maintenance = new PacketMaintenance(Dashboard.isMaintenance());
                    channel.send(motd);
                    channel.send(count);
                    channel.send(server);
                    channel.send(lobby);
                    channel.send(commands);
                    channel.send(maintenance);
                    if (Dashboard.isMaintenance()) {
                        PacketMaintenanceWhitelist whitelist = new PacketMaintenanceWhitelist(Dashboard.getMaintenanceWhitelist());
                        channel.send(whitelist);
                    }
                }
                if (type.equals(PacketConnectionType.ConnectionType.INSTANCE)) {
                    PacketOnlineCount count = new PacketOnlineCount(Dashboard.getOnlinePlayers().size());
                    channel.send(count);
                }
                break;
            }
            /**
             * PlayerJoin
             */
            case 23: {
                PacketPlayerJoin packet = new PacketPlayerJoin().fromJSON(object);
                Player player = new Player(packet.getUniqueId(), packet.getUsername(), packet.getAddress(),
                        packet.getServer(), channel.getBungeeID());
                Dashboard.sqlUtil.login(player);
                break;
            }
            /**
             * PlayerDisconnect
             */
            case 24: {
                PacketPlayerDisconnect packet = new PacketPlayerDisconnect().fromJSON(object);
                Dashboard.logout(packet.getUniqueId());
                break;
            }
            /**
             * PlayerChat
             */
            case 25: {
                PacketPlayerChat packet = new PacketPlayerChat().fromJSON(object);
                Dashboard.chatUtil.chatEvent(packet);
                break;
            }
            /**
             * Message
             */
            case 26: {
                PacketMessage packet = new PacketMessage().fromJSON(object);
                UUID uuid = packet.getUniqueId();
                String msg = packet.getMessage();
                Player tp = Dashboard.getPlayer(uuid);
                if (tp == null) {
                    return;
                }
                tp.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
                break;
            }
            /**
             * Server Switch
             */
            case 27: {
                PacketServerSwitch packet = new PacketServerSwitch().fromJSON(object);
                UUID uuid = packet.getUniqueId();
                String target = packet.getTarget();
                final Player tp = Dashboard.getPlayer(uuid);
                if (tp == null) {
                    return;
                }
                // New connection
                if (Dashboard.serverUtil.getServer(tp.getServer()) == null) {
                    Dashboard.serverUtil.getServer(target).changeCount(1);
                    tp.setServer(target);
                    if (Dashboard.getServer(target).isPark()) {
                        tp.setInventoryUploaded(false);
                        PacketInventoryStatus update = new PacketInventoryStatus(tp.getUniqueId(), 1);
                        try {
                            Dashboard.getInstance(target).send(update);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (tp.isNewGuest()) {
                        Timer tutorial = new Timer();
                        tutorial.schedule(new TimerTask() {
                            int i = 0;

                            @Override
                            public void run() {
                                switch (i) {
                                    case 0: {
                                        tp.sendMessage(ChatColor.GREEN + "\nWelcome to " + ChatColor.AQUA + "MCMagic" +
                                                ChatColor.GREEN + ", we're happy you're here!");
                                        tp.mention();
                                        break;
                                    }
                                    case 4: {
                                        tp.sendMessage(ChatColor.GREEN + "\nWe have recreated " + ChatColor.AQUA +
                                                "Walt Disney World " + ChatColor.GREEN + "in Minecraft!");
                                        tp.mention();
                                        break;
                                    }
                                    case 7: {
                                        tp.sendMessage(ChatColor.GREEN + "\nRight now you're at the " + ChatColor.AQUA +
                                                "Transportation and Ticket Center. " + ChatColor.GREEN +
                                                "From here, you can board a monorail, bus or ferryboat to all Parks and Resorts.");
                                        tp.mention();
                                        break;
                                    }
                                    case 15: {
                                        tp.sendMessage(ChatColor.GREEN + "\nYou can also use your " + ChatColor.AQUA +
                                                "MagicBand " + ChatColor.GREEN + "to navigate to different parts of " +
                                                "our server such as Creative or the Arcade");
                                        tp.mention();
                                        break;
                                    }
                                    case 22: {
                                        tp.sendMessage(ChatColor.GREEN + "\nInstall our Resource Pack for the " +
                                                ChatColor.AQUA + "best " + ChatColor.GREEN +
                                                "experience possible! All you have to do is type " + ChatColor.AQUA +
                                                "/pack " + ChatColor.GREEN + "and select " + ChatColor.LIGHT_PURPLE +
                                                "Main. " + ChatColor.GRAY + "" + ChatColor.ITALIC +
                                                "(You can set this up when the tutorial finishes)");
                                        tp.mention();
                                        break;
                                    }
                                    case 32: {
                                        tp.sendMessage(ChatColor.GREEN + "\nAlso, connect to our " + ChatColor.BLUE +
                                                "Audio Server " + ChatColor.GREEN + "for an immersive experience! You will hear the " +
                                                ChatColor.AQUA + "sounds from rides, music from shows, and so much more! " +
                                                ChatColor.GREEN + "Just type " + ChatColor.AQUA + "/audio " + ChatColor.GREEN +
                                                "and click the message to connect. " + ChatColor.GRAY + "" + ChatColor.ITALIC +
                                                "(You can set this up when the tutorial finishes)");
                                        tp.mention();
                                        break;
                                    }
                                    case 43: {
                                        tp.sendMessage(ChatColor.GREEN + "\nBefore you start exploring, please take a " +
                                                "few minutes to review our rules: " + ChatColor.AQUA +
                                                "mcmagic.us/rules " + ChatColor.GREEN + "\nWe are a " +
                                                "family-friendly server with a goal of providing a safe, fun experience " +
                                                "to all of our Guests.");
                                        tp.mention();
                                        break;
                                    }
                                    case 52: {
                                        tp.sendMessage(ChatColor.GREEN + "\nAfter you finish reviewing our rules, " +
                                                "you're finished with the tutorial! " + ChatColor.DARK_AQUA +
                                                "Note: New Guests must wait " + ChatColor.BOLD + "15 minutes " +
                                                ChatColor.DARK_AQUA + "before using chat. Read why: " +
                                                ChatColor.AQUA + "mcmagic.us/rules#chat");
                                        tp.mention();
                                        tp.setNewGuest(false);
                                        Dashboard.sqlUtil.completeTutorial(tp.getUniqueId());
                                        cancel();
                                    }
                                }
                                i++;
                            }
                        }, 2000, 1000);
                        tp.setTutorial(tutorial);
                    }
                    break;
                }
                // Going to Park server
                if (Dashboard.getServer(target).isPark()) {
                    // Leaving non-Park server or inventory is uploaded from Park server
                    if (!Dashboard.getServer(tp.getServer()).isPark() || tp.isInventoryUploaded()) {
                        tp.setInventoryUploaded(false);
                        PacketInventoryStatus update = new PacketInventoryStatus(tp.getUniqueId(), 1);
                        try {
                            Dashboard.getInstance(target).send(update);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (tp.isPendingWarp()) {
                        tp.chat("/warp " + tp.getWarp());
                        tp.setPendingWarp(false);
                    }
                }
                us.mcmagic.dashboard.packets.audio.PacketServerSwitch change =
                        new us.mcmagic.dashboard.packets.audio.PacketServerSwitch(target);
                PacketContainer audio = new PacketContainer(uuid, change.getJSON().toString());
                for (Object o : WebSocketServerHandler.getGroup()) {
                    DashboardSocketChannel dash = (DashboardSocketChannel) o;
                    if (!dash.getType().equals(PacketConnectionType.ConnectionType.AUDIOSERVER)) {
                        continue;
                    }
                    dash.send(audio);
                }
                if (!tp.getServer().equalsIgnoreCase("unknown")) {
                    Dashboard.serverUtil.getServer(tp.getServer()).changeCount(-1);
                }
                Dashboard.serverUtil.getServer(target).changeCount(1);
                tp.setServer(target);
                break;
            }
            /**
             * Send To Server
             */
            case 32: {
                PacketSendToServer packet = new PacketSendToServer().fromJSON(object);
                UUID uuid = packet.getUniqueId();
                String server = packet.getServer();
                Player tp = Dashboard.getPlayer(uuid);
                if (tp == null) {
                    return;
                }
                Server target = Dashboard.getServer(server);
                if (target == null && Dashboard.getServer(server + "1") != null) {
                    target = Dashboard.serverUtil.getServerByType(server);
                }
                if (target == null) {
                    tp.sendMessage(ChatColor.RED + "We are having trouble connecting you to that server! Try again soon.");
                    return;
                }
                if (!target.isOnline()) {
                    tp.sendMessage(ChatColor.RED + "We are having trouble connecting you to that server! Try again soon.");
                    return;
                }
                Dashboard.serverUtil.sendPlayer(tp, target.getName());
                break;
            }
            /**
             * Tab Complete
             */
            case 43: {
                PacketTabComplete packet = new PacketTabComplete().fromJSON(object);
                UUID uuid = packet.getUniqueId();
                String command = packet.getCommand();
                List<String> args = packet.getArgs();
                List<String> results = packet.getResults();
                Player tp = Dashboard.getPlayer(uuid);
                if (tp == null) {
                    return;
                }
                Dashboard.commandUtil.tabComplete(tp, command, args, results);
                break;
            }
            /**
             * Set Player Resource Pack
             */
            case 48: {
                PacketSetPack packet = new PacketSetPack().fromJSON(object);
                UUID uuid = packet.getUniqueId();
                String pack = packet.getPack();
                Player tp = Dashboard.getPlayer(uuid);
                if (tp == null) {
                    return;
                }
                tp.setPack(pack);
                break;
            }
            /**
             * Get Player Resource Pack
             */
            case 49: {
                PacketGetPack packet = new PacketGetPack().fromJSON(object);
                UUID uuid = packet.getUniqueId();
                Player tp = Dashboard.getPlayer(uuid);
                if (tp == null) {
                    return;
                }
                PacketGetPack send = new PacketGetPack(uuid, tp.getPack());
                channel.send(send);
                break;

            }
            /**
             * Set Server Name
             */
            case 52: {
                PacketServerName packet = new PacketServerName().fromJSON(object);
                String name = packet.getName();
                channel.setServerName(name);
                Server s = Dashboard.getServer(name);
                s.setOnline(true);
                String running = "";
                if (!s.getServerType().equals(s.getName())) {
                    running = " running " + s.getServerType();
                }
                if (!name.matches("[a-z]\\d{1,3}")) {
                    Dashboard.moderationUtil.sendMessage(ChatColor.GREEN + "A new server instance (" + name + running +
                            ") has connected to Dashboard.");
                }
                break;
            }
            /**
             * WDL Protect
             */
            case 54: {
                PacketWDLProtect packet = new PacketWDLProtect().fromJSON(object);
                UUID uuid = packet.getUniqueId();
                Player tp = Dashboard.getPlayer(uuid);
                Ban ban;
                if (tp != null) {
                    ban = new Ban(tp.getUniqueId(), tp.getName(), false, System.currentTimeMillis() + 259200000,
                            "Attempting to use a World Downloader", "Dashboard");
                    tp.kickPlayer(ChatColor.RED + "MCMagic does not authorize the use of World Downloader Mods!\n" +
                            ChatColor.AQUA + "You have been temporarily banned for 3 Days.\n" + ChatColor.YELLOW +
                            "If you believe this was a mistake, send an appeal at " +
                            "mcmagic.us/appeal.");
                } else {
                    ban = new Ban(uuid, "Unknown Username", false, System.currentTimeMillis() + 259200000,
                            "Attempting to use a World Downloader", "Dashboard");
                }
                Dashboard.sqlUtil.banPlayer(uuid, ban.getReason(), true, new Date(System.currentTimeMillis()),
                        ban.getSource());
                Dashboard.moderationUtil.announceBan(ban);
                break;
            }
            /**
             * Rank Change
             */
            case 55: {
                PacketRankChange packet = new PacketRankChange().fromJSON(object);
                final UUID uuid = packet.getUniqueId();
                final Rank rank = packet.getRank();
                final String source = packet.getSource();
                final Player tp = Dashboard.getPlayer(uuid);
                Dashboard.schedulerManager.runAsync(new Runnable() {
                    @Override
                    public void run() {
                        String name = "";
                        if (tp != null) {
                            PacketPlayerRank packet = new PacketPlayerRank(uuid, rank);
                            tp.send(packet);
                            tp.setRank(rank);
                            name = tp.getName();
                        } else {
                            name = Dashboard.sqlUtil.usernameFromUUID(uuid);
                        }
                        Dashboard.moderationUtil.rankChange(name, rank, source);
                    }
                });
                break;
            }
            /**
             * Cross-server Warp
             */
            case 56: {
                PacketWarp packet = new PacketWarp().fromJSON(object);
                UUID uuid = packet.getUniqueId();
                String warp = packet.getWarp();
                String serverType = packet.getServer();
                Player tp = Dashboard.getPlayer(uuid);
                if (tp == null) {
                    return;
                }
                tp.setWarp(warp);
                tp.setPendingWarp(true);
                Dashboard.serverUtil.sendPlayerByType(tp, serverType);
                break;
            }
            /**
             * Empty Server
             */
            case 57: {
                PacketEmptyServer packet = new PacketEmptyServer().fromJSON(object);
                String name = packet.getServer();
                Server server = Dashboard.getServer(name);
                if (server == null) {
                    return;
                }
                for (Player tp : Dashboard.getOnlinePlayers()) {
                    if (tp.getServer().equals(server.getName())) {
                        Server target = Dashboard.serverUtil.getServerByType(name.replaceAll("\\d*$", ""), server.getUniqueId());
                        if (target == null) {
                            if (server.getServerType().equals("TTC")) {
                                target = Dashboard.serverUtil.getServerByType("Arcade");
                            } else {
                                target = Dashboard.serverUtil.getServerByType("TTC");
                            }
                        }
                        if (target == null) {
                            target = Dashboard.serverUtil.getEmptyParkServer(server.isPark() ? server.getUniqueId() : null);
                        }
                        if (!target.getName().toLowerCase().startsWith("ttc") && !target.getName().toLowerCase().startsWith("arcade")) {
                            tp.sendMessage(ChatColor.RED + "No fallback servers are available, so you were sent to a Park server.");
                        }
                        Dashboard.serverUtil.sendPlayer(tp, target.getName());
                    }
                }
                break;
            }
            /**
             * Inventory Status
             */
            case 58: {
                PacketInventoryStatus packet = new PacketInventoryStatus().fromJSON(object);
                UUID uuid = packet.getUniqueId();
                int status = packet.getStatus();
                String server = channel.getServerName();
                Player tp = Dashboard.getPlayer(uuid);
                if (tp == null || server.equals("") || status != 0 || !Dashboard.getServer(tp.getServer()).isPark()) {
                    return;
                }
                if (!tp.getServer().equals(server)) {
                    PacketInventoryStatus update = new PacketInventoryStatus(tp.getUniqueId(), 1);
                    DashboardSocketChannel s = Dashboard.getInstance(tp.getServer());
                    if (s == null) {
                        Dashboard.getLogger().warn("Target server " + tp.getServer() +
                                " not connected, could not complete inventory update!");
                    } else {
                        s.send(update);
                    }
                } else {
                    tp.setInventoryUploaded(true);
                }
                break;
            }
            /**
             * Refresh Hotel Rooms
             */
            case 59: {
                PacketRefreshHotels packet = new PacketRefreshHotels().fromJSON(object);
                for (Object o : WebSocketServerHandler.getGroup()) {
                    DashboardSocketChannel dash = (DashboardSocketChannel) o;
                    if (!dash.getType().equals(PacketConnectionType.ConnectionType.INSTANCE)) {
                        continue;
                    }
                    try {
                        if (Dashboard.getServer(dash.getServerName()).isPark()) {
                            dash.send(packet);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            }
            /**
             * Broadcast
             */
            case 60: {
                PacketBroadcast packet = new PacketBroadcast().fromJSON(object);
                String message = packet.getMessage();
                String source = packet.getSource();
                String msg = ChatColor.WHITE + "[" + ChatColor.AQUA + "Information" + ChatColor.WHITE + "] " +
                        ChatColor.GREEN + ChatColor.translateAlternateColorCodes('&', message);
                String staff = ChatColor.WHITE + "[" + ChatColor.AQUA + source + ChatColor.WHITE + "] " +
                        ChatColor.GREEN + ChatColor.translateAlternateColorCodes('&', message);
                for (Player tp : Dashboard.getOnlinePlayers()) {
                    if (Dashboard.getPlayer(tp.getUniqueId()).getRank().getRankId() >= Rank.CASTMEMBER.getRankId()) {
                        tp.sendMessage(staff);
                    } else {
                        tp.sendMessage(msg);
                    }
                }
                break;
            }
            /**
             * Mute Chat
             */
            case 61: {
                PacketMuteChat packet = new PacketMuteChat().fromJSON(object);
                String server = packet.getServer();
                boolean muted = Dashboard.chatUtil.isChatMuted(server);
                if (packet.isMute() == muted) {
                    return;
                }
                String msg = "";
                if (!packet.isMute()) {
                    Dashboard.chatUtil.unmuteChat(server);
                    msg = ChatColor.WHITE + "[" + ChatColor.DARK_AQUA + "MCMagic Chat" + ChatColor.WHITE + "] " +
                            ChatColor.YELLOW + "Chat has been unmuted";
                } else {
                    Dashboard.chatUtil.muteChat(server);
                    msg = ChatColor.WHITE + "[" + ChatColor.DARK_AQUA + "MCMagic Chat" + ChatColor.WHITE + "] " +
                            ChatColor.YELLOW + "Chat has been muted";
                }
                String msgname = msg + " by " + packet.getSource();
                for (Player tp : Dashboard.getOnlinePlayers()) {
                    if ((server.equals("ParkChat") && Dashboard.getServer(tp.getServer()).isPark()) || tp.getServer().equals(server)) {
                        tp.sendMessage(tp.getRank().getRankId() >= Rank.EARNINGMYEARS.getRankId() ? msgname : msg);
                    }
                }
                break;
            }
            /**
             * Refresh Warps
             */
            case 62: {
                PacketRefreshWarps packet = new PacketRefreshWarps().fromJSON(object);
                for (Object o : WebSocketServerHandler.getGroup()) {
                    DashboardSocketChannel dash = (DashboardSocketChannel) o;
                    if (!dash.getType().equals(PacketConnectionType.ConnectionType.INSTANCE)) {
                        continue;
                    }
                    try {
                        if (Dashboard.getServer(dash.getServerName()).isPark()) {
                            dash.send(packet);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            }
            /**
             * Player List
             */
            case 63: {
                PacketPlayerList packet = new PacketPlayerList().fromJSON(object);
                List<UUID> players = packet.getPlayers();
                UUID uuid = channel.getBungeeID();
                for (Player tp : Dashboard.getOnlinePlayers()) {
                    if (tp.getBungeeID().equals(uuid) && !players.contains(tp.getUniqueId())) {
                        Dashboard.logout(tp.getUniqueId());
                    }
                }
                break;
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        DashboardSocketChannel dash = (DashboardSocketChannel) ctx.channel();
        if (dash.getType().equals(PacketConnectionType.ConnectionType.WEBCLIENT)) {
            return;
        }
        boolean devs = false;
        for (Player tp : Dashboard.getOnlinePlayers()) {
            if (tp.getRank().getRankId() >= Rank.DEVELOPER.getRankId()) {
                devs = true;
                break;
            }
        }
        String addon = "";
        if (!devs) {
            addon = " No Developer or Manager is online, please notify one.";
        }
        switch (dash.getType()) {
            case BUNGEECORD: {
                Dashboard.moderationUtil.sendMessage(ChatColor.RED +
                        "A BungeeCord instance has disconnected from Dashboard!" + addon);
                SlackMessage m = new SlackMessage("");
                SlackAttachment a = new SlackAttachment("A BungeeCord Instance has disconnected from Dashboard! #devs");
                a.color("danger");
                Dashboard.slackUtil.sendDashboardMessage(m, Arrays.asList(a));
                break;
            }
            case DAEMON: {
                Dashboard.moderationUtil.sendMessage(ChatColor.RED +
                        "A daemon has disconnected from Dashboard!" + addon);
                SlackMessage m = new SlackMessage("");
                SlackAttachment a = new SlackAttachment("A daemon has disconnected from Dashboard! #devs");
                a.color("danger");
                Dashboard.slackUtil.sendDashboardMessage(m, Arrays.asList(a));
                break;
            }
            case WEBCLIENT: {
                break;
            }
            case INSTANCE: {
                String name = dash.getServerName();
                Server s = Dashboard.getServer(name);
                String running = "";
                if (!s.getServerType().equals(s.getName())) {
                    running = " running " + s.getServerType();
                }
                if (!name.matches("[a-z]\\d{1,3}")) {
                    Dashboard.moderationUtil.sendMessage(ChatColor.RED +
                            "A server instance (" + name + running + ") has disconnected from Dashboard!" + addon);
                    SlackMessage m = new SlackMessage("");
                    SlackAttachment a = new SlackAttachment("A server instance (" + name + running +
                            ") has disconnected from Dashboard! #devs");
                    a.color("danger");
                    Dashboard.slackUtil.sendDashboardMessage(m, Arrays.asList(a));
                }
                break;
            }
            case AUDIOSERVER: {
                Dashboard.moderationUtil.sendMessage(ChatColor.RED +
                        "The Audio Server has disconnected from Dashboard!" + addon);
                SlackMessage m = new SlackMessage("");
                SlackAttachment a = new SlackAttachment("The Audio Server has disconnected from Dashboard! #devs");
                a.color("danger");
                Dashboard.slackUtil.sendDashboardMessage(m, Arrays.asList(a));
                break;
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }
}
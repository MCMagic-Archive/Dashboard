package us.mcmagic.dashboard.server;

import io.netty.channel.socket.nio.NioServerSocketChannel;
import us.mcmagic.dashboard.Dashboard;

import java.nio.channels.SocketChannel;
import java.util.List;

/**
 * Created by Marc on 6/15/15
 */
public class DashboardServerSocketChannel extends NioServerSocketChannel {

    protected int doReadMessages(List<Object> buf)
            throws Exception {
        SocketChannel ch = javaChannel().accept();
        try {
            if (ch != null) {
                buf.add(new DashboardSocketChannel(this, ch));
                return 1;
            }
        } catch (Throwable t) {
            Dashboard.getLogger().error("Failed to create a new channel from an accepted socket.");
            t.printStackTrace();
            try {
                ch.close();
            } catch (Throwable t2) {
                Dashboard.getLogger().error("Failed to close a socket.");
                t2.printStackTrace();
            }
        }

        return 0;
    }
}
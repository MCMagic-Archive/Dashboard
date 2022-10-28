package us.mcmagic.dashboard.commands;

import us.mcmagic.dashboard.Dashboard;
import us.mcmagic.dashboard.handlers.MagicCommand;
import us.mcmagic.dashboard.handlers.Player;
import us.mcmagic.dashboard.handlers.Rank;

/**
 * Created by Marc on 10/8/16
 */
public class Commandpmtoggle extends MagicCommand {

    public Commandpmtoggle() {
        super(Rank.CASTMEMBER);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        boolean enabled = Dashboard.chatUtil.privateMessagesEnabled();
        Dashboard.chatUtil.setPrivateMessages(!enabled);
        Dashboard.moderationUtil.togglePrivate(Dashboard.chatUtil.privateMessagesEnabled(), player.getName());
    }
}
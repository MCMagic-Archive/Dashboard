package us.mcmagic.dashboard.commands;

import us.mcmagic.dashboard.Dashboard;
import us.mcmagic.dashboard.handlers.MagicCommand;
import us.mcmagic.dashboard.handlers.Player;
import us.mcmagic.dashboard.handlers.Rank;
import us.mcmagic.dashboard.packets.dashboard.PacketStaffListCommand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Commandstafflist extends MagicCommand {

    public Commandstafflist() {
        super(Rank.EARNINGMYEARS);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        List<String> owners = new ArrayList<>();
        List<String> mayors = new ArrayList<>();
        List<String> managers = new ArrayList<>();
        List<String> developers = new ArrayList<>();
        List<String> coordinators = new ArrayList<>();
        List<String> castmembers = new ArrayList<>();
        List<String> earningmyears = new ArrayList<>();
        for (Player tp : Dashboard.getOnlinePlayers()) {
            Rank r = tp.getRank();
            if (r.getRankId() >= Rank.EARNINGMYEARS.getRankId()) {
                switch (r) {
                    case EARNINGMYEARS:
                        earningmyears.add(tp.getName() + ":" + tp.getServer());
                        break;
                    case CASTMEMBER:
                        castmembers.add(tp.getName() + ":" + tp.getServer());
                        break;
                    case COORDINATOR:
                        coordinators.add(tp.getName() + ":" + tp.getServer());
                        break;
                    case DEVELOPER:
                        developers.add(tp.getName() + ":" + tp.getServer());
                        break;
                    case MANAGER:
                        managers.add(tp.getName() + ":" + tp.getServer());
                        break;
                    case MAYOR:
                        mayors.add(tp.getName() + ":" + tp.getServer());
                        break;
                    case OWNER:
                        owners.add(tp.getName() + ":" + tp.getServer());
                        break;
                }
            }
        }
        Collections.sort(owners);
        Collections.sort(mayors);
        Collections.sort(managers);
        Collections.sort(developers);
        Collections.sort(coordinators);
        Collections.sort(castmembers);
        Collections.sort(earningmyears);
        PacketStaffListCommand packet = new PacketStaffListCommand(player.getUniqueId(), owners, mayors, managers,
                developers, coordinators, castmembers, earningmyears);
        player.send(packet);
    }
}
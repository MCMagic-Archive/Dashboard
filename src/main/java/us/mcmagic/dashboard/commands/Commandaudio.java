package us.mcmagic.dashboard.commands;

import us.mcmagic.dashboard.handlers.MagicCommand;
import us.mcmagic.dashboard.handlers.Player;
import us.mcmagic.dashboard.packets.dashboard.PacketAudioCommand;

public class Commandaudio extends MagicCommand {

    @Override
    public void execute(Player player, String label, String[] args) {
        PacketAudioCommand packet = new PacketAudioCommand(player.getUniqueId(), player.setAudioAuth());
        player.send(packet);
    }
}
package cn.wode490390.nukkit.vcmd.command;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.network.protocol.StopSoundPacket;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.utils.TextFormat;
import cn.wode490390.nukkit.vcmd.CommandParser;
import cn.wode490390.nukkit.vcmd.exceptions.CommandSyntaxException;

import java.util.List;
import java.util.stream.Collectors;

public class StopSoundCommand extends PluginVanillaCommand {

    private final Plugin plugin;

    public StopSoundCommand(Plugin plugin) {
        super("stopsound", "Stops a sound.", "/stopsound <player: target> [sound: string]");
        this.setPermission("vanillacommand.stopsound");
        this.getCommandParameters().clear();
        this.addCommandParameters("default", new CommandParameter[]{
                new CommandParameter("player", CommandParamType.TARGET, false),
                new CommandParameter("sound", CommandParamType.STRING, true)
        });
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!this.plugin.isEnabled() || !this.testPermission(sender)) {
            return false;
        }

        CommandParser parser = new CommandParser(this, sender, args);
        try {
            List<Player> targets = parser.parseTargetPlayers();
            String sound = "";

            if (args.length > 1) {
                sound = parser.parseString();
            }

            if (targets.size() == 0) {
                sender.sendMessage(TextFormat.RED + "No targets matched selector");
                return true;
            }

            StopSoundPacket packet = new StopSoundPacket();
            packet.name = sound;
            if (sound.isEmpty()) {
                packet.stopAll = true;
            }

            Server.broadcastPacket(targets, packet);

            sender.sendMessage(String.format(packet.stopAll ? "Stopped all sounds for %2$s" : "Stopped sound '%1$s' for %2$s", sound, targets.stream().map(Player::getName).collect(Collectors.joining(", "))));
        } catch (CommandSyntaxException e) {
            sender.sendMessage(parser.getErrorMessage());
        }

        return true;
    }

    @Override
    public Plugin getPlugin() {
        return this.plugin;
    }
}

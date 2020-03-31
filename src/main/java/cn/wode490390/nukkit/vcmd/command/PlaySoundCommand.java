package cn.wode490390.nukkit.vcmd.command;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.level.Position;
import cn.nukkit.network.protocol.PlaySoundPacket;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.utils.TextFormat;
import cn.wode490390.nukkit.vcmd.CommandParser;
import cn.wode490390.nukkit.vcmd.exceptions.CommandSyntaxException;
import com.google.common.collect.Lists;

import java.util.List;

public class PlaySoundCommand extends PluginVanillaCommand {

    private final Plugin plugin;

    public PlaySoundCommand(Plugin plugin) {
        super("playsound", "Plays a sound.", "/playsound <sound: string> [player: target] [position: x y z] [volume: float] [pitch: float] [minimumVolume: float]");
        this.setPermission("vanillacommand.playsound");
        this.getCommandParameters().clear();
        this.addCommandParameters("default", new CommandParameter[]{
                new CommandParameter("sound", CommandParamType.STRING, false),
                new CommandParameter("player", CommandParamType.TARGET, true),
                new CommandParameter("position", CommandParamType.POSITION, true),
                new CommandParameter("volume", CommandParamType.FLOAT, true),
                new CommandParameter("pitch", CommandParamType.FLOAT, true),
                new CommandParameter("minimumVolume", CommandParamType.FLOAT, true)
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
            String sound = parser.parseString();
            List<Player> targets;
            Position position = null;
            double volume = 1;
            double pitch = 1;
            double minimumVolume = 0;

            if (args.length > 1) {
                targets = parser.parseTargetPlayers();
                if (args.length > 2) {
                    position = parser.parsePosition();
                    if (args.length > 5) {
                        volume = parser.parseDouble();
                        if (args.length > 6) {
                            pitch = parser.parseDouble();
                            if (args.length > 7) {
                                minimumVolume = Math.max(parser.parseDouble(), 0);
                            }
                        }
                    }
                }
            } else if (sender instanceof Player) {
                targets = Lists.newArrayList((Player) sender);
            } else {
                sender.sendMessage(TextFormat.RED + "No targets matched selector");
                return true;
            }

            if (position == null) {
                if (sender instanceof Position)  {
                    position = (Position) sender;
                } else {
                    position = new Position(0, 0, 0, parser.getTargetLevel());
                }
            }

            if (targets.size() == 0) {
                sender.sendMessage(TextFormat.RED + "No targets matched selector");
                return true;
            }

            double maxDistance = volume > 1 ? volume * 16 : 16;
            List<String> successes = Lists.newArrayList();

            for (Player player : targets) {
                String name = player.getName();
                PlaySoundPacket packet = new PlaySoundPacket();

                if (position.distance(player) > maxDistance) {
                    if (minimumVolume <= 0) {
                        sender.sendMessage(String.format(TextFormat.RED + "Player %1$s is too far away to hear the sound", name));
                        break;
                    }

                    packet.volume = (float) minimumVolume;
                    packet.x = player.getFloorX();
                    packet.y = player.getFloorY();
                    packet.z = player.getFloorZ();
                } else {
                    packet.volume = (float) volume;
                    packet.x = position.getFloorX();
                    packet.y = position.getFloorY();
                    packet.z = position.getFloorZ();
                }

                packet.name = sound;
                packet.pitch = (float) pitch;
                player.dataPacket(packet);

                successes.add(name);
            }

            sender.sendMessage(String.format("Played sound '%1$s' to %2$s", sound, String.join(", ", successes)));
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

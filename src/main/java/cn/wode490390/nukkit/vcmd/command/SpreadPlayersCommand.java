package cn.wode490390.nukkit.vcmd.command;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.math.Vector2;
import cn.nukkit.math.Vector3;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.utils.TextFormat;
import cn.wode490390.nukkit.vcmd.CommandParser;
import cn.wode490390.nukkit.vcmd.exceptions.CommandSyntaxException;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class SpreadPlayersCommand extends PluginVanillaCommand {

    private final Plugin plugin;
    private final ThreadLocalRandom random;

    public SpreadPlayersCommand(Plugin plugin) {
        super("spreadplayers", "Teleports entities to random locations.", "/spreadplayers <x: value> <z: value> <spreadDistance: float> <maxRange: float> <victim: target>");
        this.setPermission("vanillacommand.spreadplayers");
        this.getCommandParameters().clear();
        this.addCommandParameters("default", new CommandParameter[]{
                new CommandParameter("x", CommandParamType.VALUE, false),
                new CommandParameter("z", CommandParamType.VALUE, false),
                new CommandParameter("spreadDistance", CommandParamType.FLOAT, false),
                new CommandParameter("maxRange", CommandParamType.FLOAT, false),
                new CommandParameter("victim", CommandParamType.TARGET, false)
        });
        this.plugin = plugin;
        this.random = ThreadLocalRandom.current();
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!this.plugin.isEnabled() || !this.testPermission(sender)) {
            return false;
        }

        CommandParser parser = new CommandParser(this, sender, args);
        try {
            Vector2 vec2 = parser.parseVector2();
            float spreadDistance = (float) parser.parseDouble(); //TODO
            float maxRange = (float) parser.parseDouble();
            List<Player> targets = parser.parseTargetPlayers();

            if (spreadDistance < 0) {
                sender.sendMessage(String.format(TextFormat.RED + "The number you have entered (%1$.2f) is too small, it must be at least %2$.2f", spreadDistance, 0f));
                return true;
            } else if (maxRange < spreadDistance) {
                sender.sendMessage(String.format(TextFormat.RED + "The number you have entered (%1$.2f) is too small, it must be at least %2$.2f", maxRange, spreadDistance + 1));
                return true;
            }

            if (targets.size() == 0) {
                sender.sendMessage(TextFormat.RED + "No targets matched selector");
                return true;
            }

            for (Player target : targets) {
                Vector3 vec3 = this.nextXZ(vec2.getX(), vec2.getY(), (int) maxRange);
                vec3.y = target.getLevel().getHighestBlockAt(vec3.getFloorX(), vec3.getFloorZ()) + 1;
                target.teleport(vec3);
            }

            sender.sendMessage(String.format("Successfully spread %1$d players around %2$.2f,%3$.2f", targets.size(), vec2.getX(), vec2.getY()));
        } catch (CommandSyntaxException e) {
            sender.sendMessage(parser.getErrorMessage());
        }

        return true;
    }

    @Override
    public Plugin getPlugin() {
        return this.plugin;
    }

    private Vector3 nextXZ(double centerX, double centerZ, int maxRange) {
        Vector3 vec3 = new Vector3(centerX, 0, centerZ);
        vec3.x = Math.round(vec3.x) + this.random.nextInt(-maxRange, maxRange) + 0.5;
        vec3.z = Math.round(vec3.z) + this.random.nextInt(-maxRange, maxRange) + 0.5;
        return vec3;
    }
}

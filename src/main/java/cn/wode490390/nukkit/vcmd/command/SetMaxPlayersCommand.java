package cn.wode490390.nukkit.vcmd.command;

import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.plugin.Plugin;
import cn.wode490390.nukkit.vcmd.CommandParser;
import cn.wode490390.nukkit.vcmd.exceptions.CommandSyntaxException;

public class SetMaxPlayersCommand extends PluginVanillaCommand {

    private final Plugin plugin;

    public SetMaxPlayersCommand(Plugin plugin) {
        super("setmaxplayers", "Sets the maximum number of players for this game session.", "/setmaxplayers [maxPlayers: int]");
        this.setPermission("vanillacommand.setmaxplayers");
        this.getCommandParameters().clear();
        this.addCommandParameters("default", new CommandParameter[]{
                new CommandParameter("maxPlayers", CommandParamType.INT, false)
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
            int maxPlayers = parser.parseInt();
            boolean lowerBound = false;

            if (maxPlayers < 1) {
                maxPlayers = 1;
                lowerBound = true;
            }

            sender.getServer().setMaxPlayers(maxPlayers);

            sender.sendMessage(String.format("Set max players to %1$d.", maxPlayers));

            if (lowerBound) {
                sender.sendMessage("(Bound to minimum allowed connections)");
            }
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

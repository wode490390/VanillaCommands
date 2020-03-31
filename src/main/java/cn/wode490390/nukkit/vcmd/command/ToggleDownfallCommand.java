package cn.wode490390.nukkit.vcmd.command;

import cn.nukkit.command.CommandSender;
import cn.nukkit.level.Level;
import cn.nukkit.plugin.Plugin;
import cn.wode490390.nukkit.vcmd.CommandParser;

public class ToggleDownfallCommand extends PluginVanillaCommand {

    private final Plugin plugin;

    public ToggleDownfallCommand(Plugin plugin) {
        super("toggledownfall", "Toggles the weather.", "/toggledownfall [maxPlayers: int]");
        this.setPermission("vanillacommand.toggledownfall");
        this.getCommandParameters().clear();
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!this.plugin.isEnabled() || !this.testPermission(sender)) {
            return false;
        }

        CommandParser parser = new CommandParser(this, sender, args);

        Level level = parser.getTargetLevel();
        level.setRaining(!level.isRaining());

        sender.sendMessage("Toggled downfall");

        return true;
    }

    @Override
    public Plugin getPlugin() {
        return this.plugin;
    }
}

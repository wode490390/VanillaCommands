package cn.wode490390.nukkit.vcmd.command;

import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.entity.Entity;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.utils.TextFormat;
import cn.wode490390.nukkit.vcmd.CommandParser;
import cn.wode490390.nukkit.vcmd.exceptions.CommandSyntaxException;

import java.util.List;
import java.util.stream.Collectors;

public class TestForCommand extends PluginVanillaCommand {

    private final Plugin plugin;

    public TestForCommand(Plugin plugin) {
        super("testfor", "Counts entities (players, mobs, items, etc.) matching specified conditions.", "/testfor <victim: target>");
        this.setPermission("vanillacommand.testfor");
        this.getCommandParameters().clear();
        this.addCommandParameters("default", new CommandParameter[]{
                new CommandParameter("victim", CommandParamType.TARGET, false)
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
            List<Entity> targets = parser.parseTargets();

            if (targets.size() == 0) {
                sender.sendMessage(TextFormat.RED + "No targets matched selector");
                return true;
            }

            sender.sendMessage(String.format("Found %1$s", targets.stream().map(Entity::getName).collect(Collectors.joining(", "))));
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

package cn.wode490390.nukkit.vcmd.command;

import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.level.GameRule;
import cn.nukkit.level.GameRules;
import cn.nukkit.level.Level;
import cn.nukkit.plugin.Plugin;
import cn.wode490390.nukkit.vcmd.CommandParser;
import cn.wode490390.nukkit.vcmd.exceptions.CommandSyntaxException;

public class DayLockCommand extends PluginVanillaCommand {

    private final Plugin plugin;

    public DayLockCommand(Plugin plugin) {
        super("daylock", "Locks and unlocks the day-night cycle.", "/daylock [lock: Boolean]", new String[]{"alwaysday"});
        this.setPermission("vanillacommand.daylock");
        this.getCommandParameters().clear();
        this.addCommandParameters("default", new CommandParameter[]{
                new CommandParameter("lock", CommandParamType.RAWTEXT, true) {
                    {
                        this.enumData = ENUM_BOOLEAN;
                    }
                }
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
            boolean lock = true;

            if (args.length > 0) {
                lock = parser.parseBoolean();
            }

            Level level = parser.getTargetLevel();
            GameRules rules = level.getGameRules();

            if (lock) {
                rules.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
                level.stopTime();
                level.setTime(5000);
                sender.sendMessage("Day-Night cycle locked");
            } else {
                rules.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
                level.startTime();
                sender.sendMessage("Day-Night cycle unlocked");
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

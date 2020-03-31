package cn.wode490390.nukkit.vcmd;

import cn.nukkit.command.CommandMap;
import cn.nukkit.plugin.PluginBase;
import cn.wode490390.nukkit.vcmd.command.*;
import cn.wode490390.nukkit.vcmd.util.MetricsLite;

public class VanillaCommands extends PluginBase {

    @Override
    public void onEnable() {
        try {
            new MetricsLite(this, 6884);
        } catch (Throwable ignore) {

        }

        CommandMap commandMap = this.getServer().getCommandMap();
        commandMap.register("vcmd", new ClearCommand(this));
        commandMap.register("vcmd", new CloneCommand(this));
        commandMap.register("vcmd", new DayLockCommand(this));
        commandMap.register("vcmd", new FillCommand(this));
        commandMap.register("vcmd", new PlaySoundCommand(this));
        commandMap.register("vcmd", new ReplaceItemCommand(this));
        commandMap.register("vcmd", new SetBlockCommand(this));
        commandMap.register("vcmd", new SetMaxPlayersCommand(this));
        commandMap.register("vcmd", new SpreadPlayersCommand(this));
        commandMap.register("vcmd", new StopSoundCommand(this));
        //TODO: commandMap.register("vcmd", new TellRawCommand(this));
        commandMap.register("vcmd", new TestForBlockCommand(this));
        commandMap.register("vcmd", new TestForBlocksCommand(this));
        commandMap.register("vcmd", new TestForCommand(this));
        //TODO: commandMap.register("vcmd", new TitleRawCommand(this));
        commandMap.register("vcmd", new ToggleDownfallCommand(this));
    }
}

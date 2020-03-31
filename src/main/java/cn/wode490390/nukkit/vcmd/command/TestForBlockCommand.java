package cn.wode490390.nukkit.vcmd.command;

import cn.nukkit.block.Block;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.level.GlobalBlockPalette;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.utils.TextFormat;
import cn.wode490390.nukkit.vcmd.CommandParser;
import cn.wode490390.nukkit.vcmd.exceptions.CommandSyntaxException;

import java.util.NoSuchElementException;

public class TestForBlockCommand extends PluginVanillaCommand {

    private final Plugin plugin;

    public TestForBlockCommand(Plugin plugin) {
        super("testforblock", "Tests whether a certain block is in a specific location.", "/testforblock <position: x y z> <tileId: int> [dataValue: int]");
        this.setPermission("vanillacommand.testforblock");
        this.getCommandParameters().clear();
        this.addCommandParameters("default", new CommandParameter[]{
                new CommandParameter("position", CommandParamType.BLOCK_POSITION, false),
                new CommandParameter("tileId", CommandParamType.INT, false),
                new CommandParameter("dataValue", CommandParamType.INT, true)
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
            Position position = parser.parsePosition();
            int tileId = parser.parseInt();
            int dataValue = 0;

            if (args.length > 4) {
                dataValue = parser.parseInt();
            }

            try {
                GlobalBlockPalette.getOrCreateRuntimeId(tileId, dataValue);
            } catch (NoSuchElementException e) {
                sender.sendMessage(String.format(TextFormat.RED + "There is no such block with ID %1$s:%2$s", tileId, dataValue));
                return true;
            }

            Level level = position.getLevel();

            if (level.getChunkIfLoaded(position.getChunkX(), position.getChunkZ()) == null) {
                sender.sendMessage(TextFormat.RED + "Cannot test for block outside of the world");
                return true;
            }

            Block block = level.getBlock(position, false);
            int id = block.getId();
            int meta = block.getDamage();

            if (id == tileId && meta == dataValue) {
                sender.sendMessage(String.format("Successfully found the block at %1$d,%2$d,%3$d.", position.getFloorX(), position.getFloorY(), position.getFloorZ()));
            } else {
                sender.sendMessage(String.format(TextFormat.RED + "The block at %1$d,%2$d,%3$d is %4$d:%5$d (expected: %6$d:%7$d).", position.getFloorX(), position.getFloorY(), position.getFloorZ(), id, meta, tileId, dataValue));
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

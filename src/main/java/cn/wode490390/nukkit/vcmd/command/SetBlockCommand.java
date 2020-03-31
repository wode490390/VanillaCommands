package cn.wode490390.nukkit.vcmd.command;

import cn.nukkit.block.Block;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandEnum;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SetBlockCommand extends PluginVanillaCommand {

    private final Plugin plugin;

    public SetBlockCommand(Plugin plugin) {
        super("setblock", "Changes a block to another block.", "/setblock <position: x y z> <tileId: int> [tileData: int] [replace|destroy|keep]");
        this.setPermission("vanillacommand.setblock");
        this.getCommandParameters().clear();
        this.addCommandParameters("default", new CommandParameter[]{
                new CommandParameter("position", CommandParamType.BLOCK_POSITION, false),
                new CommandParameter("tileId", CommandParamType.INT, false),
                new CommandParameter("tileData", CommandParamType.INT, true),
                new CommandParameter("oldBlockHandling", CommandParamType.RAWTEXT, true) {
                    {
                        this.enumData = new CommandEnum("SetBlockMode", Stream.of(SetBlockMode.values()).map(e -> e.toString().toLowerCase()).collect(Collectors.toList()));
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
            Position position = parser.parsePosition();
            int tileId = parser.parseInt();
            int tileData = 0;
            SetBlockMode oldBlockHandling = SetBlockMode.REPLACE;

            if (args.length > 4) {
                tileData = parser.parseInt();
                if (args.length > 5) {
                    oldBlockHandling = parser.parseEnum(SetBlockMode.class);
                }
            }

            try {
                GlobalBlockPalette.getOrCreateRuntimeId(tileId, tileData);
            } catch (NoSuchElementException e) {
                sender.sendMessage(String.format(TextFormat.RED + "There is no such block with ID %1$d:%2$d", tileId, tileData));
                return true;
            }

            if (position.y < 0 || position.y > 255) {
                sender.sendMessage(TextFormat.RED + "The block couldn't be placed");
                return true;
            }

            Level level = position.getLevel();

            if (level.getChunkIfLoaded(position.getChunkX(), position.getChunkZ()) == null) {
                sender.sendMessage(TextFormat.RED + "Cannot place block outside of the world");
                return true;
            }

            Block block = Block.get(tileId, tileData);

            switch (oldBlockHandling) {
                case DESTROY:
                    level.useBreakOn(position);
                case REPLACE:
                    level.setBlock(position, block);
                    break;
                case KEEP:
                    if (level.getBlock(position).getId() == Block.AIR) {
                        level.setBlock(position, block);
                    }
                    break;
            }

            sender.sendMessage("Block placed");
        } catch (CommandSyntaxException e) {
            sender.sendMessage(parser.getErrorMessage());
        }

        return true;
    }

    @Override
    public Plugin getPlugin() {
        return this.plugin;
    }

    private enum SetBlockMode {
        REPLACE,
        DESTROY,
        KEEP
    }
}

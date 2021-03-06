package cn.wode490390.nukkit.vcmd.command;

import cn.nukkit.block.Block;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandEnum;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.level.GlobalBlockPalette;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.math.NukkitMath;
import cn.nukkit.math.SimpleAxisAlignedBB;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.utils.TextFormat;
import cn.wode490390.nukkit.vcmd.CommandParser;
import cn.wode490390.nukkit.vcmd.exceptions.CommandSyntaxException;
import com.google.common.collect.Lists;

import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FillCommand extends PluginVanillaCommand {

    private final Plugin plugin;

    public FillCommand(Plugin plugin) {
        super("fill", "Fills all or parts of a region with a specific block.", "/fill <from: x y z> <to: x y z> <tileId: int> [tileData: int] [outline|hollow|destroy|keep|replace [replaceTileId: int] [replaceDataValue: int]]");
        this.setPermission("vanillacommand.fill");
        this.getCommandParameters().clear();
        this.addCommandParameters("default", new CommandParameter[]{
                new CommandParameter("from", CommandParamType.BLOCK_POSITION, false),
                new CommandParameter("to", CommandParamType.BLOCK_POSITION, false),
                new CommandParameter("tileId", CommandParamType.INT, false),
                new CommandParameter("tileData", CommandParamType.INT, true),
                new CommandParameter("oldBlockHandling", CommandParamType.RAWTEXT, true) {
                    {
                        this.enumData = new CommandEnum("FillMode", Stream.of(FillMode.values()).filter(e -> e != FillMode.REPLACE).map(e -> e.toString().toLowerCase()).collect(Collectors.toList()));
                    }
                }
        });
        this.addCommandParameters("replace", new CommandParameter[]{
                new CommandParameter("from", CommandParamType.BLOCK_POSITION, false),
                new CommandParameter("to", CommandParamType.BLOCK_POSITION, false),
                new CommandParameter("tileId", CommandParamType.INT, false),
                new CommandParameter("tileData", CommandParamType.INT, false),
                new CommandParameter("oldBlockHandling", CommandParamType.RAWTEXT, false) {
                    {
                        this.enumData = new CommandEnum("Replace", Lists.newArrayList("replace"));
                    }
                },
                new CommandParameter("replaceTileId", CommandParamType.INT, true),
                new CommandParameter("replaceDataValue", CommandParamType.INT, true)
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
            Position from = parser.parsePosition().floor();
            Position to = parser.parsePosition().floor();
            int tileId = parser.parseInt();
            int tileData = 0;
            FillMode oldBlockHandling = FillMode.REPLACE;
            int replaceTileId = 0;
            int replaceDataValue = 0;

            if (args.length > 7) {
                tileData = parser.parseInt();
                if (args.length > 8) {
                    oldBlockHandling = parser.parseEnum(FillMode.class);
                    if (args.length > 9) {
                        replaceTileId = parser.parseInt();
                        if (args.length > 10) {
                            replaceDataValue = parser.parseInt();
                        }
                    }
                }
            }

            try {
                GlobalBlockPalette.getOrCreateRuntimeId(tileId, tileData);
                GlobalBlockPalette.getOrCreateRuntimeId(replaceTileId, replaceDataValue);
            } catch (NoSuchElementException e) {
                sender.sendMessage(String.format(TextFormat.RED + "There is no such block with ID %1$d:%2$d", tileId, tileData));
                return true;
            }

            AxisAlignedBB aabb = new SimpleAxisAlignedBB(Math.min(from.getX(), to.getX()), Math.min(from.getY(), to.getY()), Math.min(from.getZ(), to.getZ()), Math.max(from.getX(), to.getX()), Math.max(from.getY(), to.getY()), Math.max(from.getZ(), to.getZ()));

            if (aabb.getMinY() < 0 || aabb.getMaxY() > 255) {
                sender.sendMessage(TextFormat.RED + "Cannot place blocks outside of the world");
                return true;
            }

            int size = NukkitMath.floorDouble((aabb.getMaxX() - aabb.getMinX() + 1) * (aabb.getMaxY() - aabb.getMinY() + 1) * (aabb.getMaxZ() - aabb.getMinZ() + 1));
            if (size > 16 * 16 * 16 * 8) {
                sender.sendMessage(String.format(TextFormat.RED + "Too many blocks in the specified area (%1$d > %2$d)", size, 16 * 16 * 16 * 8));
                return true;
            }

            Level level = from.getLevel();

            for (int chunkX = NukkitMath.floorDouble(aabb.getMinX()) >> 4; chunkX <= NukkitMath.floorDouble(aabb.getMaxX()) >> 4; chunkX++) {
                for (int chunkZ = NukkitMath.floorDouble(aabb.getMinZ()) >> 4; chunkZ <= NukkitMath.floorDouble(aabb.getMaxZ()) >> 4; chunkZ++) {
                    if (level.getChunkIfLoaded(chunkX, chunkZ) == null) {
                        sender.sendMessage(TextFormat.RED + "Cannot place blocks outside of the world");
                        return true;
                    }
                }
            }

            Block[] blocks;
            int count = 0;

            switch (oldBlockHandling) {
                case OUTLINE:
                    for (int x = NukkitMath.floorDouble(aabb.getMinX()); x <= NukkitMath.floorDouble(aabb.getMaxX()); x++) {
                        for (int z = NukkitMath.floorDouble(aabb.getMinZ()); z <= NukkitMath.floorDouble(aabb.getMaxZ()); z++) {
                            for (int y = NukkitMath.floorDouble(aabb.getMinY()); y <= NukkitMath.floorDouble(aabb.getMaxY()); y++) {
                                if (x == from.x || x == to.x || z == from.z || z == to.z || y == from.y || y == to.y) {
                                    level.setBlock(x, y, z, Block.get(tileId, tileData), false ,true);
                                    ++count;
                                }
                            }
                        }
                    }

                    break;
                case HOLLOW:
                    for (int x = NukkitMath.floorDouble(aabb.getMinX()); x <= NukkitMath.floorDouble(aabb.getMaxX()); x++) {
                        for (int z = NukkitMath.floorDouble(aabb.getMinZ()); z <= NukkitMath.floorDouble(aabb.getMaxZ()); z++) {
                            for (int y = NukkitMath.floorDouble(aabb.getMinY()); y <= NukkitMath.floorDouble(aabb.getMaxY()); y++) {
                                Block block;

                                if (x == from.x || x == to.x || z == from.z || z == to.z || y == from.y || y == to.y) {
                                    block = Block.get(tileId, tileData);
                                } else {
                                    block = Block.get(Block.AIR);
                                }

                                level.setBlock(x, y, z, block, false ,true);
                                ++count;
                            }
                        }
                    }

                    break;
                case REPLACE:
                    blocks = getLevelBlocks(level, aabb);

                    for (Block block : blocks) {
                        if (block.getId() == replaceTileId && block.getDamage() == replaceDataValue) {
                            level.setBlock(block, Block.get(tileId, tileData));
                            ++count;
                        }
                    }

                    break;
                case DESTROY:
                    blocks = getLevelBlocks(level, aabb);

                    for (Block block : blocks) {
                        level.useBreakOn(block);
                        level.setBlock(block, Block.get(tileId, tileData));
                        ++count;
                    }

                    break;
                case KEEP:
                    blocks = getLevelBlocks(level, aabb);

                    for (Block block : blocks) {
                        if (block.getId() == Block.AIR) {
                            level.setBlock(block, Block.get(tileId, tileData));
                            ++count;
                        }
                    }

                    break;
            }

            if (count == 0) {
                sender.sendMessage(TextFormat.RED + "0 blocks filled");
            } else {
                sender.sendMessage(String.format("%1$d blocks filled", count));
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

    private enum FillMode {
        REPLACE,
        OUTLINE,
        HOLLOW,
        DESTROY,
        KEEP
    }
}

package cn.wode490390.nukkit.vcmd.command;

import cn.nukkit.block.Block;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandEnum;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
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

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CloneCommand extends PluginVanillaCommand {

    private final Plugin plugin;

    public CloneCommand(Plugin plugin) {
        super("clone", "Clones blocks from one region to another.", "/clone <begin: x y z> <end: x y z> <destination: x y z> [filtered|masked|replace] [force|move|normal] [tileId: int] [tileData: int]");
        this.setPermission("vanillacommand.clone");
        this.getCommandParameters().clear();
        this.addCommandParameters("default", new CommandParameter[]{
                new CommandParameter("begin", CommandParamType.BLOCK_POSITION, false),
                new CommandParameter("end", CommandParamType.BLOCK_POSITION, false),
                new CommandParameter("destination", CommandParamType.BLOCK_POSITION, false),
                new CommandParameter("maskMode", CommandParamType.RAWTEXT, true) {
                    {
                        this.enumData = new CommandEnum("MaskMode", Stream.of(MaskMode.values()).filter(e -> e != MaskMode.FILTERED).map(e -> e.toString().toLowerCase()).collect(Collectors.toList()));
                    }
                },
                new CommandParameter("cloneMode", CommandParamType.RAWTEXT, true) {
                    {
                        this.enumData = new CommandEnum("CloneMode", Stream.of(CloneMode.values()).map(e -> e.toString().toLowerCase()).collect(Collectors.toList()));
                    }
                }
        });
        this.addCommandParameters("filtered", new CommandParameter[]{
                new CommandParameter("begin", CommandParamType.BLOCK_POSITION, false),
                new CommandParameter("end", CommandParamType.BLOCK_POSITION, false),
                new CommandParameter("destination", CommandParamType.BLOCK_POSITION, false),
                new CommandParameter("maskMode", CommandParamType.RAWTEXT, false) {
                    {
                        this.enumData = new CommandEnum("MaskModeFiltered", Lists.newArrayList("filtered"));
                    }
                },
                new CommandParameter("cloneMode", CommandParamType.RAWTEXT, false) {
                    {
                        this.enumData = new CommandEnum("CloneMode", Stream.of(CloneMode.values()).map(e -> e.toString().toLowerCase()).collect(Collectors.toList()));
                    }
                },
                new CommandParameter("tileId", CommandParamType.INT, false),
                new CommandParameter("tileData", CommandParamType.INT, false)
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
            Position begin = parser.parsePosition().floor();
            Position end = parser.parsePosition().floor();
            Position destination = parser.parsePosition().floor();
            MaskMode maskMode = MaskMode.REPLACE;
            CloneMode cloneMode = CloneMode.NORMAL;
            int tileId = 0;
            int tileData = 0;

            if (args.length > 9) {
                maskMode = parser.parseEnum(MaskMode.class);
                if (args.length > 10) {
                    cloneMode = parser.parseEnum(CloneMode.class);
                    if (args.length > 11) {
                        tileId = parser.parseInt();
                        tileData = parser.parseInt();
                    }
                }
            }

            AxisAlignedBB blocksAABB = new SimpleAxisAlignedBB(Math.min(begin.getX(), end.getX()), Math.min(begin.getY(), end.getY()), Math.min(begin.getZ(), end.getZ()), Math.max(begin.getX(), end.getX()), Math.max(begin.getY(), end.getY()), Math.max(begin.getZ(), end.getZ()));
            int size = NukkitMath.floorDouble((blocksAABB.getMaxX() - blocksAABB.getMinX() + 1) * (blocksAABB.getMaxY() - blocksAABB.getMinY() + 1) * (blocksAABB.getMaxZ() - blocksAABB.getMinZ() + 1));

            if (size > 16 * 16 * 256 * 8) {
                sender.sendMessage(String.format(TextFormat.RED + "Too many blocks in the specified area (%1$d > %2$d)", size, 16 * 16 * 256 * 8));
                return true;
            }

            Position to = new Position(destination.getX() + (blocksAABB.getMaxX() - blocksAABB.getMinX()), destination.getY() + (blocksAABB.getMaxY() - blocksAABB.getMinY()), destination.getZ() + (blocksAABB.getMaxZ() - blocksAABB.getMinZ()));
            AxisAlignedBB destinationAABB = new SimpleAxisAlignedBB(Math.min(destination.getX(), to.getX()), Math.min(destination.getY(), to.getY()), Math.min(destination.getZ(), to.getZ()), Math.max(destination.getX(), to.getX()), Math.max(destination.getY(), to.getY()), Math.max(destination.getZ(), to.getZ()));

            if (blocksAABB.getMinY() < 0 || blocksAABB.getMaxY() > 255 || destinationAABB.getMinY() < 0 || destinationAABB.getMaxY() > 255) {
                sender.sendMessage(TextFormat.RED + "Cannot access blocks outside of the world");
                return true;
            }
            if (blocksAABB.intersectsWith(destinationAABB) && cloneMode != CloneMode.FORCE) {
                sender.sendMessage(TextFormat.RED + "Source and destination can not overlap");
                return true;
            }

            Level level = begin.getLevel();

            for (int sourceChunkX = NukkitMath.floorDouble(blocksAABB.getMinX()) >> 4, destinationChunkX = NukkitMath.floorDouble(destinationAABB.getMinX()) >> 4; sourceChunkX <= NukkitMath.floorDouble(blocksAABB.getMaxX()) >> 4; sourceChunkX++, destinationChunkX++) {
                for (int sourceChunkZ = NukkitMath.floorDouble(blocksAABB.getMinZ()) >> 4, destinationChunkZ = NukkitMath.floorDouble(destinationAABB.getMinZ()) >> 4; sourceChunkZ <= NukkitMath.floorDouble(blocksAABB.getMaxZ()) >> 4; sourceChunkZ++, destinationChunkZ++) {
                    if (level.getChunkIfLoaded(sourceChunkX, sourceChunkZ) == null) {
                        sender.sendMessage(TextFormat.RED + "Cannot access blocks outside of the world");
                        return true;
                    }
                    if (level.getChunkIfLoaded(destinationChunkX, destinationChunkZ) == null) {
                        sender.sendMessage(TextFormat.RED + "Cannot access blocks outside of the world");
                        return true;
                    }
                }
            }

            Block[] blocks = getLevelBlocks(level, blocksAABB);
            Block[] destinationBlocks = getLevelBlocks(level, destinationAABB);
            int count = 0;

            boolean move = cloneMode == CloneMode.MOVE;
            switch (maskMode) {
                case REPLACE:
                    for (int i = 0; i < blocks.length; i++) {
                        Block block = blocks[i];
                        Block destinationBlock = destinationBlocks[i];

                        level.setBlock(destinationBlock, Block.get(block.getId(), block.getDamage()));
                        ++count;

                        if (move) {
                            level.setBlock(block, Block.get(Block.AIR));
                        }
                    }

                    break;
                case MASKED:
                    for (int i = 0; i < blocks.length; i++) {
                        Block block = blocks[i];
                        Block destinationBlock = destinationBlocks[i];

                        if (block.getId() != Block.AIR) {
                            level.setBlock(destinationBlock, Block.get(block.getId(), block.getDamage()));
                            ++count;

                            if (move) {
                                level.setBlock(block, Block.get(Block.AIR));
                            }
                        }
                    }

                    break;
                case FILTERED:
                    for (int i = 0; i < blocks.length; i++) {
                        Block block = blocks[i];
                        Block destinationBlock = destinationBlocks[i];

                        if (block.getId() == tileId && block.getDamage() == tileData) {
                            level.setBlock(destinationBlock, Block.get(block.getId(), block.getDamage()));
                            ++count;

                            if (move) {
                                level.setBlock(block, Block.get(Block.AIR));
                            }
                        }
                    }

                    break;
            }

            if (count == 0) {
                sender.sendMessage(TextFormat.RED + "No blocks cloned");
            } else {
                sender.sendMessage(String.format("%1$d blocks cloned", count));
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

    private enum MaskMode {
        REPLACE,
        MASKED,
        FILTERED
    }

    private enum CloneMode {
        NORMAL,
        FORCE,
        MOVE
    }
}

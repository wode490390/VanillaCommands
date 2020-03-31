package cn.wode490390.nukkit.vcmd.command;

import cn.nukkit.block.Block;
import cn.nukkit.command.PluginIdentifiableCommand;
import cn.nukkit.command.data.CommandEnum;
import cn.nukkit.command.defaults.VanillaCommand;
import cn.nukkit.level.Level;
import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.math.NukkitMath;
import cn.nukkit.math.Vector3;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract  class PluginVanillaCommand extends VanillaCommand implements PluginIdentifiableCommand {

    protected static final CommandEnum ENUM_BOOLEAN = new CommandEnum("Boolean", Lists.newArrayList()) {
        {
            this.getValues().addAll(Arrays.asList("true", "false"));
        }
    };

    protected PluginVanillaCommand(String name) {
        super(name);
    }

    protected PluginVanillaCommand(String name, String description) {
        super(name, description);
    }

    protected PluginVanillaCommand(String name, String description, String usageMessage) {
        super(name, description, usageMessage);
    }

    protected PluginVanillaCommand(String name, String description, String usageMessage, String[] aliases) {
        super(name, description, usageMessage, aliases);
    }

    protected static Block[] getLevelBlocks(Level level, AxisAlignedBB bb) {
        int minX = NukkitMath.floorDouble(Math.min(bb.getMinX(), bb.getMaxX()));
        int minY = NukkitMath.floorDouble(Math.min(bb.getMinY(), bb.getMaxY()));
        int minZ = NukkitMath.floorDouble(Math.min(bb.getMinZ(), bb.getMaxZ()));
        int maxX = NukkitMath.floorDouble(Math.max(bb.getMinX(), bb.getMaxX()));
        int maxY = NukkitMath.floorDouble(Math.max(bb.getMinY(), bb.getMaxY()));
        int maxZ = NukkitMath.floorDouble(Math.max(bb.getMinZ(), bb.getMaxZ()));

        List<Block> blocks = new ArrayList<>();
        Vector3 vec = new Vector3();

        for (int z = minZ; z <= maxZ; ++z) {
            for (int x = minX; x <= maxX; ++x) {
                for (int y = minY; y <= maxY; ++y) {
                    blocks.add(level.getBlock(vec.setComponents(x, y, z), false));
                }
            }
        }

        return blocks.toArray(new Block[0]);
    }
}

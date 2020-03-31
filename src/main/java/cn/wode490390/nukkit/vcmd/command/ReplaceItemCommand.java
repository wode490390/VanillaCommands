package cn.wode490390.nukkit.vcmd.command;

import cn.nukkit.Player;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandEnum;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.inventory.InventoryHolder;
import cn.nukkit.inventory.InventoryType;
import cn.nukkit.inventory.PlayerEnderChestInventory;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.utils.TextFormat;
import cn.wode490390.nukkit.vcmd.CommandParser;
import cn.wode490390.nukkit.vcmd.exceptions.CommandSyntaxException;
import com.google.common.collect.Lists;

import java.util.List;

public class ReplaceItemCommand extends PluginVanillaCommand {

    private final Plugin plugin;

    public ReplaceItemCommand(Plugin plugin) {
        super("replaceitem", "Replaces items in inventories.", "/replaceitem <block <position: x y z>|entity <target: target>> <slotType: EquipmentSlot> <slotId: int> <itemId: int> <amount: int> <data: int> <components: JSON>");
        this.setPermission("vanillacommand.replaceitem");
        this.getCommandParameters().clear();
        this.addCommandParameters("block", new CommandParameter[]{
                new CommandParameter("block", CommandParamType.RAWTEXT, false) {
                    {
                        this.enumData = new CommandEnum("ReplaceItemBlock", Lists.newArrayList("block"));
                    }
                },
                new CommandParameter("position", CommandParamType.BLOCK_POSITION, false),
                new CommandParameter("slotType", CommandParamType.RAWTEXT, false) {
                    {
                        this.enumData = new CommandEnum("BlockEquipmentSlot", Lists.newArrayList("slot.container"));
                    }
                },
                new CommandParameter("slotId", CommandParamType.INT, false),
                new CommandParameter("itemId", CommandParamType.INT, false),
                new CommandParameter("amount", CommandParamType.INT, true),
                new CommandParameter("data", CommandParamType.INT, true),
                new CommandParameter("components", CommandParamType.JSON, true) //TODO
        });
        this.addCommandParameters("entity", new CommandParameter[]{
                new CommandParameter("entity", CommandParamType.RAWTEXT, false) {
                    {
                        this.enumData = new CommandEnum("ReplaceItemEntity", Lists.newArrayList("entity"));
                    }
                },
                new CommandParameter("target", CommandParamType.TARGET, false),
                new CommandParameter("slotType", CommandParamType.RAWTEXT, false) {
                    {
                        this.enumData = new CommandEnum("EntityEquipmentSlot", Lists.newArrayList("slot.weapon.mainhand", "slot.weapon.offhand", "slot.armor.head", "slot.armor.chest", "slot.armor.legs", "slot.armor.feet", "slot.hotbar", "slot.inventory", "slot.enderchest", "slot.saddle", "slot.armor", "slot.chest"));
                    }
                },
                new CommandParameter("slotId", CommandParamType.INT, false),
                new CommandParameter("itemId", CommandParamType.INT, false),
                new CommandParameter("amount", CommandParamType.INT, true),
                new CommandParameter("data", CommandParamType.INT, true),
                new CommandParameter("components", CommandParamType.JSON, true) //TODO
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
            String slotType;
            int slotId;
            int itemId;
            int amount = 1;
            int data = 0;

            Item item;

            switch (parser.parseString().toLowerCase()) {
                case "block":
                    Position position = parser.parsePosition();
                    BlockEquipmentSlot blockSlotType = parseBlockEquipmentSlot(slotType = parser.parseString());
                    slotId = parser.parseInt();
                    itemId = parser.parseInt();

                    if (args.length > 7) {
                        amount = parser.parseInt();
                        if (args.length > 8) {
                            data = parser.parseInt();
                        }
                    }

                    item = Item.get(itemId, data, amount);
                    Level level = position.getLevel();

                    switch (blockSlotType) {
                        case SLOT_CONTAINER:
                            BlockEntity blockEntity = level.getBlockEntity(position);
                            Inventory inventory;

                            if (blockEntity instanceof InventoryHolder && (inventory = ((InventoryHolder) blockEntity).getInventory()) != null) {
                                int size = inventory.getSize();

                                if (size > slotId) {
                                    inventory.setItem(slotId, item);
                                    sender.sendMessage(String.format("Replaced %1$s slot %2$d with %3$d * %4$s", slotType, slotId, amount, item.getName()));
                                } else {
                                    sender.sendMessage(String.format(TextFormat.RED + "Could not replace slot %1$s, must be a value between %2$d and %3$d.", slotType, 0, size));
                                }
                            } else {
                                sender.sendMessage(String.format(TextFormat.RED + "Block at Pos(%1$d,%2$d,%3$d) is not a container", position.getFloorX(), position.getFloorY(), position.getFloorZ()));
                            }

                            break;
                    }

                    break;
                case "entity":
                    List<Player> targets = parser.parseTargetPlayers(); //TODO: entities
                    EntityEquipmentSlot entitySlotType = parseEntityEquipmentSlot(slotType = parser.parseString());
                    slotId = parser.parseInt();
                    itemId = parser.parseInt();

                    item = Item.get(itemId, data, amount);

                    for (Player target : targets) {
                        switch (entitySlotType) {
                            case SLOT_WEAPON_MAINHAND:
                                target.getInventory().setItemInHand(item);
                                sender.sendMessage(String.format("Replaced %1$s slot %2$d of %3$s with %4$d * %5$s", slotType, slotId, target.getName(), amount, item.getName()));
                                break;
                            case SLOT_WEAPON_OFFHAND: //TODO: check
                                target.getOffhandInventory().setItem(0, item);
                                sender.sendMessage(String.format("Replaced %1$s slot %2$d of %3$s with %4$d * %5$s", slotType, slotId, target.getName(), amount, item.getName()));
                                break;
                            case SLOT_ARMOR_HEAD: //TODO: check
                                target.getInventory().setHelmet(item);
                                sender.sendMessage(String.format("Replaced %1$s slot %2$d of %3$s with %4$d * %5$s", slotType, slotId, target.getName(), amount, item.getName()));
                                break;
                            case SLOT_ARMOR_CHEST: //TODO: check
                                target.getInventory().setChestplate(item);
                                sender.sendMessage(String.format("Replaced %1$s slot %2$d of %3$s with %4$d * %5$s", slotType, slotId, target.getName(), amount, item.getName()));
                                break;
                            case SLOT_ARMOR_LEGS: //TODO: check
                                target.getInventory().setLeggings(item);
                                sender.sendMessage(String.format("Replaced %1$s slot %2$d of %3$s with %4$d * %5$s", slotType, slotId, target.getName(), amount, item.getName()));
                                break;
                            case SLOT_ARMOR_FEET: //TODO: check
                                target.getInventory().setBoots(item);
                                sender.sendMessage(String.format("Replaced %1$s slot %2$d of %3$s with %4$d * %5$s", slotType, slotId, target.getName(), amount, item.getName()));
                                break;
                            case SLOT_HOTBAR:
                                PlayerInventory playerInventory = target.getInventory();
                                int size = playerInventory.getSize();

                                if (slotId >= playerInventory.getHotbarSize() || slotId < 0) {
                                    sender.sendMessage(String.format(TextFormat.RED + "Could not replace slot %1$s, must be a value between %2$d and %3$d.", slotType, 0, size));
                                } else {
                                    playerInventory.setItem(slotId, item);
                                    sender.sendMessage(String.format("Replaced %1$s slot %2$d of %3$s with %4$d * %5$s", slotType, slotId, target.getName(), amount, item.getName()));
                                }

                                break;
                            case SLOT_INVENTORY:
                                size = InventoryType.CHEST.getDefaultSize();

                                if (slotId >= size || slotId < 0) {
                                    sender.sendMessage(String.format(TextFormat.RED + "Could not replace slot %1$s, must be a value between %2$d and %3$d.", slotType, 0, size));
                                } else {
                                    target.getInventory().setItem(8 + slotId, item);
                                    sender.sendMessage(String.format("Replaced %1$s slot %2$d of %3$s with %4$d * %5$s", slotType, slotId, target.getName(), amount, item.getName()));
                                }

                                break;
                            case SLOT_ENDERCHEST:
                                PlayerEnderChestInventory enderChestInventory = target.getEnderChestInventory();
                                size = enderChestInventory.getSize();

                                if (slotId >= enderChestInventory.getSize() || slotId < 0) {
                                    sender.sendMessage(String.format(TextFormat.RED + "Could not replace slot %1$s, must be a value between %2$d and %3$d.", slotType, 0, size));
                                } else {
                                    enderChestInventory.setItem(slotId, item);
                                    sender.sendMessage(String.format("Replaced %1$s slot %2$d of %3$s with %4$d * %5$s", slotType, slotId, target.getName(), amount, item.getName()));
                                }

                                break;
                            case SLOT_SADDLE:
                            case SLOT_ARMOR:
                            case SLOT_CHEST:
                                sender.sendMessage(String.format(TextFormat.RED + "Could not replace %1$s slot %2$d with %3$d * %4$s", slotType, slotId, amount, item.getName()));
                                break;
                        }
                    }

                    break;
                default:
                    throw new CommandSyntaxException();
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

    private static BlockEquipmentSlot parseBlockEquipmentSlot(String arg) throws CommandSyntaxException {
        switch (arg.toLowerCase()) {
            case "slot.container":
                return BlockEquipmentSlot.SLOT_CONTAINER;
        }

        throw new CommandSyntaxException();
    }

    private static EntityEquipmentSlot parseEntityEquipmentSlot(String arg) throws CommandSyntaxException {
        switch (arg.toLowerCase()) {
            case "slot.weapon.mainhand":
                return EntityEquipmentSlot.SLOT_WEAPON_MAINHAND;
            case "slot.weapon.offhand":
                return EntityEquipmentSlot.SLOT_WEAPON_OFFHAND;
            case "slot.armor.head":
                return EntityEquipmentSlot.SLOT_ARMOR_HEAD;
            case "slot.armor.chest":
                return EntityEquipmentSlot.SLOT_ARMOR_CHEST;
            case "slot.armor.legs":
                return EntityEquipmentSlot.SLOT_ARMOR_LEGS;
            case "slot.armor.feet":
                return EntityEquipmentSlot.SLOT_ARMOR_FEET;
            case "slot.hotbar":
                return EntityEquipmentSlot.SLOT_HOTBAR;
            case "slot.inventory":
                return EntityEquipmentSlot.SLOT_INVENTORY;
            case "slot.enderchest":
                return EntityEquipmentSlot.SLOT_ENDERCHEST;
            case "slot.saddle":
                return EntityEquipmentSlot.SLOT_SADDLE;
            case "slot.armor":
                return EntityEquipmentSlot.SLOT_ARMOR;
            case "slot.chest":
                return EntityEquipmentSlot.SLOT_CHEST;
        }

        throw new CommandSyntaxException();
    }

    private enum BlockEquipmentSlot {
        SLOT_CONTAINER
    }

    private enum EntityEquipmentSlot {
        SLOT_WEAPON_MAINHAND,
        SLOT_WEAPON_OFFHAND,
        SLOT_ARMOR_HEAD,
        SLOT_ARMOR_CHEST,
        SLOT_ARMOR_LEGS,
        SLOT_ARMOR_FEET,
        SLOT_HOTBAR,
        SLOT_INVENTORY,
        SLOT_ENDERCHEST,
        SLOT_SADDLE,
        SLOT_ARMOR,
        SLOT_CHEST
    }
}

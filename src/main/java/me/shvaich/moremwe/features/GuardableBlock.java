package me.shvaich.moremwe.features;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumChatFormatting;

import java.util.HashMap;
import java.util.Map;

public final class GuardableBlock {

    private static final Map<Block, GuardableBlock> blocksMap = new HashMap<>();
    private static final GuardableBlock[] guardableBlocks;

    public final String key;
    public final String displayName;
    public final String specialText;
    public final Block mainBlock;

    static {
        guardableBlocks = new GuardableBlock[]{
            new GuardableBlock("chest", "Chest", Blocks.chest, Blocks.trapped_chest),
            new GuardableBlock("furnace", "Furnace", Blocks.furnace, Blocks.lit_furnace),
            new GuardableBlock("crafting_table", "Crafting Table", EnumChatFormatting.BLUE + "Table" + EnumChatFormatting.WHITE + " de " + EnumChatFormatting.RED + "fabrication", Blocks.crafting_table),
            new GuardableBlock("anvil", "Anvil", Blocks.anvil),
            new GuardableBlock("beacon", "Beacon", Blocks.beacon),
            new GuardableBlock("hopper", "Hopper", EnumChatFormatting.BLACK + "That thing that hops", Blocks.hopper)
        };
    }

    private GuardableBlock(String key, String displayName, String specialText, Block... blocks) {
        this.key = key;
        this.displayName = displayName;
        this.specialText = specialText;
        this.mainBlock = blocks[0];
        for (final Block block : blocks) {
            blocksMap.put(block, this);
        }
    }

    private GuardableBlock(String key, String displayName, Block... blocks) {
        this(key, displayName, "", blocks);
    }

    public static GuardableBlock fromBlock(Block block) {
        return blocksMap.get(block);
    }

    public static GuardableBlock[] values() {
        return guardableBlocks.clone();
    }
}
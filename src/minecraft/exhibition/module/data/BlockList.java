/*
 * Copyright (c) MineSense.pub 2018.
 * Developed by Arithmo
 */

package exhibition.module.data;

import exhibition.module.data.settings.Setting;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BlockList {

    private List<Block> blockList;

    public BlockList(List<Block> defaultList) {
        this.blockList = defaultList;
    }

    public boolean isBlockInList(Block block) {
        return this.blockList.contains(block);
    }

    public void setBlockList(int... ids) {
        blockList = new ArrayList<>();
        for (int id : ids) {
            addBlockFromID(id);
        }
    }

    public Integer[] getBlockListIds() {
        List<Integer> idList = new ArrayList<>();
        for (Block block : blockList) {
            idList.add(Block.getIdFromBlock(block));
        }
        return idList.toArray(new Integer[blockList.size()]);
    }

    public Block attemptAdd(String str) {
        Block resolvedBlock;
        try {
            int blockId = Integer.parseInt(str);
            resolvedBlock = Block.getBlockById(blockId);
            blockList.add(resolvedBlock);
            return resolvedBlock;
        } catch (Exception e) {
        }
        resolvedBlock = Block.getBlockFromName(str);
        if (resolvedBlock != null) {
            blockList.add(resolvedBlock);
            return resolvedBlock;
        }
        return null;
    }

    public boolean attemptRemove(String str) {
        Block resolvedBlock;
        try {
            int blockId = Integer.parseInt(str);
            resolvedBlock = Block.getBlockById(blockId);
        } catch (Exception ignored) {
        }
        resolvedBlock = Block.getBlockFromName(str);
        return resolvedBlock != null && blockList.remove(resolvedBlock);
    }

    private void addBlockFromID(int id) {
        Block block = Block.getBlockById(id);
        if (block != null)
            blockList.add(block);
    }

    @Override
    public String toString() {
        List<Integer> blockIds = new ArrayList<>();
        for (Block block : blockList) {
            blockIds.add(Block.getIdFromBlock(block));
        }
        return Arrays.toString(blockIds.toArray());
    }

}

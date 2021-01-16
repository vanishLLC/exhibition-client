package net.minecraft.block.state;

import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.BlockPos;

public interface IBlockState
{
    Collection<IProperty> getPropertyNames();

    <T extends Comparable<T>> T getValue(IProperty<T> property);

    <T extends Comparable<T>, V extends T> IBlockState withProperty(IProperty<T> property, V value);

    <T extends Comparable<T>> IBlockState cycleProperty(IProperty<T> property);

    ImmutableMap<IProperty, Comparable> getProperties();

    Block getBlock();

    default Material getMaterial() {
        return this.getBlock().getMaterial();
    }

    default float getPlayerRelativeBlockHardness(EntityPlayerSP player, WorldClient world, BlockPos blockPos) {
        return this.getBlock().getPlayerRelativeBlockHardness(player, world, blockPos);
    }

}

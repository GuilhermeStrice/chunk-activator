package me.sargunvohra.mcmods.chunkactivator.block;

import java.util.Random;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class ChunkActivatorBlock extends BlockWithEntity {
    public ChunkActivatorBlock() {
        super(
            FabricBlockSettings.of(Material.METAL, MaterialColor.GOLD)
                .strength(3f, 6f)
                .sounds(BlockSoundGroup.METAL));
    }

    @Override
    public BlockEntity createBlockEntity(BlockView blockView) {
        return new ChunkActivatorBlockEntity();
    }

    @Override
    public BlockRenderType getRenderType(BlockState blockState_1) {
        return BlockRenderType.MODEL;
    }
}

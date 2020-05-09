package me.sargunvohra.mcmods.chunkactivator.block;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.dimension.DimensionType;

public class ChunkActivatorBlock extends Block 
{
    public static final BooleanProperty POWERED;

    static
    {
        POWERED = Properties.POWERED;
    }
    
    public ChunkActivatorBlock() 
    {
        super(
            FabricBlockSettings.of(Material.METAL, MaterialColor.GOLD)
                .strength(3f, 6f)
                .sounds(BlockSoundGroup.METAL));
        this.setDefaultState(this.getStateManager().getDefaultState().with(ChunkActivatorBlock.POWERED, false));
    }

    @Override
    protected void appendProperties(final StateManager.Builder<Block, BlockState> builder)
    {
        builder.add(ChunkActivatorBlock.POWERED);
    }

    @Override
    public void neighborUpdate(final BlockState state, final World world, final BlockPos pos, final Block block, final BlockPos neighborPos, final boolean moved) 
    {
        if (!world.isClient && block != this)
        {
            final boolean gotPower = world.isReceivingRedstonePower(pos);
            final boolean isPowered = state.get(ChunkActivatorBlock.POWERED);
            
            if (gotPower != isPowered)
            {
                world.setBlockState(pos, state.with(ChunkActivatorBlock.POWERED, gotPower && !isPowered), 4);
                world.getBlockTickScheduler().schedule(pos, this, this.getTickRate(world));
            }
        }
    }

    @Override
    public int getTickRate(final WorldView arg)
    {
        return 1;
    }

    @Override
    public void scheduledTick(final BlockState state, final ServerWorld world, final BlockPos pos, final Random random) 
    {
        final boolean isPowered = state.get(ChunkActivatorBlock.POWERED);
        if (isPowered)
        {
            forceLoad(world, pos, "add");
        }
        else
        {
            forceLoad(world, pos, "remove");
        }
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState state2, boolean bool)
    {
        if (!world.isClient && world.isReceivingRedstonePower(pos))
        {
            world.setBlockState(pos, state.with(ChunkActivatorBlock.POWERED, true), 4);
            world.getBlockTickScheduler().schedule(pos, this, this.getTickRate(world));
        }
		super.onBlockAdded(state, world, pos, state2, bool);
	}

    @Override
    public void onBlockRemoved(BlockState state, World world, BlockPos pos, BlockState state2, boolean bool)
    {
        if (state.get(ChunkActivatorBlock.POWERED))
            forceLoad(world, pos, "remove");
		super.onBlockRemoved(state, world, pos, state2, bool);
    }
    
    private static ServerCommandSource getCommandSource(World world, BlockPos pos) 
    {
        return new ServerCommandSource(
            world.getServer(),
            new Vec3d(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D),
            Vec2f.ZERO,
            (ServerWorld) world,
            4,
            "Chunk Activator",
            new LiteralText("Chunk Activator"),
            world.getServer(),
            null
        );
    }

    private static void forceLoad(World world, BlockPos pos, String addOrRemove) 
    {
        if (!world.isClient)
        {
            MinecraftServer server = world.getServer();
            String command = String.format(
                "forceload %s %s %s", addOrRemove, pos.getX(), pos.getZ());
            server.getCommandManager().execute(getCommandSource(world, pos), command);
        }
    }
}

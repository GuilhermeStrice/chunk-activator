package me.sargunvohra.mcmods.chunkactivator.block;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.text.LiteralText;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.dimension.DimensionType;

public class ChunkActivatorBlock extends Block 
{
    public static final BooleanProperty POWERED;

    public static Map<BetterChunkPos, Integer> refCount = new HashMap<>();

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
    }

    @Override
    protected void appendProperties(final StateManager.Builder<Block, BlockState> builder) {
        builder.add(ChunkActivatorBlock.POWERED);
    }

    @Override
    public void neighborUpdate(final BlockState state, final World world, final BlockPos pos, final Block block, final BlockPos neighborPos, final boolean moved) 
    {
        if (!world.isClient)
        {
            final boolean gotPower = world.isReceivingRedstonePower(pos) || world.isReceivingRedstonePower(pos.up());
            final boolean isPowered = state.get(ChunkActivatorBlock.POWERED);
            if (gotPower && !isPowered)
            {
                world.getBlockTickScheduler().schedule(pos, this, this.getTickRate(world));
                world.setBlockState(pos, state.with(ChunkActivatorBlock.POWERED, true), 4);
            }
            else if (!gotPower && isPowered)
            {
                world.getBlockTickScheduler().schedule(pos, this, this.getTickRate(world));
                world.setBlockState(pos, state.with(ChunkActivatorBlock.POWERED, false), 4);
            }
        }
    }

    @Override
    public int getTickRate(final WorldView arg) {
        return 1;
    }

    @Override
    public void scheduledTick(final BlockState state, final ServerWorld world, final BlockPos pos, final Random random) 
    {
        final boolean isPowered = state.get(ChunkActivatorBlock.POWERED);
        if (isPowered)
        {
            addChunkLoader(world, pos);
        }
        else
        {
            removeChunkLoader(world, pos);
        }
    }

    private int count(World world, BlockPos pos, int change) {
        if (world != null) {
            MinecraftServer server = world.getServer();
            if (server != null) {
                DimensionType dt = world.dimension.getType();
                BetterChunkPos key = new BetterChunkPos(new ChunkPos(pos), dt);
                int ret = refCount.getOrDefault(key, 0) + change;
                if (ret < 0) {
                    ret = 0;
                }
                refCount.put(key, ret);
                return ret;
            }
        }
        return -1;
    }

    private ServerCommandSource getCommandSource(World world, BlockPos pos) {
        Objects.requireNonNull(world);
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

    private void forceLoad(World world, BlockPos pos, String addOrRemove) {
        if (world != null) {
            MinecraftServer server = world.getServer();
            if (server != null) {
                String command = String.format(
                    "forceload %s %s %s", addOrRemove, pos.getX(), pos.getZ());
                server.getCommandManager().execute(getCommandSource(world, pos), command);
            }
        }
    }

    public void addChunkLoader(World world, BlockPos pos)
    {
        int c = count(world, pos, 1);
        if (c == 1)
        {
            forceLoad(world, pos, "add");
        }

        System.out.println(c);
    }

    public void removeChunkLoader(World world, BlockPos pos)
    {
        int c = count(world, pos, -1);
        if (c == 0) 
        {
            forceLoad(world, pos, "remove");
        }

        System.out.println(c);
    }

    public static class BetterChunkPos {
        private ChunkPos chunkPos;
        private DimensionType dimensionType;

        BetterChunkPos(ChunkPos chunkPos, DimensionType dimensionType) {
            this.chunkPos = chunkPos;
            this.dimensionType = dimensionType;
        }

        @Override
        public String toString() {
            return String.format(
                "RealChunkPos{chunkPos=%s, dimensionType=%s}", chunkPos, dimensionType);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BetterChunkPos that = (BetterChunkPos) o;
            return Objects.equals(chunkPos, that.chunkPos)
                && Objects.equals(dimensionType, that.dimensionType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(chunkPos, dimensionType);
        }
    }
}

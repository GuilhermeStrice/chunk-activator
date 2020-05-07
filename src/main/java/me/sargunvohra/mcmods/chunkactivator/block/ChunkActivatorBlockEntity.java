package me.sargunvohra.mcmods.chunkactivator.block;

import me.sargunvohra.mcmods.chunkactivator.ChunkActivator;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.logging.Log;
import org.apache.logging.log4j.core.appender.ConsoleAppender;

// TODO This class uses a hacky ref counting solution to fix issue #2
//  this should really be replaced with storing data on the chunk itself or something better idk
//  but this works for now, I'll fix it later (lol yeah, right) 
//  #clowntown

public class ChunkActivatorBlockEntity extends BlockEntity implements Tickable {

    public static Map<BetterChunkPos, Integer> refCount = new HashMap<>();
    private boolean alreadyLoading = false;

    public ChunkActivatorBlockEntity() {
        super(ChunkActivator.CHUNK_ACTIVATOR_ENTITY);
    }

    private int count(int change) {
        World world = getWorld();
        if (world != null) {
            MinecraftServer server = world.getServer();
            if (server != null) {
                BlockPos pos = getPos();
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

    private ServerCommandSource getCommandSource() {
        Objects.requireNonNull(world);
        return new ServerCommandSource(
            world.getServer(),
            new Vec3d(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D),
            Vec2f.ZERO,
            (ServerWorld) world,
            4,
            "Chunk Activator",
            new LiteralText("Chunk Activator"),
            world.getServer(),
            null
        );
    }

    private void forceLoad(String addOrRemove) {
        World world = getWorld();
        if (world != null) {
            MinecraftServer server = world.getServer();
            if (server != null) {
                BlockPos pos = getPos();
                String command = String.format(
                    "forceload %s %s %s", addOrRemove, pos.getX(), pos.getZ());
                server.getCommandManager().execute(getCommandSource(), command);
            }
        }
    }

    @Override
    public void markRemoved() {
        System.out.println("removed");
        super.markRemoved();
        if (count(-1) == 0) {
            forceLoad("remove");
            this.alreadyLoading = false;
        }
    }

    @Override
    public void cancelRemoval() {
        System.out.println("cancelled removed");
        super.cancelRemoval();
        this.alreadyLoading = true;
    }

    @Override
    public void tick() 
    {
        boolean gotRedstone = this.world.isReceivingRedstonePower(this.getPos());
        if (!this.alreadyLoading)
        {
            if (gotRedstone)
            {
                forceLoad("add");
                count(1);
                this.alreadyLoading = true;
            }
        }
        else
        {
            if (!gotRedstone)
            {
                this.alreadyLoading = false;
                if (count(-1) == 0) {
                    forceLoad("remove");
                }
            }
        }
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

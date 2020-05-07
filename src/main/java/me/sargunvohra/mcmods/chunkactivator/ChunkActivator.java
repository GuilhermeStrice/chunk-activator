package me.sargunvohra.mcmods.chunkactivator;

import me.sargunvohra.mcmods.chunkactivator.block.ChunkActivatorBlock;
import me.sargunvohra.mcmods.chunkactivator.block.ChunkActivatorBlockEntity;
import net.fabricmc.api.ModInitializer;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ChunkActivator implements ModInitializer {

    private static final Identifier CHUNK_ACTIVATOR_ID =
        new Identifier("chunkactivator", "chunk_activator");
    private static final Block CHUNK_ACTIVATOR_BLOCK = new ChunkActivatorBlock();
    private static final Item CHUNK_ACTIVATOR_ITEM =
        new BlockItem(CHUNK_ACTIVATOR_BLOCK, new Item.Settings().group(ItemGroup.DECORATIONS));
    public static final BlockEntityType<ChunkActivatorBlockEntity> CHUNK_ACTIVATOR_ENTITY =
        BlockEntityType.Builder.create(ChunkActivatorBlockEntity::new, CHUNK_ACTIVATOR_BLOCK).build(null);

    @Override
    public void onInitialize() {
        Registry.register(Registry.ITEM, CHUNK_ACTIVATOR_ID, CHUNK_ACTIVATOR_ITEM);
        Registry.register(Registry.BLOCK, CHUNK_ACTIVATOR_ID, CHUNK_ACTIVATOR_BLOCK);
        Registry.register(Registry.BLOCK_ENTITY_TYPE, CHUNK_ACTIVATOR_ID, CHUNK_ACTIVATOR_ENTITY);
    }
}

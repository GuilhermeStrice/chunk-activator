package me.sargunvohra.mcmods.chunkactivator.mixin;

import me.sargunvohra.mcmods.chunkactivator.block.ChunkActivatorBlockEntity;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer {

    @Inject(method = "run", at = @At("HEAD"))
    private void onRun(CallbackInfo ci) {
        ChunkActivatorBlockEntity.refCount.clear();
    }
}
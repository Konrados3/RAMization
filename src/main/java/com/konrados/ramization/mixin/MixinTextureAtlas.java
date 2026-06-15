package com.konrados.ramization.mixin;

import com.konrados.ramization.Config;
import com.konrados.ramization.RAMization;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;


@Mixin(TextureAtlas.class)
public abstract class MixinTextureAtlas {

    @Shadow
    private Map<ResourceLocation, ?> texturesByName;

    @Unique
    private long ramization$lastTrimMs = 0L;

    
    @Inject(method = "cycleAnimationFrames()V", at = @At("TAIL"))
    private void ramization$maybeTrimCache(CallbackInfo ci) {
        if (!Config.ENABLE_TEXTURE_CACHE_TRIM.getAsBoolean()) return;

        long now = System.currentTimeMillis();
        long intervalMs = (long) Config.TEXTURE_CACHE_TRIM_INTERVAL_SECONDS.getAsInt() * 1000L;

        if (now - ramization$lastTrimMs < intervalMs) return;
        ramization$lastTrimMs = now;

        
        if (texturesByName == null) return;
        try {
            int before = texturesByName.size();
            
            
            
            
            texturesByName.entrySet().removeIf(entry -> {
                String path = entry.getKey().getPath();
                
                
                return path.startsWith("block/destroy_stage_")
                    || path.startsWith("misc/effect_")
                    || path.startsWith("gui/options_background");
            });
            int after = texturesByName.size();
            if (before != after) {
                RAMization.LOGGER.debug(
                    "[RAMization][TextureAtlas] Trimmed {} cached sprite entries ({} → {}).",
                    before - after, before, after
                );
            }
        } catch (UnsupportedOperationException ignored) {
            
        }
    }
}




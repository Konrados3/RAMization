package com.konrados.ramization.mixin;

import com.konrados.ramization.memory.StringInternPool;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;


@Mixin(ResourceLocation.class)
public abstract class MixinResourceLocation {

    @ModifyVariable(
        method = "<init>(Ljava/lang/String;Ljava/lang/String;)V",
        at = @At("HEAD"),
        ordinal = 0,
        argsOnly = true
    )
    private static String ramization$internNamespace(String namespace) {
        if (!StringInternPool.resourceLocationInterningEnabled) return namespace;
        return StringInternPool.intern(namespace);
    }

    @ModifyVariable(
        method = "<init>(Ljava/lang/String;Ljava/lang/String;)V",
        at = @At("HEAD"),
        ordinal = 1,
        argsOnly = true
    )
    private static String ramization$internPath(String path) {
        if (!StringInternPool.resourceLocationInterningEnabled) return path;
        return StringInternPool.intern(path);
    }
}




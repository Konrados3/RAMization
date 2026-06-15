package com.konrados.ramization.mixin;

import com.konrados.ramization.memory.StringInternPool;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;


@Mixin(CompoundTag.class)
public abstract class MixinCompoundTag {

    

    @ModifyVariable(
        method = "put(Ljava/lang/String;Lnet/minecraft/nbt/Tag;)Lnet/minecraft/nbt/Tag;",
        at = @At("HEAD"), ordinal = 0, argsOnly = true
    )
    private String ramization$internPutKey(String key) {
        if (!StringInternPool.nbtKeyInterningEnabled) return key;
        return StringInternPool.intern(key);
    }

    @ModifyVariable(
        method = "putByte(Ljava/lang/String;B)V",
        at = @At("HEAD"), ordinal = 0, argsOnly = true
    )
    private String ramization$internPutByteKey(String key) {
        if (!StringInternPool.nbtKeyInterningEnabled) return key;
        return StringInternPool.intern(key);
    }

    @ModifyVariable(
        method = "putShort(Ljava/lang/String;S)V",
        at = @At("HEAD"), ordinal = 0, argsOnly = true
    )
    private String ramization$internPutShortKey(String key) {
        if (!StringInternPool.nbtKeyInterningEnabled) return key;
        return StringInternPool.intern(key);
    }

    @ModifyVariable(
        method = "putInt(Ljava/lang/String;I)V",
        at = @At("HEAD"), ordinal = 0, argsOnly = true
    )
    private String ramization$internPutIntKey(String key) {
        if (!StringInternPool.nbtKeyInterningEnabled) return key;
        return StringInternPool.intern(key);
    }

    @ModifyVariable(
        method = "putLong(Ljava/lang/String;J)V",
        at = @At("HEAD"), ordinal = 0, argsOnly = true
    )
    private String ramization$internPutLongKey(String key) {
        if (!StringInternPool.nbtKeyInterningEnabled) return key;
        return StringInternPool.intern(key);
    }

    @ModifyVariable(
        method = "putFloat(Ljava/lang/String;F)V",
        at = @At("HEAD"), ordinal = 0, argsOnly = true
    )
    private String ramization$internPutFloatKey(String key) {
        if (!StringInternPool.nbtKeyInterningEnabled) return key;
        return StringInternPool.intern(key);
    }

    @ModifyVariable(
        method = "putDouble(Ljava/lang/String;D)V",
        at = @At("HEAD"), ordinal = 0, argsOnly = true
    )
    private String ramization$internPutDoubleKey(String key) {
        if (!StringInternPool.nbtKeyInterningEnabled) return key;
        return StringInternPool.intern(key);
    }

    @ModifyVariable(
        method = "putString(Ljava/lang/String;Ljava/lang/String;)V",
        at = @At("HEAD"), ordinal = 0, argsOnly = true
    )
    private String ramization$internPutStringKey(String key) {
        if (!StringInternPool.nbtKeyInterningEnabled) return key;
        return StringInternPool.intern(key);
    }

    @ModifyVariable(
        method = "putByteArray(Ljava/lang/String;[B)V",
        at = @At("HEAD"), ordinal = 0, argsOnly = true
    )
    private String ramization$internPutByteArrayKey(String key) {
        if (!StringInternPool.nbtKeyInterningEnabled) return key;
        return StringInternPool.intern(key);
    }

    @ModifyVariable(
        method = "putIntArray(Ljava/lang/String;[I)V",
        at = @At("HEAD"), ordinal = 0, argsOnly = true
    )
    private String ramization$internPutIntArrayKey(String key) {
        if (!StringInternPool.nbtKeyInterningEnabled) return key;
        return StringInternPool.intern(key);
    }

    @ModifyVariable(
        method = "putLongArray(Ljava/lang/String;[J)V",
        at = @At("HEAD"), ordinal = 0, argsOnly = true
    )
    private String ramization$internPutLongArrayKey(String key) {
        if (!StringInternPool.nbtKeyInterningEnabled) return key;
        return StringInternPool.intern(key);
    }

    @ModifyVariable(
        method = "putBoolean(Ljava/lang/String;Z)V",
        at = @At("HEAD"), ordinal = 0, argsOnly = true
    )
    private String ramization$internPutBooleanKey(String key) {
        if (!StringInternPool.nbtKeyInterningEnabled) return key;
        return StringInternPool.intern(key);
    }

    

    @ModifyVariable(
        method = "get(Ljava/lang/String;)Lnet/minecraft/nbt/Tag;",
        at = @At("HEAD"), ordinal = 0, argsOnly = true
    )
    private String ramization$internGetKey(String key) {
        if (!StringInternPool.nbtKeyInterningEnabled) return key;
        return StringInternPool.intern(key);
    }

    @ModifyVariable(
        method = "contains(Ljava/lang/String;)Z",
        at = @At("HEAD"), ordinal = 0, argsOnly = true
    )
    private String ramization$internContainsKey(String key) {
        if (!StringInternPool.nbtKeyInterningEnabled) return key;
        return StringInternPool.intern(key);
    }
}




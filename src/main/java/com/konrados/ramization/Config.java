package com.konrados.ramization;

import com.konrados.ramization.memory.StringInternPool;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = RAMization.MODID)
public class Config {

    public static final String MOD_VERSION = "1.0.0";

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLE_SMART_GC = BUILDER
            .comment("Enable the SmartGC Scheduler.",
                     "When true, garbage collection is only triggered during idle server ticks",
                     "to avoid lag spikes caused by GC pauses mid-tick.")
            .define("smartGC.enabled", true);

    public static final ModConfigSpec.IntValue GC_HEAP_THRESHOLD_PERCENT = BUILDER
            .comment("How full the JVM heap must be (%) before SmartGC will request a collection.",
                     "Range: 40-95. Default: 70.")
            .defineInRange("smartGC.heapThresholdPercent", 70, 40, 95);

    public static final ModConfigSpec.IntValue GC_IDLE_TICK_MS = BUILDER
            .comment("Maximum milliseconds a recent tick may have taken to be considered 'idle'.",
                     "Range: 10-48. Default: 30.")
            .defineInRange("smartGC.idleTickMs", 30, 10, 48);

    public static final ModConfigSpec.IntValue GC_IDLE_TICKS_REQUIRED = BUILDER
            .comment("How many consecutive idle ticks must occur before GC is triggered.",
                     "Range: 2-20. Default: 5.")
            .defineInRange("smartGC.idleTicksRequired", 5, 2, 20);

    public static final ModConfigSpec.IntValue GC_COOLDOWN_SECONDS = BUILDER
            .comment("Minimum seconds between two SmartGC-triggered collections.",
                     "Range: 30-600. Default: 120.")
            .defineInRange("smartGC.cooldownSeconds", 120, 30, 600);

    public static final ModConfigSpec.BooleanValue ENABLE_RESOURCE_LOCATION_INTERNING = BUILDER
            .comment("Deduplicate ResourceLocation namespace/path strings via a shared intern pool.")
            .define("interning.resourceLocations", true);

    public static final ModConfigSpec.BooleanValue ENABLE_NBT_KEY_INTERNING = BUILDER
            .comment("Deduplicate CompoundTag key strings via the intern pool.")
            .define("interning.nbtKeys", true);

    public static final ModConfigSpec.IntValue INTERN_POOL_MAX_SIZE = BUILDER
            .comment("Maximum number of unique strings held in the intern pool.",
                     "0 = unlimited. Range: 0-500000. Default: 100000.")
            .defineInRange("interning.poolMaxSize", 100_000, 0, 500_000);

    public static final ModConfigSpec.BooleanValue ENABLE_TEXTURE_CACHE_TRIM = BUILDER
            .comment("Periodically trim unused textures from the atlas cache on the client. CLIENT-SIDE ONLY.")
            .define("textureCache.enableTrim", true);

    public static final ModConfigSpec.IntValue TEXTURE_CACHE_TRIM_INTERVAL_SECONDS = BUILDER
            .comment("How often (in seconds) to check and trim the texture cache. Range: 30-3600. Default: 300.")
            .defineInRange("textureCache.trimIntervalSeconds", 300, 30, 3600);

    public static final ModConfigSpec.BooleanValue LOG_PERIODIC_STATS = BUILDER
            .comment("Log memory savings statistics to the console every 5 minutes.")
            .define("logging.periodicStats", true);

    public static final ModConfigSpec.BooleanValue LOG_GC_EVENTS = BUILDER
            .comment("Log a message each time SmartGC triggers a garbage collection.")
            .define("logging.gcEvents", false);

    static final ModConfigSpec SPEC = BUILDER.build();

    @SubscribeEvent
    static void onLoad(ModConfigEvent.Loading event) {
        syncFlags();
    }

    @SubscribeEvent
    static void onReload(ModConfigEvent.Reloading event) {
        syncFlags();
    }

    private static void syncFlags() {
        StringInternPool.resourceLocationInterningEnabled = ENABLE_RESOURCE_LOCATION_INTERNING.getAsBoolean();
        StringInternPool.nbtKeyInterningEnabled           = ENABLE_NBT_KEY_INTERNING.getAsBoolean();
        StringInternPool.poolMaxSize                      = INTERN_POOL_MAX_SIZE.getAsInt();
    }
}




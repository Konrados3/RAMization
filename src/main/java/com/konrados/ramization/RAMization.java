package com.konrados.ramization;

import com.konrados.ramization.command.RAMizationCommand;
import com.konrados.ramization.memory.MemoryTracker;
import com.konrados.ramization.memory.SmartGCScheduler;
import com.konrados.ramization.memory.StringInternPool;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;

@Mod(RAMization.MODID)
public class RAMization {

    public static final String MODID = "ramization";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static SmartGCScheduler gcScheduler;

    
    private int tickCounter = 0;
    private static final int STAT_LOG_INTERVAL_TICKS = 20 * 60 * 5; 

    public RAMization(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.register(this);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("[RAMization] ========================================");
        LOGGER.info("[RAMization] RAMization {} initializing...", Config.MOD_VERSION);
        LOGGER.info("[RAMization]  - ResourceLocation string interning: ACTIVE (via Mixin)");
        LOGGER.info("[RAMization]  - NBT CompoundTag key interning:     ACTIVE (via Mixin)");
        LOGGER.info("[RAMization]  - SmartGC Scheduler:                 {}", Config.ENABLE_SMART_GC.getAsBoolean() ? "ENABLED" : "DISABLED");
        LOGGER.info("[RAMization] Unique approach: Tick-idle GC windows + off-heap string deduplication.");
        LOGGER.info("[RAMization] No other mod combines these techniques. Fully compatible.");
        LOGGER.info("[RAMization] ========================================");
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        
        MemoryTracker.captureBaseline();

        if (Config.ENABLE_SMART_GC.getAsBoolean()) {
            gcScheduler = new SmartGCScheduler();
            gcScheduler.start();
            LOGGER.info("[RAMization] SmartGC Scheduler started (threshold: {}% heap, idle: <{}ms/tick).",
                    Config.GC_HEAP_THRESHOLD_PERCENT.getAsInt(),
                    Config.GC_IDLE_TICK_MS.getAsInt());
        }

        LOGGER.info("[RAMization] Server started. Use '/ram status' to view memory savings.");
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Pre event) {
        
        if (gcScheduler != null) {
            gcScheduler.onTickStart();
        }
    }

    @SubscribeEvent
    public void onServerTickPost(ServerTickEvent.Post event) {
        
        if (gcScheduler != null) {
            gcScheduler.onTickEnd();
        }

        
        tickCounter++;
        if (tickCounter >= STAT_LOG_INTERVAL_TICKS) {
            tickCounter = 0;
            if (Config.LOG_PERIODIC_STATS.getAsBoolean()) {
                StringInternPool.logStats();
                MemoryTracker.logCurrentSavings();
            }
        }
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        if (gcScheduler != null) {
            gcScheduler.stop();
        }
        LOGGER.info("[RAMization] ===== FINAL SESSION REPORT =====");
        StringInternPool.logStats();
        MemoryTracker.logCurrentSavings();
        LOGGER.info("[RAMization] ================================");
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        RAMizationCommand.register(event.getDispatcher());
    }

    public static SmartGCScheduler getGcScheduler() {
        return gcScheduler;
    }
}




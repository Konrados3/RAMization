package com.konrados.ramization.memory;

import com.konrados.ramization.RAMization;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;


public final class MemoryTracker {

    private static final MemoryMXBean MEMORY_BEAN = ManagementFactory.getMemoryMXBean();

    private static long baselineHeapMb   = -1L;
    private static long baselineTimestamp = 0L;

    private MemoryTracker() {}

    
    public static void captureBaseline() {
        MemoryUsage heap   = MEMORY_BEAN.getHeapMemoryUsage();
        baselineHeapMb     = heap.getUsed() / (1024 * 1024);
        baselineTimestamp  = System.currentTimeMillis();
        RAMization.LOGGER.info("[RAMization] Baseline heap captured: {} MB used.", baselineHeapMb);
    }

    
    public static long currentHeapMb() {
        return MEMORY_BEAN.getHeapMemoryUsage().getUsed() / (1024 * 1024);
    }

    
    public static long maxHeapMb() {
        return MEMORY_BEAN.getHeapMemoryUsage().getMax() / (1024 * 1024);
    }

    
    public static long estimatedMbSavedVsBaseline() {
        if (baselineHeapMb < 0) return 0L;
        return baselineHeapMb - currentHeapMb();
    }

    
    public static double totalEstimatedMbSaved(SmartGCScheduler gc) {
        double gcSaved      = gc != null ? gc.getTotalMbReclaimed() : 0.0;
        double internSaved  = StringInternPool.getMbSaved();
        return gcSaved + internSaved;
    }

    
    public static void logCurrentSavings() {
        SmartGCScheduler gc = RAMization.getGcScheduler();
        RAMization.LOGGER.info(
            "[RAMization][MemoryTracker] heap={}/{} MB | GC reclaimed≈{} MB | string dedup≈{:.2f} MB | combined≈{:.2f} MB",
            currentHeapMb(),
            maxHeapMb(),
            gc != null ? gc.getTotalMbReclaimed() : 0,
            StringInternPool.getMbSaved(),
            totalEstimatedMbSaved(gc)
        );
    }

    public static long getBaselineHeapMb()   { return baselineHeapMb; }
    public static long getBaselineTimestamp() { return baselineTimestamp; }
}




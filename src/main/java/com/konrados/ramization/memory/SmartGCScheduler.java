package com.konrados.ramization.memory;

import com.konrados.ramization.Config;
import com.konrados.ramization.RAMization;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;


public final class SmartGCScheduler {

    private static final MemoryMXBean MEMORY_BEAN = ManagementFactory.getMemoryMXBean();

    
    private final Deque<Long> tickWindow = new ArrayDeque<>();
    private final int windowSize;

    private long tickStartNano = 0L;
    private final AtomicLong lastGcTimeMs = new AtomicLong(0L);
    private final AtomicLong totalGcTriggered = new AtomicLong(0L);
    private final AtomicLong totalMbReclaimed = new AtomicLong(0L);
    private final AtomicBoolean running = new AtomicBoolean(false);

    public SmartGCScheduler() {
        this.windowSize = Config.GC_IDLE_TICKS_REQUIRED.getAsInt();
    }

    public void start() {
        running.set(true);
    }

    public void stop() {
        running.set(false);
        tickWindow.clear();
    }

    
    public void onTickStart() {
        if (!running.get()) return;
        tickStartNano = System.nanoTime();
    }

    
    public void onTickEnd() {
        if (!running.get() || tickStartNano == 0L) return;

        long durationNano = System.nanoTime() - tickStartNano;
        long durationMs   = durationNano / 1_000_000L;

        
        if (tickWindow.size() >= windowSize) {
            tickWindow.poll();
        }
        tickWindow.offer(durationMs);

        
        if (tickWindow.size() < windowSize) return;

        
        long idleThresholdMs = Config.GC_IDLE_TICK_MS.getAsInt();
        for (long t : tickWindow) {
            if (t >= idleThresholdMs) return; 
        }

        
        long cooldownMs = Config.GC_COOLDOWN_SECONDS.getAsInt() * 1000L;
        long now = System.currentTimeMillis();
        if (now - lastGcTimeMs.get() < cooldownMs) return;

        
        MemoryUsage heap = MEMORY_BEAN.getHeapMemoryUsage();
        long maxHeap  = heap.getMax();
        long usedHeap = heap.getUsed();
        if (maxHeap <= 0) return;

        int usedPercent = (int) ((usedHeap * 100L) / maxHeap);
        int threshold   = Config.GC_HEAP_THRESHOLD_PERCENT.getAsInt();
        if (usedPercent < threshold) return;

        
        triggerGC(usedHeap, usedPercent);
    }

    private void triggerGC(long heapBefore, int heapPercentBefore) {
        lastGcTimeMs.set(System.currentTimeMillis());
        long countBefore = getTotalGcCollections();

        if (Config.LOG_GC_EVENTS.getAsBoolean()) {
            RAMization.LOGGER.info(
                "[RAMization][SmartGC] Requesting GC — heap at {}% ({} MB used). Server tick window was idle.",
                heapPercentBefore,
                heapBefore / (1024 * 1024)
            );
        }

        System.gc();

        
        long heapAfter      = MEMORY_BEAN.getHeapMemoryUsage().getUsed();
        long reclaimedBytes = Math.max(0L, heapBefore - heapAfter);
        long reclaimedMb    = reclaimedBytes / (1024 * 1024);
        totalMbReclaimed.addAndGet(reclaimedMb);

        long countAfter = getTotalGcCollections();
        boolean actualGcRan = countAfter > countBefore;

        totalGcTriggered.incrementAndGet();

        if (Config.LOG_GC_EVENTS.getAsBoolean()) {
            RAMization.LOGGER.info(
                "[RAMization][SmartGC] GC {} — reclaimed ≈{} MB. Total reclaimed this session: {} MB.",
                actualGcRan ? "ran" : "hint ignored by JVM",
                reclaimedMb,
                totalMbReclaimed.get()
            );
        }
    }

    private long getTotalGcCollections() {
        List<GarbageCollectorMXBean> beans = ManagementFactory.getGarbageCollectorMXBeans();
        return beans.stream().mapToLong(GarbageCollectorMXBean::getCollectionCount).sum();
    }

    

    public long getTotalGcTriggered()   { return totalGcTriggered.get(); }
    public long getTotalMbReclaimed()   { return totalMbReclaimed.get(); }

    
    public long getCurrentHeapMb() {
        return MEMORY_BEAN.getHeapMemoryUsage().getUsed() / (1024 * 1024);
    }

    
    public long getMaxHeapMb() {
        return MEMORY_BEAN.getHeapMemoryUsage().getMax() / (1024 * 1024);
    }

    
    public int getCurrentHeapPercent() {
        MemoryUsage h = MEMORY_BEAN.getHeapMemoryUsage();
        if (h.getMax() <= 0) return 0;
        return (int) ((h.getUsed() * 100L) / h.getMax());
    }

    public boolean isRunning() { return running.get(); }
}




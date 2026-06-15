package com.konrados.ramization.memory;

import com.konrados.ramization.RAMization;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;


public final class StringInternPool {

    
    
    
    public static volatile boolean resourceLocationInterningEnabled = true;
    public static volatile boolean nbtKeyInterningEnabled           = true;
    public static volatile int     poolMaxSize                      = 100_000;

    
    private static final ConcurrentHashMap<String, String> POOL = new ConcurrentHashMap<>(65_536);

    
    private static final AtomicLong HITS          = new AtomicLong(0);
    private static final AtomicLong MISSES        = new AtomicLong(0);
    private static final AtomicLong BYTES_SAVED   = new AtomicLong(0);
    private static final AtomicLong STRINGS_ADDED = new AtomicLong(0);

    
    private static final int JAVA_STRING_OVERHEAD_BYTES = 32;

    private StringInternPool() {}

    
    public static String intern(String s) {
        if (s == null) return null;

        
        String existing = POOL.get(s);
        if (existing != null) {
            if (existing != s) {
                long saved = JAVA_STRING_OVERHEAD_BYTES + (long) s.length() * 2;
                BYTES_SAVED.addAndGet(saved);
                HITS.incrementAndGet();
            }
            return existing;
        }

        
        MISSES.incrementAndGet();
        int max = poolMaxSize;
        if (max > 0 && POOL.size() >= max) {
            return s;
        }

        
        String winner = POOL.putIfAbsent(s, s);
        if (winner == null) {
            STRINGS_ADDED.incrementAndGet();
            return s;
        } else {
            long saved = JAVA_STRING_OVERHEAD_BYTES + (long) s.length() * 2;
            BYTES_SAVED.addAndGet(saved);
            HITS.incrementAndGet();
            return winner;
        }
    }

    
    public static void clear() {
        POOL.clear();
        HITS.set(0);
        MISSES.set(0);
        BYTES_SAVED.set(0);
        STRINGS_ADDED.set(0);
        RAMization.LOGGER.info("[RAMization] StringInternPool cleared.");
    }

    

    public static long getHits()        { return HITS.get(); }
    public static long getMisses()      { return MISSES.get(); }
    public static long getPoolSize()    { return POOL.size(); }
    public static long getBytesSaved()  { return BYTES_SAVED.get(); }

    public static double getMbSaved() {
        return BYTES_SAVED.get() / (1024.0 * 1024.0);
    }

    public static double getHitRate() {
        long total = HITS.get() + MISSES.get();
        return total == 0 ? 0.0 : (HITS.get() * 100.0 / total);
    }

    public static void logStats() {
        RAMization.LOGGER.info(
            "[RAMization][StringInternPool] pool={} strings | hits={} | misses={} | hit-rate={:.1f}% | saved≈{:.2f} MB",
            getPoolSize(), getHits(), getMisses(), getHitRate(), getMbSaved()
        );
    }
}




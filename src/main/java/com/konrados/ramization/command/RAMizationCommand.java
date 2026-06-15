package com.konrados.ramization.command;

import com.konrados.ramization.RAMization;
import com.konrados.ramization.memory.MemoryTracker;
import com.konrados.ramization.memory.SmartGCScheduler;
import com.konrados.ramization.memory.StringInternPool;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;


public final class RAMizationCommand {

    private RAMizationCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("ram")
                .requires(src -> src.hasPermission(2))

                
                .then(Commands.literal("status")
                    .executes(RAMizationCommand::executeStatus))

                
                .then(Commands.literal("gc")
                    .executes(RAMizationCommand::executeGc))

                
                .then(Commands.literal("clearpool")
                    .executes(RAMizationCommand::executeClearPool))

                
                .executes(RAMizationCommand::executeStatus)
        );
    }

    

    private static int executeStatus(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        SmartGCScheduler gc   = RAMization.getGcScheduler();

        long usedMb  = MemoryTracker.currentHeapMb();
        long maxMb   = MemoryTracker.maxHeapMb();
        long baseMb  = MemoryTracker.getBaselineHeapMb();
        int  pct     = maxMb > 0 ? (int) (usedMb * 100 / maxMb) : 0;

        double internSaved = StringInternPool.getMbSaved();
        long   gcSaved     = gc != null ? gc.getTotalMbReclaimed() : 0L;
        double total       = internSaved + gcSaved;

        send(src, "§6§l━━━ RAMization Status ━━━");
        send(src, String.format("§eHeap: §f%d / %d MB §7(%d%%)", usedMb, maxMb, pct));
        if (baseMb >= 0) {
            long delta = baseMb - usedMb;
            String sign = delta >= 0 ? "§a-" : "§c+";
            send(src, String.format("§eBaseline: §f%d MB §7(current is %s%d MB vs startup§7)",
                baseMb, sign, Math.abs(delta)));
        }
        send(src, "§6§lString Intern Pool");
        send(src, String.format("  §ePool size: §f%,d strings", StringInternPool.getPoolSize()));
        send(src, String.format("  §eHit rate:  §f%.1f%%", StringInternPool.getHitRate()));
        send(src, String.format("  §eSaved:      §f%.2f MB §7(deduplicated strings)", internSaved));
        send(src, "§6§lSmartGC Scheduler");
        if (gc != null && gc.isRunning()) {
            send(src, String.format("  §eStatus:    §aRUNNING"));
            send(src, String.format("  §eGC events: §f%d", gc.getTotalGcTriggered()));
            send(src, String.format("  §eReclaimed: §f%d MB §7(heap reclaimed by GC)", gc.getTotalMbReclaimed()));
        } else {
            send(src, "  §eStatus:    §cDISABLED §7(set smartGC.enabled=true to enable)");
        }
        send(src, String.format("§a§lTotal estimated savings: %.2f MB", total));
        send(src, "§6§l━━━━━━━━━━━━━━━━━━━━━━━");

        return 1;
    }

    

    private static int executeGc(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        long before = MemoryTracker.currentHeapMb();
        send(src, String.format("§eRequesting GC... heap before: §f%d MB", before));

        System.gc();

        
        try { Thread.sleep(200); } catch (InterruptedException ignored) {}

        long after = MemoryTracker.currentHeapMb();
        long diff  = before - after;
        send(src, String.format(
            "§aGC complete. Heap: §f%d MB §7(reclaimed ≈%d MB)", after, diff));

        return 1;
    }

    

    private static int executeClearPool(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        long size = StringInternPool.getPoolSize();
        StringInternPool.clear();
        send(src, String.format("§aString intern pool cleared. Removed §f%,d §aentries.", size));
        send(src, "§7Note: The pool will repopulate naturally over the next few minutes.");
        return 1;
    }

    

    private static void send(CommandSourceStack src, String message) {
        src.sendSuccess(() -> Component.literal(message), false);
    }
}




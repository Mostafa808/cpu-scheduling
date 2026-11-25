import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A single priority tier (Tier-2) that holds processes waiting for the CPU.
 * Aging boosts the effective priority by 1 for every {@code agingInterval}
 * time units a process has been waiting in this tier.
 *
 * Lower numeric priority value == higher urgency (0 is most urgent).
 */
public class PriorityTier {
    private final List<SimProcess> queue = new ArrayList<>();
    /** Tracks the time each process entered this tier (independent of SimProcess state). */
    private final Map<SimProcess, Integer> entryTimes = new HashMap<>();
    private final int agingInterval;

    public PriorityTier(int agingInterval) {
        if (agingInterval <= 0) throw new IllegalArgumentException("Aging interval must be > 0");
        this.agingInterval = agingInterval;
    }

    /** Add a process to this tier at the given simulation time. */
    public void add(SimProcess process, int time) {
        if (process == null) throw new IllegalArgumentException("Process cannot be null");
        queue.add(process);
        entryTimes.put(process, time);
    }

    /**
     * Pick the process with the lowest effective priority (highest urgency).
     * Ties are broken by arrival time (FCFS).
     * Does NOT remove the process from the queue.
     */
    public SimProcess chooseNext(int time) {
        if (queue.isEmpty()) return null;
        SimProcess best = null;
        int bestPriority = Integer.MAX_VALUE;
        for (SimProcess p : queue) {
            int ep = effectivePriority(p, time);
            if (ep < bestPriority || (ep == bestPriority && best != null &&
                    p.getArrivalTime() < best.getArrivalTime())) {
                bestPriority = ep;
                best = p;
            }
        }
        return best;
    }

    /**
     * Effective priority = initialPriority – agingBoost.
     * agingBoost = floor(waitingInTier2 / agingInterval), clamped so result >= 0.
     */
    public int effectivePriority(SimProcess process, int time) {
        if (process == null) throw new IllegalArgumentException("Process cannot be null");
        Integer entryTime = entryTimes.get(process);
        if (entryTime == null) return process.getInitialPriority();
        int waited = Math.max(0, time - entryTime);
        int boost  = waited / agingInterval;
        return Math.max(0, process.getInitialPriority() - boost);
    }

    /**
     * Human-readable snapshot of the tier's queue at the given time.
     */
    public String snapshot(int time) {
        if (queue.isEmpty()) return "tier2:[]";
        StringBuilder sb = new StringBuilder("tier2:[");
        for (SimProcess p : queue) {
            sb.append(p.getId())
              .append("(ep:").append(effectivePriority(p, time))
              .append(",rem:").append(p.getRemainingTime())
              .append("), ");
        }
        sb.setLength(sb.length() - 2);
        sb.append("]");
        return sb.toString();
    }

    public boolean isEmpty() { return queue.isEmpty(); }

    /** Direct access to the backing list (e.g. to remove a chosen process). */
    public List<SimProcess> getQueue() { return queue; }

    public int getAgingInterval() { return agingInterval; }
}

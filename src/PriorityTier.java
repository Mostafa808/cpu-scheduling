import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tier-2 aging queue.
 *
 * Lower numeric priority means higher urgency.
 */
public class PriorityTier {

    private final List<SimProcess> queue = new ArrayList<>();
    private final Map<SimProcess, Integer> entryTimes = new HashMap<>();

    private final int agingInterval;

    public PriorityTier(int agingInterval) {
        if (agingInterval <= 0) {
            throw new IllegalArgumentException(
                    "Aging interval must be > 0");
        }

        this.agingInterval = agingInterval;
    }

    /**
     * First entry into Tier-2.
     */
    public void add(SimProcess process, int time) {
        if (process == null) {
            throw new IllegalArgumentException(
                    "Process cannot be null");
        }

        queue.add(process);
        entryTimes.put(process, time);
    }

    /**
     * Reinsert a process that was already in Tier-2
     * without resetting aging history.
     */
    public void requeuePreempted(SimProcess process) {
        if (process == null) {
            throw new IllegalArgumentException(
                    "Process cannot be null");
        }

        if (!queue.contains(process)) {
            queue.add(process);
        }
    }

    /**
     * Remove selected process from Tier-2.
     */
    public void remove(SimProcess process) {
        queue.remove(process);
        entryTimes.remove(process);
    }

    public SimProcess chooseNext(int time) {

        if (queue.isEmpty()) {
            return null;
        }

        SimProcess best = null;
        int bestPriority = Integer.MAX_VALUE;

        for (SimProcess p : queue) {

            int ep = effectivePriority(p, time);

            if (best == null
                    || ep < bestPriority
                    || (ep == bestPriority
                    && p.getArrivalTime()
                    < best.getArrivalTime())) {

                best = p;
                bestPriority = ep;
            }
        }

        return best;
    }

    public int effectivePriority(
            SimProcess process,
            int time) {

        Integer entryTime = entryTimes.get(process);

        if (entryTime == null) {
            return process.getInitialPriority();
        }

        int waited = Math.max(0, time - entryTime);

        int boost = waited / agingInterval;

        return Math.max(
                0,
                process.getInitialPriority() - boost);
    }

    public String snapshot(int time) {

        StringBuilder sb = new StringBuilder();

        sb.append("tier2:[");

        for (SimProcess p : queue) {

            sb.append(p.getId())
                    .append("(remaining:")
                    .append(p.getRemainingTime())
                    .append(", effectivePriority:")
                    .append(effectivePriority(p, time))
                    .append("), ");
        }

        if (!queue.isEmpty()) {
            sb.setLength(sb.length() - 2);
        }

        sb.append("]");

        return sb.toString();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public List<SimProcess> getQueue() {
        return queue;
    }

    public int getAgingInterval() {
        return agingInterval;
    }
}
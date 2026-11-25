import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * ACS – Aging + Context-Switch Priority Scheduler
 *
 * Two-tier model:
 *   Tier-1 (ready queue) : Round-Robin with a fixed quantum, sorted by initial priority.
 *   Tier-2 (waiting queue): Processes demoted after their quantum expires. Aging boosts
 *                            their effective priority every {@code agingInterval} time units.
 *
 * A process starts in Tier-1. After using its full quantum without finishing,
 * it moves to Tier-2. When Tier-1 is empty, the best process from Tier-2 is promoted.
 */
public class ACSScheduler {

    public static SimulationResult run(List<SimProcess> processes, int quantum,
                                       int cs, int agingInterval) {
        // Work on copies so the originals remain pristine
        List<SimProcess> copies = new ArrayList<>();
        for (SimProcess p : processes) copies.add(p.copy());

        SimulationResult result = new SimulationResult("ACSScheduler", copies);

        // Arrival queue – ordered by arrival time
        PriorityQueue<SimProcess> arrivalQueue =
                new PriorityQueue<>(new SimProcess.DefaultComparator());
        arrivalQueue.addAll(copies);

        // Tier-1: simple FIFO (priority preserved by insertion order at arrival)
        LinkedList<SimProcess> tier1 = new LinkedList<>();

        // Tier-2: aging priority queue
        PriorityTier tier2 = new PriorityTier(agingInterval);

        List<SimProcess> finished = new ArrayList<>();

        int time          = 0;
        int quantumLeft   = 0;
        boolean switching = false;
        int switchLeft    = 0;
        SimProcess current = null;
        TimeStep lastStep  = null;

        while (finished.size() < copies.size()) {

            // --- arrivals ---
            while (!arrivalQueue.isEmpty() && arrivalQueue.peek().getArrivalTime() == time) {
                tier1.add(arrivalQueue.poll());
            }

            // --- CPU cycle ---
            if (switching) {
                switchLeft--;
            } else if (current != null) {
                current.executeOneUnit();
                quantumLeft--;
            }

            // --- process finished? ---
            if (current != null && current.getRemainingTime() == 0) {
                current.finish(time);
                finished.add(current);
                current = null;
                quantumLeft = 0;
            }

            // --- context-switch finished? ---
            if (switching && switchLeft == 0) {
                switching = false;
            }

            if (switching) {
                // record CS step
                String snap1 = snapshotTier1(tier1);
                String snap2 = tier2.snapshot(time);
                lastStep = new TimeStep(time, "CS",
                        (current != null ? current.getId() : ""),
                        snap1, snap2, "context switching");
                result.addStep(lastStep);
                time++;
                continue;
            }

            // --- quantum expired => demote to Tier-2 ---
            if (current != null && quantumLeft == 0) {
                tier2.add(current, time);
                current = null;
            }

            // --- pick next process ---
            if (current == null) {
                SimProcess next = null;

                if (!tier1.isEmpty()) {
                    next = tier1.poll();
                } else if (!tier2.isEmpty()) {
                    next = tier2.chooseNext(time);
                    tier2.getQueue().remove(next);
                }

                if (next != null) {
                    if (cs > 0 && lastStep != null &&
                            !"IDLE".equals(lastStep.getKind()) &&
                            !"CS".equals(lastStep.getKind())) {
                        // insert context-switch delay
                        switching   = true;
                        switchLeft  = cs;
                        quantumLeft = quantum;
                        current     = next;
                        String snap1 = snapshotTier1(tier1);
                        String snap2 = tier2.snapshot(time);
                        lastStep = new TimeStep(time, "CS", next.getId(),
                                snap1, snap2, "switching to " + next.getId());
                        result.addStep(lastStep);
                        time++;
                        continue;
                    } else {
                        current     = next;
                        quantumLeft = quantum;
                    }
                }
            }

            // --- record time step ---
            String snap1 = snapshotTier1(tier1);
            String snap2 = tier2.snapshot(time);

            if (current != null) {
                current.startIfFirst(time);
                lastStep = new TimeStep(time, "RUN", current.getId(),
                        snap1, snap2, current.getId() + " running");
            } else {
                lastStep = new TimeStep(time, "IDLE", null, snap1, snap2, "cpu idle");
            }
            result.addStep(lastStep);
            time++;
        }

        return result;
    }

    private static String snapshotTier1(LinkedList<SimProcess> tier1) {
        if (tier1.isEmpty()) return "tier1:[]";
        StringBuilder sb = new StringBuilder("tier1:[");
        for (SimProcess p : tier1) {
            sb.append(p.getId())
              .append("(rem:").append(p.getRemainingTime()).append("), ");
        }
        sb.setLength(sb.length() - 2);
        sb.append("]");
        return sb.toString();
    }
}

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

public class ACSScheduler {

    public static SimulationResult run(List<SimProcess> processes, int quantum, int cs, int agingInterval) {

        List<SimProcess> copies = new ArrayList<>();

        for (SimProcess p : processes) {
            copies.add(p.copy());
        }

        SimulationResult result = new SimulationResult("ACSScheduler", copies);
        PriorityQueue<SimProcess> arrivalQueue = new PriorityQueue<>(new SimProcess.DefaultComparator());
        
        arrivalQueue.addAll(copies);
        LinkedList<SimProcess> tier1 = new LinkedList<>();
        PriorityTier tier2 = new PriorityTier(agingInterval);
        List<SimProcess> finished = new ArrayList<>();
        
        int time = 0;
        int quantumLeft = 0;
        boolean switching = false;
        int switchLeft = 0;
        boolean runningFromTier2 = false;
        SimProcess current = null;
        TimeStep lastStep = null;
        while (finished.size() < copies.size()) {
            /*
             * ARRIVALS
             */
            boolean arrivalOccurred = false;
            while (!arrivalQueue.isEmpty() && arrivalQueue.peek().getArrivalTime() == time) {
                tier1.add(arrivalQueue.poll());
                arrivalOccurred = true;
            }

            /*
             * Tier-2 preemption by Tier-1 arrivals.
             */
            if (arrivalOccurred && runningFromTier2 && current != null) {
                tier2.requeuePreempted(current);
                current = null;
                runningFromTier2 = false;

                if (cs > 0) {
                    switching = true;
                    switchLeft = cs;
                }
            }

            /*
             * CPU CYCLE
             */
            if (switching) {
                switchLeft--;
            } else if (current != null) {
                current.executeOneUnit();
                if (!runningFromTier2) {
                    quantumLeft--;
                }
            }

            /*
             * FINISHED
             */
            if (current != null && current.getRemainingTime() == 0) {
                current.finish(time);
                finished.add(current);
                
                current = null;
                quantumLeft = 0;
                runningFromTier2 = false;
            }

            /*
             * CONTEXT SWITCH FINISHED
             */
            if (switching && switchLeft == 0) {
                switching = false;
            }

            /*
             * DURING CONTEXT SWITCH
             */
            if (switching) {
                lastStep = new TimeStep(time, "CS", null, snapshot(current, tier1, time), tier2.snapshot(time), "context switching");
                result.addStep(lastStep);
                time++;
                continue;
            }

            /*
             * QUANTUM EXPIRATION
             */
            if (current != null && !runningFromTier2 && quantumLeft == 0) {
                tier2.add(current, time);
                current = null;
            }

            /*
             * DISPATCH
             */
            if (current == null) {
                SimProcess next = null;
                boolean nextFromTier2 = false;

                if (!tier1.isEmpty()) {
                    next = tier1.poll();
                } else if (!tier2.isEmpty()) {
                    next = tier2.chooseNext(time);
                    tier2.remove(next);
                    nextFromTier2 = true;
                }

                if (next != null) {
                    if (cs > 0 && lastStep != null && !"CS".equals(lastStep.getKind()) && !"IDLE".equals(lastStep.getKind())) {
                        switching = true;
                        switchLeft = cs;

                        current = next;
                        runningFromTier2 = nextFromTier2;

                        quantumLeft = nextFromTier2 ? -1: quantum;
                        lastStep = new TimeStep(time, "CS", next.getId(), snapshot(current, tier1, time), tier2.snapshot(time), "switching to " + next.getId());
                        result.addStep(lastStep);

                        time++;
                        continue;
                    }

                    current = next;
                    
                    runningFromTier2 = nextFromTier2;
                    quantumLeft = nextFromTier2 ? -1: quantum;
                }
            }

            /*
             * LOG STEP
             */
            if (current != null) {
                current.startIfFirst(time);
                lastStep = new TimeStep(time, "RUN", current.getId(), snapshot(current, tier1, time), tier2.snapshot(time), current.getId() + " running " + "(remaining=" + current.getRemainingTime() + ")");
            } else {
                lastStep = new TimeStep(time, "IDLE", null, snapshot(null, tier1, time), tier2.snapshot(time), "CPU idle");
            }

            result.addStep(lastStep);
            time++;
        }

        return result;
    }

    private static String snapshot(SimProcess current, LinkedList<SimProcess> tier1, int time) {
        
        StringBuilder sb = new StringBuilder();
        sb.append("active process: ");
        if (current != null) {
            sb.append(current.getId()).append("(remaining:").append(current.getRemainingTime()).append(")");
        } else {
            sb.append("none");
        }
        sb.append("; ");
        sb.append(snapshotTier1(tier1));
        sb.append("; ");
        return sb.toString();
    }

    private static String snapshotTier1(LinkedList<SimProcess> tier1) {
        StringBuilder sb = new StringBuilder();
        sb.append("tier1:[");

        for (SimProcess p : tier1) {
            sb.append(p.getId()).append("(remaining:").append(p.getRemainingTime()).append(", priority:").append(p.getCurrentPriority()).append("), ");
        }

        if (!tier1.isEmpty()) {
            sb.setLength(sb.length() - 2);
        }
        sb.append("]");
        return sb.toString();
    }
}
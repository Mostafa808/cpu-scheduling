import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

public class SRTFScheduler {
    public static SimulationResult run(List<SimProcess> processes, int cs) {
        SimulationResult result = new SimulationResult("SRTFScheduler", processes);
        List<SimProcess> finished_processes = new LinkedList<>();

        PriorityQueue<SimProcess> arrival_time_queue = new PriorityQueue<>(new SimProcess.DefaultComparator());
        PriorityQueue<SimProcess> remaining_time_queue = new PriorityQueue<>(new ProcessComparator());

        for (SimProcess process : processes) {
            arrival_time_queue.offer(process);
        }

        int simualation_current_step = 0;
        boolean is_switching = false;
        boolean is_cpu_started = false;
        int remaining_switching_time = 0;

        SimProcess current_process = null;
        TimeStep last_time_step = null;

        while (processes.size() > finished_processes.size()) {
            TimeStep current_time_step = null;
            boolean is_switching_signaled = false;

            // handle arrival
            while (arrival_time_queue.peek() != null
                    && arrival_time_queue.peek().getArrivalTime() == simualation_current_step) {
                remaining_time_queue.offer(arrival_time_queue.poll());
            }

            // CPU cycle
            if (is_switching) {
                remaining_switching_time--;
            } else {
                if (current_process != null) {
                    current_process.executeOneUnit();
                }
            }

            // handle process completion
            if (current_process != null && current_process.getRemainingTime() == 0) {
                current_process.finish(simualation_current_step);
                finished_processes.add(current_process);
                current_process = null;
                if (!is_cpu_started) {
                    is_switching_signaled = true;
                } else {
                    is_switching_signaled = true; // also signal switch on first-ever completion
                }
                is_cpu_started = true;
            }
            // handle context switching completion
            if (is_switching && remaining_switching_time == 0) {
                is_switching = false;
            }

            // revaluate and check if switching is possible
            if (is_switching) {
                // still in the middle of a multi-unit context switch
            } else if (current_process == null && !is_switching_signaled) {
                // start the cpu if ready queue has a process
                if (!remaining_time_queue.isEmpty()) {
                    current_process = remaining_time_queue.poll();
                    current_time_step = new TimeStep(simualation_current_step, "RUN", current_process.getId(),
                            snapshot(remaining_time_queue, current_process), null, current_process.getId() + " started!");
                    last_time_step = current_time_step;
                    result.addStep(current_time_step);
                }
            } else {
                // cpu is either not idle or switching is signaled
                boolean current_process_null = (current_process == null);
                if (!current_process_null) {
                    remaining_time_queue.offer(current_process);
                }
                SimProcess shortest_remaining_process = remaining_time_queue.peek();
                if (current_process == shortest_remaining_process || ((current_process != null)
                        && (shortest_remaining_process != null)
                        && (current_process.getRemainingTime() <= shortest_remaining_process.getRemainingTime()))) {
                    // do nothing
                    if (!current_process_null) {
                        remaining_time_queue.poll();
                    }
                } else if (shortest_remaining_process != null) {
                    // switch to the other process
                    SimProcess next_process = remaining_time_queue.peek();

                    is_switching = true;
                    remaining_switching_time = cs;
                    if (remaining_switching_time > 0) {
                        current_process = null;

                        current_time_step = new TimeStep(simualation_current_step, "CS", next_process.getId(),
                                snapshot(remaining_time_queue, next_process), null,
                                "switching to " + next_process.getId());
                        last_time_step = current_time_step;
                        result.addStep(current_time_step);
                    } else {
                        is_switching = false;
                        current_process = remaining_time_queue.poll();

                        current_time_step = new TimeStep(simualation_current_step, "RUN", current_process.getId(),
                                snapshot(remaining_time_queue, current_process), null,
                                current_process.getId() + " started!");
                        last_time_step = current_time_step;
                        result.addStep(current_time_step);
                    }
                }
            }
            // trigger start
            if (current_process != null) {
                current_process.startIfFirst(simualation_current_step);
            } else if (current_process == null && is_switching) {

            } else if (current_process == null && (last_time_step == null || !"IDLE".equals(last_time_step.getKind()))) {
                current_time_step = new TimeStep(simualation_current_step, "IDLE", null,
                        snapshot(remaining_time_queue, null), null, "cpu is idle");
                last_time_step = current_time_step;
                result.addStep(current_time_step);
            }
            simualation_current_step++;
        }

        return result;
    }

    static String snapshot(PriorityQueue<SimProcess> active_processes, SimProcess current_process) {
        String result = "";
        if (current_process != null) {
            result += "active process: " + current_process.getId() + "; ";
        }
        result += "processes queue[";
        for (SimProcess process : active_processes) {
            result += process.getId() + "(remaining: " + process.getRemainingTime() + ", priority: "
                    + process.getCurrentPriority() + "), ";
        }
        result += "]";
        return result;
    }

    public static class ProcessComparator implements Comparator<SimProcess> {
        @Override
        public int compare(SimProcess first, SimProcess second) {
            // sorted by remaining time
            if (first.getRemainingTime() < second.getRemainingTime()) {
                return -1;
            } else if (first.getRemainingTime() == second.getRemainingTime()) {
                return 0;
            }
            return 1;
        }
    }
}

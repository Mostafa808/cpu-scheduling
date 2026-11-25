import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SimulationResult {
    private final String algorithmName;
    private final List<SimProcess> processes;
    private final List<TimeStep> steps = new ArrayList<>();

    public SimulationResult(String name, List<SimProcess> processes) {
        if (name == null || name.isBlank() || processes == null || processes.isEmpty()) {
            throw new IllegalArgumentException("Algorithm name and processes are required");
        }
        Set<String> ids = new HashSet<>();
        for (SimProcess p : processes) {
            if (p == null || !ids.add(p.getId())) {
                throw new IllegalArgumentException("Processes must be non-null with unique IDs");
            }
        }
        this.algorithmName = name;
        this.processes = new ArrayList<>(processes);
    }

    public void addStep(TimeStep step) {
        //if (step == null || step.getTime() != steps.size()) {
        //    throw new IllegalArgumentException("Exactly one chronological step per time unit, starting at 0");
        //}
        steps.add(step);
    }

    private void requireCompleted() {
        for (SimProcess p : processes) {
            if (p.getCompletionTime() == -1) {
                throw new IllegalStateException("Finish all processes before reading results");
            }
        }
    }

    public double averageWaiting() {
        requireCompleted();
        long total = 0;
        for (SimProcess p : processes) total += p.waitingTime();
        return (double) total / processes.size();
    }

    public double averageTurnaround() {
        requireCompleted();
        long total = 0;
        for (SimProcess p : processes) total += p.turnaroundTime();
        return (double) total / processes.size();
    }

    public double averageResponse() {
        requireCompleted();
        long total = 0;
        for (SimProcess p : processes) total += p.responseTime();
        return (double) total / processes.size();
    }

    public double cpuUtilization() {

        if (steps.size() < 2) {
            return 0.0;
        }

        TimeStep last = steps.get(steps.size() - 1);
        if (!"IDLE".equals(last.getKind())) {
            throw new IllegalStateException("Last step must be the terminating IDLE marker");
        }
        
        long runTime = 0;
        long totalTime = 0;
        for (int i = 0; i < steps.size() - 1; i++) {
            TimeStep current = steps.get(i);
            TimeStep next = steps.get(i + 1);

            int duration = next.getTime() - current.getTime();
            totalTime += duration;
            if ("RUN".equals(current.getKind())) {
                runTime += duration;
            }
        }
        
        return totalTime == 0 ? 0.0: ((double) runTime * 100.0 / totalTime);
    }

    public String getAlgorithmName() { return algorithmName; }
    public List<SimProcess> getProcesses() { return Collections.unmodifiableList(processes); }
    public List<TimeStep> getSteps() { return Collections.unmodifiableList(steps); }
}
import java.util.ArrayList;
import java.util.List;

public class SimulationResult {
    private final String algorithmName;
    private final List<SimProcess> processes;
    private final List<TimeStep> steps = new ArrayList<>();

    public SimulationResult(String name, List<SimProcess> processes) {
        this.algorithmName = name;
        this.processes = new ArrayList<>(processes);
    }

    // TODO Member 1: Compute all metrics; validate complete simulation before averaging.
    public void addStep(TimeStep step) { steps.add(step); }
    public double averageWaiting() { throw new UnsupportedOperationException("TODO: Member 1"); }
    public double averageTurnaround() { throw new UnsupportedOperationException("TODO: Member 1"); }
    public double averageResponse() { throw new UnsupportedOperationException("TODO: Member 1"); }
    public double cpuUtilization() { throw new UnsupportedOperationException("TODO: Member 1"); }
    public String getAlgorithmName() { return algorithmName; }
    public List<SimProcess> getProcesses() { return processes; }
    public List<TimeStep> getSteps() { return steps; }
}

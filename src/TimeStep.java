public class TimeStep {
    private final int time;
    private final String kind, cpuActivity, tier1Snapshot, tier2Snapshot, event;

    public TimeStep(int time, String kind, String cpu, String tier1, String tier2, String event) {
        this.time = time;
        this.kind = kind;
        this.cpuActivity = cpu;
        this.tier1Snapshot = tier1;
        this.tier2Snapshot = tier2;
        this.event = event;
    }

    // TODO Member 1: Validate step kinds (RUN / CS / IDLE) and timestamps.
    public int getTime() { return time; }
    public String getKind() { return kind; }
    public String getCpuActivity() { return cpuActivity; }
    public String getTier1Snapshot() { return tier1Snapshot; }
    public String getTier2Snapshot() { return tier2Snapshot; }
    public String getEvent() { return event; }
}

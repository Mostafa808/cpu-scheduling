public class SimProcess {
    private final String id;
    private final int arrivalTime, burstTime, initialPriority;
    private int remainingTime;
    private int firstStartTime = -1, completionTime = -1, tier2EntryTime = -1;

    public SimProcess(String id, int arrival, int burst, int priority) {
        if (id == null || id.isBlank() || arrival < 0 || burst <= 0)
            throw new IllegalArgumentException("Invalid process input");
        this.id = id;
        this.arrivalTime = arrival;
        this.burstTime = burst;
        this.initialPriority = priority;
        this.remainingTime = burst;
    }

    // TODO Member 1: Complete and verify against the team contracts.
    public SimProcess copy() { return new SimProcess(id, arrivalTime, burstTime, initialPriority); }
    public void startIfFirst(int time) { throw new UnsupportedOperationException("TODO: Member 1"); }
    public void executeOneUnit() { throw new UnsupportedOperationException("TODO: Member 1"); }
    public void finish(int time) { throw new UnsupportedOperationException("TODO: Member 1"); }
    public void enterTier2(int time) { throw new UnsupportedOperationException("TODO: Member 1"); }
    public int waitingTime() { throw new UnsupportedOperationException("TODO: Member 1"); }
    public int turnaroundTime() { throw new UnsupportedOperationException("TODO: Member 1"); }
    public int responseTime() { throw new UnsupportedOperationException("TODO: Member 1"); }
    public String getId() { return id; }
    public int getArrivalTime() { return arrivalTime; }
    public int getBurstTime() { return burstTime; }
    public int getInitialPriority() { return initialPriority; }
    public int getRemainingTime() { return remainingTime; }
    public int getTier2EntryTime() { return tier2EntryTime; }
    public int getCompletionTime() { return completionTime; }
}

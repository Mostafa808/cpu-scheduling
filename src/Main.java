import java.util.LinkedList;

public class Main {
    public static void main(String[] args) {
        System.out.println();
        System.out.println();

        // SRTF Schedule
        LinkedList<SimProcess> srtf_processes = new LinkedList<>();
        srtf_processes.push(new SimProcess("P1", 0, 8, 0));
        srtf_processes.push(new SimProcess("P2", 1, 4, 0));
        srtf_processes.push(new SimProcess("P3", 2, 9, 0));
        srtf_processes.push(new SimProcess("P4", 3, 5, 0));

        SimulationResult result = SRTFScheduler.run(srtf_processes, 1);
        System.out.println(result.averageResponse()+ ", "+ result.averageTurnaround()+ ", "+ result.averageWaiting());
    }
}

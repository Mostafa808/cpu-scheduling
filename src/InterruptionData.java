public class InterruptionData {
	public enum InterruptionType {
		PROCESS_ARRIVAL,
		PROCESS_COMPLETION,
		QUANTUM_TIMEOUT,
		SWITCHING_COMPLETION,
	}
	public int step;
	public InterruptionType type;
	public InterruptionData(int t_step, InterruptionType t_type){
		step = t_step;
		type = t_type;
	}
}

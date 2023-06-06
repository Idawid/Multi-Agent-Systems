package simulationUtils;

import agents.TruckAgent;
import java.util.List;

public class TaskAllocator {
    public static void assignTasksRoundRobin(List<Task> tasks, List<TruckAgent> trucks) {
        int i = 0;
        for (Task task : tasks) {
            trucks.get(i).assignTask(task);
            i = (i + 1) % trucks.size();
        }
    }
}

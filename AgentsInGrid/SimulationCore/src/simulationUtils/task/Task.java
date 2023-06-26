package simulationUtils.task;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Queue;

public class Task implements Serializable {
    private Queue<Step> steps = new LinkedList<>();

    public void addStep(Step step) {
        steps.add(step);
    }

    public void execute() {
        while (!steps.isEmpty()) {
            steps.poll().perform();
        }
    }
}

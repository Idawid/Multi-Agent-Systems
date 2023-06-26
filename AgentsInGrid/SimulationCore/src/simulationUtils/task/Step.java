package simulationUtils.task;

import java.io.Serializable;

public abstract class Step implements Serializable {
    protected String initiator;
    protected String subject;

    public Step(String initiator, String subject) {
        this.initiator = initiator;
        this.subject = subject;
    }

    public abstract void perform();
}

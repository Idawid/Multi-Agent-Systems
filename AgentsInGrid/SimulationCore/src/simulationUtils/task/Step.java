package simulationUtils.task;

import jade.core.AID;

import java.io.Serializable;

public abstract class Step implements Serializable {
    protected AID initiator;
    protected AID subject;

    public Step(AID initiator, AID subject) {
        this.initiator = initiator;
        this.subject = subject;
    }

    public abstract void perform();
}

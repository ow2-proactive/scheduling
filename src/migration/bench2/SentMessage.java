package migration.bench2;

import java.io.Serializable;


public class SentMessage implements Serializable {
    private int valueReceived;
    private int senderId;

    public SentMessage(int v, int s) {
        valueReceived = v;
        senderId = s;
    }

    public String toString() {
        return new String("Receiver = " + senderId + " value =" +
            valueReceived);
    }
}

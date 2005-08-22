package migration.bench2;

import java.io.Serializable;


public class ReceivedMessage implements Serializable {
    private int valueReceived;
    private String senderId;

    public ReceivedMessage(int v, String s) {
        valueReceived = v;
        senderId = s;
    }

    public String toString() {
        return new String("SequenceNumber = " + senderId + " value =" +
            valueReceived);
    }
}

package migration.bench2;

import java.io.Serializable;


public class Message implements Serializable {
    private int valueReceived;
    private int senderId;

    public Message(int v, int s) {
        v = valueReceived;
        s = senderId;
    }

    public String toString() {
        return new String("Sender = " + senderId + " value =" + valueReceived);
    }
}

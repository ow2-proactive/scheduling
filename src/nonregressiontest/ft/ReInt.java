package nonregressiontest.ft;

import java.io.Serializable;


public class ReInt implements Serializable {
    private int value;

    public ReInt() {
    }

    public ReInt(int v) {
        this.value = v;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}

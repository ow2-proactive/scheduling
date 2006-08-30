package nonregressiontest.component.collectiveitf.gathercast;

import java.io.Serializable;

public class B implements Serializable {
    
    int value;
    
    public B() {}
    
    public B(int value) {
        this.value = value;
    }
    
    public int getValue() {
        return value;
    }

}

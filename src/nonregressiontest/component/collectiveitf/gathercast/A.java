package nonregressiontest.component.collectiveitf.gathercast;

import java.io.Serializable;

public class A implements Serializable {
    
    int value;
    
    public A() {}
    
    public A(int value) {
        this.value = value;
    }
    
    public int getValue() {
        return value;
    }

}

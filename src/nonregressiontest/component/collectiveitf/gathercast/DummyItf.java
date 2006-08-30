package nonregressiontest.component.collectiveitf.gathercast;

import org.objectweb.proactive.core.util.wrapper.IntMutableWrapper;

public interface DummyItf {
    
    
    public void foo(IntMutableWrapper i);
    
    public B bar(A a);
    
    public B timeout();

}

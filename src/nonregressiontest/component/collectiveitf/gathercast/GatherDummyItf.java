package nonregressiontest.component.collectiveitf.gathercast;

import java.util.List;

import org.objectweb.proactive.core.component.type.annotations.gathercast.MethodSynchro;
import org.objectweb.proactive.core.util.wrapper.IntMutableWrapper;


public interface GatherDummyItf {
    
    public void foo(List<IntMutableWrapper> l);
    
    public List<B> bar(List<A> l);

    @org.objectweb.proactive.core.component.type.annotations.gathercast.MethodSynchro(timeout=0)
    public List<B> timeout();
}

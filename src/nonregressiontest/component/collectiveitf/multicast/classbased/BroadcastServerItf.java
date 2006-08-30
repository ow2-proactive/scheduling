package nonregressiontest.component.collectiveitf.multicast.classbased;

import java.util.List;

import nonregressiontest.component.collectiveitf.multicast.WrappedInteger;


public interface BroadcastServerItf {
    
    public WrappedInteger dispatch(List<WrappedInteger> l);

}

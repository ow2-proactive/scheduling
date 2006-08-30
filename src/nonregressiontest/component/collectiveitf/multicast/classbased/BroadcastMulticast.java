package nonregressiontest.component.collectiveitf.multicast.classbased;

import java.util.List;

import nonregressiontest.component.collectiveitf.multicast.WrappedInteger;


public interface BroadcastMulticast {
    
    public List<WrappedInteger> dispatch(List<WrappedInteger> l);

}

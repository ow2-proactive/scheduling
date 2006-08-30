package nonregressiontest.component.collectiveitf.multicast.classbased;

import java.util.List;

import nonregressiontest.component.collectiveitf.multicast.WrappedInteger;

import org.objectweb.proactive.core.component.type.annotations.multicast.ClassDispatchMetadata;
import org.objectweb.proactive.core.component.type.annotations.multicast.ParamDispatchMetadata;
import org.objectweb.proactive.core.component.type.annotations.multicast.ParamDispatchMode;

// only testing one mode
@ClassDispatchMetadata(mode=@ParamDispatchMetadata(mode=ParamDispatchMode.ONE_TO_ONE))
public interface OneToOneMulticast {
    
    public List<WrappedInteger> dispatch(List<WrappedInteger> l);

}

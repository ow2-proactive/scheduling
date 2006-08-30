package nonregressiontest.component.collectiveitf.multicast;

import org.objectweb.proactive.core.component.type.annotations.multicast.ClassDispatchMetadata;
import org.objectweb.proactive.core.component.type.annotations.multicast.ParamDispatchMetadata;
import org.objectweb.proactive.core.component.type.annotations.multicast.ParamDispatchMode;

@ClassDispatchMetadata(mode=@ParamDispatchMetadata(mode=ParamDispatchMode.ONE_TO_ONE))
public interface OneToOneItfAnnotatedServerItf extends MulticastTestItf {

}

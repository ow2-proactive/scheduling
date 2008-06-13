package functionalTests.component.collectiveitf.unicast;

import java.util.List;

import org.objectweb.proactive.core.component.type.annotations.multicast.MethodDispatchMetadata;
import org.objectweb.proactive.core.component.type.annotations.multicast.ParamDispatchMetadata;
import org.objectweb.proactive.core.component.type.annotations.multicast.ParamDispatchMode;
import org.objectweb.proactive.core.component.type.annotations.multicast.Reduce;
import org.objectweb.proactive.core.component.type.annotations.multicast.ReduceMode;
import org.objectweb.proactive.core.group.DispatchMode;
import org.objectweb.proactive.core.group.Dispatch;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;


public interface RequiredService {

    @Reduce(reductionMode = ReduceMode.SELECT_UNIQUE_VALUE)
    @MethodDispatchMetadata(mode = @ParamDispatchMetadata(mode = ParamDispatchMode.UNICAST))
    @Dispatch(mode = DispatchMode.CUSTOM, customMode = CustomUnicastDispatch.class)
    public StringWrapper method1(List<String> parameters);

}

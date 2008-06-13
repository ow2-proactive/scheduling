package functionalTests.component.collectiveitf.dynamicdispatch;

import java.util.List;

import org.objectweb.proactive.core.component.type.annotations.multicast.MethodDispatchMetadata;
import org.objectweb.proactive.core.component.type.annotations.multicast.ParamDispatchMetadata;
import org.objectweb.proactive.core.component.type.annotations.multicast.ParamDispatchMode;
import org.objectweb.proactive.core.group.DispatchMode;
import org.objectweb.proactive.core.group.Dispatch;


public interface RequiredService {

    //	@Reduce(reductionMode=ReductionMode.CUSTOM, customReductionMode=SumReduction.class)
    @MethodDispatchMetadata(mode = @ParamDispatchMetadata(mode = ParamDispatchMode.ROUND_ROBIN))
    //	@Dispatch(mode = AllocationMode.KNOWLEDGE_BASED)
    @Dispatch(mode = DispatchMode.DYNAMIC)
    public List<Result> execute(List<Task> t);

    //	 @Reduce(reductionMode=ReductionMode.CUSTOM, customReductionMode=SumReduction.class)
    //    @MethodDispatchMetadata(mode=@ParamDispatchMetadata(mode=ParamDispatchMode.ONE_TO_ONE))
    //    @Dispatch(mode = AllocationMode.KNOWLEDGE_BASED)
    //    public IntWrapper executeAndReduce(List<Task> t);

}

package nonregressiontest.component.collectiveitf.multicast;

import java.util.List;

import org.objectweb.proactive.core.component.type.annotations.multicast.MethodDispatchMetadata;
import org.objectweb.proactive.core.component.type.annotations.multicast.ParamDispatchMetadata;
import org.objectweb.proactive.core.component.type.annotations.multicast.ParamDispatchMode;


public interface MulticastTestItf  {
    // public void
// processOutputMessage(@ParamDispatchMetadata(mode=ParamDispatchMode.ONE_TO_ONE)
// List<Message> message);
    List<WrappedInteger> testBroadcast_Param(
        @ParamDispatchMetadata(mode = ParamDispatchMode.BROADCAST)
    List<WrappedInteger> list);

    @MethodDispatchMetadata(mode = @ParamDispatchMetadata(mode =ParamDispatchMode.BROADCAST))
    List<WrappedInteger> testBroadcast_Method(List<WrappedInteger> list);

    List<WrappedInteger> testOneToOne_Param(
       @ParamDispatchMetadata(mode = ParamDispatchMode.ONE_TO_ONE)
   List<WrappedInteger> list);

    @MethodDispatchMetadata(mode = @ParamDispatchMetadata(mode =ParamDispatchMode.ONE_TO_ONE))
    List<WrappedInteger> testOneToOne_Method(List<WrappedInteger> list);

    List<WrappedInteger> testRoundRobin_Param(
       @ParamDispatchMetadata(mode = ParamDispatchMode.ROUND_ROBIN)
   List<WrappedInteger> list);

    @MethodDispatchMetadata(mode = @ParamDispatchMetadata(mode =ParamDispatchMode.ROUND_ROBIN))
    List<WrappedInteger> testRoundRobin_Method(List<WrappedInteger> list);

    List<WrappedInteger> testCustom_Param(
       @ParamDispatchMetadata(mode = ParamDispatchMode.CUSTOM, customMode = CustomParametersDispatch.class)
   List<WrappedInteger> list);

    @MethodDispatchMetadata(mode = @ParamDispatchMetadata(mode =ParamDispatchMode.CUSTOM, customMode = CustomParametersDispatch.class))
    List<WrappedInteger> testCustom_Method(List<WrappedInteger> list);

    List<WrappedInteger> testAllStdModes_Param(List<WrappedInteger> list1,
                                               @ParamDispatchMetadata(mode = ParamDispatchMode.BROADCAST)
                                           List<WrappedInteger> list2,
                                               @ParamDispatchMetadata(mode = ParamDispatchMode.ONE_TO_ONE)
                                           List<WrappedInteger> list3,
                                               @ParamDispatchMetadata(mode = ParamDispatchMode.ROUND_ROBIN)
                                           List<WrappedInteger> list4, WrappedInteger a);
}

package nonregressiontest.component.collectiveitf.multicast;

import java.util.List;


public interface ClientTestItf {
    List<WrappedInteger> testBroadcast_Param(List<WrappedInteger> list);

    List<WrappedInteger> testBroadcast_Method(List<WrappedInteger> list);

    List<WrappedInteger> testOneToOne_Param(List<WrappedInteger> list);

    List<WrappedInteger> testOneToOne_Method(List<WrappedInteger> list);

    List<WrappedInteger> testRoundRobin_Param(List<WrappedInteger> list);

    List<WrappedInteger> testRoundRobin_Method(List<WrappedInteger> list);

    List<WrappedInteger> testCustom_Param(List<WrappedInteger> list);

    List<WrappedInteger> testCustom_Method(List<WrappedInteger> list);

    List<WrappedInteger> testAllStdModes_Param(List<WrappedInteger> list1,
        List<WrappedInteger> list2, List<WrappedInteger> list3,
        List<WrappedInteger> list4, WrappedInteger a);
}

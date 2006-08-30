package nonregressiontest.component.collectiveitf.multicast;


import java.util.List;

import nonregressiontest.component.Message;


public interface NonAnnotatedClientItf {
    public List<Message> processOutputMessage(List<Message> message);

    // public void
    // processOutputMessage(@ParamDispatchMetadata(mode=ParamDispatchMode.ONE_TO_ONE)
    // List<Message> message);
    public List<WrappedInteger> testBroadcast_Param(List<WrappedInteger> list);
    public List<WrappedInteger> testBroadcast_Method(List<WrappedInteger> list);
    public List<WrappedInteger> testBroadcast_Itf(List<WrappedInteger> list);

    public List<WrappedInteger> testOneToOne_Param(List<WrappedInteger> list);
    public List<WrappedInteger> testOneToOne_Method(List<WrappedInteger> list);
    public List<WrappedInteger> testOneToOne_Itf(List<WrappedInteger> list);
    
    public List<WrappedInteger> testRoundRobin_Param(List<WrappedInteger> list);
    public List<WrappedInteger> testRoundRobin_Method(List<WrappedInteger> list);
    public List<WrappedInteger> testRoundRobin_Itf(List<WrappedInteger> list);

    public List<WrappedInteger> testAllStdModes_Param(List<WrappedInteger> list1,
        List<WrappedInteger> list2, List<WrappedInteger> list3, List<WrappedInteger> list4,
        WrappedInteger a);

    public List<WrappedInteger> testCustom_Param(List<WrappedInteger> list);
    public List<WrappedInteger> testCustom_Method(List<WrappedInteger> list);
    public List<WrappedInteger> testCustom_Itf(List<WrappedInteger> list);
    
    
}

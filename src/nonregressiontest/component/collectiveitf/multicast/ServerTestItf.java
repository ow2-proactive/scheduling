package nonregressiontest.component.collectiveitf.multicast;

import java.util.List;


public interface ServerTestItf {
    

    //  public void  processOutputMessage(@ParamDispatchMetadata(mode=ParamDispatchMode.ONE_TO_ONE) List<Message> message);
    public WrappedInteger testBroadcast_Param(List<WrappedInteger> listOfMyObject);
    public WrappedInteger testBroadcast_Method(List<WrappedInteger> listOfMyObject);


    public WrappedInteger testOneToOne_Param(WrappedInteger a);
    public WrappedInteger testOneToOne_Method(WrappedInteger a);

    public WrappedInteger testRoundRobin_Param(WrappedInteger a);
    public WrappedInteger testRoundRobin_Method(WrappedInteger a);

    public WrappedInteger testCustom_Param(WrappedInteger a);
    public WrappedInteger testCustom_Method(WrappedInteger a);

    public WrappedInteger testAllStdModes_Param(
            List<WrappedInteger> defaultDispatch,
            List<WrappedInteger> broadcastDispatch, 
            WrappedInteger oneToOneDispatch,
            WrappedInteger roundRobinDispatch,
            WrappedInteger singleElement);
    
}

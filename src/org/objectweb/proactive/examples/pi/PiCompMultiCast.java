package org.objectweb.proactive.examples.pi;

import java.util.List;
import org.objectweb.proactive.core.component.type.annotations.multicast.MethodDispatchMetadata;
import org.objectweb.proactive.core.component.type.annotations.multicast.ParamDispatchMetadata;
import org.objectweb.proactive.core.component.type.annotations.multicast.ParamDispatchMode;
import org.objectweb.proactive.core.component.type.annotations.multicast.ClassDispatchMetadata;



@ClassDispatchMetadata(
		mode=@ParamDispatchMetadata(mode=ParamDispatchMode.ONE_TO_ONE))
public interface PiCompMultiCast {
	
	public List<Result> compute(List<Interval> msg);
	
	public void setScale(List<Integer> scale);

}

package nonregressiontest.component.migration;

import java.util.List;

import org.objectweb.proactive.core.component.type.annotations.multicast.MethodDispatchMetadata;
import org.objectweb.proactive.core.component.type.annotations.multicast.ParamDispatchMetadata;
import org.objectweb.proactive.core.component.type.annotations.multicast.ParamDispatchMode;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;

public interface C {
	
	@MethodDispatchMetadata(mode = @ParamDispatchMetadata(mode =ParamDispatchMode.ONE_TO_ONE))
	public List<StringWrapper> bar(List<StringWrapper> l );
	

}

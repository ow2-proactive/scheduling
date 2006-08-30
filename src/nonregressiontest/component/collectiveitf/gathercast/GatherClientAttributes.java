package nonregressiontest.component.collectiveitf.gathercast;

import org.objectweb.fractal.api.control.AttributeController;

public interface GatherClientAttributes extends AttributeController {
	
	public void setId(String id);
	
	public String getId();

}

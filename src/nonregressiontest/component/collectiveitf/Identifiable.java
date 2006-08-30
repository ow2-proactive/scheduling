package nonregressiontest.component.collectiveitf;

import org.objectweb.fractal.api.control.AttributeController;

public interface Identifiable extends AttributeController {
	
	public void setID(int id) ;
	
	public int getID();

}

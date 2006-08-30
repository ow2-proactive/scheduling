package nonregressiontest.component.collectiveitf.multicast;

import org.objectweb.fractal.api.control.AttributeController;


public interface Identifiable extends AttributeController {
    
    public void setID(String id);
    
    public String getID();


}

package nonregressiontest.component.lookup;

import java.io.IOException;
import java.net.URL;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.component.body.ComponentInitActive;
import org.objectweb.proactive.core.util.UrlBuilder;

import nonregressiontest.component.I1;
import nonregressiontest.component.Message;


public class A implements ComponentInitActive, I1 {
    public static final String COMPONENT_A_LOCATION = "localhost";
    public static final String COMPONENT_A_NAME = "componentA";

    public void initComponentActivity(Body body) {
        try {
            ProActive.register(Fractive.getComponentRepresentativeOnThis(),
                UrlBuilder.buildUrlFromProperties(COMPONENT_A_LOCATION,
                    COMPONENT_A_NAME));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Message processInputMessage(Message message) {
        return null;
    }
}

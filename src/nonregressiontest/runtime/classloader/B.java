package nonregressiontest.runtime.classloader;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;

/**
 * @author Matthieu Morel
 *
 */
public class B {
    
    public B() {}
    
    public B(String param) {
        try {
            createActiveObjectC();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ProActiveRuntimeException(e);
        }
    }

    public void createActiveObjectC() throws Exception {
        ProActiveDescriptor descriptor = ProActive.getProactiveDescriptor(System.getProperty("user.home") + "/ProActive/src/nonregressiontest/runtime/classloader/deployment.xml");
        descriptor.activateMappings();
        Object ao = ProActive.newActive("nonregressiontest.runtime.classloader.C", new Object[]{"sdfasdf"}, descriptor.getVirtualNode("VN1").getNode());
        descriptor.killall(false);
    }
    
}

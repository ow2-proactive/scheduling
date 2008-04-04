package functionalTests;

import org.junit.BeforeClass;
import org.objectweb.proactive.core.config.PAProperties;


public class ComponentTestDefaultNodes extends GCMFunctionalTestDefaultNodes {

    public ComponentTestDefaultNodes(DeploymentType type) {
        super(type);
    }

    @BeforeClass
    public static void componentPreConditions() throws Exception {
        if (!PAProperties.PA_FUTURE_AC.isTrue()) {
            throw new Exception(
                "The components framework needs the automatic continuations (system property 'proactive.future.ac' set to 'enable') to be operative");
        }

        //-Dfractal.provider=org.objectweb.proactive.core.component.Fractive
        PAProperties.FRACTAL_PROVIDER.setValue("org.objectweb.proactive.core.component.Fractive");
    }
}

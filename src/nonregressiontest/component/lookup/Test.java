package nonregressiontest.component.lookup;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.util.UrlBuilder;

import nonregressiontest.component.ComponentTest;
import nonregressiontest.component.I1;

import testsuite.test.Assertions;


public class Test extends ComponentTest {
    private ComponentType typeA;
    private Component componentA;

    public Test() {
        super("registration and lookup",
            "registration and lookup");
    }

    public void action() throws Exception {
        Component boot = Fractal.getBootstrapComponent();
        TypeFactory typeFactory = Fractal.getTypeFactory(boot);
        GenericFactory componentFactory = Fractal.getGenericFactory(boot);
        typeA = typeFactory.createFcType(new InterfaceType[] {
                    typeFactory.createFcItfType("i1", I1.class.getName(),
                        TypeFactory.SERVER, TypeFactory.MANDATORY,
                        TypeFactory.SINGLE)
                });
        componentA = componentFactory.newFcInstance(typeA,
                new ControllerDescription("component-a", Constants.PRIMITIVE),
                new ContentDescription(A.class.getName()));
        Fractive.register(componentA,
            UrlBuilder.buildUrlFromProperties("localhost", "componentA"));
        Component retreived = Fractive.lookup(UrlBuilder.buildUrlFromProperties(
                    "localhost", "componentA"));
        Assertions.assertEquals(componentA, retreived);
    }

    public void initTest() throws Exception {
    }

    public boolean postConditions() throws Exception {
        return true;
    }

    public void endTest() throws Exception {
    }
}

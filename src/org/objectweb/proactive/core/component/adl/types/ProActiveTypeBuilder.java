package org.objectweb.proactive.core.component.adl.types;

import org.objectweb.fractal.adl.types.FractalTypeBuilder;
import org.objectweb.proactive.core.component.type.ProActiveTypeFactory;
import org.objectweb.proactive.core.component.type.ProActiveTypeFactoryImpl;

/**
 * @author Matthieu Morel
 */
public class ProActiveTypeBuilder extends FractalTypeBuilder {
    public Object createInterfaceType(final String name, final String signature, final String role,
                                      final String contingency, final String cardinality,
                                      final Object context) throws Exception {

    // TODO : cache already created types ?

    boolean client = "client".equals(role);
    boolean optional = "optional".equals(contingency);

    String checkedCardinality = (cardinality==null)?ProActiveTypeFactory.SINGLETON_CARDINALITY:cardinality;
        // TODO_M should use bootstrap type factory with extended createFcItfType method

        return ProActiveTypeFactoryImpl.instance().createFcItfType(name, signature, client, optional, checkedCardinality);
    }
}

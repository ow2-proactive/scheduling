package org.objectweb.proactive.core.component.body;

import org.apache.log4j.Logger;

import org.objectweb.proactive.core.body.BodyImpl;
import org.objectweb.proactive.core.body.MetaObjectFactory;
import org.objectweb.proactive.core.body.ProActiveMetaObjectFactory;
import org.objectweb.proactive.core.component.ComponentParameters;
import org.objectweb.proactive.core.component.identity.ProActiveComponent;
// COMPONENTS

/**
 * This class is placed in the hierarchy of bodies in order to provide access to the
 * component metaobjects (through ProActiveComponentImpl)
 *
 * @author Matthieu Morel
 */
import java.util.Hashtable;


/** This class has been inserted into the bodies hierarchy in order to instantiate the
 * component metaobject (ProActiveComponent).
 */
public class ComponentBodyImpl extends BodyImpl implements ComponentBody {
    protected static Logger logger = Logger.getLogger(ComponentBodyImpl.class.getName());

    //private static Category cat = Category.getInstance(ComponentBodyImpl.class.getName());
    private ProActiveComponent componentIdentity = null;

    /**
     * Constructor for ComponentBodyImpl.
     */
    public ComponentBodyImpl() {
        super();
    }

    /** Constructor for ComponentBodyImpl.
     *
     * It creates the component metaobject only if the MetaObjectFactory is parameterized
     * with ComponentParameters (thus implicitely constructing components)
     * @param reifiedObject a reference on the reified object
     * @param nodeURL node url
     * @param factory factory for the corresponding metaobjects
     */
    public ComponentBodyImpl(Object reifiedObject, String nodeURL,
        MetaObjectFactory factory) {
        super(reifiedObject, nodeURL, factory);

        // create the component metaobject if necessary
        // --> check the value of the "parameters" field
        Hashtable factory_parameters = factory.getParameters();
        if ((factory_parameters != null)) {
            if (factory_parameters.get(
                        ProActiveMetaObjectFactory.COMPONENT_PARAMETERS_KEY) != null) {
                if (factory_parameters.get(
                            ProActiveMetaObjectFactory.COMPONENT_PARAMETERS_KEY) instanceof ComponentParameters) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("creating metaobject component identity");
                    }
                    componentIdentity = factory.newComponentFactory()
                                               .newProActiveComponent(this);
                } else {
                    logger.error(
                        "component parameters for the components factory are not of type ComponentParameters");
                }
            }
        }
    }

    /**
     * Returns the a reference on the Component meta object
     * @return the ProActiveComponent meta-object
     */
    public ProActiveComponent getProActiveComponent() {
        return componentIdentity;
    }

}

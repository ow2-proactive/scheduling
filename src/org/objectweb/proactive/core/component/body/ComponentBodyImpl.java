/* 
 * ################################################################
 * 
 * ProActive: The Java(TM) library for Parallel, Distributed, 
 *            Concurrent computing with Security and Mobility
 * 
 * Copyright (C) 1997-2004 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *  
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *  
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s): 
 * 
 * ################################################################
 */ 
package org.objectweb.proactive.core.component.body;

import org.apache.log4j.Logger;

import org.objectweb.proactive.core.body.BodyImpl;
import org.objectweb.proactive.core.body.MetaObjectFactory;
import org.objectweb.proactive.core.body.ProActiveMetaObjectFactory;
import org.objectweb.proactive.core.component.ComponentParameters;
import org.objectweb.proactive.core.component.identity.ProActiveComponent;

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
        MetaObjectFactory factory, String jobID) {
        super(reifiedObject, nodeURL, factory, jobID);

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

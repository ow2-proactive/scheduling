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

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.fractal.util.Fractal;

import org.objectweb.proactive.Active;
import org.objectweb.proactive.core.body.MetaObjectFactory;
import org.objectweb.proactive.core.body.ProActiveMetaObjectFactory;
import org.objectweb.proactive.core.body.migration.MigratableBody;
import org.objectweb.proactive.core.component.ComponentParameters;
import org.objectweb.proactive.core.component.identity.ProActiveComponent;
import org.objectweb.proactive.core.component.request.Shortcut;
import org.objectweb.proactive.core.mop.ConstructorCallExecutionFailedException;

import java.util.HashMap;
import java.util.Map;


/** This class has been inserted into the bodies hierarchy in order to instantiate the
 * component metaobject (ProActiveComponent).
 */
public class ComponentBodyImpl extends MigratableBody implements ComponentBody {
    private ProActiveComponent componentIdentity = null;
    private Map shortcutsOnThis = null; // key = functionalItfName, value = shortcut

    //    private RequestFilter filterOnNFRequests;
    private boolean insideFunctionalActivity = false;

    /**
     * Constructor for ComponentBodyImpl.
     */
    public ComponentBodyImpl() {
        super();
    }

    /** Constructor for ComponentBodyImpl.
     *
     * It creates the component metaobject only if the MetaObjectFactory is parameterized
     * with ComponentParameters (thus implicitely constructing components).
     *
     * It may also modifiy the activity to be compatible with the life cycle of the component and
     * the management of non functional invocations.
     * @param reifiedObject a reference on the reified object
     * @param nodeURL node url
     * @param factory factory for the corresponding metaobjects
     */
    public ComponentBodyImpl(Object reifiedObject, String nodeURL,
        Active activity, MetaObjectFactory factory, String jobID)
        throws java.lang.reflect.InvocationTargetException, 
            ConstructorCallExecutionFailedException {
        super(reifiedObject, nodeURL, factory, jobID);
        //        filterOnNFRequests = new RequestFilterOnPrioritizedNFRequests();
        // create the component metaobject if necessary
        // --> check the value of the "parameters" field
        Map factory_parameters = factory.getParameters();
        if ((null != factory_parameters)) {
            if (null != factory_parameters.get(
                        ProActiveMetaObjectFactory.COMPONENT_PARAMETERS_KEY)) {
                if (factory_parameters.get(
                            ProActiveMetaObjectFactory.COMPONENT_PARAMETERS_KEY) instanceof ComponentParameters) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("creating metaobject component identity");
                    }
                    componentIdentity = factory.newComponentFactory()
                                               .newProActiveComponent(this);

                    // change activity into a component activity
                    // activity = new ComponentActivity(activity, reifiedObject);
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

    /**
     * overrides the @link{Body#isActive()} method :
     * when the process flow is inside a functional activity of a component,
     * isActive corresponds to the started state in the lifecycle of the component, while
     * !isActive corresponds to the stopped state.
     * If the process flow is outside of the functional activity of a component, then return the
     * default result for isActive() (unoverriden)
     *
     */
    public boolean isActive() {
        if (insideFunctionalActivity) {
            try {
                return LifeCycleController.STARTED.equals(Fractal.getLifeCycleController(
                        getProActiveComponent()).getFcState());
            } catch (NoSuchInterfaceException e) {
                logger.error(
                    "could not find the life cycle controller of this component");
                return false;
            }
        } else {
            return super.isActive();
        }
    }

    /*
     * @see org.objectweb.proactive.core.component.body.ComponentBody#isComponent()
     */
    public boolean isComponent() {
        return (getProActiveComponent() != null);
    }

    /*
     * @see org.objectweb.proactive.core.component.body.ComponentBody#finishedFunctionalActivity()
     */
    public void finishedFunctionalActivity() {
        insideFunctionalActivity = false;
    }

    /*
     * @see org.objectweb.proactive.core.component.body.ComponentBody#startingFunctionalActivity()
     */
    public void startingFunctionalActivity() {
        insideFunctionalActivity = true;
    }

    public void keepShortcut(Shortcut shortcut) {
        if (shortcutsOnThis == null) {
            shortcutsOnThis = new HashMap();
        }
        shortcutsOnThis.put(shortcut.getFcFunctionalInterfaceName(), shortcut);
    }

    //    public void serve(ComponentRequest request) {
    //            // handle FT
    //        if (request.isControllerRequest()) {
    //            super.serve(request);
    //            return;
    //        }
    //        
    //        ComponentRequest nextNFRequest = (ComponentRequest)getRequestQueue().getOldest(filterOnNFRequests);
    //        if (nextNFRequest != null) {
    //           }
    //                
    //            //TODO handle prioritized NF requests
    //        }
    //        
    //    }
    //    public void createShortcut(UniqueID id, UniversalBody body, String functionalInterfaceName) {
    //          // TODO get or create a new proxy
    //          location.putBody(id, body);
    //        try {
    //            FunctionalInterfaceProxy proxy = (FunctionalInterfaceProxy)((StubObject)getProActiveComponent().getFcInterface(functionalInterfaceName)).getProxy();
    ////            /proxy.setRefOnBody(body, id);
    //            System.out.println();
    //        } catch (NoSuchInterfaceException e) {
    //            e.printStackTrace();
    //        }
    //
    //          throw new ProActiveRuntimeException("create shortcut method not implemented yet");
    //      }
}

/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.component.controller;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.ContentController;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.component.group.ProxyForComponentInterfaceGroup;
import org.objectweb.proactive.core.component.type.ProActiveInterfaceType;
import org.objectweb.proactive.core.component.type.ProActiveTypeFactory;
import org.objectweb.proactive.core.component.type.ProActiveTypeFactoryImpl;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * Implementation of the {@link org.objectweb.fractal.api.control.LifeCycleController} interface.<br>
 *
 * @author The ProActive Team
 *
 */
public class ProActiveLifeCycleControllerImpl extends AbstractProActiveController implements
        ProActiveLifeCycleController, Serializable {
    static final Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS_CONTROLLERS);
    protected String fcState = LifeCycleController.STOPPED;

    public ProActiveLifeCycleControllerImpl(Component owner) {
        super(owner);
    }

    @Override
    protected void setControllerItfType() {
        try {
            setItfType(ProActiveTypeFactoryImpl.instance().createFcItfType(Constants.LIFECYCLE_CONTROLLER,
                    ProActiveLifeCycleController.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY,
                    TypeFactory.SINGLE));
        } catch (InstantiationException e) {
            throw new ProActiveRuntimeException("cannot create controller " + this.getClass().getName());
        }
    }

    /*
     * @see org.objectweb.fractal.api.control.LifeCycleController#getFcState()
     */
    public String getFcState() {
        return fcState;
        //        return getRequestQueue().isStarted() ? LifeCycleController.STARTED
        //                                             : LifeCycleController.STOPPED;
    }

    /*
     *@see org.objectweb.fractal.api.control.LifeCycleController#startFc()
     * recursive if composite
     * ( recursivity is allowed here as we do not implement sharing )
     */
    public void startFc() {
        try {
            //check that all mandatory client interfaces are bound
            //            Object[] itfs = getFcItfOwner().getFcInterfaces();
            InterfaceType[] itfTypes = ((ComponentType) getFcItfOwner().getFcType()).getFcInterfaceTypes();

            for (int i = 0; i < itfTypes.length; i++) {
                if (itfTypes[i].isFcClientItf() && !itfTypes[i].isFcOptionalItf()) {
                    if (itfTypes[i].isFcCollectionItf()) {
                        // look for collection members
                        Object[] itfs = owner.getFcInterfaces();
                        for (int j = 0; j < itfs.length; j++) {
                            Interface itf = (Interface) itfs[j];
                            if (itf.getFcItfName().startsWith(itfTypes[i].getFcItfName())) {
                                if (itf.getFcItfName().equals(itfTypes[i].getFcItfName())) {
                                    throw new IllegalLifeCycleException(
                                        "invalid collection interface name at runtime (suffix required)");
                                }
                                if (Fractal.getBindingController(owner).lookupFc(itf.getFcItfName()) == null) {
                                    throw new IllegalLifeCycleException(
                                        "compulsory collection client interface " +
                                            itfTypes[i].getFcItfName() + " in component " +
                                            Fractal.getNameController(getFcItfOwner()).getFcName() +
                                            " is not bound. ");
                                }
                            }
                        }
                    } else if (((ProActiveInterfaceType) itfTypes[i]).isFcMulticastItf() &&
                        !!itfTypes[i].isFcOptionalItf()) {
                        ProxyForComponentInterfaceGroup clientSideProxy = Fractive.getMulticastController(
                                getFcItfOwner()).lookupFcMulticast(itfTypes[i].getFcItfName());

                        //                        if (clientSideProxy == null) {
                        //                        	System.out.println("client side proxy is null from " + ProActiveRuntimeImpl.getProActiveRuntime().getURL());
                        //                        }
                        if (clientSideProxy.getDelegatee().isEmpty()) {
                            throw new IllegalLifeCycleException("compulsory multicast client interface " +
                                itfTypes[i].getFcItfName() + " in component " +
                                Fractal.getNameController(getFcItfOwner()).getFcName() + " is not bound. ");
                        }
                    } else if ((((ProActiveInterfaceType) itfTypes[i]).getFcCardinality().equals(
                            ProActiveTypeFactory.SINGLETON_CARDINALITY) || ((ProActiveInterfaceType) itfTypes[i])
                            .getFcCardinality().equals(ProActiveTypeFactory.GATHER_CARDINALITY)) &&
                        (Fractal.getBindingController(getFcItfOwner()).lookupFc(itfTypes[i].getFcItfName()) == null)) {
                        throw new IllegalLifeCycleException("compulsory client interface " +
                            itfTypes[i].getFcItfName() + " in component " +
                            Fractal.getNameController(getFcItfOwner()).getFcName() + " is not bound. ");
                    }

                    // TODO check compulsory client gathercast interface in composite
                    // TODO add a test for client gathercast interface in composite
                }
            }

            String hierarchical_type = Fractive.getComponentParametersController(getFcItfOwner())
                    .getComponentParameters().getHierarchicalType();
            if (hierarchical_type.equals(Constants.COMPOSITE)) {
                // start all inner components
                Component[] inner_components = Fractal.getContentController(getFcItfOwner())
                        .getFcSubComponents();
                if (inner_components != null) {
                    for (int i = 0; i < inner_components.length; i++) {
                        ((LifeCycleController) inner_components[i]
                                .getFcInterface(Constants.LIFECYCLE_CONTROLLER)).startFc();
                    }
                }
            }

            //getRequestQueue().start();
            fcState = LifeCycleController.STARTED;
            if (logger.isDebugEnabled()) {
                logger.debug("started " +
                    Fractive.getComponentParametersController(getFcItfOwner()).getComponentParameters()
                            .getName());
            }
        } catch (NoSuchInterfaceException nsie) {
            logger.error("interface not found : " + nsie.getMessage());
            nsie.printStackTrace();
        } catch (IllegalLifeCycleException ilce) {
            logger.error("illegal life cycle operation : " + ilce.getMessage());
            ilce.printStackTrace();
        }
    }

    /*
     *@see org.objectweb.fractal.api.control.LifeCycleController#stopFc()
     * recursive if composite
     */
    public void stopFc() {
        try {
            String hierarchical_type = Fractive.getComponentParametersController(getFcItfOwner())
                    .getComponentParameters().getHierarchicalType();
            if (hierarchical_type.equals(Constants.COMPOSITE)) {
                // stop all inner components
                Component[] inner_components = Fractal.getContentController(getFcItfOwner())
                        .getFcSubComponents();
                if (inner_components != null) {
                    for (int i = 0; i < inner_components.length; i++) {
                        ((LifeCycleController) inner_components[i]
                                .getFcInterface(Constants.LIFECYCLE_CONTROLLER)).stopFc();
                    }
                }
            }

            //getRequestQueue().stop();
            fcState = LifeCycleController.STOPPED;
            if (logger.isDebugEnabled()) {
                logger.debug("stopped" +
                    Fractive.getComponentParametersController(getFcItfOwner()).getComponentParameters()
                            .getName());
            }
        } catch (NoSuchInterfaceException nsie) {
            logger.error("interface not found : " + nsie.getMessage());
        } catch (IllegalLifeCycleException ilce) {
            logger.error("illegal life cycle operation : " + ilce.getMessage());
        }
    }

    /*
     * @see org.objectweb.proactive.core.component.controller.ProActiveLifeCycleController#getFcState(short)
     */
    public String getFcState(short priority) {
        return getFcState();
    }

    /*
     * @see org.objectweb.proactive.core.component.controller.ProActiveLifeCycleController#startFc(short)
     */
    public void startFc(short priority) {
        startFc();
    }

    /*
     * @see org.objectweb.proactive.core.component.controller.ProActiveLifeCycleController#stopFc(short)
     */
    public void stopFc(short priority) {
        stopFc();
    }
}

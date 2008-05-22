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
package org.objectweb.proactive.core.component.body;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.Active;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.component.controller.MembraneController;
import org.objectweb.proactive.core.component.controller.PriorityController;


/**
 * The class for the activity of a component having the membrane and the priority controller inside its membrane
 * @author The ProActive Team
 *
 */
public class ComponentMembraneActivityPriority extends ComponentActivityPriority {

    public ComponentMembraneActivityPriority() {
        super();
    }

    public ComponentMembraneActivityPriority(Active activity, Object reifiedObject) {
        super(activity, reifiedObject);
    }

    /**
     * <p>
     * Runs the activity as defined in @see ComponentRunActive. The default behaviour is to
     * serve non-functional requests in FIFO order, until the component is started. Then the functional
     * activity (as defined in @see InitActive, @see RunActive and @see EndActive) begins.</p><p>
     * When redefining the @see RunActive#runActivity(Body) method, the @see Body#isActive() returns true
     * as long as the lifecycle of the component is @see LifeCycleController#STARTED. When the lifecycle of
     * the component is @see LifeCycleController#STOPPED, @see Body#isActive() returns false.</p>
     *
     */
    @Override
    public void runActivity(Body body) {
        if ((componentRunActive != null) && (componentRunActive != this)) {
            componentRunActive.runActivity(body);
        } else {
            // this is the default activity of the active object
            // the activity of the component has been initialized and started, now
            // what we have to do is to manage the life cycle, i.e. start and stop the
            // activity
            // that can be redefined on the reified object.
            try {
                Service componentService = new Service(body);
                NFRequestFilterImpl nfRequestFilter = new NFRequestFilterImpl();
                MembraneControllerRequestFilter memRequestFilter = new MembraneControllerRequestFilter();
                while (body.isActive()) {
                    ComponentBody componentBody = (ComponentBody) body;

                    /*
                     * While the membrane is stopped, serve calls only on the Membrane Controller
                     */
                    while (Fractive.getMembraneController(componentBody.getProActiveComponentImpl())
                            .getMembraneState().equals(MembraneController.MEMBRANE_STOPPED)) {
                        componentService.blockingServeOldest(memRequestFilter);
                    }

                    while (LifeCycleController.STOPPED.equals(Fractal.getLifeCycleController(
                            componentBody.getProActiveComponentImpl()).getFcState())) {
                        PriorityController pc = (PriorityController) componentBody
                                .getProActiveComponentImpl().getFcInterface(
                                        Constants.REQUEST_PRIORITY_CONTROLLER);
                        NF3RequestFilter nf3RequestFilter = new NF3RequestFilter(pc);
                        if (componentService.getOldest(nf3RequestFilter) != null) {
                            // NF3 bypass all other request 
                            // System.err.println(
                            //   "STOPPED ComponentActivity : NF3");
                            componentService.blockingServeOldest(nf3RequestFilter);
                        } else {
                            componentService.blockingServeOldest(nfRequestFilter);
                        }

                        if (!body.isActive()) {
                            // in case of a migration 
                            break;
                        }
                    }
                    if (!body.isActive()) {
                        // in case of a migration 
                        break;
                    }

                    // 3.1. init object Activity
                    // life cycle started : starting activity of the object
                    if (functionalInitActive != null) {
                        functionalInitActive.initActivity(activeBody);
                        //functionalInitActive = null; // we won't do it again
                    }

                    ((ComponentBody) body).startingFunctionalActivity();
                    // 3.2 while object activity
                    // componentServe (includes filter on priority)
                    functionalRunActive.runActivity(body);
                    ((ComponentBody) body).finishedFunctionalActivity();
                    if (functionalEndActive != null) {
                        functionalEndActive.endActivity(body);
                    }

                    /*
                     * While the membrane is started, serve non-functional calls with priority (the same as for Lifecycle Stopped)
                     */
                    while (Fractive.getMembraneController(componentBody.getProActiveComponentImpl())
                            .getMembraneState().equals(MembraneController.MEMBRANE_STARTED)) {
                        PriorityController pc = (PriorityController) componentBody
                                .getProActiveComponentImpl().getFcInterface(
                                        Constants.REQUEST_PRIORITY_CONTROLLER);
                        NF3RequestFilter nf3RequestFilter = new NF3RequestFilter(pc);
                        if (componentService.getOldest(nf3RequestFilter) != null) {
                            // NF3 bypass all other request 
                            // System.err.println(
                            //   "STOPPED ComponentActivity : NF3");
                            componentService.blockingServeOldest(nf3RequestFilter);
                        } else {
                            componentService.blockingServeOldest(nfRequestFilter);
                        }
                        if (!body.isActive()) {//Don't know if this is OK
                            // in case of a migration 
                            break;
                        }

                    }
                }
            } catch (NoSuchInterfaceException e) {
                logger
                        .error("could not retreive an interface, probably the life cycle controller of this component; terminating the component. Error message is : " +
                            e.getMessage());
            }
        }
    }

}

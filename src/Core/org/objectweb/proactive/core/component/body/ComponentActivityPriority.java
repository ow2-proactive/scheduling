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

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.Active;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.EndActive;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.core.body.ActiveBody;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.controller.PriorityController;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * This class defines the activity of active objects that are components
 * supporting prioritized request.
 *
 * @author Cedric Dalmasso
 *
 */
public class ComponentActivityPriority extends ComponentActivity implements RunActive, InitActive, EndActive,
        Serializable {
    //    private static Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS_ACTIVITY);
    //    private transient InitActive componentInitActive; // used only once
    //    private RunActive componentRunActive;
    //    private EndActive componentEndActive;
    //    private ActiveBody activeBody;
    //    private InitActive functionalInitActive;
    //    private RunActive functionalRunActive;
    //    private EndActive functionalEndActive;
    public ComponentActivityPriority() {
        super();
        functionalRunActive = new ComponentPriotirizedRequestRunActive();
    }

    public ComponentActivityPriority(Active activity, Object reifiedObject) {
        super();

        // RunActive
        if ((activity != null) && activity instanceof RunActive) {
            functionalRunActive = (RunActive) activity;
        } else if (reifiedObject instanceof RunActive) {
            functionalRunActive = (RunActive) reifiedObject;
        } else {
            functionalRunActive = new ComponentPriotirizedRequestRunActive();
        }
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
                while (body.isActive()) {
                    ComponentBody componentBody = (ComponentBody) body;
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
                }
            } catch (NoSuchInterfaceException e) {
                logger
                        .error("could not retreive an interface, probably the life cycle controller of this component; terminating the component. Error message is : " +
                            e.getMessage());
            }
        }
    }

    private class ComponentPriotirizedRequestRunActive implements RunActive, Serializable {
        public void runActivity(Body body) {
            Service componentService = new Service(body);
            ComponentBody componentBody = (ComponentBody) body;
            NF3RequestFilter nf3RequestFilter = null;
            NF1RequestFilter nf1RequestFilter = null;
            NF2RequestFilter nf2RequestFilter = null;
            NF1NF2RequestFilter nf1nf2RequestFilter = null;
            PriorityController pc = null;
            try {
                pc = (PriorityController) componentBody.getProActiveComponentImpl().getFcInterface(
                        Constants.REQUEST_PRIORITY_CONTROLLER);
                nf3RequestFilter = new NF3RequestFilter(pc);
                nf2RequestFilter = new NF2RequestFilter(pc);
                nf1nf2RequestFilter = new NF1NF2RequestFilter(pc);

                nf1RequestFilter = new NF1RequestFilter(pc);
            } catch (NoSuchInterfaceException e) {
                logger.fatal("The " + Constants.REQUEST_PRIORITY_CONTROLLER +
                    " interface is required to create a component.", e);
            }

            try {
                while (LifeCycleController.STARTED.equals(Fractal.getLifeCycleController(
                        componentBody.getProActiveComponentImpl()).getFcState())) {
                    if (componentService.getOldest(nf3RequestFilter) != null) {
                        // NF3 bypass all other request 
                        //System.err.println("ComponentActivity : NF3");
                        componentService.blockingServeOldest(nf3RequestFilter);
                    } else if ((componentService.getOldest(nf1nf2RequestFilter) != null) &&
                        componentService.getOldest(nf1nf2RequestFilter).equals(
                                componentService.getOldest(nf1RequestFilter))) {
                        // NF2 can't bypass NF1
                        //System.err.println("ComponentActivity : F/NF1 oldest");
                        componentService.blockingServeOldest();
                    } else if (componentService.getOldest(nf2RequestFilter) != null) {
                        // NF2 bypass functional
                        //System.err.println("ComponentActivity : NF2");
                        componentService.blockingServeOldest(nf2RequestFilter);
                    } else {
                        // F and NF1
                        //System.err.println("ComponentActivity : else F/NF1");
                        componentService.blockingServeOldest();
                    }

                    //componentService.blockingServeOldest(nfRequestFilter);
                    if (!body.isActive()) {
                        // in case of a migration 
                        break;
                    }
                }
            } catch (NoSuchInterfaceException e) {
                logger.fatal("Can't retrieve life-cycle interface!", e);
            }
        }
    }
}

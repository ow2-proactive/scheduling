package org.objectweb.proactive.core.component.body;

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
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;

import java.io.Serializable;


/**
 * @author Matthieu Morel
 *
 */
public class ComponentActivity implements ComponentRunActive,
    ComponentInitActive, ComponentEndActive {
    private static Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS_ACTIVITY);
    private transient ComponentInitActive componentInitActive; // used only once
    private ComponentRunActive componentRunActive;
    private ComponentEndActive componentEndActive;
    private ActiveBody activeBody;
    private InitActive functionalInitActive;
    private RunActive functionalRunActive;
    private EndActive functionalEndActive;

    public ComponentActivity() {
        // default component activity
        componentInitActive = null;
        componentRunActive = this;
        componentEndActive = null;

        functionalInitActive = new DefaultInitActive();
        functionalRunActive = new ComponentFIFORunActive();
        functionalEndActive = new DefaultEndActive();
    }

    public ComponentActivity(Active activity, Object reifiedObject) {
        componentInitActive = null;
        componentRunActive = this;
        componentEndActive = null;

        if ((activity != null) && activity instanceof InitActive) {
            functionalInitActive = (InitActive) activity;
        } else if (reifiedObject instanceof InitActive) {
            functionalInitActive = (InitActive) reifiedObject;
        } else {
            functionalInitActive = new DefaultInitActive();
        }

        // RunActive
        if ((activity != null) && activity instanceof RunActive) {
            functionalRunActive = (RunActive) activity;
        } else if (reifiedObject instanceof RunActive) {
            functionalRunActive = (RunActive) reifiedObject;
        } else {
            functionalRunActive = new ComponentFIFORunActive();
        }

        // EndActive
        if ((activity != null) && activity instanceof EndActive) {
            functionalEndActive = (EndActive) activity;
        } else if (reifiedObject instanceof EndActive) {
            functionalEndActive = (EndActive) reifiedObject;
        } else {
            functionalEndActive = new DefaultEndActive();
        }
    }

    // stores the activity operations from the reified object. They will be called
    // during the activity of the component
    public ComponentActivity(ComponentActive componentActivity,
        Object reifiedObject) {
        if ((componentActivity != null) &&
                componentActivity instanceof ComponentInitActive) {
            componentInitActive = (ComponentInitActive) componentActivity;
        } else if (reifiedObject instanceof ComponentInitActive) {
            componentInitActive = (ComponentInitActive) reifiedObject;
        } else {
            componentInitActive = null;
        }

        if ((componentActivity != null) &&
                componentActivity instanceof ComponentRunActive) {
            componentRunActive = (ComponentRunActive) componentActivity;
            functionalInitActive = ((ComponentRunActive) componentActivity).getFunctionalInitActive();
            if (functionalInitActive == null) {
                if (reifiedObject instanceof InitActive) {
                    functionalInitActive = (InitActive) reifiedObject;
                }
            }
            functionalRunActive = ((ComponentRunActive) componentActivity).getFunctionalRunActive();
            if (functionalRunActive == null) {
                if (reifiedObject instanceof RunActive) {
                    functionalEndActive = (EndActive) reifiedObject;
                } else {
                    functionalRunActive = new ComponentFIFORunActive();
                }
            }
            functionalEndActive = ((ComponentRunActive) componentActivity).getFunctionalEndActive();
            if (functionalEndActive == null) {
                if (reifiedObject instanceof EndActive) {
                    functionalEndActive = (EndActive) reifiedObject;
                }
            }
        } else if (reifiedObject instanceof ComponentRunActive) {
            componentRunActive = (ComponentRunActive) reifiedObject;
        } else {
            componentRunActive = this;
        }

        if ((componentActivity != null) &&
                componentActivity instanceof ComponentEndActive) {
            componentEndActive = (ComponentEndActive) componentActivity;
        } else if (reifiedObject instanceof ComponentEndActive) {
            componentEndActive = (ComponentEndActive) reifiedObject;
        } else {
            componentEndActive = null;
        }
    }

    // this is the default activity of the active object
    // the activity of the component has been initialized and started, now
    // what we have to do is to manage the life cycle, i.e. start and stop the
    // activity
    // that can be redefined on the reified object.
    public void runActivity(Body body) {
        try {
            Service componentService = new Service(body);
            NFRequestFilterImpl nfRequestFilter = new NFRequestFilterImpl();
            while (body.isActive()) {
                ComponentBody componentBody = (ComponentBody) body;
                while (LifeCycleController.STOPPED.equals(
                            Fractal.getLifeCycleController(
                                componentBody.getProActiveComponent())
                                       .getFcState())) {
                    componentService.blockingServeOldest(nfRequestFilter);
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
            logger.error(
                "could not retreive an interface, probably the life cycle controller of this component; terminating the component. Error message is : " +
                e.getMessage());
        }
    }

    public EndActive getFunctionalEndActive() {
        return functionalEndActive;
    }

    public void setFunctionalEndActive(EndActive endActive) {
        this.functionalEndActive = endActive;
    }

    public InitActive getFunctionalInitActive() {
        return functionalInitActive;
    }

    public void setFunctionalInitActive(InitActive initActive) {
        this.functionalInitActive = initActive;
    }

    public RunActive getFunctionalRunActive() {
        return functionalRunActive;
    }

    public void setFunctionalRunActive(RunActive runActive) {
        this.functionalRunActive = runActive;
    }

    /*
     * @see org.objectweb.proactive.InitActive#initActivity(org.objectweb.proactive.Body)
     */
    public void initActivity(Body body) {
        if (logger.isDebugEnabled()) {
            logger.debug(
                "initializing component activity ... (component will be active but not yet started)");
        }
    }

    /*
     * @see org.objectweb.proactive.EndActive#endActivity(org.objectweb.proactive.Body)
     */
    public void endActivity(Body body) {
        if (logger.isDebugEnabled()) {
            logger.debug(
                "ending component activity ... (object is still active)");
        }
    }

    private class ComponentFIFORunActive implements RunActive, Serializable {
        public void runActivity(Body body) {
            new Service(body).fifoServing();
        }
    }
    
    private class DefaultInitActive implements InitActive, Serializable {
        public void initActivity(Body body) {
            if (logger.isDebugEnabled()) {
                logger.debug(
                    "initializing default functional activity of the component");
            }
        }
    }

    private class DefaultEndActive implements EndActive, Serializable {
        public void endActivity(Body body) {
            if (logger.isDebugEnabled()) {
                logger.debug(
                    "ending default functional activity of this component");
            }
        }
    }
}

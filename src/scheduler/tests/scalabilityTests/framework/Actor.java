/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package scalabilityTests.framework;

import org.apache.log4j.Logger;


/**
 * 
 * An Actor is a participant in the test scenario.
 * Actors execute Actions, when the appropriate doAction signals are sent to them.
 * Actors also have default actions and default parameters for those actions
 * 
 * Action results are not returned, but stored locally. 
 * The philosophy is that results will be treated locally by the actors
 * 
 * @author fabratu
 *
 */
public abstract class Actor<T, V> {

    protected static final Logger logger = Logger.getLogger(ActiveActor.class);
    protected Action<T, V> defaultAction;
    protected T defaultParameter;
    protected V result;

    public Actor() {
        this.defaultAction = null;
        this.defaultParameter = null;
    }

    public Actor(Action<T, V> action) {
        this.defaultAction = action;
        this.defaultParameter = null;
    }

    public Actor(Action<T, V> action, T parameter) {
        this.defaultAction = action;
        this.defaultParameter = parameter;
    }

    public void doAction() {
        if (this.defaultAction == null)
            throw new IllegalStateException("There is no default action attached to this Actor");
        if (this.defaultParameter == null)
            throw new IllegalStateException("There is no default parameter attached to this Actor");
        doAction(this.defaultAction, this.defaultParameter);
    }

    public void doAction(T parameter) {
        if (this.defaultAction == null)
            throw new IllegalStateException("There is no default action attached to this Actor");
        doAction(this.defaultAction, parameter);
    }

    public void doAction(Action<T, V> action) {
        if (this.defaultParameter == null)
            throw new IllegalStateException("There is no default parameter attached to this Actor");
        doAction(action, this.defaultParameter);
    }

    public void doAction(Action<T, V> action, T parameter) {
        try {
            logger.trace("Executing the Action " + action + " on the parameter " + parameter);
            // store the result for future reference
            this.result = action.execute(parameter);
        } catch (Exception e) {
            logger.warn("Error while executing the action, reason:" + e.getMessage());
            logger.debug("Stacktrace iz:", e);
            this.result = null;
        }
    }
}

/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2013 INRIA/University of
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
 *  * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.task.utils;

import java.io.Serializable;
import java.util.concurrent.Callable;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.annotation.ImmediateService;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.body.AbstractBody;


/**
 * Active Object used to execute Callable objects, similarly to a singleThreadExecutor
 */
public class ActiveObjectExecutor implements InitActive {

    private Callable<Serializable> callable;

    private AbstractBody bodyOnThis;

    private ActiveObjectExecutor stubOnThis;

    private ClassLoader classLoader;

    @Override
    public void initActivity(Body body) {
        bodyOnThis = ((AbstractBody) PAActiveObject.getBodyOnThis());
        stubOnThis = (ActiveObjectExecutor) PAActiveObject.getStubOnThis();
    }

    public boolean ping() {
        return true;
    }

    /**
     * Used to set the Context class loader of this Active Object
     * as a ClassLoader is not serializable, this method must not be called via the stub
     * @param cl
     */
    void setContextClassLoader(ClassLoader cl) {
        classLoader = cl;
        stubOnThis.updateContextClassLoader();
    }

    /**
     * Update the Context class loader using the instance variable "ClassLoader".
     * This request is automatically called when calling setContextClassLoader
     */
    public void updateContextClassLoader() {
        Thread.currentThread().setContextClassLoader(classLoader);
    }

    /**
     * Sets the callable object to execute
     * @param callable
     */
    public void setCallable(Callable<Serializable> callable) {
        this.callable = callable;
    }

    /**
     * Call the current callable object
     * @return
     */
    public Serializable call() {
        try {
            return callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Cancel the current execution
     */
    @ImmediateService
    public void cancel() {
        // this call will send an interrupt message either if the body is waiting for a request or is serving a request
        // it will not terminate the active object
        bodyOnThis.interruptService();
    }

}

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
package org.objectweb.proactive.core.migration;

import java.lang.reflect.Method;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.api.PAMobileAgent;
import org.objectweb.proactive.core.body.migration.Migratable;
import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.core.event.MigrationEvent;
import org.objectweb.proactive.core.event.MigrationEventListener;
import org.objectweb.proactive.core.mop.MethodCall;


public class MigrationStrategyManagerImpl implements MigrationStrategyManager, MigrationEventListener,
        java.io.Serializable {

    /**
     * Name of the method to be called when the agent reaches a new site
     */
    private String methodOnArrival = null;

    /**
     * Name of the method to be called before an agents leaves the current site
     */
    private String methodOnDeparture = null;

    /**
     * MigrationStrategyImpl for the mobile object
     */
    private MigrationStrategy migrationStrategy;

    /**
     * Indicates if the object follows an migrationStrategy
     */
    private boolean onItinerary;

    /**
     * Method used to migrate by default  = migrateTo
     * @see migrateTo
     */
    private transient MethodCall migrationMethodCall = null;

    /**
     * An indication regarding the migration strategy
     * Indicates wether we first serve pending requests
     * before applying the startegy
     */
    private boolean fifoFirst;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public MigrationStrategyManagerImpl() {
    }

    public MigrationStrategyManagerImpl(Migratable migratableBody) {
        migratableBody.addMigrationEventListener(this);
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    //
    // -- Implements MigrationStrategyManager -----------------------------------------------
    //
    public void onDeparture(String s) {
        this.methodOnDeparture = s;
    }

    public void onArrival(String s) {
        this.methodOnArrival = s;
    }

    public void startStrategy(Body body) throws MigrationException {
        getMigrationStrategy().reset();
        fifoFirst = false;
        onItinerary = true;
        continueStrategy(body);
    }

    public MigrationStrategy getMigrationStrategy() {
        if (migrationStrategy == null) {
            migrationStrategy = new MigrationStrategyImpl();
        }
        return migrationStrategy;
    }

    public void setMigrationStrategy(MigrationStrategy s) {
        migrationStrategy = s;
    }

    //
    // -- Implements MigrationEventListener -----------------------------------------------
    //
    public void migrationAboutToStart(MigrationEvent event) {
        //System.out.println("MigrationStrategyManagerImpl.migrationAboutToStart");
        Body body = (Body) event.getSource();
        try {
            executeMethodOnDeparture(body);
        } catch (MigrationException e) {
            e.printStackTrace();
        }
    }

    public void migrationFinished(MigrationEvent event) {
        //System.out.println("MigrationStrategyManagerImpl.migrationFinished");
    }

    public void migrationExceptionThrown(MigrationEvent event) {
        //System.out.println("MigrationStrategyManagerImpl.migrationExceptionThrown");
        MigrationException e = (MigrationException) event.getSource();
        e.printStackTrace();
    }

    public void migratedBodyRestarted(MigrationEvent event) {
        //System.out.println("MigrationStrategyManagerImpl.migratedBodyRestarted");
        Body body = (Body) event.getSource();
        try {
            executeMethodOnArrival(body);
            continueStrategy(body);
        } catch (MigrationException e) {
            e.printStackTrace();
        }
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //
    protected void executeMethodOnDeparture(Body body) throws MigrationException {
        if (methodOnDeparture == null) {
            return;
        }

        //NE PAS SUPPRIMER!!!
        //	System.out.println("MigrationManagerImpl: calling onDeparture()");
        executeMethod(body.getReifiedObject(), methodOnDeparture);
    }

    protected void executeMethodOnArrival(Body body) throws MigrationException {
        if (methodOnArrival == null) {
            return;
        }
        executeMethod(body.getReifiedObject(), methodOnArrival);
    }

    protected void continueStrategy(Body body) throws MigrationException {
        if (!onItinerary) {
            return;
        }

        //	Method autoExecMethod = reifiedObject.getClass().getMethod(methodOnArrival, null);
        //	MethodCall autoExecMethodCall = org.objectweb.proactive.core.mop.MethodCall.getMethodCall(autoExecMethod, null);
        Destination r = migrationStrategy.next();
        if (r == null) {
            this.onItinerary = false;
            return;
        }
        methodOnArrival = r.getMethodName();
        PAMobileAgent.migrateTo(body, r.getDestination(), fifoFirst);
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //
    private void executeMethod(Object target, String methodName) throws MigrationException {
        try {
            Method m = target.getClass().getMethod(methodName, (Class[]) null);
            m.invoke(target, (Object[]) null);
        } catch (NoSuchMethodException e) {
            throw new MigrationException("Cannot find method " + methodName + " in class " +
                target.getClass().getName(), e);
        } catch (IllegalAccessException e) {
            throw new MigrationException("Cannot access method " + methodName + " in class " +
                target.getClass().getName(), e);
        } catch (java.lang.reflect.InvocationTargetException e) {
            throw new MigrationException("Error while trying to execute method " + methodName, e);
        }
    }
}

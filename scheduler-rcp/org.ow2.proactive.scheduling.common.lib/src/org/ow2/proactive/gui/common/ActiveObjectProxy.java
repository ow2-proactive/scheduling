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
package org.ow2.proactive.gui.common;

import java.io.Serializable;

import org.objectweb.proactive.annotation.ImmediateService;
import org.objectweb.proactive.api.PAActiveObject;


/**
 * With current implementation of active object security creation of active
 * object and all methods calls on this object should be executed from the same
 * thread or from the same active object. 
 * GUI clients usually have multiple threads which need access to active object 
 * (e.g. main GUI thread, thread for active object pinging), and this class manages 
 * special active object instance which is needed to provide access to some secured 
 * active object from different threads.
 * </p>
 * ActiveObjectProxy class provides synchronous and asynchronous access to the
 * secured active object via special callback interfaces (ActiveObjectAccess, ActiveObjectSyncAccess)
 * and special method for active object pinging.
 * 
 * @author The ProActive Team
 * 
 */
public abstract class ActiveObjectProxy<T> implements Serializable {

    /*
     * Active object class creating secured active object and providing access to this object. <p/>
     * Note: this class shouldn't be used outside of ActiveObjectProxy class.
     */
    public static class ActiveObjectHolder<T> {

        private T activeObject;

        private ActiveObjectActivityListener activityListener;

        private long activeCallsCounter;

        public ActiveObjectHolder() {
        }

        public void createActiveObject(ActiveObjectProxy<T> proxy) throws Exception {
            // System.out.println("Creating active - " + Thread.currentThread());
            activeObject = proxy.doCreateActiveObject();
        }

        /*
         * Method synchronously executes given callback in the active object's thread
         */
        public void asyncCallActiveObject(ActiveObjectAccess<T> access) {
            // System.out.println("Start async call active - " + Thread.currentThread());
            activityStarted();
            try {
                access.accessActiveObject(activeObject);
            } finally {
                // System.out.println("End async call active - " + Thread.currentThread());
                activityFinished();
            }
        }

        /*
         * Method which is supposed to be called from separate thread to check that active object is
         * alive. <p/> Note: this method and method 'asyncCallActiveObject' executing business logic
         * shouldn't be handled by the same thread, to achieve this effect pinging method is
         * annotated as ImmediateService.
         */
        @ImmediateService
        public boolean syncPingActiveObject(ActiveObjectProxy<T> proxy) {
            // System.out.println("Start ping active - " + Thread.currentThread());
            if (activeObject == null) {
                return false;
            }
            return proxy.doPingActiveObject(activeObject);
        }

        /*
         * Method synchronously executes given callback in the calling thread
         */
        @ImmediateService
        public <V> V syncCallActiveObject(ActiveObjectSyncAccess<T> access) throws Exception {
            // System.out.println("Start sync call active - " + Thread.currentThread());
            activityStarted();
            try {
                V result = access.accessActiveObject(activeObject);
                return result;
            } finally {
                // System.out.println("End sync call active - " + Thread.currentThread());
                activityFinished();
            }
        }

        private synchronized void activityFinished() {
            if (activeCallsCounter <= 0) {
                throw new IllegalStateException("Activity didn't start");
            }
            activeCallsCounter--;
            if (activeCallsCounter == 0 && activityListener != null) {
                activityListener.activityFinished();
            }
        }

        private synchronized void activityStarted() {
            if (activeCallsCounter == 0 && activityListener != null) {
                activityListener.activityStarted();
            }
            activeCallsCounter++;
        }

    }

    public static interface ActiveObjectAccess<T> extends Serializable {

        void accessActiveObject(T activeObject);

    }

    public static interface ActiveObjectSyncAccess<T> extends Serializable {

        <V> V accessActiveObject(T activeObject) throws Exception;

    }

    /**
     * Listens for active object events, there are following events:
     * <ul>
     * <li>activityStarted is called when method call on active object
     * is started 
     * <li>activityFinished is called  when method call on active object
     * finished
     * 
     * (note: if one asynchronous call is in progress and another active
     * object's method is called then 'activityStarted' event isn't fired,
     * and activityFinished event will be fired only once when both methods
     * finish) 
     * 
     * @author sboikov
     *
     */
    public static abstract class ActiveObjectActivityListener {

        private boolean stop;

        private boolean activityInProgress;

        public synchronized final void activityStarted() {
            if (stop) {
                return;
            } else {
                if (!activityInProgress) {
                    activityInProgress = true;
                    onActivityStarted();
                }
            }
        }

        public synchronized final void activityFinished() {
            if (stop) {
                return;
            } else {
                if (activityInProgress) {
                    activityInProgress = false;
                    onActivityFinished();
                }
            }
        }

        private synchronized final void stop() {
            if (stop) {
                return;
            } else {
                stop = true;
                if (activityInProgress) {
                    activityInProgress = false;
                    onActivityFinished();
                }
            }
        }

        public boolean isActivityInProgress() {
            return activityInProgress;
        }

        protected abstract void onActivityStarted();

        protected abstract void onActivityFinished();

    }

    private ActiveObjectHolder<T> activeObjectHolder;

    private transient ActiveObjectActivityListener activityListener;

    public ActiveObjectProxy() {
    }

    public void createActiveObject() throws Exception {
        activeObjectHolder = new ActiveObjectHolder<T>();
        activityListener = doCreateActivityListener();
        activeObjectHolder.activityListener = activityListener;
        activeObjectHolder = PAActiveObject.turnActive(activeObjectHolder);
        activeObjectHolder.createActiveObject(this);
    }

    /**
     * Synchronous method supposed to be called from the special thread to check
     * that active object is alive.
     */
    public boolean syncPingActiveObject() {
        return activeObjectHolder.syncPingActiveObject(this);
    }

    public void terminateActiveObjectHolder() {
        if (activeObjectHolder != null) {
            if (activityListener != null) {
                activityListener.stop();
            }
            PAActiveObject.terminateActiveObject(activeObjectHolder, false);
        }
    }

    /**
     * At the time of this writing first method call on active object instance
     * triggers body initialization and this may require network connection so
     * potentially it can hang.
     * <p/>
     * To avoid this problem this method can be called right after
     * ActiveObjectHolder creation to trigger its initialization.
     */
    public void initActiveObjectHolderForCurrentThread() {
        asyncCallActiveObject(new ActiveObjectAccess<T>() {
            @Override
            public void accessActiveObject(T activeObject) {
                // do nothing, just trigger initialization of activeObjectHolder
            }
        });
    }

    public boolean isActiveObjectCreated() {
        return activeObjectHolder != null;
    }

    protected void asyncCallActiveObject(ActiveObjectAccess<T> access) {
        activeObjectHolder.asyncCallActiveObject(access);
    }

    protected <V> V syncCallActiveObject(ActiveObjectSyncAccess<T> access) throws Exception {
        return activeObjectHolder.syncCallActiveObject(access);
    }

    /**
     * 
     * @param activeObject
     * @return
     */
    protected abstract boolean doPingActiveObject(T activeObject);

    /**
     * 
     * @return
     * @throws Exception
     */
    protected abstract T doCreateActiveObject() throws Exception;

    protected ActiveObjectActivityListener doCreateActivityListener() {
        return null;
    }

    protected ActiveObjectActivityListener getActivityListener() {
        return activityListener;
    }

}

/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive_grid_cloud_portal.scheduler;

import java.io.File;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.mop.MOPClassLoader;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.util.SchedulerProxyUserInterface;
import org.ow2.proactive_grid_cloud_portal.webapp.PortalConfiguration;


public class SchedulerStateListener {

    private static final Logger logger = Logger.getLogger(SchedulerStateListener.class);

    private static final SchedulerStateListener instance = new SchedulerStateListener();

    public static SchedulerStateListener getInstance() {
        return instance;
    }

    /*
     * Object shared between event listener active object and SchedulerStateListener
     */
    static class State {

        private AtomicLong revision = new AtomicLong(-1);

        private volatile SchedulerStatus status;

        void incrementRevision() {
            revision.incrementAndGet();
        }

        long getRevision() {
            return revision.get();
        }

        public SchedulerStatus getStatus() {
            return status;
        }

        public void setStatus(SchedulerStatus status) {
            this.status = status;
        }

    }

    private final State state = new State();

    private SchedulerProxyUserInterface scheduler;

    private EventListener eventListener;

    private volatile boolean killed;

    public void start() {
        Thread connectThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    connect();
                } catch (InterruptedException e) {
                }
            }
        }, "Scheduler connect thread");
        connectThread.start();
    }

    public void kill() {
        killed = true;
        if (scheduler != null) {
            try {
                scheduler.disconnect();
            } catch (Exception e) {
                logger.warn("Failed to disconnect", e);
            }
        }
    }

    private void connect() throws InterruptedException {
        String url = PortalConfiguration.SCHEDULER_URL.getValueAsString();
        String cred_path = PortalConfiguration.SCHEDULER_CACHE_CREDENTIALS.getValueAsStringOrNull();

        while (scheduler == null && !killed) {
            try {
                scheduler = PAActiveObject.newActive(SchedulerProxyUserInterface.class, new Object[] {});

                // check is we use a credential file
                if (cred_path != null && (new File(cred_path)).exists()) {
                    Credentials credential = Credentials.getCredentials(cred_path);
                    scheduler.init(url, credential);
                } else {
                    String login = PortalConfiguration.SCHEDULER_CACHE_LOGIN.getValueAsString();
                    String password = PortalConfiguration.SCHEDULER_CACHE_PASSWORD.getValueAsString();
                    scheduler.init(url, login, password);
                }

                eventListener = new EventListener(state);
                // for PROACTIVE-1027 PROACTIVE-1233
                synchronized (MOPClassLoader.getMOPClassLoader()) {
                    eventListener = PAActiveObject.turnActive(eventListener);
                }
                scheduler.addEventListener(eventListener, false, false);
            } catch (Exception e) {
                logger.warn("no scheduler found on " + url + " retrying in 8 seconds", e);
                if (scheduler != null) {
                    PAActiveObject.terminateActiveObject(scheduler, true);
                    scheduler = null;
                }
                Thread.sleep(8000);
                continue;
            }
        }
    }

    public long getSchedulerStateRevision() {
        return state.getRevision();
    }

    public SchedulerStatus getSchedulerStatus(Scheduler scheduler) throws PermissionException, NotConnectedException {
        SchedulerStatus status = state.getStatus();
        if (status == null) {
            status = scheduler.getStatus();
            state.setStatus(status);
        }
        return status;
    }

}

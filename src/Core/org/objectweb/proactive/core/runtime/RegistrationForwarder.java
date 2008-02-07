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
package org.objectweb.proactive.core.runtime;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.management.Notification;
import javax.management.NotificationListener;

import org.objectweb.proactive.core.jmx.notification.GCMRuntimeRegistrationNotificationData;
import org.objectweb.proactive.core.jmx.notification.NotificationType;
import org.objectweb.proactive.core.jmx.util.JMXNotificationManager;


/**
 *
 */
public class RegistrationForwarder implements NotificationListener {
    final static int DEFAULT_THRESHOLD = 20;
    final static long DEFAULT_TIMEOUT = 1000;
    private Timer timer;
    private Flusher flusher;
    private int threshold;
    private long timeout;
    List<GCMRuntimeRegistrationNotificationData> messages;
    Set<Long> doNotForward;

    public RegistrationForwarder() {
        this(DEFAULT_THRESHOLD, DEFAULT_TIMEOUT);
    }

    public RegistrationForwarder(int threshold, long timeout) {
        this.messages = new ArrayList<GCMRuntimeRegistrationNotificationData>();
        this.timer = new Timer(true);
        this.flusher = null;
        this.doNotForward = new HashSet<Long>();
        this.threshold = threshold;
        this.timeout = timeout;

        subscribeJMXRuntimeEvent();
    }

    synchronized public void handleNotification(Notification notification, Object handback) {
        String type = notification.getType();

        if (NotificationType.GCMRuntimeRegistered.equals(type)) {
            GCMRuntimeRegistrationNotificationData data = (GCMRuntimeRegistrationNotificationData) notification
                    .getUserData();
            addMessage(data);
        }
    }

    public void addMessage(GCMRuntimeRegistrationNotificationData message) {
        if (doNotForward.contains(message.getDeploymentId())) {
            return;
        }

        synchronized (messages) {
            messages.add(message);
            if (messages.size() == threshold) {
                cancelFlusher();
                flush();
                messages.clear();
            } else {
                cancelFlusher();
                scheduleFlusher();
            }
        }
    }

    public void doNotForward(long deploymentId) {
        doNotForward.add(deploymentId);
    }

    private void cancelFlusher() {
        if (flusher != null) {
            flusher.cancel();
        }
    }

    private void scheduleFlusher() {
        flusher = new Flusher();
        timer.schedule(flusher, timeout);
    }

    public void flush() {
        synchronized (messages) {
            for (GCMRuntimeRegistrationNotificationData message : messages) {
                System.out.println(message.getChildRuntime());
            }
        }
    }

    class Flusher extends TimerTask {
        @Override
        public void run() {
            flush();
        }
    }

    private void subscribeJMXRuntimeEvent() {
        JMXNotificationManager.getInstance().subscribe(
                ProActiveRuntimeImpl.getProActiveRuntime().getMBean().getObjectName(), this);
    }
}

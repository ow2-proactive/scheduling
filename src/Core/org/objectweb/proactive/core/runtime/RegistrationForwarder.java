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

    synchronized public void handleNotification(Notification notification,
        Object handback) {
        String type = notification.getType();

        if (NotificationType.GCMRuntimeRegistered.equals(type)) {
            GCMRuntimeRegistrationNotificationData data = (GCMRuntimeRegistrationNotificationData) notification.getUserData();
            addMessage(data);
        }
    }

    public void addMessage(GCMRuntimeRegistrationNotificationData message) {
        if (doNotForward.contains(message.getDeploymentId())) {
            System.out.println("DO NOT FORWARD");
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
            System.out.println("Flusher cancelled");
        }
    }

    private void scheduleFlusher() {
        flusher = new Flusher();
        timer.schedule(flusher, timeout);
    }

    public void flush() {
        synchronized (messages) {
            for (GCMRuntimeRegistrationNotificationData message : messages) {
                System.out.println(message.getChildURL());
            }
        }
    }

    class Flusher extends TimerTask {
        public void run() {
            flush();
        }
    }

    private void subscribeJMXRuntimeEvent() {
        JMXNotificationManager.getInstance()
                              .subscribe(ProActiveRuntimeImpl.getProActiveRuntime()
                                                             .getMBean()
                                                             .getObjectName(),
            this);
    }
}

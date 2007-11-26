package org.objectweb.proactive.ic2d.jmxmonitoring;

import org.objectweb.proactive.ic2d.jmxmonitoring.MVCNotifications.MVC_Notifications;


public class Notification {
    private MVC_Notifications notification;
    private Object data;

    public Notification(MVC_Notifications notif, Object data) {
        this.notification = notif;
        this.data = data;
    }

    public Notification(MVC_Notifications notif) {
        this.notification = notif;
        this.data = null;
    }

    public MVC_Notifications getNotification() {
        return notification;
    }

    public Object getData() {
        return data;
    }
}

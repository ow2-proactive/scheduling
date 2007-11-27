package org.objectweb.proactive.ic2d.jmxmonitoring;

public class Notification {
    private MVCNotification notification;
    private Object data;

    public Notification(MVCNotification notif, Object data) {
        this.notification = notif;
        this.data = data;
    }

    public Notification(MVCNotification notif) {
        this.notification = notif;
        this.data = null;
    }

    public MVCNotification getMVCNotification() {
        return notification;
    }

    public Object getData() {
        return data;
    }
}

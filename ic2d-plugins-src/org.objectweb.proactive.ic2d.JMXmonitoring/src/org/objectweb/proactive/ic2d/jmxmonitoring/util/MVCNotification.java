package org.objectweb.proactive.ic2d.jmxmonitoring.util;

public class MVCNotification {
    private MVCNotificationTag notification;
    private Object data;

    public MVCNotification(MVCNotificationTag notif, Object data) {
        this.notification = notif;
        this.data = data;
    }

    public MVCNotification(MVCNotificationTag notif) {
        this.notification = notif;
        this.data = null;
    }

    public MVCNotificationTag getMVCNotification() {
        return notification;
    }

    public Object getData() {
        return data;
    }
}

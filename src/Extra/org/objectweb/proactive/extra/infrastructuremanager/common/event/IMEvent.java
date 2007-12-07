package org.objectweb.proactive.extra.infrastructuremanager.common.event;

import java.io.Serializable;


public class IMEvent implements Serializable {

    /** serial version UID */
    private static final long serialVersionUID = -7781655355601704944L;

    /**Infrastructure manager URL */
    private String IMUrl = null;

    /**
     * ProActive empty constructor
     */
    public IMEvent() {
    }

    /**
     * Creates the node event object.
     * @param url URL of the node.
     */
    public IMEvent(String url) {
        this.IMUrl = url;
    }

    /**
     * Returns the IM's URL of the event.
     * @return node source type of the event.
     */
    public String getIMUrl() {
        return this.IMUrl;
    }

    /**
     * Set the IM's URL of the event.
     * @param IMURL URL of the IM to set
     */
    public void setIMUrl(String IMURL) {
        this.IMUrl = IMURL;
    }
}

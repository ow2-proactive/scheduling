package org.objectweb.proactive.extensions.resourcemanager.common.event;

import java.io.Serializable;


public class RMEvent implements Serializable {

    /** serial version UID */
    private static final long serialVersionUID = -7781655355601704944L;

    /**Infrastructure manager URL */
    private String RMUrl = null;

    /**
     * ProActive empty constructor
     */
    public RMEvent() {
    }

    /**
     * Creates the node event object.
     * @param url URL of the node.
     */
    public RMEvent(String url) {
        this.RMUrl = url;
    }

    /**
     * Returns the RM's URL of the event.
     * @return node source type of the event.
     */
    public String getIMUrl() {
        return this.RMUrl;
    }

    /**
     * Set the RM's URL of the event.
     * @param IMURL URL of the RM to set
     */
    public void setIMUrl(String IMURL) {
        this.RMUrl = IMURL;
    }
}

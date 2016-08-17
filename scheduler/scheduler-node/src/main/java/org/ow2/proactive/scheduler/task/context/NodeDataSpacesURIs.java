package org.ow2.proactive.scheduler.task.context;

import java.io.Serializable;

public class NodeDataSpacesURIs implements Serializable {
    private final String scratchURI;
    private final String cacheURI;
    private final String inputURI;
    private final String outputURI;
    private final String userURI;
    private final String globalURI;

    public NodeDataSpacesURIs(String scratchURI, String cacheURI, String inputURI, String outputURI, String userURI, String globalURI) {
        this.scratchURI = scratchURI;
        this.cacheURI = cacheURI;
        this.inputURI = inputURI;
        this.outputURI = outputURI;
        this.userURI = userURI;
        this.globalURI = globalURI;
    }

    public String getScratchURI() {
        return scratchURI;
    }

    public String getCacheURI() {
        return cacheURI;
    }

    public String getInputURI() {
        return inputURI;
    }

    public String getOutputURI() {
        return outputURI;
    }

    public String getUserURI() {
        return userURI;
    }

    public String getGlobalURI() {
        return globalURI;
    }
}

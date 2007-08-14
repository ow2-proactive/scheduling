package org.objectweb.proactive.core.jmx.notification;

import java.io.Serializable;

import org.objectweb.proactive.core.UniqueID;


public class RequestNotificationData implements Serializable {
    private UniqueID source;
    private UniqueID destination;
    private String methodName;
    private int requestQueueLength;
    private String sourceNode;
    private String destinationNode;

    /**
     * Creates a new requestData used by the JMX user data.
     * @param source The source of the request
     * @param destination The destination of the request
     * @param methodName The name of the method called
     * @param requestQueueLength The request queue length of the destination active object
     */
    public RequestNotificationData(UniqueID source, String sourceNode,
        UniqueID destination, String destinationNode, String methodName,
        int requestQueueLength) {
        this.source = source;
        this.sourceNode = sourceNode;
        this.destination = destination;
        this.destinationNode = destinationNode;
        this.methodName = methodName;
        this.requestQueueLength = requestQueueLength;
    }

    /**
     * Returns the id of the source object.
     * @return the id of the source object.
     */
    public UniqueID getSource() {
        return this.source;
    }

    /**
     * Returns the id of the destination object.
     * @return the if of the destination object.
     */
    public UniqueID getDestination() {
        return this.destination;
    }

    /**
     * Returns the URL of the source node object.
     * @return the URL of the source node object.
     */
    public String getSourceNode() {
        return this.sourceNode;
    }

    /**
     * Returns the URL of the destination node object.
     * @return the URL of the destination node object.
     */
    public String getDestinationNode() {
        return this.destinationNode;
    }

    /**
     * Returns the method name called on the destination object.
     * @return the method name called on the destination object.
     */
    public String getMethodName() {
        return this.methodName;
    }

    /**
     * Returns the request queue length of the destination object.
     * @return
     */
    public int getRequestQueueLength() {
        return this.requestQueueLength;
    }

    @Override
    public String toString() {
        return "Request source: " + source + ", destination: " + destination +
        ", methodName: " + methodName + ", destination request queue length: " +
        requestQueueLength;
    }
}

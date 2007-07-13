package org.objectweb.proactive.core.jmx.notification;


/**
 * @author ProActive Team
 */
public class NotificationType {
    public final static String unknown = "unknown";

    // --- Corresponds to the RequestQueueEvent --------------------
    public final static String requestQueueModified = "requestQueueModified";
    public final static String addRequest = "addRequest";
    public final static String waitForRequest = "waitForRequest";
    public final static String removeRequest = "removeRequest";

    // --- Corresponds to the MessageEvent -------------------------
    public final static String replyReceived = "replyReceived";
    public final static String replySent = "replySent";
    public final static String requestReceived = "requestReceived";
    public final static String requestSent = "requestSent";
    public final static String servingStarted = "servingStarted";
    public final static String voidRequestServed = "voidRequestServed";

    // --- Corresponds to the MigrationEvent -----------------------
    public final static String migratedBodyRestarted = "migratedBodyRestarted";
    public final static String migrationAboutToStart = "migrationAboutToStart";
    public final static String migrationExceptionThrown = "migrationExceptionThrown";
    public final static String migrationFinished = "migrationFinished";

    // --- Corresponds to the FuturEvent ---------------------------
    public final static String receivedFutureResult = "receivedFutureResult";
    public final static String waitByNecessity = "waitByNecessity";

    // --- Corresponds to the NodeCreationEvent --------------------
    public final static String nodeCreated = "nodeCreated";
    public final static String nodeDestroyed = "nodeDestroyed";

    // --- Corresponds to the BodyEventListener --------------------
    public final static String bodyChanged = "bodyChanged";
    public final static String bodyCreated = "bodyCreated";
    public final static String bodyDestroyed = "bodyDestroyed";

    // --- Corresponds to the RuntimeRegistrationEvent -------------
    public final static String runtimeRegistered = "runtimeRegistered";
    public final static String runtimeUnregistered = "runtimeUnregistered";
    public final static String runtimeAcquired = "runtimeAcquired";
    public final static String forwarderRuntimeRegistered = "forwarderRuntimeRegistered";
    public final static String runtimeDestroyed = "runtimeDestroyed";
}

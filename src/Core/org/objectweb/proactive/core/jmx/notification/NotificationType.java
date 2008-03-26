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
package org.objectweb.proactive.core.jmx.notification;

/**
 * @author The ProActive Team
 */
public class NotificationType {
    public final static String unknown = "unknown";
    public final static String setOfNotifications = "setOfNotifications";

    // --- Corresponds to the RequestQueueEvent --------------------
    /* Not Used */
    // public final static String requestQueueModified = "requestQueueModified";
    // public final static String addRequest = "addRequest";
    // public final static String removeRequest = "removeRequest";
    public final static String waitForRequest = "waitForRequest";

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
    /* Not Used */
    // public final static String bodyChanged = "bodyChanged";
    public final static String bodyCreated = "bodyCreated";
    public final static String bodyDestroyed = "bodyDestroyed";

    // --- Corresponds to the RuntimeRegistrationEvent -------------
    public final static String runtimeRegistered = "runtimeRegistered";
    public final static String runtimeUnregistered = "runtimeUnregistered";
    public final static String runtimeAcquired = "runtimeAcquired";

    // --- GCM Deployment
    public final static String GCMRuntimeRegistered = "GCMRuntimeRegistered";

    /* TODO Send this notification */
    // public final static String forwarderRuntimeRegistered = "forwarderRuntimeRegistered";
    public final static String runtimeDestroyed = "runtimeDestroyed";

    // --- Used in the message of the JMX notifications -------------
    public final static String migrationMessage = "Migration Finished";
}

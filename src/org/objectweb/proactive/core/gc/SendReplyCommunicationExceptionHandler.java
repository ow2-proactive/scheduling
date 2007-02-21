package org.objectweb.proactive.core.gc;

import org.apache.log4j.Level;
import org.objectweb.proactive.core.body.AbstractBody;
import org.objectweb.proactive.core.exceptions.NonFunctionalException;
import org.objectweb.proactive.core.exceptions.body.SendReplyCommunicationException;
import org.objectweb.proactive.core.exceptions.manager.NFEListener;


/**
 * Sending a reply to an object that has been garbage collected will fail.
 * This is not an error.
 */
public class SendReplyCommunicationExceptionHandler implements NFEListener {
    public static final SendReplyCommunicationExceptionHandler instance = new SendReplyCommunicationExceptionHandler();

    public boolean handleNFE(NonFunctionalException e) {
        SendReplyCommunicationException srce = (SendReplyCommunicationException) e;
        GarbageCollector gc = ((AbstractBody) srce.getSender()).getGarbageCollector();
        gc.log(Level.INFO,
            "Failure to send a reply to " + srce.getReceiverID().shortString());
        return true;
    }
}

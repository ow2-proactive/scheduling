/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.ic2d.gui.recording;

import org.objectweb.proactive.ic2d.data.ActiveObject;
import org.objectweb.proactive.ic2d.event.CommunicationEventListener;
import org.objectweb.proactive.ic2d.spy.SpyEvent;


public class ThreadPlayer {
    private java.util.LinkedList eventList;
    private java.util.LinkedList activeObjectList;
    private int recordMark = 0;
    private CommunicationEventListener communicationEventListener;
    private boolean record = false;
    private boolean pause = false;
    private boolean play = false;
    private Thread t;
    private javax.swing.JProgressBar eventReplayProgressBar;

    public ThreadPlayer(CommunicationEventListener communicationEventListener,
        javax.swing.JProgressBar eventReplayProgressBar) {
        this.communicationEventListener = communicationEventListener;
        this.eventReplayProgressBar = eventReplayProgressBar;
        eventList = new java.util.LinkedList();
        activeObjectList = new java.util.LinkedList();
    }

    public int recordEvent(ActiveObject activeObject, SpyEvent evt) {
        if (record && (!pause)) {
            activeObjectList.add(activeObject);
            eventList.add(evt);
            return eventList.size();
        }
        return -1;
    }

    public void record() {
        if (record && pause) {
            pause = false;
        } else if (record) {
            record = false;
        } else if (!record) {
            recordMark = 0;
            activeObjectList.clear();
            eventList.clear();
            record = true;
        }
    }

    public void pause() {
        if (pause == false) {
            pause = true;
        } else {
            pause = false;
        }
    }

    public void play() {
        if (play && pause) {
            pause = false;
        } else if (play) {
            play = false;
            t = null;
        } else {
            recordMark = 0;
            play = true;
            t = new Thread(new Runner(), "IC2D ThreadPlayer");
            t.start();
        }
    }

    private void dispatchEvent(ActiveObject activeObject, SpyEvent event) {
        switch (event.getType()) {
        case SpyEvent.OBJECT_WAIT_FOR_REQUEST_TYPE:
            communicationEventListener.objectWaitingForRequest(activeObject,
                event);
            break;
        case SpyEvent.OBJECT_WAIT_BY_NECESSITY_TYPE:
            communicationEventListener.objectWaitingByNecessity(activeObject,
                event);
            break;
        case SpyEvent.REQUEST_SENT_MESSAGE_TYPE:
            communicationEventListener.requestMessageSent(activeObject, event);
            break;
        case SpyEvent.REPLY_SENT_MESSAGE_TYPE:
            communicationEventListener.replyMessageSent(activeObject, event);
            break;
        case SpyEvent.REQUEST_RECEIVED_MESSAGE_TYPE:
            communicationEventListener.requestMessageReceived(activeObject,
                event);
            break;
        case SpyEvent.REPLY_RECEIVED_MESSAGE_TYPE:
            communicationEventListener.replyMessageReceived(activeObject, event);
            break;
        case SpyEvent.VOID_REQUEST_SERVED_TYPE:
            communicationEventListener.voidRequestServed(activeObject, event);
            break;
        }
        communicationEventListener.allEventsProcessed();
    }

    private class Runner implements Runnable {
        public void run() {
            eventReplayProgressBar.setMinimum(1);
            eventReplayProgressBar.setMaximum(eventList.size());
            while (((recordMark < eventList.size()) && play) || pause) {
                if (!pause) {
                    eventReplayProgressBar.setString((recordMark + 1) + "/" +
                        eventList.size());
                    eventReplayProgressBar.setValue(recordMark + 1);
                    dispatchEvent((ActiveObject) activeObjectList.get(
                            recordMark), (SpyEvent) eventList.get(recordMark));
                    recordMark++;
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }
            }
        }
    }
}

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
package org.objectweb.proactive.core.body.ft.checkpointing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.rmi.server.RemoteObject;
import java.rmi.server.RemoteStub;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.util.converter.ByteToObjectConverter;


/**
 * This class defines a checkpoint object. A checkpoint is used for recovering
 * a failed active object by being sent to a free ProActive runtime.
 * A checkpoint contains a <i>serialized</i> copy of the owner. It is identified
 * by an index and the id of the owner.
 * @author The ProActive Team
 * @since ProActive 2.2
 */
public class Checkpoint implements java.io.Serializable {

    /**
     *
     */

    //id of the checkpointed body
    private UniqueID bodyID;

    //checkpointed body and infos
    private byte[] checkpointedBody;

    //additionnal infos
    private CheckpointInfo ci;

    public Checkpoint() {
    }

    /**
     * Create a checkpoint. The body given as parameter is serialized in this constructor.
     * @param bodyToCheckpoint the body that have to be checkpointed
     * @param additionalCodebase the URL of the CheckpointServer classserver
     */
    public Checkpoint(Body bodyToCheckpoint, String additionalCodebase) {
        try {
            // put futures in copy mode
            bodyToCheckpoint.getFuturePool().setCopyMode(true);
            this.bodyID = bodyToCheckpoint.getID();
            String codebase = PAProperties.JAVA_RMI_SERVER_CODEBASE.getValue();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            CheckpointingOutputStream objectOutputStream = new CheckpointingOutputStream(
                byteArrayOutputStream, codebase + " " + additionalCodebase);
            objectOutputStream.writeObject(bodyToCheckpoint);
            objectOutputStream.flush();
            objectOutputStream.close();
            byteArrayOutputStream.close();
            this.checkpointedBody = byteArrayOutputStream.toByteArray();
            bodyToCheckpoint.getFuturePool().setCopyMode(false);
        } catch (IOException e) {
            System.err.println("Error while checkpointing the body " + bodyToCheckpoint);
            e.printStackTrace();
        }
    }

    // GETTER SETTER
    public UniqueID getBodyID() {
        return bodyID;
    }

    public void setBodyID(UniqueID uniqueID) {
        bodyID = uniqueID;
    }

    public void setCheckpointInfo(CheckpointInfo ci) {
        this.ci = ci;
    }

    public CheckpointInfo getCheckpointInfo() {
        return ci;
    }

    /**
     * Recovers an active object from this checkpoint. A body is deserialized in this method.
     * @return the new instance of the checkpointed body.
     */
    public Body recover() {
        try {
            ///Body recoveredBody = (Body) ByteToObjectConverter.MarshallStream.convert(this.checkpointedBody);
            Body recoveredBody = (Body) ByteToObjectConverter.ProActiveObjectStream
                    .convert(this.checkpointedBody);
            // Communcations are blocked until the activity is restarted
            recoveredBody.blockCommunication();
            return recoveredBody;
        } catch (IOException e) {
            System.err.println("Error while recovering the body with ID = " + this.bodyID);
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            System.err.println("Error while recovering the body with ID = " + this.bodyID);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Pretty printing
     */
    @Override
    public String toString() {
        return "Checkpoint for body " + this.bodyID;
    }

    /*
     * The output stream is extended so as to add in the annotation of the
     * serialization stream the URL of the classserver of the checkpoint server.
     * The new codebase is the concatenation of the standard codebase and the
     * checkpoint server codebase.
     * @author The ProActive Team
     */
    private static class CheckpointingOutputStream extends ObjectOutputStream {
        private String codebase;

        public CheckpointingOutputStream(OutputStream out, String codebase) throws IOException {
            super(out);
            this.enableReplaceObject(true);
            this.codebase = codebase;
        }

        /*
         * Write the codebase in the stream.
         */
        @Override
        protected void annotateClass(Class<?> cl) throws IOException {
            writeObject(this.codebase);
        }

        /*
         * Checks for objects that are instances of java.rmi.Remote
         * that need to be serialized as RMI stubs !
         */
        @Override
        protected final Object replaceObject(Object obj) throws IOException {
            if ((obj instanceof RemoteObject) && !(obj instanceof RemoteStub)) {
                return RemoteObject.toStub((RemoteObject) obj);
            } else {
                return obj;
            }
        }
    }
}

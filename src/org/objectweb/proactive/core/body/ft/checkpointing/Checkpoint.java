/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
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
package org.objectweb.proactive.core.body.ft.checkpointing;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.UniqueID;

import sun.rmi.server.MarshalInputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;


/**
 * This class defines a checkpoint object. A checkpoint is used for recovering
 * a failed active object by being sent to a free ProActive runtime.
 * A checkpoint contains a <i>serialized</i> copy of the owner. It is identified
 * by an index and the id of the owner.
 * @author cdelbe
 * @since ProActive 2.2
 */
public class Checkpoint implements java.io.Serializable {
    //id of the checkpointed body
    private UniqueID bodyID;

    //checkpointed body and infos
    private byte[] checkpointedBody;

    //index of this checkpoint
    private int index;

    //additionnal infos
    private CheckpointInfo ci;

    // CONSTRUCTORS
    private Checkpoint(Body bodyToCheckpoint, String additionalCodebase) {
        try {
            this.bodyID = bodyToCheckpoint.getID();
            String codebase = System.getProperty("java.rmi.server.codebase");
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            CheckpointingOutputStream objectOutputStream = new CheckpointingOutputStream(byteArrayOutputStream,
                    codebase + " " + additionalCodebase);
            objectOutputStream.writeObject(bodyToCheckpoint);
            objectOutputStream.flush();
            objectOutputStream.close();
            byteArrayOutputStream.close();
            this.checkpointedBody = byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            System.err.println("Error while checkpointing the body " +
                bodyToCheckpoint);
            e.printStackTrace();
        }
    }

    /**
     * Create a checkpoint. The body given as parameter is serialized in this constructor.
     * @param bodyToCheckpoint the body that have to be checkpointed
     * @param index the index of the checkpoint
     * @param additionalCodebase the URL of the CheckpointServer classserver
     */
    public Checkpoint(Body bodyToCheckpoint, int index,
        String additionalCodebase) {
        this(bodyToCheckpoint, additionalCodebase);
        this.index = index;
    }

    // GETTER SETTER
    public UniqueID getBodyID() {
        return bodyID;
    }

    public void setBodyID(UniqueID uniqueID) {
        bodyID = uniqueID;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
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
            System.out.println("[FT] Recovering body " + this.bodyID);
            ByteArrayInputStream bais = new ByteArrayInputStream(this.checkpointedBody);
            MarshalInputStream mis = new MarshalInputStream(bais);
            Body recoveredBody = (Body) (mis.readObject());
            mis.close();
            bais.close();
            // Communcations are blocked until the activity is restarted
            recoveredBody.blockCommunication();
            return recoveredBody;
        } catch (IOException e) {
            System.err.println("Error while recovering the body with ID = " +
                this.bodyID);
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            System.err.println("Error while recovering the body with ID = " +
                this.bodyID);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Pretty printing
     */
    public String toString() {
        return "Checkpoint " + this.index + " for body " + this.bodyID;
    }

    /*
     * The output stream is extended so as to add in the annotation of the
     * serialization stream the URL of the classserver of the checkpoint server.
     * The new codebase is the concatenation of the standard codebase and the
     * checkpoint server codebase.
     * @author cdelbe
     */
    private class CheckpointingOutputStream extends ObjectOutputStream {
        private String codebase;

        public CheckpointingOutputStream(OutputStream out, String codebase)
            throws IOException {
            super(out);
            this.codebase = codebase;
        }

        protected void annotateClass(Class cl) throws IOException {
            writeObject(this.codebase);
        }
    }
}

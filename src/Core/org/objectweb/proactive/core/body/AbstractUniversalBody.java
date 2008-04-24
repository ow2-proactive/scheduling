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
package org.objectweb.proactive.core.body;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.component.request.Shortcut;
import org.objectweb.proactive.core.remoteobject.InternalRemoteRemoteObject;
import org.objectweb.proactive.core.remoteobject.RemoteObjectAdapter;
import org.objectweb.proactive.core.remoteobject.RemoteObjectExposer;
import org.objectweb.proactive.core.remoteobject.RemoteObjectHelper;
import org.objectweb.proactive.core.remoteobject.RemoteRemoteObject;
import org.objectweb.proactive.core.remoteobject.exception.UnknownProtocolException;


/**
 * <i><font size="-1" color="#FF0000">**For internal use only** </font></i><br>
 * <p>
 * This class gives a common implementation of the UniversalBody interface. It provides all
 * the non specific behavior allowing sub-class to write the detail implementation.
 * </p><p>
 * Each body is identify by an unique identifier.
 * </p>
 *
 * @author The ProActive Team
 * @version 1.0,  2002/06
 * @since   ProActive 0.9.3
 *
 */
public abstract class AbstractUniversalBody implements UniversalBody, Serializable {
    //
    // -- PROTECTED MEMBERS -----------------------------------------------
    //

    /** Unique ID of the body. */
    protected UniqueID bodyID;

    /** A table containing a mapping from a UniqueID toward a Body. The location table
       caches the location of all known bodies to whom this body communicated */
    protected BodyMap location;

    /** The URL of the node this body is attached to */
    protected String nodeURL;

    /** A remote version of this body that is used to send to remote peer */
    protected transient UniversalBody remoteBody;

    //    protected RemoteBodyFactory remoteBodyFactory;
    protected String jobID;
    protected Map shortcuts = null; // key = functionalItfID, value=shortcut

    protected transient RemoteObjectExposer roe;

    //
    // -- PRIVATE MEMBERS -----------------------------------------------
    //
    //
    // -- CONSTRUCTORS -----------------------------------------------
    //

    /**
     * Creates a new AbstractBody.
     * Used for serialization.
     */
    public AbstractUniversalBody() {
    }

    /**
     * Creates a new AbstractBody for an active object attached to a given node.
     * @param nodeURL the URL of the node that body is attached to
     * @param remoteBodyFactory the factory able to construct new factories for each type of meta objects
     *                needed by this body
     */
    public AbstractUniversalBody(String nodeURL, String jobID) throws ActiveObjectCreationException {
        this.nodeURL = nodeURL;
        this.bodyID = new UniqueID();
        this.location = new BodyMap();
        this.jobID = jobID;

        this.roe = new RemoteObjectExposer<UniversalBody>(UniversalBody.class.getName(), this,
            new UniversalBodyRemoteObjectAdapter());

        URI uri = RemoteObjectHelper.generateUrl(this.bodyID.toString());

        try {
            RemoteRemoteObject rro = register(uri);
            this.remoteBody = (UniversalBody) new RemoteObjectAdapter(rro).getObjectProxy();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ActiveObjectCreationException(e);
        }
    }

    protected RemoteRemoteObject register(URI uri) throws UnknownProtocolException {
        return this.roe.activateProtocol(uri);
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    //
    // -- implements UniversalBody -----------------------------------------------
    //
    public String getJobID() {
        return this.jobID;
    }

    public String getNodeURL() {
        return this.nodeURL;
    }

    public UniversalBody getRemoteAdapter() {
        return this.remoteBody;
    }

    public UniqueID getID() {
        return this.bodyID;
    }

    public void updateLocation(UniqueID bodyID, UniversalBody body) {
        this.location.updateBody(bodyID, body);
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //
    //
    // -- PRIVATE METHODS -----------------------------------------------
    //
    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        if (this.bodyID == null) {
            // it may happen that the bodyID is set to null before serialization if we want to
            // create a copy of the Body that is distinct from the original
            this.bodyID = new UniqueID();
        }

        // remoteBody is transient so we recreate it here
        this.roe = new RemoteObjectExposer(UniversalBody.class.getName(), this,
            new UniversalBodyRemoteObjectAdapter());

        URI uri = RemoteObjectHelper.generateUrl(this.bodyID.toString());

        try {
            InternalRemoteRemoteObject rro = this.roe.activateProtocol(uri);
            this.remoteBody = (UniversalBody) rro.getObjectProxy();
            //  TODO remove me cache is now initialized via adapters          /// Initializing cache
            this.remoteBody.getID();
            //			this.remoteBody =(UniversalBody) rro.getObjectProxy();
        } catch (ProActiveException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /*
     * @see org.objectweb.proactive.core.body.UniversalBody#createShortcut(org.objectweb.proactive.core.component.request.Shortcut)
     */
    public void createShortcut(Shortcut shortcut) throws IOException {
        if (this.shortcuts == null) {
            this.shortcuts = new HashMap();
        }
        this.shortcuts.put(shortcut.getLinkedInterfaceID(), shortcut);
    }

    /*
     * @see org.objectweb.proactive.core.body.UniversalBody#register(java.lang.String)
     */
    public void register(String url) throws IOException, UnknownProtocolException {
        this.roe.activateProtocol(RemoteObjectHelper.expandURI(URI.create(url)));
    }

    public RemoteObjectExposer getRemoteObjectExposer() {
        return this.roe;
    }
}

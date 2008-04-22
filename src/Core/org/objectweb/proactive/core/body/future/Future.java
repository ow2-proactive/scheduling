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
package org.objectweb.proactive.core.body.future;

import org.objectweb.proactive.core.ProActiveTimeoutException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.UniversalBody;


/**
 * <p>
 * An object implementing this interface if a place holder object for an upcomming result yet
 * to come.
 * </p><p>
 * <b>see <a href="../../../../html/ActiveObjectCreation.html#FutureObjectCreation">active object creation doumentation</a></b>
 * </p>
 *
 * @author The ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 *
 */
public interface Future extends LocalFuture {
    public boolean isAwaited();

    /**
     * Blocks the calling thread until the future object is available or the timeout expires
     * @param timeout
     * @throws ProActiveTimeoutException if the timeout expires
     */
    public void waitFor(long timeout) throws ProActiveTimeoutException;

    /**
     * Blocks the calling thread until the future object is available.
     */
    public void waitFor();

    /**
     * Returns the exception that would be thrown upon access to this future, null if no exception
     * would be thrown. This call is blocking on the future update.
     */
    public Throwable getRaisedException();

    /**
     * Returns the object wrapped by this future or throw the exception. This call is blocking on the future update.
     */
    public Object getResult();

    /**
     * Get the encapsulation of the result and the potential exception.
     */
    public MethodCallResult getMethodCallResult();

    /**
     * To set the sequence id of this future.
     */
    public void setID(long id);

    /**
     * To get the sequence id of this future.
     */
    public long getID();

    /**
     * To set the ID of the creator, ie the body which created this future
     */
    public void setCreatorID(UniqueID creator);

    /**
     * To get the creatorID.
     */
    public UniqueID getCreatorID();

    /**
     * Get the unique tuple <creatorID,ID>
     */
    public FutureID getFutureID();

    /**
     * Used when creating a future to track its eventual updater
     */
    public void setUpdater(UniversalBody updater);

    /**
     * Used for monitoring the future, we ping the body that should update
     * it when available.
     */
    public UniversalBody getUpdater();

    /**
     * To set the senderID, ie the UniqueID of the body that will send this future,
     * in case of automatic continuation.
     */
    public void setSenderID(UniqueID i);

    /**
     * Set the copyMode for this future. If copyMode is true, the serialisation of this future
     * will not trigger wait-by-necessity nor AC registration.
     * @param mode true is the copyMode is set
     */
    public void setCopyMode(boolean mode);
}

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
package org.objectweb.proactive.extensions.scilab;

import java.util.ArrayList;
import java.util.List;


public class GeneralResultImpl implements GeneralResult {

    /**
     *
     */
    private String id;
    private int state;
    private long timeExecution;
    private String message = null;
    private Exception exp = null;
    protected ArrayList<AbstractData> listResults;

    public GeneralResultImpl() {
        // empty no arg cons
    }

    public GeneralResultImpl(String id) {
        this.id = id;
        listResults = new ArrayList<AbstractData>();
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extensions.scilab.GeneralResult#getState()
     */
    public int getState() {
        return state;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extensions.scilab.GeneralResult#setState(int)
     */
    public void setState(int state) {
        this.state = state;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extensions.scilab.GeneralResult#getTimeExecution()
     */
    public long getTimeExecution() {
        return timeExecution;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extensions.scilab.GeneralResult#setTimeExecution(long)
     */
    public void setTimeExecution(long timeExecution) {
        this.timeExecution = timeExecution;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extensions.scilab.GeneralResult#getId()
     */
    public String getId() {
        return id;
    }

    public void setException(Exception exp) {
        this.exp = exp;
    }

    public boolean isException() {
        return this.exp != null;
    }

    public Exception getException() {
        return this.exp;
    }

    /**
     *
     * @param name data id
     * @return the data
     */
    public AbstractData get(String name) {
        for (AbstractData data : listResults) {
            if (data.getName().equals(name)) {
                return data;
            }
        }

        return null;
    }

    /**
     *
     * @return list of all out data
     */
    public List<AbstractData> getList() {
        return listResults;
    }

    /**
     * add an Out data
     * @param data
     */
    public void add(AbstractData data) {
        this.listResults.add(data);
    }

    public boolean hasMessage() {
        return this.message != null;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}

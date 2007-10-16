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

import java.io.Serializable;
import java.util.List;


public interface GeneralResult extends Serializable {
    public static final int SUCCESS = 0;
    public static final int ABORT = 1;

    public abstract int getState();

    public abstract void setState(int state);

    public abstract long getTimeExecution();

    public abstract void setTimeExecution(long timeExecution);

    public abstract String getId();

    public void add(AbstractData data);

    public List<AbstractData> getList();

    public AbstractData get(String name);

    public void setException(Exception exp);

    public void setMessage(String message);

    public boolean hasMessage();

    public String getMessage();

    public boolean isException();

    public Exception getException();
}

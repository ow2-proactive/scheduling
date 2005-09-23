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
package org.objectweb.proactive.branchnbound.core.queue;

import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.branchnbound.core.Task;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;


public interface TaskQueue extends Serializable {
    public final static Logger logger = ProActiveLogger.getLogger(Loggers.P2P_SKELETONS_MANAGER);
    public static final String backupTaskFile = System.getProperty("user.home") +
        System.getProperty("file.separator") + "framework.tasks.backup"; // TODO turn it configurable 

    public abstract void addAll(Collection tasks);

    public abstract IntWrapper size();

    public abstract BooleanWrapper hasNext();

    public abstract Task next();

    public abstract void flushAll();

    public abstract BooleanWrapper isHungry();

    public abstract void setHungryLevel(int level);

    public abstract void backupTasks(Task rootTask, Vector pendingTasks);

    public abstract void loadTasks(File taskFile);

    public abstract Task getRootTaskFromBackup();

    public abstract Vector getPendingTasksFromBackup();
}

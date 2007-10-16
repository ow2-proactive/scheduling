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
package org.objectweb.proactive.core.util;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * Provide an incremental per VM unique ID
 *
 * unique id starts from zero and is incremented each time getUniqID is called.
 * If Long.MAX_VALUE is reached then then IllegalStateArgument exception is thrown
 *
 * @See {@link ProActiveRandom}
 *
 */
public class ProActiveCounter {
    static Logger logger = ProActiveLogger.getLogger(Loggers.CORE);
    static Object lock = new Object();
    static long counter = 0;
    static boolean loop = false;

    synchronized static public long getUniqID() {
        long ret;

        if (loop) {
            throw new IllegalStateException(ProActiveCounter.class.getSimpleName() +
                " counter reached max value");
        }

        synchronized (lock) {
            ret = counter;
            counter++;
        }

        if (ret == Long.MAX_VALUE) {
            loop = true;
        }

        return ret;
    }
}

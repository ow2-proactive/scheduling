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
package org.objectweb.proactive.extra.scheduler.core;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map.Entry;

import org.objectweb.proactive.extra.scheduler.common.job.JobId;
import org.objectweb.proactive.extra.scheduler.common.job.JobResult;


/**
 * Class Serializer provide method to serialize result object or even job and other scheduler informations
 * in order to resume the scheduler after system or network failure.
 *
 * @author ProActive Team
 * @version 1.0, Jun 28, 2007
 * @since ProActive 3.2
 */
public class Serializer {

    /** Prefix for the generated filename */
    private static final String SERIALIZATION_PREFIX = "SCHED_";
    private static final String SERIALIZATION_POSTFIX = ".result";

    /**
     * Serialize every jobs results on hard drive before shutting down.
     * file will be named like that pattern : SCHED_{date}_{numJob}.result
     *
     * @param path the path on which to saves the results.
     * @param path the path on which to saves the results.
     * @throws IOException
     * @throws FileNotFoundException
     */
    public static void serializeResults(HashMap<JobId, JobResult> results,
        String path) throws FileNotFoundException, IOException {
        for (Entry<JobId, JobResult> e : results.entrySet()) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            String f = path + SERIALIZATION_PREFIX +
                String.format("%1$tm-%1$te-%1$tY", calendar) + "_" +
                e.getKey().toString() + SERIALIZATION_POSTFIX;
            serialize(f, e.getValue());
        }
    }

    /**
     * Serialize the given object in the given file path.
     *
     * @param path the path on which to saves the object.
     * @param toSerialize the object to serialize.
     * @throws FileNotFoundException
     * @throws IOException
     */
    private static void serialize(String path, Object toSerialize)
        throws FileNotFoundException, IOException {
        ObjectOutputStream objOut = new ObjectOutputStream(new FileOutputStream(
                    path));
        objOut.writeObject(toSerialize);
        objOut.close();
    }
}

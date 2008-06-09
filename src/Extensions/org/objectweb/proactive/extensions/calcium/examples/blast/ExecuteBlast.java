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
package org.objectweb.proactive.extensions.calcium.examples.blast;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.calcium.exceptions.EnvironmentException;
import org.objectweb.proactive.extensions.calcium.muscle.Execute;
import org.objectweb.proactive.extensions.calcium.stateness.StateFul;
import org.objectweb.proactive.extensions.calcium.system.PrefetchFilesMatching;
import org.objectweb.proactive.extensions.calcium.system.SkeletonSystem;
import org.objectweb.proactive.extensions.calcium.system.WSpace;


@StateFul(value = false)
@PrefetchFilesMatching(name = "db.*|query.*|blastall")
public class ExecuteBlast implements Execute<BlastParams, File> {
    static Logger logger = ProActiveLogger.getLogger(Loggers.SKELETONS_APPLICATION);

    public File execute(BlastParams param, SkeletonSystem system) throws IOException, InterruptedException,
            EnvironmentException {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing Blast");
        }

        //Put the native program in the wspace
        WSpace wspace = system.getWorkingSpace();

        //File blastProg = wspace.copyInto(param.blastProg);

        //Create a reference to a file in the wspace
        File result = wspace.newFile("result.blast");
        String args = param.getBlastParemeterString(result);

        //Execute the native blast
        system.execCommand(param.blastProg, args);

        return result;
    }
}

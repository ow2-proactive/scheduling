/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed, Concurrent
 * computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis Contact:
 * proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Initial developer(s): The ProActive Team
 * http://www.inria.fr/oasis/ProActive/contacts.html Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.calcium.examples.blast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.calcium.exceptions.ParameterException;
import org.objectweb.proactive.calcium.interfaces.Conquer;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;

public class ConquerResults implements Conquer<BlastParameters> {
    static Logger logger = ProActiveLogger.getLogger(Loggers.SKELETONS_APPLICATION);

    public int id;
    
    public ConquerResults(int id){
    	this.id=id;
    }
    
    public BlastParameters conquer(BlastParameters parent, Vector<BlastParameters> param) {
        try {

            BufferedWriter bw;

            if (logger.isDebugEnabled()) {
                logger.debug("Conquering results into File: " +
                    parent.getOutPutFile().getAbsolutePath());
            }

            bw = new BufferedWriter(new FileWriter(parent.getOutPutFile()));

            for (BlastParameters p : param) {
                appendTo(bw, p.getOutPutFile());
                p.getOutPutFile().delete();   //delete partial results
                p.getDatabaseFile().delete(); //delete partial files
            }

            bw.close();
            return parent;
        } catch (Exception e) {
            throw new ParameterException(e);
        }
    }

    private void appendTo(BufferedWriter bw, File dest)
        throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(dest));

        if (logger.isDebugEnabled()) {
            logger.debug("Conquering File: " + dest.getAbsolutePath() + " " +
                dest.length() + "[bytes]");
        }

        String line = br.readLine();
        while (line != null) {
            bw.write(line + System.getProperty("line.separator"));
            line = br.readLine();
        }
        br.close();
    }

	public int getMuscleId() {
		return id;
	}
}
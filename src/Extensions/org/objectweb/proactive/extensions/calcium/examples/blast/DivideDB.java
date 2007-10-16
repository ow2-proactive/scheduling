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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.calcium.muscle.Divide;
import org.objectweb.proactive.extensions.calcium.system.PrefetchFilesMatching;
import org.objectweb.proactive.extensions.calcium.system.SkeletonSystem;
import org.objectweb.proactive.extensions.calcium.system.WSpace;


//@PrefetchFilesMatching(name="db.*")
public class DivideDB implements Divide<BlastParams, BlastParams> {
    static Logger logger = ProActiveLogger.getLogger(Loggers.SKELETONS_APPLICATION);

    public Vector<BlastParams> divide(SkeletonSystem system, BlastParams param)
        throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("Dividing database file");
        }

        //Divide the file
        Vector<File> files = divideFile(system.getWorkingSpace(), param.dbFile,
                param.divideDBInto);

        //Create a new object for each file
        Vector<BlastParams> children = new Vector<BlastParams>();
        for (File newDBFile : files) {
            BlastParams newParam = new BlastParams(param);
            newParam.dbFile = newDBFile;
            children.add(newParam);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Dividing database file done");
        }

        return children;
    }

    /**
     * Divides a blast file into at most the specified number of parts.
     * If the sequences inside the file are less than numParts, then
     * the each sequeneces will be stored in a different file.
     * Note that sequences are undividable, thus the length of the divided
     * files will vary.
     * @param space
     *
     * @param file The file to split.
     * @param numParts The number of parts to split into.
     * @return A vector containing File representations of the divided files.
     * @throws IOException In case there are reading/writing problems.
     */
    private static Vector<File> divideFile(WSpace wspace, File file,
        int numParts) throws IOException {
        Vector<File> children = new Vector<File>();

        //Create the ouput files
        BufferedWriter[] bw = new BufferedWriter[numParts];
        for (int i = 0; i < bw.length; i++) {
            File f = wspace.newFile(file.getName() + "-" + i);
            bw[i] = new BufferedWriter(new FileWriter(f));
            children.add(f);
        }

        //Read from the file and write to the output files
        BufferedReader br = new BufferedReader(new FileReader(file));
        String registry = getNextRegistry(br, 8192);
        int i = -1;
        while (registry != null) {
            i = ++i % numParts;
            bw[i].write(registry + System.getProperty("line.separator"));
            registry = getNextRegistry(br, 8192);
        }

        //Cleanup and set return values
        br.close();
        for (i = numParts - 1; i >= 0; i--) {
            bw[i].close();

            File f = children.get(i);
            if (f.length() <= 0) {
                f.delete();
                children.remove(i);
            }
        }
        return children;
    }

    /**
     * Reads a sequence from the stream. The sequence can span through multiple line,
     * this method will group the sequence lines an return a single String with the header
     * of the sequence and its contente. The header line is identified because it begins with
     * the character ">".
     *
     * @param br The stream from which to read the file.
     * @param readAheadLimit The buffer is reset to the beggining of a new sequence when the sequence is found. To do this,
     * this parameter should be bigger than the longest line in the buffer.
     * @return The sequence lines, including the header. If no more sequences are available from the buffer null is returned.
     * @throws IOException If problems are encountered when reading from the stream.
     */
    private static String getNextRegistry(BufferedReader br, int readAheadLimit)
        throws IOException {
        String line = br.readLine();
        if ((line != null) && (line.indexOf(">") == 0)) { //look for the header line
            String registry = line;
            while (true) { //add the sequence
                br.mark(readAheadLimit);
                line = br.readLine();
                if (line == null) {
                    break;
                }
                if (line.indexOf(">") == 0) {
                    br.reset();
                    break;
                } else {
                    registry += (System.getProperty("line.separator") + line);
                }
            }
            return registry;
        }

        return null;
    }

    /*
        public static void main(String[] args) throws IOException {
            Vector<File> files = divideFile(new File("/tmp/10"), 3);
            for (File f : files) {
                System.out.println(f.getAbsolutePath() + " " + f.length() +  "[bytes]");
            }
        }
        */
}

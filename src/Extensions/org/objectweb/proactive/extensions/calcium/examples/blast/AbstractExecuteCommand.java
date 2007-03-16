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
package org.objectweb.proactive.extensions.calcium.examples.blast;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.objectweb.proactive.extensions.calcium.exceptions.EnvironmentException;
import org.objectweb.proactive.extensions.calcium.exceptions.MuscleException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public abstract class AbstractExecuteCommand {
    static Logger logger = ProActiveLogger.getLogger(Loggers.SKELETONS_APPLICATION);

    Process execCommand(URL program, String[] args, File workingDir,
        String add2path) throws IOException {
        if ((program == null) || (program.getPath().length() <= 0)) {
            throw new IllegalArgumentException("Program path is not specified");
        }

        List<String> command = new ArrayList<String>();
        command.add(program.getPath());

        for (String s : args) {
            command.add(s);
        }
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);

        pb.directory(workingDir);

        if ((add2path != null) && (add2path.length() > 0)) {
            Map<String, String> env = pb.environment();
            env.put("PATH",
                env.get("PATH") + System.getProperty("path.separator") +
                add2path);
        }

        Process p = pb.start();

        return p;
    }

    private static void printStream(InputStream in) throws IOException {
        String line;
        BufferedReader input = new BufferedReader(new InputStreamReader(in));
        while ((line = input.readLine()) != null) {
            System.out.println(line);
        }
    }

    protected abstract URL getProgramURL()
        throws EnvironmentException, MuscleException;

    protected void execProcess(String arguments, File workingDirectory)
        throws EnvironmentException {
        URL programURL = getProgramURL();

        try {
            Process process = execCommand(programURL, arguments.split(" "),
                    workingDirectory, "");
            if (process.waitFor() != 0) {
                String msg = "Command did not finish successfully: " +
                    programURL + " " + arguments;
                logger.error(msg);
                throw new MuscleException(msg);
            }

            //TODO perhaps do some verification here?
        } catch (Exception e) {
            throw new MuscleException(e);
        }
    }
}

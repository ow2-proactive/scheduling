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
package org.objectweb.proactive.extensions.masterworker.tasks;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.extensions.masterworker.interfaces.Task;
import org.objectweb.proactive.extensions.masterworker.interfaces.WorkerMemory;


/**
 * This native task is a basic example of how to write a task that will execute a native command.<br/>
 * The result of the task will be the task's standard output <br/>
 * @author fviale
 *
 */
@PublicAPI
public class NativeTask implements Task<String[]> {

    /**
     *
     */
    private String[] commandArray = null;
    private String[] envp = null;
    private URL urlDir = null;
    private static long main_ID = 0;
    private long id;

    /**
     * Creates a Native Task with the given command (it can include command and arguments in a single String)
     * @param command
     */
    public NativeTask(String command) {
        this(command, null, null);
    }

    /**
     * Creates a Native Task with the given commandArray (i.e. command and arguments)
     * @param commandArray
     */
    public NativeTask(String[] commandArray) {
        this(commandArray, null, null);
    }

    /**
     * Creates a Native Task with the given command (it can include command and arguments in a single String) and URL of the working directory
     * @param command
     * @param urlDir
     * @see java.lang.Runtime#exec(String, String[], File)
     */
    public NativeTask(String command, URL urlDir) {
        this(command, null, urlDir);
    }

    /**
     * Creates a Native Task with the given commandArray (i.e. command and arguments) and URL of the working dir
     * @param commandArray
     * @param urlDir
     * @see java.lang.Runtime#exec(String[], String[], File)
     */
    public NativeTask(String[] commandArray, URL urlDir) {
        this(commandArray, null, urlDir);
    }

    /**
     * Creates a Native Task with the given command (it can include command and arguments in a single String) and environment
     * @param command
     * @param envp
     * @see java.lang.Runtime#exec(String, String[])
     */
    public NativeTask(String command, String[] envp) {
        this(command, envp, null);
    }

    /**
     * Creates a Native Task with the given commandArray (i.e. command and arguments) and environment
     * @param commandArray
     * @param envp
     * @see java.lang.Runtime#exec(String[], String[])
     */
    public NativeTask(String[] commandArray, String[] envp) {
        this(commandArray, envp, null);
    }

    /**
     * Creates a Native Task with the given command (it can include command and arguments in a single String), URL of the working dir and environment
     * @param command
     * @param envp
     * @param urlDir
     */
    public NativeTask(String command, String[] envp, URL urlDir) {
        this(command.split(" "), envp, urlDir);
    }

    /**
     * Creates a Native Task with the given commandArray (i.e. command and arguments) , URL of the working dir and environment
     * @param commandArray
     * @param envp
     * @param urlDir
     */
    public NativeTask(String[] commandArray, String[] envp, URL urlDir) {
        this.commandArray = commandArray;
        this.envp = envp;
        this.urlDir = urlDir;
        this.id = main_ID++;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extensions.masterworker.interfaces.Task#run(org.objectweb.proactive.extensions.masterworker.interfaces.WorkerMemory)
     */
    public String[] run(WorkerMemory memory) throws IOException, URISyntaxException {
        ArrayList<String> lines = new ArrayList<String>();
        Runtime runtime = Runtime.getRuntime();
        Process process = null;
        if (urlDir != null) {
            process = runtime.exec(commandArray, envp, new File(urlDir.toURI()));
        } else {
            process = runtime.exec(commandArray, envp, null);
        }
        BufferedReader d = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = d.readLine()) != null) {
            lines.add(line);
        }
        return lines.toArray(new String[0]);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return (int) id;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NativeTask) {
            return id == ((NativeTask) obj).id;
        }
        return false;
    }
}

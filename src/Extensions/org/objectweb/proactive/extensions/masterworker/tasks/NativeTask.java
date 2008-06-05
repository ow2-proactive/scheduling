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

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.extensions.masterworker.interfaces.Task;
import org.objectweb.proactive.extensions.masterworker.interfaces.WorkerMemory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;


/**
 * This native task is a basic example of how to write a task that will execute a native command.<br/>
 * The result of the task will be the task's standard output <br/>
 * @author The ProActive Team
 *
 */
@PublicAPI
public class NativeTask implements Task<ArrayList<String>> {

    /**
     *
     */
    private String[] commandArray = null;
    private String[] envp = null;
    private File fileDir = null;
    private static long main_ID = 0;
    private long id;

    /**
     * Creates a Native Task with the given command (it can include command and arguments in a single String)
     * @param command command that this native task will execute
     * @see java.lang.Runtime#exec(String)
     */
    public NativeTask(String command) {
        this(command, null, null);
    }

    /**
     * Creates a Native Task with the given commandArray (i.e. command and arguments)
     * @param commandArray command array that this native task will execute
     * @see java.lang.Runtime#exec(String[])
     */
    public NativeTask(String[] commandArray) {
        this(commandArray, null, null);
    }

    /**
     * Creates a Native Task with the given command (it can include command and arguments in a single String) and URL of the working directory
     * @param command a specified system command
     * @param fileDir directory in which the native command will be executed
     * @see java.lang.Runtime#exec(String, String[], File)
     */
    public NativeTask(String command, File fileDir) {
        this(command, null, fileDir);
    }

    /**
     * Creates a Native Task with the given commandArray (i.e. command and arguments) and URL of the working dir
     * @param commandArray command that this native task will execute
     * @param fileDir directory in which the native command will be executed
     * @see java.lang.Runtime#exec(String[], String[], File)
     */
    public NativeTask(String[] commandArray, File fileDir) {
        this(commandArray, null, fileDir);
    }

    /**
     * Creates a Native Task with the given command (it can include command and arguments in a single String) and environment
     * @param command
     * @param envp array of strings, each element of which has environment variable settings in the format name=value,
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
     * @param fileDir
     */
    public NativeTask(String command, String[] envp, File fileDir) {
        this(command.split(" "), envp, fileDir);
    }

    /**
     * Creates a Native Task with the given commandArray (i.e. command and arguments) , URL of the working dir and environment
     * @param commandArray
     * @param envp
     * @param fileDir
     */
    public NativeTask(String[] commandArray, String[] envp, File fileDir) {
        setCommand(commandArray, envp, fileDir);
        this.id = main_ID++;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.extensions.masterworker.interfaces.Task#run(org.objectweb.proactive.extensions.masterworker.interfaces.WorkerMemory)
     */
    public ArrayList<String> run(WorkerMemory memory) throws IOException, URISyntaxException {
        Runtime runtime = Runtime.getRuntime();
        Process process = null;
        if (fileDir != null) {
            process = runtime.exec(commandArray, envp, fileDir);
        } else {
            process = runtime.exec(commandArray, envp, null);
        }

        ArrayList<String> lines = getContentAsList(process.getInputStream());
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return lines;
    }

    protected void setCommand(String command, String[] envp, File fileDir) {
        setCommand(command.split(" "), envp, fileDir);
    }

    protected void setCommand(String[] commandArray, String[] envp, File fileDir) {
        this.commandArray = commandArray;
        this.envp = envp;
        this.fileDir = fileDir;
    }

    /**
     * Return the content read through the given text input stream as a list of file
     * @param is input stream to read
     * @return content as list of strings
     */
    private ArrayList<String> getContentAsList(InputStream is) {
        ArrayList<String> lines = new ArrayList<String>();
        BufferedReader d = new BufferedReader(new InputStreamReader(new BufferedInputStream(is)));

        String line = null;

        try {
            line = d.readLine();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        while (line != null) {
            lines.add(line);

            try {
                line = d.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                line = null;
            }
        }

        try {
            d.close();
        } catch (IOException e) {
        }

        return lines;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return (int) id;
    }

    /*
     * (non-Javadoc)
     * 
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

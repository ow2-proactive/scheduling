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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * Task interface, it can be either a ScilabTask or a MatlabTask
 * @author fviale
 *
 */
@PublicAPI
public interface GeneralTask extends Serializable {

    /**
     * Initialize a task
     * @throws TaskException
     */
    public void init() throws TaskException;

    /**
     * Retrieve the job script textual form
     * @return script text
     */
    public String getJob();

    /**
     * Set the job script text
     * @param job script text
     */
    public void setJob(String job);

    /**
     * Set the job initialization script
     * @param jobInit initialization script
     */
    public void setJobInit(String jobInit);

    /**
     * Set the job script from a file
     * @param fileJob file to get the script from
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void setJob(File fileJob) throws FileNotFoundException, IOException;

    /**
     * Retrieve the task id
     * @return task id
     */
    public String getId();

    /**
     * Retrieve the job initialization script
     * @return
     */
    public String getJobInit();

    /**
     * Retrieve the last message from scilab or matlab
     * @return message text
     */
    public String getLastMessage();

    /**
     * Send the list of input data into the engine
     * @throws TaskException
     */
    public void sendListDataIn() throws TaskException;

    /**
     * Receive the list of output data from the engine
     * @return list of data
     * @throws TaskException
     */
    public List<AbstractData> receiveDataOut() throws TaskException;

    /**
     * Clear the engine workspace
     * @throws TaskException
     */
    public void clearWorkspace() throws TaskException;

    /**
     * Execute the job script
     * @return boolean value depending on the engine
     * @throws TaskException
     */
    public boolean execute() throws TaskException;

    /**
     * Add an output data
     * @param data data to add
     */
    public void addDataOut(String data);
}

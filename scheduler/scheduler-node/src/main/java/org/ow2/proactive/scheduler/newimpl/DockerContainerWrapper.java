/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2014 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 *  * Tobias Wiens
 */
package org.ow2.proactive.scheduler.newimpl;

import org.apache.log4j.Logger;
import org.objectweb.proactive.extensions.processbuilder.OSProcessBuilder;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.task.utils.ForkerUtils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class DockerContainerWrapper {

    private static final Logger logger = Logger.getLogger(DockerContainerWrapper.class);

    // CONSTANTS
    // TODO bring the, in config files
    private static String sudoCommand = "/usr/bin/sudo";
    private static String dockerCommand = "/usr/bin/docker";


    private static String killArgument = "stop";

    private String taskId;
    //TODO default value can be written somehow nice
    private String containerName = "tobwiens/proactive-executer";

    private Map<String, String> volumeDirectoryMap;

    private boolean useSudo = true;

    public DockerContainerWrapper(String taskId) {
        this.taskId = taskId;

        this.volumeDirectoryMap = new HashMap< >();
    }

    public DockerContainerWrapper(String taskId, String containerName){
        this(taskId); // Call taskid constructor
        this.containerName = containerName;
    }

    public void addVolumeDirectory(String localPath, String containerPath) {
        this.volumeDirectoryMap.put(localPath, containerPath);
    }

    /**
     *
     * @param localPath Local path which was used to mount.
     * @return Returns null when local path was not found and localPath otherwise
     */
    public String removeVolumeDirectoryByLocalPath(String localPath) {
        return this.volumeDirectoryMap.remove(localPath);
    }

    /**
     * Adds sudo command and the front of the command
     * if it is configured to do so and if there is not sudo command already
     * otherwise it does add nothing.
     * @param commands ArrayList which holds the commands
     */
    private void sudoCommand(ArrayList<String> commands) {
        if(this.useSudo )
            commands.add(0 ,sudoCommand);
    }

    public void kill() {

        ProcessStreamsReader processStreamsReader = null;

        Process process;

        ByteArrayOutputStream outputByteArray = new ByteArrayOutputStream();
        PrintStream outputSink = new PrintStream(outputByteArray);

        ByteArrayOutputStream errorByteArray = new ByteArrayOutputStream();
        PrintStream errorSink = new PrintStream(errorByteArray);
        try {
            // Kill docker container, therefore execute a docker stop command
            OSProcessBuilder pb = this.createProcessBuilder();

            // Create commands array
            ArrayList<String> commands = new ArrayList< >();

            // Create sudo if necessary
            this.sudoCommand(commands);

            // Add docker command
            commands.add(dockerCommand);

            // Add stop to stop/kill the container
            commands.add(killArgument);

            // Add taskid, because the container got it as a name
            commands.add(this.taskId);

            // Finished building commands, now create array
            String[] commandsArray = commands.toArray(new String[commands.size()]);

            // Add commands to process builder and execute
            pb.command(commandsArray);
            process = pb.start();
            processStreamsReader = new ProcessStreamsReader(process, outputSink, errorSink);

            // The thread might have been interrupted, but it has to wait for the next command to finish.
            // Save the current interrupted state and reset interrupt flag
            boolean isInterrupted = Thread.interrupted();

            // Wait until executed
            int returnCode = process.waitFor();

            // Restore interrupt state of thread
            if (isInterrupted)
                Thread.currentThread().interrupt();

            logger.info("Docker container successfully killed with name:"+this.taskId+" return code:"+returnCode);


        } catch (Exception e) {
            logger.warn("Failed to kill docker container with name:"+this.taskId+".\n Error and standard output is: Error: "+
                    outputByteArray.toString()+"\n stdout: "+errorByteArray.toString(), e);
        } finally {

            outputSink.close();
            errorSink.close();

            if (processStreamsReader != null) {
                processStreamsReader.close();
            }
        }

    }

    private OSProcessBuilder createProcessBuilder() {
        OSProcessBuilder pb;
        String nativeScriptPath = PASchedulerProperties.SCHEDULER_HOME.getValueAsString(); // TODO inject

        pb = ForkerUtils.getOSProcessBuilderFactory(nativeScriptPath).getBuilder();

        return pb;
    }
}

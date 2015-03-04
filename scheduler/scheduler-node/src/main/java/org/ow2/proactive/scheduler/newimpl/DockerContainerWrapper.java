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
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.extensions.processbuilder.exception.CoreBindingException;
import org.objectweb.proactive.extensions.processbuilder.exception.FatalProcessBuilderException;
import org.objectweb.proactive.extensions.processbuilder.exception.OSUserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;


/**
 *
 */
public class DockerContainerWrapper {

    private static final Logger logger = Logger.getLogger(DockerContainerWrapper.class);

    // CONSTANTS
    // TODO bring the, in config files
    public static final String SUDO_COMMAND_PROPERTY = "SUDO_COMMAND";
    public static final String DOCKER_COMMAND_PROPERTY = "DOCKER_COMMAND";

    public static final String DOCKER_SETTINGS_FILENAME = "docker-settings.ini";
    public static final String DOCKER_SETTINGS_DOCKER_COMMAND = "docker.docker-command";
    public static final String DOCKER_SETTINGS_SUDO_COMMAND = "docker.sudo-dommand";
    public static final String DOCKER_SETTINGS_STANDARD_IMAGE = "docker.standard-image";

    public static final String INSIDE_CONTAINER_SCHEDULING_HOME = "/data/scheduling";
    public static final String INSIDE_CONTAINER_JAVA_COMMAND = "java";
    public static final String INSIDE_CONTAINER_CLASSPATH_SWITCH = "-cp";
    public static final String INSIDE_CONTAINER_CLASSPATH = "/data/scheduling/dist/lib/*:/data/scheduling/addons/*:/data/scheduling/addons/";
    //public static final String INSIDE_CONTAINER_SECURITY_POLICY =
    //        "-Djava.security.policy="
    //        +CentralPAPropertyRepository
    //        .JAVA_SECURITY_POLICY
    //        .getValue()
    //        .replace(CentralPAPropertyRepository.PA_HOME.getValue(),INSIDE_CONTAINER_SCHEDULING_HOME);

    // :ro means to mount it as read-only -- IMPORTANT
    public static final String SCHEDULING_CONTAINER_HOME_MOUNT = INSIDE_CONTAINER_SCHEDULING_HOME + ":ro";
    public static final String DOCKER_IMAGE_PROPERTY = "STANDARD_DOCKER_CONTAINER";

    public static final String KILL_ARGUMENT = "stop";
    public static final String REMOVE_ARGUMENT = "rm";
    public static final String START_ARGUMENT = "run";
    public static final String NAME_SWITCH = "--name";
    public static final String VOLUME_SWITCH = "-v";

    private String name;
    private String image;

    static {// TODO Put that in config
        DockerContainerWrapper.logger.info("Initialize docker container support.");
        // Write dummy values - makes error clearer instead of having null in the properties
        //TODO Find a way to get path for config and set those commands to dummies
        //System.setProperty(DockerContainerWrapper.SUDO_COMMAND_PROPERTY, "sudo_cmd_not_set");
        //System.setProperty(DockerContainerWrapper.DOCKER_COMMAND_PROPERTY, "docker_cmd_not_set");
        //System.setProperty(DockerContainerWrapper.DOCKER_IMAGE_PROPERTY,  "standard_container_not_set");

        System.setProperty(DockerContainerWrapper.SUDO_COMMAND_PROPERTY, "/usr/bin/sudo");
        System.setProperty(DockerContainerWrapper.DOCKER_COMMAND_PROPERTY, "/usr/bin/docker");
        System.setProperty(DockerContainerWrapper.DOCKER_IMAGE_PROPERTY,  "tobwiens/proactive-executor");

        // Those dummy values will now be overwritten with actual values - but if something goes wrong it is
        // visible in the logs. But the scheduler will show an uninformative null pointer exception. Those dummy values
        // will throw exception which are more meaningful

        InputStream dockerSettings = DockerContainerWrapper.class.getClassLoader().getResourceAsStream(DOCKER_SETTINGS_FILENAME);

        if (dockerSettings != null) {
            try {
                Properties props = new Properties();
                props.load(dockerSettings);
                System.setProperty(DockerContainerWrapper.SUDO_COMMAND_PROPERTY, props.getProperty(DOCKER_SETTINGS_SUDO_COMMAND));
                System.setProperty(DockerContainerWrapper.DOCKER_COMMAND_PROPERTY, props.getProperty(DOCKER_SETTINGS_DOCKER_COMMAND));
                System.setProperty(DockerContainerWrapper.DOCKER_IMAGE_PROPERTY,  props.getProperty(DOCKER_SETTINGS_STANDARD_IMAGE));

                dockerSettings.close();
                DockerContainerWrapper.logger.info("Docker settings file found and properties successfully read");
            } catch (IOException e) {
                DockerContainerWrapper.logger.warn("Could not load docker-settings.ini in classpath.", e);
            } catch (Throwable e) {
                DockerContainerWrapper.logger.warn("Error during DockerContainerWrapper initialization.", e);
            }
        }  else {
            DockerContainerWrapper.logger.warn("Could not find docker-settings.ini in classpath. " +
                    "Docker containers won't work.");

        }
    }

    private boolean useSudo = true;

    private Map<String, String> volumeDirectoryMap;

    public boolean isUseSudo() {
        return useSudo;
    }

    public void setUseSudo(boolean useSudo) {
        this.useSudo = useSudo;
    }

    public String getName() {
        return name;
    }

    public DockerContainerWrapper(String name) {
        this.name = name;

        this.image = System.getProperty(DockerContainerWrapper.DOCKER_IMAGE_PROPERTY);
        this.volumeDirectoryMap = new HashMap<String, String>();

        // Mount scheduling from disk -- add as volume
        this.addVolumeDirectory(CentralPAPropertyRepository.PA_HOME.getValue(),
                SCHEDULING_CONTAINER_HOME_MOUNT);
    }

    public DockerContainerWrapper(String name, String image) {
        this(name); // Call taskid constructor
        this.image = image;
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
        if (this.useSudo)
            commands.add(0, System.getProperty(DockerContainerWrapper.SUDO_COMMAND_PROPERTY));
    }

    /**
     * Creates an remove command.
     * @return Array of strings, which represent the command to remove the docker container.
     */
    public String[] remove() {
        // Create commands array
        ArrayList<String> command = new ArrayList<String>();

        // Create sudo if necessary
        this.sudoCommand(command);

        // Add docker command
        command.add(System.getProperty(DockerContainerWrapper.DOCKER_COMMAND_PROPERTY));

        // Add stop to stop/kill the container
        command.add(REMOVE_ARGUMENT);

        // Add taskid, because the container got it as a name
        command.add(this.name);

        // Return command as array
        return command.toArray(new String[command.size()]);
    }

    /**
     * Starts a docker container and returns a string array which represents to start the docker container.
     * @return String array, representing the start command.
     */
    public String[] start(String classToExecute, String... arguments)  {
        // Create commands array
        ArrayList<String> command = new ArrayList<String>();

        // Create sudo if necessary
        this.sudoCommand(command);

        // Add docker command
        command.add(System.getProperty(DockerContainerWrapper.DOCKER_COMMAND_PROPERTY));

        // Add stop to stop/kill the container
        command.add(START_ARGUMENT);

        // Add name switch
        command.add(NAME_SWITCH);

        // Add taskid, because the container got it as a name
        command.add(this.name);

        // Add volumes
        for (Map.Entry<String, String> volume : this.volumeDirectoryMap.entrySet()) {
            command.add(VOLUME_SWITCH);
            command.add(volume.getKey() + ":" + volume.getValue());
        }

        // DEBUG forward debugging port
        //command.add("-p");
        //command.add("5007:5007");

        // Add container name
        command.add(this.image);

        // Add java command
        command.add(INSIDE_CONTAINER_JAVA_COMMAND);

        // TODO Add java security policy.
        /**
         * The CentralPAPropertyRepository
         .JAVA_SECURITY_POLICY
         .getValue()
         return jar:file/path/to/jar!/config/java-client-policy, which has nothing to do with the
         real location of the policy file. Therefore it is missing a possibility to find the policy file
         and apply it to the client.
         */
        /*
        // Add security policy
        command.add(INSIDE_CONTAINER_SECURITY_POLICY);
         */
        // ADD classpath
        command.add(INSIDE_CONTAINER_CLASSPATH_SWITCH);
        command.add(INSIDE_CONTAINER_CLASSPATH);

        // DEBUG - debug
        //command.add("-Xdebug");
        //command.add("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5007");

        // Add java class and arguments
        command.add(classToExecute);

        // Add all arguments
        Collections.addAll(command, arguments);

        return command.toArray(new String[command.size()]);
    }

    /**
     * Creates a stop command.
     * @return String array to stop the container.
     */
    public String[] stop() {

        // Create commands array
        ArrayList<String> command = new ArrayList<String>();

        // Create sudo if necessary
        this.sudoCommand(command);

        // Add docker command
        command.add(System.getProperty(DockerContainerWrapper.DOCKER_COMMAND_PROPERTY));

        // Add stop to stop/kill the container
        command.add(KILL_ARGUMENT);

        // Add taskid, because the container got it as a name
        command.add(this.name);

        // Return command as array
        return command.toArray(new String[command.size()]);
    }
}

package org.ow2.proactive.scheduler.newimpl;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import org.junit.Test;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.extensions.processbuilder.exception.CoreBindingException;
import org.objectweb.proactive.extensions.processbuilder.exception.FatalProcessBuilderException;
import org.objectweb.proactive.extensions.processbuilder.exception.OSUserException;
import org.ow2.proactive.scripting.InvalidScriptException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class DockerContainerWrapperTest {

    private static String oldDocker;
    private static String oldSudo;
    private static String oldImage;

    @BeforeClass
    public static void prepare() throws InvalidScriptException {

        //Set system properties
        oldDocker = System.getProperty(DockerContainerWrapper.DOCKER_COMMAND_PROPERTY);
        oldSudo = System.getProperty(DockerContainerWrapper.SUDO_COMMAND_PROPERTY);
        oldImage = System.getProperty(DockerContainerWrapper.DOCKER_IMAGE_PROPERTY);
        System.setProperty(DockerContainerWrapper.DOCKER_COMMAND_PROPERTY, "docker");
        System.setProperty(DockerContainerWrapper.SUDO_COMMAND_PROPERTY, "sudo");
        System.setProperty(DockerContainerWrapper.DOCKER_IMAGE_PROPERTY, "test/image");
    }

    private static void resetProperty(String key, String value) {
        if (value != null) {
            System.setProperty(key, value);
        } else {
            System.clearProperty(key);
        }
    }

    @AfterClass
    public static void tearDown() throws Exception {
        // Restore System properties
        resetProperty(DockerContainerWrapper.DOCKER_COMMAND_PROPERTY, oldDocker);
        resetProperty(DockerContainerWrapper.SUDO_COMMAND_PROPERTY, oldSudo);
        resetProperty(DockerContainerWrapper.DOCKER_IMAGE_PROPERTY, oldImage);
    }

    @Test
    public void dockerCommandAndSudo() throws OSUserException, FatalProcessBuilderException,
            CoreBindingException, IOException {
        dockerStartSudo(true);
        dockerStartSudo(false);

        dockerStopSudo(true);
        dockerStopSudo(false);

        dockerRemoveSudo(true);
        dockerRemoveSudo(false);
    }

    public void dockerStartSudo(boolean useSudo) throws OSUserException, IOException,
            FatalProcessBuilderException, CoreBindingException {
        String name = "container";
        DockerContainerWrapper container = new DockerContainerWrapper(name);

        container.setUseSudo(useSudo);

        String[] command = container.start("Class.To.Exec", new String[] { "1", "2", "3" });

        // Take a running index to go through commands and check for commands to be made correctly
        int index = 0;

        // Check if sudo was used
        if (useSudo) {
            assertEquals(System.getProperty(DockerContainerWrapper.SUDO_COMMAND_PROPERTY), command[index++]);
        }
        // Check if docker command comes next
        assertEquals(System.getProperty(DockerContainerWrapper.DOCKER_COMMAND_PROPERTY), command[index++]);
        // check if run comes next
        assertEquals(DockerContainerWrapper.START_ARGUMENT, command[index++].toLowerCase());
    }

    public void dockerStopSudo(boolean useSudo) throws OSUserException, IOException,
            FatalProcessBuilderException, CoreBindingException {
        String name = "container";
        DockerContainerWrapper container = new DockerContainerWrapper(name);

        container.setUseSudo(useSudo);

        String[] command = container.stop();

        // Take a running index to go through commands and check for commands to be made correctly
        int index = 0;

        // Check if sudo was used
        if (useSudo) {
            assertEquals(System.getProperty(DockerContainerWrapper.SUDO_COMMAND_PROPERTY), command[index++]);
        }
        // Check if docker command comes next
        assertEquals(System.getProperty(DockerContainerWrapper.DOCKER_COMMAND_PROPERTY), command[index++]);
        // check if run comes next
        assertEquals(DockerContainerWrapper.KILL_ARGUMENT, command[index++].toLowerCase());

        assertEquals(name, command[index++]);
    }

    public void dockerRemoveSudo(boolean useSudo) throws OSUserException, IOException,
            FatalProcessBuilderException, CoreBindingException {
        String name = "container";
        DockerContainerWrapper container = new DockerContainerWrapper(name);

        container.setUseSudo(useSudo);

        String[] command = container.remove();

        // Take a running index to go through commands and check for commands to be made correctly
        int index = 0;

        // Check if sudo was used
        if (useSudo) {
            assertEquals(System.getProperty(DockerContainerWrapper.SUDO_COMMAND_PROPERTY), command[index++]);
        }
        // Check if docker command comes next
        assertEquals(System.getProperty(DockerContainerWrapper.DOCKER_COMMAND_PROPERTY), command[index++]);
        // check if run comes next
        assertEquals(DockerContainerWrapper.REMOVE_ARGUMENT, command[index++].toLowerCase());

        assertEquals(name, command[index++]);
    }

    public void dockerContainerNameTest() throws OSUserException, FatalProcessBuilderException,
            CoreBindingException, IOException {
        dockerContainerName(randomString(20));
    }

    public void dockerContainerName(String name) throws OSUserException, IOException,
            FatalProcessBuilderException, CoreBindingException {
        DockerContainerWrapper container = new DockerContainerWrapper(name);

        String[] command = container.start("Class.To.Exec", new String[] { "1", "2", "3" });

        String argument = null;
        for (Iterator<String> it = Arrays.asList(command).iterator(); it.hasNext(); argument = it.next()) {
            // When the name switch is found look for name as next argument
            if (argument.contains(DockerContainerWrapper.NAME_SWITCH)) {
                assertEquals(name, it.next());
                break;
            }
        }
    }

    @Test
    public void dockerDirectoryVolumesTest() throws OSUserException, FatalProcessBuilderException,
            CoreBindingException, IOException {
        ArrayList<String> localPath = new ArrayList<String>();
        ArrayList<String> containerPath = new ArrayList<String>();

        // Try with no volume switches
        dockerDirectoryVolumes(localPath, containerPath);

        // Both the same
        localPath.add("/user/Tom/myfiles");
        containerPath.add("/user/Tom/myfiles");
        dockerDirectoryVolumes(localPath, containerPath);

        // Windows and linux
        localPath.add("C://user/Tom/myfiles");
        containerPath.add("/user/Tom/myfiles");
        dockerDirectoryVolumes(localPath, containerPath);

        localPath.add(randomString(20));
        containerPath.add(randomString(20));
        dockerDirectoryVolumes(localPath, containerPath);

    }

    private String randomString(int length) {
        char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTWYZ1234567890-_/\\".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        return sb.toString();
    }

    public void dockerDirectoryVolumes(ArrayList<String> localPath, ArrayList<String> containerPath)
            throws OSUserException, IOException, FatalProcessBuilderException, CoreBindingException {
        String name = "container";
        DockerContainerWrapper container = new DockerContainerWrapper(name);

        // The PA_HOME is always mounted inside the container, to provide the source for running the
        // task. Make sure it is mounted as read-only with the :ro options attached
        assertTrue(container.removeVolumeDirectoryByLocalPath(CentralPAPropertyRepository.PA_HOME.getValue())
                .endsWith(":ro"));

        for (int i = 0; i < localPath.size(); i++) {
            container.addVolumeDirectory(localPath.get(0), containerPath.get(0));
        }
        String[] command = container.start("Class.To.Exec", new String[] { "1", "2", "3" });

        String argument = null;

        // Index will start with one, because the scheduling directory is mounted from disk
        // and that must always happen and offsets the number of volumes plus 1
        int index = 0;
        for (Iterator<String> it = Arrays.asList(command).iterator(); it.hasNext();) {
            argument = it.next();
            // When the volume switch is found look for correct directory mount as next argument
            try {
                if (argument.contains(DockerContainerWrapper.VOLUME_SWITCH)) {
                    assertEquals(localPath.get(index) + ":" + containerPath.get(index), it.next());
                    index++;

                }
            } catch (IndexOutOfBoundsException e) {
                assertTrue(
                        "Number of volume switches does not fit number of volume entries given to container wrapper",
                        false);
            }
        }
    }

}

package org.ow2.proactive.scheduler.newimpl;

import org.codehaus.plexus.util.StringInputStream;
import org.junit.Test;
import org.objectweb.proactive.extensions.processbuilder.CoreBindingDescriptor;
import org.objectweb.proactive.extensions.processbuilder.OSProcessBuilder;
import org.objectweb.proactive.extensions.processbuilder.OSUser;
import org.objectweb.proactive.extensions.processbuilder.exception.CoreBindingException;
import org.objectweb.proactive.extensions.processbuilder.exception.FatalProcessBuilderException;
import org.objectweb.proactive.extensions.processbuilder.exception.OSUserException;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class PBCommandExecutorTest {

    // Save if method was called
    public boolean waitFor = false;
    public boolean destroy = false;

    public int plannedReturnCode = 0;

    public String[] givenCommand;

    public String standardOutput = "Blah blah command ran 5 seconds";
    public String errorOutput = "Maybe something went wrong";

    public class ProcessImpl extends Process {

        @Override
        public OutputStream getOutputStream() {
            return null;
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(standardOutput.getBytes());
        }

        @Override
        public InputStream getErrorStream() {
            return new ByteArrayInputStream(errorOutput.getBytes());
        }

        @Override
        public int waitFor() throws InterruptedException {
            waitFor = true;
            return plannedReturnCode;
        }

        @Override
        public int exitValue() {
            return 0;
        }

        @Override
        public void destroy() {

        }
    }

    public class OSProcessbuildImpl implements OSProcessBuilder {

        private List<String> command;

        @Override
        public List<String> command() {
            return this.command;
        }

        @Override
        public OSProcessBuilder command(String... command) {
            givenCommand = command;
            this.command = Arrays.asList(command);
            return this;
        }

        @Override
        public OSUser user() {
            return null;
        }

        @Override
        public boolean canExecuteAsUser(OSUser user) throws FatalProcessBuilderException {
            return false;
        }

        @Override
        public CoreBindingDescriptor cores() {
            return null;
        }

        @Override
        public boolean isCoreBindingSupported() {
            return false;
        }

        @Override
        public CoreBindingDescriptor getAvaliableCoresDescriptor() {
            return null;
        }

        @Override
        public File directory() {
            return null;
        }

        @Override
        public OSProcessBuilder directory(File directory) {
            return null;
        }

        @Override
        public Map<String, String> environment() {
            return null;
        }

        @Override
        public boolean redirectErrorStream() {
            return false;
        }

        @Override
        public OSProcessBuilder redirectErrorStream(boolean redirectErrorStream) {
            return null;
        }

        @Override
        public Process start() throws IOException, OSUserException, CoreBindingException,
                FatalProcessBuilderException {
            return new ProcessImpl();
        }
    }

    @Test
    public void executeCommandTest() throws FailedExecutionException, InterruptedException {

        PBCommandExecutor executor = new PBCommandExecutor(new OSProcessbuildImpl());

        ByteArrayOutputStream standardInputArray = new ByteArrayOutputStream();
        PrintStream standard = new PrintStream(standardInputArray);

        ByteArrayOutputStream errorInputArray = new ByteArrayOutputStream();
        PrintStream error = new PrintStream(errorInputArray);

        String[] commandExecuted = new String[] { "My", "command", "here" };

        int returnCode = executor.executeCommand(standard, error, commandExecuted);

        // Check if standard/error output is the same and given to the calling method
        // contains is used because newline characters and added whitespaces make equals fail
        standard.flush();
        assertTrue("Output needs to be similar that the one given by task.", standardInputArray.toString()
                .contains(standardOutput));

        error.flush();
        assertTrue("Output needs to be similar that the one given by task.", errorInputArray.toString()
                .contains(errorOutput));

        // Check if command is the same
        assertArrayEquals(commandExecuted, givenCommand);

        // Waitfor was called, meaning process was started
        assertEquals(waitFor, true);

        // Process was not destroyed/killed it executed normally
        assertEquals(destroy, false);

        // Correct return code is given to calling method
        assertEquals(plannedReturnCode, returnCode);

    }
}

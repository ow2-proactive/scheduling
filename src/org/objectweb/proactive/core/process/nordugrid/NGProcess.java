package org.objectweb.proactive.core.process.nordugrid;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.process.AbstractExternalProcessDecorator;
import org.objectweb.proactive.core.process.UniversalProcess;


public class NGProcess extends AbstractExternalProcessDecorator {
    private static final String FILE_SEPARATOR = System.getProperty(
            "file.separator");
    private static final String DEFAULT_SCRIPT_LOCATION = System.getProperty(
            "user.home") + FILE_SEPARATOR + "ProActive" + FILE_SEPARATOR +
        "scripts" + FILE_SEPARATOR + "unix" + FILE_SEPARATOR + "cluster" +
        FILE_SEPARATOR + "ngStartRuntime.sh ";
    public final static String DEFAULT_NGPATH = "ngsub";
    protected String count = "1";
    protected String stderr = null;
    protected String stdout = null;
    protected String queue = null;
    protected String executable = DEFAULT_SCRIPT_LOCATION;
    protected String jobname = null;
    protected String executable_path;
    protected File tmp_executable;

    //===========================================================
    // Constructor
    //===========================================================

    /**
     * Creates a new instance of NGProcess
     */
    public NGProcess() {
        super();
        setCompositionType(GIVE_COMMAND_AS_PARAMETER);
        this.command_path = DEFAULT_NGPATH;
        this.hostname = null;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public void setStderr(String stderr) {
        this.stderr = stderr;
    }

    public void setStdout(String stdout) {
        this.stdout = stdout;
    }

    public void setJobname(String jobname) {
        this.jobname = jobname;
    }

    protected String internalBuildCommand() {
        buildExecutable();
        return buildNGSUBCommand() + " " + buildXRSLCommand();
    }

    protected void internalStartProcess(String xRslCommand)
        throws java.io.IOException {
        int j = new Integer(count).intValue();
        try {
            for (int i = 0; i < j; i++) {
                //here we simulate the deployment of // jobs on multiple procs
                //indeed at this point Ng does support // executions on the site
                // we have access. This should change with // RTEs
                super.internalStartProcess(xRslCommand);
            }
        } catch (IOException e) {
            removeExecutable();
            throw e;
        }
        removeExecutable();
    }

    public String getProcessId() {
        return "nordugrid_" + targetProcess.getProcessId();
    }

    public int getNodeNumber() {
        return (new Integer(count).intValue());
    }

    public UniversalProcess getFinalProcess() {
        checkStarted();
        return targetProcess.getFinalProcess();
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            ProActiveDescriptor pad = ProActive.getProactiveDescriptor(
                    "/0/user/team0/ProActiveNQueens/descriptor/nqueen.xml");
            pad.activateMappings();
        } catch (ProActiveException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void setExecutable(String exec) {
        int index = exec.lastIndexOf("/");
        this.executable = exec.substring(index + 1);
        this.executable_path = exec;
    }

    private String buildNGSUBCommand() {
        return command_path + " -c " + hostname;
    }

    private String buildXRSLCommand() {
        String xRSL_command = "'&(executable=" + executable + ")";
        if (jobname != null) {
            xRSL_command = xRSL_command + "(jobname=" + jobname + ")";
        }
        if (stdout != null) {
            xRSL_command = xRSL_command + "(stdout=" + stdout + ")";
        }

        if (stderr != null) {
            xRSL_command = xRSL_command + "(stderr=" + stderr + ")";
        }

        if (queue != null) {
            xRSL_command = xRSL_command + "(queue=" + queue + ")";
        }

        //following line should be uncommented in case parallel environment
        //        if (count != "1") {
        //            xRSL_command = xRSL_command + "(count=" + count + ")";
        //        }
        xRSL_command = xRSL_command + "'";
        return xRSL_command;
    }

    private void buildExecutable() {
        //first we build the temporary execuable, where we put the initial content
        try {
            tmp_executable = new File(executable_path.replaceAll(executable,
                        "tmp_" + executable));
            BufferedReader reader = new BufferedReader(new FileReader(
                        executable_path));

            BufferedWriter writer = new BufferedWriter(new FileWriter(
                        tmp_executable));
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                writer.write(line);
                writer.newLine();
            }
            reader.close();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Then in the real executable we append the java command
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(
                        executable_path, true));
            System.out.println(executable_path);
            //            writer.newLine();
            System.out.println("java " + targetProcess.getCommand());
            writer.write(targetProcess.getCommand());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void removeExecutable() {
        File exec = new File(executable_path);
        boolean  status =  exec.delete();
        boolean status1 = tmp_executable.renameTo(exec);
    }
}

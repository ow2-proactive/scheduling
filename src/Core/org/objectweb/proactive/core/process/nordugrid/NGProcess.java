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
package org.objectweb.proactive.core.process.nordugrid;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.process.AbstractExternalProcessDecorator;
import org.objectweb.proactive.core.process.UniversalProcess;
import org.objectweb.proactive.core.process.filetransfer.FileTransferDefinition;
import org.objectweb.proactive.core.process.filetransfer.FileTransferDefinition.FileDescription;
import org.objectweb.proactive.core.process.filetransfer.FileTransferWorkShop;
import org.objectweb.proactive.core.util.ProActiveRandom;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * NorduGrid Process implementation.
 * This implementation works only for ProActive deployment, and not to submit single commands
 * @author  ProActive Team
 * @version 1.0,  2005/09/20
 * @since   ProActive 2.3
 */
public class NGProcess extends AbstractExternalProcessDecorator {
    private static final String DEFAULT_SCRIPT_LOCATION = System.getProperty(
            "user.home") + File.separator + "ProActive" + File.separator +
        "scripts" + File.separator + "unix" + File.separator + "cluster" +
        File.separator + "ngStartRuntime.sh ";
    public final static String DEFAULT_NGPATH = "ngsub";
    protected String count = "1";
    protected String stderr = null;
    protected String stdout = null;
    protected String queue = null;
    protected String jobname = null;
    protected String executable_path = DEFAULT_SCRIPT_LOCATION;
    protected String tmp_executable;
    protected String DEFAULT_INPUT_FILE = "(inputfiles = ";
    protected String inputFiles;
    protected ArrayList<String> command_buffer;

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
        FILE_TRANSFER_DEFAULT_PROTOCOL = "nordugrid";
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

    @Override
    protected String internalBuildCommand() {
        buildExecutable();
        command_buffer = new ArrayList<String>();
        return buildNGSUBCommand() + " " + buildXRSLCommand();
    }

    @Override
    protected void internalStartProcess(String xRslCommand)
        throws java.io.IOException {
        String[] ng_command = command_buffer.toArray(new String[] {  });
        int j = new Integer(count).intValue();

        for (int i = 0; i < j; i++) {
            //here we simulate the deployment of // jobs on multiple procs
            //indeed at this point Ng does support // executions on the site
            // we have access. This should change with // RTEs
            try {
                externalProcess = Runtime.getRuntime().exec(ng_command);
                java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(
                            externalProcess.getInputStream()));
                java.io.BufferedReader err = new java.io.BufferedReader(new java.io.InputStreamReader(
                            externalProcess.getErrorStream()));
                java.io.BufferedWriter out = new java.io.BufferedWriter(new java.io.OutputStreamWriter(
                            externalProcess.getOutputStream()));
                handleProcess(in, out, err);
            } catch (java.io.IOException e) {
                isFinished = true;
                //throw e;
                e.printStackTrace();
            }
        }
    }

    @Override
    protected boolean internalFileTransferDefaultProtocol() {
        FileTransferWorkShop fts = getFileTransferWorkShopDeploy();
        FileTransferDefinition[] ftDefinitions = fts.getAllFileTransferDefinitions();
        Logger fileTransferLogger = ProActiveLogger.getLogger(Loggers.DEPLOYMENT_FILETRANSFER);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ftDefinitions.length; i++) {
            //Files and Dirs
            FileDescription[] files = ftDefinitions[i].getAll();
            for (int j = 0; j < files.length; j++) {
                String fullfilename = fts.getAbsoluteSrcPath(files[j]);

                //Skipping non existant local filenames, keep remote files
                if (!FileTransferWorkShop.isLocalReadable(fullfilename) &&
                        !FileTransferWorkShop.isRemote(fullfilename)) {
                    logger.info(fullfilename);
                    if (fileTransferLogger.isDebugEnabled()) {
                        fileTransferLogger.debug(
                            "Skiping. Unreadable for FileTransfer:" +
                            fullfilename);
                    }
                    continue;
                }

                sb.append("(\"" + files[j].getDestName() + "\" \"" +
                    fullfilename + "\")");
            }
        }
        if (fileTransferLogger.isDebugEnabled()) {
            fileTransferLogger.debug("NorduGrid FileTransfer:" + sb.toString());
        }
        if (sb.length() > 0) {
            inputFiles = sb.toString();
        }

        //Because FileTransfer will be submited with the process,
        //we return success so no other protocols will be tried.
        return true;
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

    public void setExecutable(String exec) {
        this.executable_path = exec;
    }

    private String buildNGSUBCommand() {
        command_buffer.add(command_path);
        command_buffer.add("-c");
        command_buffer.add(hostname);
        return command_path + " -c " + hostname;
    }

    private String buildXRSLCommand() {
        //It is always the temporary file that is sent to NG server
        //The original executable is kept unchanged
        String xRSL_command = "&(executable=" + tmp_executable + ")";
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
        if (inputFiles != null) {
            xRSL_command = xRSL_command + DEFAULT_INPUT_FILE + inputFiles +
                ")";
        }

        //following line should be uncommented in case parallel environment
        //        if (count != "1") {
        //            xRSL_command = xRSL_command + "(count=" + count + ")";
        //        }
        if (logger.isDebugEnabled()) {
            logger.debug(xRSL_command);
        }
        command_buffer.add(xRSL_command);
        return xRSL_command;
    }

    private void buildExecutable() {
        File tmp_executableFile;
        int index = executable_path.lastIndexOf("/");
        String executable = executable_path.substring(index + 1);
        this.tmp_executable = "tmp_" + executable +
            ProActiveRandom.nextPosInt();
        String tmp_executable_path = executable_path.replace(executable,
                tmp_executable);

        //first we build the temporary execuable, that will be sent
        try {
            tmp_executableFile = new File(tmp_executable_path);
            if (tmp_executableFile.exists()) {
                tmp_executableFile.delete();
            }
            tmp_executableFile.deleteOnExit();
            BufferedReader reader = new BufferedReader(new FileReader(
                        executable_path));

            BufferedWriter writer = new BufferedWriter(new FileWriter(
                        tmp_executableFile));
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                writer.write(line);
                writer.newLine();
            }
            reader.close();
            //we append in the tmp file the java command
            writer.write(targetProcess.getCommand());
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!tmp_executable_path.startsWith("file://")) {
            //check if it follows NG syntax: the local location should start
            // with file:///, so if not present, the local file must start with /: absolute path
            //First implementation !
            tmp_executable_path = "file://" + tmp_executable_path;
        }

        //then we append the executable in the inputfiles, to be transfered on the server
        if (inputFiles != null) {
            inputFiles = inputFiles + "(\"" + tmp_executable + "\" \"" +
                tmp_executable_path + "\")";
        } else {
            inputFiles = "(\"" + tmp_executable + "\" \"" +
                tmp_executable_path + "\")";
        }
    }
}

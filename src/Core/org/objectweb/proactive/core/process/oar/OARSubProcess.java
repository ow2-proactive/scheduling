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
package org.objectweb.proactive.core.process.oar;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.objectweb.proactive.core.process.AbstractExternalProcessDecorator;
import org.objectweb.proactive.core.process.ExternalProcess;
import org.objectweb.proactive.core.process.JVMProcessImpl;
import org.objectweb.proactive.core.process.MessageSink;
import org.objectweb.proactive.core.process.UniversalProcess;


/**
 * <p>
 * The OARSubProcess class is able to start any class, of the ProActive library,
 * on a cluster managed by OAR protocol. An istance of this class can be coupled for instance with
 * RLoginProcess or SSHProcess classes in order to log into the cluster's front end with rlogin or
 * ssh and then to run a job with OARSubProcess.
 * </p>
 * <p>
 * For instance:
 * </p><pre>
 * ..............
 * OARSubProcess oar = new OARSubProcess(new SimpleExternalProcess("ls -lsa"));
 * SSHProcess ssh = new SSHProcess(oar, false);
 * ssh.setHostname("cluster_front_end_name");
 * ssh.startProcess();
 * ...............
 * </pre>
 * Anyway it is strongly advised to use XML Deployment files to run such processes
 * @author The ProActive Team
 * @version 1.0,  2004/09/20
 * @since   ProActive 2.0.1
 */
public class OARSubProcess extends AbstractExternalProcessDecorator {
    public final static String DEFAULT_OARSUBPATH = "oarsub";
    private static final String DEFAULT_SCRIPT_LOCATION = System.getProperty("user.home") + File.separator +
        "ProActive" + File.separator + "scripts" + File.separator + "unix" + File.separator + "cluster" +
        File.separator + "oarStartRuntime.sh ";
    protected static final String DEFAULT_HOSTS_NUMBER = "1";
    protected String hostNumber = DEFAULT_HOSTS_NUMBER;
    protected String weight = "2";
    protected String scriptLocation = DEFAULT_SCRIPT_LOCATION;
    protected String interactive = "false";
    protected int jobID;
    protected String queueName;
    protected String accessProtocol = "rsh";
    protected String resources;

    //protected String properties;
    public OARSubProcess() {
        super();
        setCompositionType(GIVE_COMMAND_AS_PARAMETER);
        this.hostname = null;
        this.command_path = DEFAULT_OARSUBPATH;
    }

    public OARSubProcess(ExternalProcess targetProcess) {
        super(targetProcess);
        this.hostname = null;
        this.command_path = DEFAULT_OARSUBPATH;
    }

    //  ----------------------------------------------------------------------------------------
    //-----------------------Extends AbstractExternalProcessDecorator-------------------------
    //  ----------------------------------------------------------------------------------------
    @Override
    public void setOutputMessageSink(MessageSink outputMessageSink) {
        if (outputMessageSink == null) {
            super.setOutputMessageSink(new SimpleMessageSink());
        } else {
            super.setOutputMessageSink(outputMessageSink);
        }
    }

    /**
     * @see org.objectweb.proactive.core.process.UniversalProcess#getProcessId()
     */
    public String getProcessId() {
        return "oar_" + targetProcess.getProcessId();
    }

    /**
     * @see org.objectweb.proactive.core.process.UniversalProcess#getNodeNumber()
     */
    public int getNodeNumber() {
        try {
            int n = hostNumber.indexOf('/');
            if (n == -1) {
                n = hostNumber.length();
            }
            return (new Integer(hostNumber.substring(0, n)).intValue() * new Integer(weight).intValue());
        } catch (NumberFormatException e) {
            if (!hostNumber.matches("all")) {
                logger.warn(hostname + " is not an integer", e);
            }
            return UniversalProcess.UNKNOWN_NODE_NUMBER;
        }
    }

    /**
     * @see org.objectweb.proactive.core.process.UniversalProcess#getFinalProcess()
     */
    public UniversalProcess getFinalProcess() {
        checkStarted();
        return targetProcess.getFinalProcess();
    }

    /**
     * Sets the location of the script on the remote host
     * @param location
     */
    public void setScriptLocation(String location) {
        checkStarted();
        //     if (location != null) {
        this.scriptLocation = location;
        //    }
    }

    /**
     * Sets the protocol to access booked nodes.
     * Two possibilities, rsh, ssh. Default is rsh.
     * @param accessProtocol
     */
    public void setAccessProtocol(String accessProtocol) {
        this.accessProtocol = accessProtocol;
    }

    /**
     * Set the resource option in the OAR command.
     * Represents the -l option of OAR
     * @param res resources to set
     */
    public void setResources(String res) {
        checkStarted();
        if (res != null) {
            this.resources = res;
            parseRes(res);
        }
    }

    //    /**
    //     * Sets the value of the hostList parameter with the given value
    //     * Not yet included in the oar command
    //     * @param hostList
    //     */
    //    public void setProperties(String props) {
    //        checkStarted();
    //        if (props != null) {
    //            this.properties = checkProperties(props);
    //        }
    //    }

    /**
     * Allows to launch this OARSubProcess with -I (interactive option)
     * Not yet included in the oar command
     * @param interactive true for -I option false otherwise
     */
    public void setInteractive(String interactive) {
        this.interactive = interactive;
    }

    /**
     * Sets the value of the queue where the job will be launched. The default is 'normal'
     * Not yet included in the oar command
     * @param queueName
     */
    public void setQueueName(String queueName) {
        checkStarted();
        if (queueName == null) {
            throw new NullPointerException();
        }
        this.queueName = queueName;
    }

    protected String parseHostname(String message) {
        //To be modified for OAR, does not work with the present code
        String result = new String();
        if (logger.isDebugEnabled()) {
            logger.debug("parseHostname() analyzing " + message);
        }
        java.util.StringTokenizer st = new java.util.StringTokenizer(message);
        if (st.countTokens() < 2) {
            return null; //at least two tokens
        }
        if (!":".equals(st.nextToken())) {
            return null; //should start with :
        }

        while (st.hasMoreTokens()) {
            result += (st.nextToken());
            result += " ";
        }
        return result;
    }

    //    protected String internalBuildCommand() {
    //        return buildEnvironmentCommand(); // + buildPSubCommand();
    //    }
    @Override
    protected void internalStartProcess(String commandToExecute) throws java.io.IOException {
        ArrayList<String> al = new ArrayList<String>();

        //we divide the command into tokens
        //it's basically 3 blocks, the script path, the option and the rest
        Pattern p = Pattern.compile("(.*) .*(-c).*'(.*)'");
        Matcher m = p.matcher(command);
        if (!m.matches()) {
            System.err.println("Could not match command ");
            System.err.println(commandToExecute);
        }
        for (int i = 1; i <= m.groupCount(); i++) {
            //            System.out.println(m.group(i));
            al.add(m.group(i));
        }
        String[] command = al.toArray(new String[] {});

        try {
            externalProcess = Runtime.getRuntime().exec(command);
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

    /**
     * oarsub is not able to receive env variables or parameters for a script
     * we thus rely on the following trick, the command has the form
     * echo "real command" | qsub -I ...   oarStartRuntime.sh
     */
    @Override
    protected String internalBuildCommand() {
        StringBuilder oarsubCommand = new StringBuilder();
        oarsubCommand.append("/bin/sh -c  'echo for i in \\`cat \\$OAR_NODEFILE\\` \\; do " + accessProtocol +
            " \\$i  ");
        oarsubCommand.append(targetProcess.getCommand());
        oarsubCommand.append(" \\&  done  \\; wait > ");
        oarsubCommand.append(scriptLocation).append(" ; ");
        oarsubCommand.append(command_path);
        oarsubCommand.append(" ");
        if (resources != null) {
            oarsubCommand.append("-l " + resources).append(" ");
        }

        //To implement if needed
        //        if(properties != null){
        //            oarsubCommand.append("-p "+properties).append(" ");
        //        }
        oarsubCommand.append(scriptLocation).append(" '");
        if (logger.isDebugEnabled()) {
            logger.debug("oarsub command is " + oarsubCommand.toString());
        }
        return oarsubCommand.toString();
    }

    /**
     * @param res
     */
    private void parseRes(String res) {
        String[] resTab = res.split(",");
        for (int i = 0; i < resTab.length; i++) {
            if (!(resTab[i].indexOf("nodes") < 0)) {
                hostNumber = resTab[i].substring(resTab[i].indexOf("=") + 1, resTab[i].length());
            }
            if (!(resTab[i].indexOf("weight") < 0)) {
                weight = resTab[i].substring(resTab[i].indexOf("=") + 1, resTab[i].length());
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("Testing OARSubProcess");
        JVMProcessImpl p = new JVMProcessImpl();
        OARSubProcess oar = new OARSubProcess(p);
        oar.setResources("nodes=2");
        System.out.println(oar.buildCommand());
    }
}

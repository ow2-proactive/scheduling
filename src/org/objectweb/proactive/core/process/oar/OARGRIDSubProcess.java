/*
 * Created on May 31, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.objectweb.proactive.core.process.oar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.objectweb.proactive.core.process.AbstractExternalProcessDecorator;
import org.objectweb.proactive.core.process.ExternalProcess;
import org.objectweb.proactive.core.process.JVMProcessImpl;
import org.objectweb.proactive.core.process.UniversalProcess;


/**
 * <p>
 * The OARGRIDSubProcess class is able to start any class, of the ProActive library,
 * on a cluster managed by OARGRID protocol.
 * It is strongly advised to use XML Deployment files to run such processes
 * @author  ProActive Team
 * @version 1.0,  2005/09/20
 * @since   ProActive 3.0
 */
public class OARGRIDSubProcess extends AbstractExternalProcessDecorator {
    private static final String FILE_SEPARATOR = System.getProperty(
            "file.separator");
    public final static String DEFAULT_OARGRIDSUBPATH = "/usr/local/bin/oargridsub";
    private static final String DEFAULT_SCRIPT_LOCATION = System.getProperty(
            "user.home") + FILE_SEPARATOR + "ProActive" + FILE_SEPARATOR +
        "scripts" + FILE_SEPARATOR + "unix" + FILE_SEPARATOR + "cluster" +
        FILE_SEPARATOR + "oarGRIDStartRuntime.sh ";
    protected static final int DEFAULT_HOSTS_NUMBER = 1;
    protected static final int DEFAULT_WEIGHT = 1;
    protected static final String DEFAULT_WALLTIME = "01:00:00"; //1 hour
    protected OarSite[] oarsite = null; //OAR site array
    protected String scriptLocation = DEFAULT_SCRIPT_LOCATION;
    protected int jobID;
    protected String queueName = "default";
    protected String accessProtocol = "ssh";
    protected String resources = "";
    protected String walltime = DEFAULT_WALLTIME;

    public OARGRIDSubProcess() {
        super();
        setCompositionType(GIVE_COMMAND_AS_PARAMETER);
        this.hostname = null;
        this.command_path = DEFAULT_OARGRIDSUBPATH;
    }

    public OARGRIDSubProcess(ExternalProcess targetProcess) {
        super(targetProcess);
        this.hostname = null;
        this.command_path = DEFAULT_OARGRIDSUBPATH;
    }

    /**
     * @see org.objectweb.proactive.core.process.UniversalProcess#getProcessId()
     */
    public String getProcessId() {
        return "oargrid_" + targetProcess.getProcessId();
    }

    /**
     * @see org.objectweb.proactive.core.process.UniversalProcess#getNodeNumber()
     */
    public int getNodeNumber() {
        int num = 0;

        //if(oarsite==null) return 0;
        for (int i = 0; i < oarsite.length; i++) {
            if (oarsite[i].getNodes() == UniversalProcess.UNKNOWN_NODE_NUMBER) {
                return UniversalProcess.UNKNOWN_NODE_NUMBER;
            }
            num += (oarsite[i].getNodes() * oarsite[i].getWeight());
        }
        return num;
    }

    /**
     * @see org.objectweb.proactive.core.process.UniversalProcess#getFinalProcess()
     */
    public UniversalProcess getFinalProcess() {
        checkStarted();
        return targetProcess.getFinalProcess();
    }

    public String getWallTime() {
        return walltime;
    }

    /**
     * Set the script location on the remote host *
     *
     * @param location
     */
    public void setScriptLocation(String location) {
        checkStarted();
        //     if (location != null) {
        this.scriptLocation = location;
        //    }
    }

    /**
     * Sets the protocol to access booked nodes. Two possibilities, rsh, ssh.
     * Default is ssh.
     *
     * @param accessProtocol
     */
    public void setAccessProtocol(String accessProtocol) {
        this.accessProtocol = accessProtocol;
    }

    /**
     * Set the resource option in the OARGRID command. Represents the DESC
     * option of OARGRID
     *
     * @param resources
     *            (cluster1:nodes=2,cluster2:nodes=3:weight=2)
     */
    public void setResources(String resources) {
        checkStarted();
        if (resources != null) {
            ArrayList al = new ArrayList();
            this.resources = resources;
            String[] resTab = resources.split(",");
            for (int i = 0; i < resTab.length; i++)
                al.add(new OarSite(resTab[i]));

            this.oarsite = (OarSite[]) al.toArray((new OarSite[] {  }));
        }
    }

    /**
     * Sets the value of the queue where the job will be launched. The default
     * is 'default'
     *
     * @param queueName
     */
    public void setQueueName(String queueName) {
        checkStarted();
        if (queueName == null) {
            throw new NullPointerException();
        }
        this.queueName = queueName;
    }

    public void setWallTime(String walltime) {
        checkStarted();
        if (walltime == null) {
            throw new NullPointerException();
        }
        this.walltime = walltime;
    }

    protected void internalStartProcess(String commandToExecute)
        throws java.io.IOException {
        ArrayList al = new ArrayList();

        //we divide the command into tokens
        //it's basically 3 blocks, the script path, the option and the rest
        Pattern p = Pattern.compile("(.*) .*(-c).*'(.*)'");
        Matcher m = p.matcher(command);
        if (!m.matches()) {
            System.err.println("Could not match command ");
            System.err.println(command);
        }
        for (int i = 1; i <= m.groupCount(); i++) {
            //            System.out.println(m.group(i));
            al.add(m.group(i));
        }
        String[] command = (String[]) al.toArray(new String[] {  });

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
     * oargridsub is not able to receive env variables or parameters for a
     * script we thus rely on the following trick, the command has the form echo
     * "real command" | qsub -I ... oarStartRuntime.sh
     */
    protected String internalBuildCommand() {
        StringBuffer oarsubCommand = new StringBuffer();
        oarsubCommand.append(
            "/bin/sh -c  'echo for i in \\`cat \\$OAR_NODEFILE\\` \\; do " +
            accessProtocol + " \\$i  ");

        oarsubCommand.append(targetProcess.getCommand());
        oarsubCommand.append(" \\&  done  \\; wait > ");
        oarsubCommand.append(scriptLocation).append(" ; ");

        //scp oarStartRuntuime.sh to selected clusters frontend
        for (int i = 0; i < oarsite.length; i++)
            oarsubCommand.append(getScpRunTimeStr(oarsite[i]));

        oarsubCommand.append(command_path);
        oarsubCommand.append(" -q ");
        oarsubCommand.append(this.queueName);
        oarsubCommand.append(" -w ");
        oarsubCommand.append(this.walltime);
        oarsubCommand.append(" -p ");
        oarsubCommand.append(this.scriptLocation);

        if (resources != null) {
            oarsubCommand.append(" ").append(resources).append(" ");
        }
        oarsubCommand.append("'");

        if (logger.isDebugEnabled()) {
            logger.debug("oarsub command is " + oarsubCommand.toString());
        }
        System.err.println(oarsubCommand);
        return oarsubCommand.toString();
    }

    /**
     * Internal generation of string to copy oarGRIDStartRuntime.sh
     *
     * @param oarsite
     *            OarSite object representing a cluster site, generated when
     *            setting the resource parameter
     * @return String that copies (scp) the RunTime file to oar clusters
     *         frontend
     */
    private String getScpRunTimeStr(OarSite oarsite) {
        if (oarsite == null) {
            return "";
        }
        if ((oarsite.getClusterFrontEndName() == null) ||
                (oarsite.getClusterFrontEndName() == "")) {
            return "";
        }

        StringBuffer str = new StringBuffer();

        str.append("scp -p ");
        str.append(this.scriptLocation);
        str.append(" ");
        str.append(oarsite.getClusterFrontEndName());
        str.append(":" + this.scriptLocation + " ; ");
        return str.toString();
    }

    public class OarSite implements Serializable {
        private String clusterName;
        private String clusterFrontEndName;
        private int nodes;
        private int weight;

        /**
         * OarSite constructor
         *
         * @param resource
         *            (cluster2:nodes=3:weight=2)
         */
        public OarSite(String resource) {
            setResource(resource);
            //Trying to guess oar site front end
            //TODO unharcode this
            if (clusterName.equalsIgnoreCase("idpot")) {
                //clusterFrontEndName = "oar.grenoble.grid5000.fr";
                clusterFrontEndName = "caddo.imag.fr";
            } else if (clusterName.equalsIgnoreCase("sophia")) {
                clusterFrontEndName = "oar.sophia.grid5000.fr";
            } else if (clusterName.equalsIgnoreCase("gdx")) {
                //clusterFrontEndName = "oar.orsay.grid5000.fr";
                clusterFrontEndName = "devgdx002.orsay.grid5000.fr";
            } else if (clusterName.equalsIgnoreCase("lyon")) {
                clusterFrontEndName = "oar.lyon.grid5000.fr";
            } else if (clusterName.equalsIgnoreCase("toulouse")) {
                clusterFrontEndName = "oar.toulouse.grid5000.fr";
            } else if (clusterName.equalsIgnoreCase("parasol")) {
                clusterFrontEndName = "oar.rennes.grid5000.fr";
            } else if (clusterName.equalsIgnoreCase("paraci")) {
                //clusterFrontEndName = "paraci01.irisa.fr";
                clusterFrontEndName = "dev-xeon.rennes.grid5000.fr";
            } else if (clusterName.equalsIgnoreCase("tartopom")) {
                //clusterFrontEndName = "tartopom01.irisa.fr";
                clusterFrontEndName = "dev-powerpc.rennes.grid5000.fr";
            } else if (clusterName.equalsIgnoreCase("icluster2")) {
                clusterFrontEndName = "ita101.imag.fr";
            }
            //ToDo, handle Error case
            else {
                System.out.println(
                    "clusterFrontEndName not found for cluster: " +
                    clusterName);
                if (logger.isDebugEnabled()) {
                    logger.debug("clusterFrontEndName not found for cluster: " +
                        clusterName);
                }
            }
        }

        /**
         * OarSite constructor
         *
         * @param resource
         *            (cluster2:nodes=3:weight=2)
         * @param clusterFrontEndName
         *            oar.site.grid5000.fr
         */
        public OarSite(String resource, String clusterFrontEndName) {
            this.clusterFrontEndName = clusterFrontEndName;
            setResource(resource);
        }

        /**
         * Sets de resources on this object
         *
         * @param resource
         *            (cluster2:nodes=3:weight=2)
         */
        public void setResource(String resource) {
            this.clusterName = "";
            this.clusterFrontEndName = "";
            nodes = OARGRIDSubProcess.DEFAULT_HOSTS_NUMBER;
            weight = OARGRIDSubProcess.DEFAULT_WEIGHT;

            String[] resTab = resource.split(":");
            for (int i = 0; i < resTab.length; i++) {
                //System.out.println(resTab[i]);
                if (resTab[i].indexOf("=") < 0) { //Clustername has no "="
                    clusterName = resTab[i];
                } else if (resTab[i].indexOf("nodes=") >= 0) {
                    String count = resTab[i].substring(resTab[i].indexOf("=") +
                            1);
                    if (count.equals("all")) {
                        nodes = UniversalProcess.UNKNOWN_NODE_NUMBER;
                    } else {
                        nodes = Integer.parseInt(count);
                    }
                } else if (resTab[i].indexOf("weight=") >= 0) {
                    weight = Integer.parseInt(resTab[i].substring(resTab[i].indexOf(
                                    "=") + 1));
                }
            }
        }

        public String getResource() {
            return this.clusterName + ":nodes=" + this.nodes + ":weight=" +
            this.weight;
        }

        public String getClusterFrontEndName() {
            return clusterFrontEndName;
        }

        public void setClusterFrontEndName(String clusterFrontEndName) {
            this.clusterFrontEndName = clusterFrontEndName;
        }

        public String getClusterName() {
            return clusterName;
        }

        public void setClusterName(String clusterName) {
            this.clusterName = clusterName;
        }

        public int getNodes() {
            return nodes;
        }

        public void setNodes(int nodes) {
            this.nodes = nodes;
        }

        public int getWeight() {
            return weight;
        }

        public void setWeight(int weight) {
            this.weight = weight;
        }
    }

    public static void main(String[] args) {
        System.out.println("Testing OARGRIDSubProcess");
        JVMProcessImpl p = new JVMProcessImpl();
        OARGRIDSubProcess oargrid = new OARGRIDSubProcess(p);
        oargrid.setResources("sophia:nodes=2:weight=3,idpot:nodes=10");
        System.out.println(oargrid.buildCommand());
        System.out.println(oargrid.getCommand());
    }
}

package org.objectweb.proactive.core.process.rsh.maprsh;

import org.objectweb.proactive.core.process.AbstractExternalProcessDecorator;
import org.objectweb.proactive.core.process.ExternalProcess;
import org.objectweb.proactive.core.process.JVMProcess;
import org.objectweb.proactive.core.process.JVMProcessImpl;
import org.objectweb.proactive.core.process.SimpleExternalProcess;


/**
 * @author rquilici
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class MapRshProcess extends AbstractExternalProcessDecorator {
    private static final String FILE_SEPARATOR = System.getProperty(
            "file.separator");
    private static final String DEFAULT_SCRIPT_LOCATION = System.getProperty(
            "user.home") + FILE_SEPARATOR + "ProActive" + FILE_SEPARATOR +
        "scripts" + FILE_SEPARATOR + "unix" + FILE_SEPARATOR +
        "gridexperiment" + FILE_SEPARATOR + "oasis-exp";
    protected String scriptLocation = DEFAULT_SCRIPT_LOCATION;
    private String parallelize = null;

    //private String logFile = System.getProperty("user.home")+System.getProperty("file.separator")+"oasisgridlog.txt";
    public MapRshProcess() {
        super();
        setCompositionType(GIVE_COMMAND_AS_PARAMETER);
    }

    public MapRshProcess(ExternalProcess targetProcess) {
        super(targetProcess);
        setCompositionType(GIVE_COMMAND_AS_PARAMETER);
    }

    public void setParallelization(String parallelize) {
        this.parallelize = parallelize;
    }

    public String getParallelization() {
        return this.parallelize;
    }

    /**
     * Method setScriptLocation.
     * @param string
     */
    public void setScriptLocation(String scriptLocation) {
        this.scriptLocation = scriptLocation;
    }

    public String getScriptLocation() {
        return scriptLocation;
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //
    protected String internalBuildCommand() {
        return buildMapRshCommand() + buildEnvironmentCommand();
    }

    protected String buildMapRshCommand() {
        StringBuffer command = new StringBuffer();
        try {
            java.io.File script = new java.io.File(scriptLocation);
            byte[] b = getBytesFromInputStream(new java.io.FileInputStream(
                        script));
            String scriptText = new String(b);
            scriptText = removeJavaCommand(scriptText);
            //System.out.println(scriptText);
            scriptText = appendJavaCommand(scriptText);
            if (logger.isDebugEnabled()) {
                logger.debug(scriptText);
            }
            b = scriptText.getBytes();
            // script.delete();
            java.io.OutputStream out = new java.io.BufferedOutputStream(new java.io.FileOutputStream(
                        script));
            out.write(b, 0, b.length);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        command.append("/usr/local/bin/maprsh ");
        if (parallelize != null) {
            command.append(parallelize + " ");
        }
        command.append(scriptLocation + " " + hostname);
        if (logger.isDebugEnabled()) {
            logger.debug(command.toString());
        }
        return command.toString();
    }

    /**
     * Method appendJavaCommand.
     * @param scriptText
     * @return String
     */
    private String appendJavaCommand(String scriptText) {
        StringBuffer newScriptText = new StringBuffer(scriptText.length());
        String targetCommand = targetProcess.getCommand();

        newScriptText.append(scriptText);
        newScriptText.append("\ntime " + targetCommand + " ) & \n");
        return newScriptText.toString();
    }

    /**
     * Method removeJavaCommand.
     * @param scriptText
     * @return String
     */
    private String removeJavaCommand(String scriptText) {
        int marker = scriptText.lastIndexOf("}");
        String newScriptText = scriptText.substring(0, marker + 1);

        //System.out.println(newScriptText);
        return newScriptText;
    }

    public static void main(String[] args) {
        try {
            JVMProcess process = new JVMProcessImpl(new StandardOutputMessageLogger());
            process.setParameters("///toto");

            //ExternalProcess process = new SimpleExternalProcess("ls -la");
            MapRshProcess maprsh = new MapRshProcess(process);
            maprsh.setHostname("waha owenii");
            maprsh.startProcess();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
    * Returns an array of bytes containing the bytecodes for
    * the class represented by the InputStream
    * @param in the inputstream of the class file
    * @return the bytecodes for the class
    * @exception java.io.IOException if the class cannot be read
    */
    private static byte[] getBytesFromInputStream(java.io.InputStream in)
        throws java.io.IOException {
        java.io.DataInputStream din = new java.io.DataInputStream(in);
        byte[] bytecodes = new byte[in.available()];
        try {
            din.readFully(bytecodes);
        } finally {
            if (din != null) {
                din.close();
            }
        }
        return bytecodes;
    }
}

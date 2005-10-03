/*
 * Created on May 30, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

package org.objectweb.proactive.core.process.glite;

import javax.naming.directory.InvalidAttributeValueException;
import org.glite.wms.jdlj.*; // /lib/glite/glite-wms-jdlj.jar
import org.objectweb.proactive.core.process.AbstractExternalProcessDecorator;
import org.objectweb.proactive.core.process.ExternalProcess;
import org.objectweb.proactive.core.process.JVMProcess;
import org.objectweb.proactive.core.process.UniversalProcess;


/**
 * @author  marc ozonne
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class GLiteProcess extends AbstractExternalProcessDecorator {
    /**
	 * Firsts parameters
	 */
	private static final long serialVersionUID = 1L;
	private static final String FILE_SEPARATOR = System.getProperty(
            "file.separator");
    protected static final String DEFAULT_PROCESSOR_NUMBER = "1";
    protected static final String DEFAULT_COMMAND_PATH = "glite-job-submit";
    protected static final String DEFAULT_FILE_LOCATION = System.getProperty("user.home") 
    														+ FILE_SEPARATOR + "public" 
    														+ FILE_SEPARATOR + "JDL"; 		
    
    protected static final String DEFAULT_STDOUPUT = System.getProperty("user.home") 
    													+ FILE_SEPARATOR + "out.log";
    
    protected static final String DEFAULT_CONFIG_FILE = System.getProperty("user.home") 
    													+ FILE_SEPARATOR + "public" 
    													+ FILE_SEPARATOR + "JDL" 
    													+ FILE_SEPARATOR + "vo.conf";
    protected int jobID;
    protected String hostList;
    protected String processor = DEFAULT_PROCESSOR_NUMBER;
    protected String command_path = DEFAULT_COMMAND_PATH;
    protected String interactive = "false";
    protected String filePath = DEFAULT_FILE_LOCATION;
    protected String stdOutput = DEFAULT_STDOUPUT;
    protected String fileName = "job.jdl";
    protected String configFile = DEFAULT_CONFIG_FILE;
    protected String remoteFilePath = null;
    protected boolean confFileOption = false, jdlRemote = false;
    protected String netServer, logBook;
    // WARNING : variable appartenant a toutes les instances de la classe GLiteProcess
   public static GLiteJobAd jad; 
    
    
	/**
     * Create a new GLiteProcess
     * Used with XML Descriptors
     */
    public GLiteProcess() {
        super();
        setCompositionType(GIVE_COMMAND_AS_PARAMETER);
        this.hostname = null;
        command_path = DEFAULT_COMMAND_PATH;
        jad = new GLiteJobAd();
    }

    /**
     * Create a new GLiteProcess
     * @param targetProcess The target process associated to this process. The target process
     * represents the process that will be launched with the glite-job-submit command
     */
    public GLiteProcess(ExternalProcess targetProcess) {
        super(targetProcess);
        this.hostname = null;
        jad = new GLiteJobAd();
    }

    
    /**
     * Create the jdl file with all the options specified in the descriptor 
     */
    public void buildJdlFile() {
    	
    	StringBuffer gLiteCommand = new StringBuffer();	
    	String args;
    	gLiteCommand.append(command_path); 
    	String initial_args = ((JVMProcess) getTargetProcess()).getCommand();
    	String java_cmd = System.getProperty("java.home") + "/bin/java";
    	//if(jdlRemote) {
    		//args = initial_args.substring(initial_args.indexOf("-Djava"));
    	//}else { 
    		args = initial_args.substring(initial_args.indexOf(java_cmd) + java_cmd.length());
    	//}
    	
    	args = checkSyntax(args);
  
    	try {
    		if(jad.hasAttribute(Jdl.ARGUMENTS))
    			jad.delAttribute(Jdl.ARGUMENTS);
    		jad.setAttribute(Jdl.ARGUMENTS, args);			
    		jad.toFile(filePath + "/" + fileName);
    		
    		
    		//examples of requirements
    		//jad.setAttributeExpr(Jdl.REQUIREMENTS,"other.GlueCEUniqueID ==\"pps-ce.egee.cesga.es:2119/blah-pbs-picard\"");
    		//jad.setAttributeExpr(Jdl.REQUIREMENTS, "!(RegExp(\"*lxb2039*\",other.GlueCEUniqueID))");
    		
    	} catch (IllegalArgumentException e) {
    		e.printStackTrace();
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    protected String internalBuildCommand() {	
		return  buildGLiteCommand();
    }

    /**
     * Add java arguments to the jdl file.
     * Set the name of jdl file.
     * Mandatory attributes : Requirements, rank'
     *
     * @throws llegalArgumentException, Exception
     * @return Empty string. Command line is not necessary there.
     */
    protected String buildGLiteCommand() {
    	String path = filePath;
    	buildJdlFile();
    	
    	if(jdlRemote)
    		path = remoteFilePath;
    	
    	if(!confFileOption)
		return DEFAULT_COMMAND_PATH + " " +  path + FILE_SEPARATOR + fileName ;
   
    	return DEFAULT_COMMAND_PATH + " --config-vo " + configFile + " " +  path + FILE_SEPARATOR + fileName ;
	
    }

  
	/**
	 * Check is java arguments are well formatted. 
     * @param java arguments
     * @return java argments well formatted
     */
    private String checkSyntax(String args) {
        String formatted_args = "";
        String[] splitted_args = args.split("\\s");
        for (int i = 0; i < splitted_args.length; i++) {
            if (!(splitted_args[i].indexOf("=") < 0)) {
                splitted_args[i] = "\"" + splitted_args[i] + "\"";
            }
            formatted_args = formatted_args + " " + splitted_args[i];
        }
        return formatted_args;
    }
    
	
	/************************************************************************
	 *                              GETTERS AND SETTERS                     *
	 ************************************************************************/
	
    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.process.UniversalProcess#getProcessId()
     */
    public String getProcessId() {
        return "glite_" + targetProcess.getProcessId();
    }

	public int getNodeNumber() {
		return (new Integer(getProcessorNumber()).intValue());
	}

	/**
	 * Returns the number of processor requested for the job
	 * @return String
	 */
	public String getProcessorNumber() {
		return processor;
	}
	    
	public UniversalProcess getFinalProcess() {
		checkStarted();
        return targetProcess.getFinalProcess();
	}
	/**
	 * @return Returns the fileName.
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName The fileName to set.
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return Returns the filePath.
	 */
	public String getFilePath() {
		return filePath;
	}

	/**
	 * @param filePath The filePath to set.
	 */
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	/**
	 * @return Returns the command_path.
	 */
	public String getCommand_path() {
		return command_path;
	}

	/**
	 * @param command_path The command_path to set.
	 */
	public void setCommand_path(String command_path) {
		this.command_path = command_path;
	}

	/**
	 * @return Returns the jad.
	 */
	public GLiteJobAd getJad() {
		return jad;
	}


	/**
	 * @param attributes to add to the GliteJobAd object
	 * @throws InvalidAttributeValueException 
	 * @throws IllegalArgumentException 
	 */
	public void addAtt(String attrName, Ad attrValue) throws Exception {
		jad.addAttribute(attrName, attrValue);
	}
	
	/**
	 * @param attributes to add to the GliteJobAd object
	 * @throws InvalidAttributeValueException 
	 * @throws IllegalArgumentException 
	 */
	public void addAtt(String attrName, int attrValue) throws Exception {
		jad.addAttribute(attrName, attrValue);
	}
	
	/**
	 * @param attributes to add to the GliteJobAd object
	 * @throws InvalidAttributeValueException 
	 * @throws IllegalArgumentException 
	 */
	public void addAtt(String attrName, double attrValue) throws Exception {
		jad.addAttribute(attrName, attrValue);
	}
	
	/**
	 * @param attributes to add to the GliteJobAd object
	 * @throws InvalidAttributeValueException 
	 * @throws IllegalArgumentException 
	 */
	public void addAtt(String attrName, String attrValue) throws Exception {
		jad.addAttribute(attrName, attrValue);
	}
	
	/**
	 * @param attributes to add to the GliteJobAd object
	 * @throws InvalidAttributeValueException 
	 * @throws IllegalArgumentException 
	 */
	public void addAtt(String attrName, boolean attrValue) throws Exception {
		jad.addAttribute(attrName, attrValue);
	}
	/**
	 * @return Returns the netServer.
	 */
	public String getNetServer() {
		return netServer;
	}

	/**
	 * @param netServer The netServer to set.
	 */
	public void setNetServer(String netServer) {
		this.netServer = netServer;
	}

	/**
	 * @return Returns the configFile.
	 */
	public String getConfigFile() {
		return configFile;
	}

	/**
	 * @param configFile The configFile to set.
	 */
	public void setConfigFile(String configFile) {
		this.configFile = configFile;
	}

	public void setConfigFileOption(boolean b) {
		confFileOption = b;
		
	}

	/**
	 * @return Returns the jdlRemote.
	 */
	public boolean isJdlRemote() {
		return jdlRemote;
	}

	/**
	 * @param jdlRemote The jdlRemote to set.
	 */
	public void setJdlRemote(boolean jdlRemote) {
		this.jdlRemote = jdlRemote;
	}

	/**
	 * @return Returns the remoteFilePath.
	 */
	public String getRemoteFilePath() {
		return remoteFilePath;
	}

	/**
	 * @param remoteFilePath The remoteFilePath to set.
	 */
	public void setRemoteFilePath(String remoteFilePath) {
		this.remoteFilePath = remoteFilePath;
	}	
	
	
	/******************************************************************************************
	 *                                END OF GETTERS AND SETTERS                              *      
	 ******************************************************************************************/
}
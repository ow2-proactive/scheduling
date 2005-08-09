package org.objectweb.proactive.core.process.unicore;

/*
 * Created on Jun 6, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

import org.objectweb.proactive.core.process.AbstractExternalProcessDecorator;
import org.objectweb.proactive.core.process.ExternalProcess;
import org.objectweb.proactive.core.process.UniversalProcess;


/**
 * @author ProActive Team
 * 
 * 
 */
public class UnicoreProcess extends AbstractExternalProcessDecorator{

	public UnicoreParameters uParam;
	
	public UnicoreProcess(){
		
		super();
		setCompositionType(GIVE_COMMAND_AS_PARAMETER);
		
		//Create an UnicoreParameters instance
		uParam = new UnicoreParameters(); 
	}
	
	public UnicoreProcess(ExternalProcess targetProcess){
		super(targetProcess);
		
		uParam= new UnicoreParameters();
	}
	
	protected void internalStartProcess(String commandToExecute)
	throws java.io.IOException {
		
		logger.debug(commandToExecute);
		
		/* Depending on the system property UnicoreProActiveClient
		 * can be forked or called directly.
		 */
		String forkclient=System.getProperty("proactive.unicore.forkclient");
		
		if(forkclient.equals("false")){
			logger.debug("Not Forking UnicoreProActiveClient");
			UnicoreProActiveClient uProClient;
			
			//Build a UnicoreProActive client with this parameters
			uProClient= new UnicoreProActiveClient(uParam);
			
			uProClient.build();
			uProClient.saveJob();
			uProClient.submitJob();
		}
		else{
			
			logger.debug("Forking UnicoreProActiveClient");
			
			try {
				externalProcess = Runtime.getRuntime().exec(command);
				java.io.BufferedReader in = new java.io.BufferedReader(
						new java.io.InputStreamReader(externalProcess
								.getInputStream()));
				java.io.BufferedReader err = new java.io.BufferedReader(
						new java.io.InputStreamReader(externalProcess
								.getErrorStream()));
				java.io.BufferedWriter out = new java.io.BufferedWriter(
						new java.io.OutputStreamWriter(externalProcess
								.getOutputStream()));
				handleProcess(in, out, err);
			} catch (java.io.IOException e) {
				isFinished = true;
				//throw e;
				e.printStackTrace();
			}
		}
	}
	
	protected String internalBuildCommand() {
		uParam.setScriptContent(targetProcess.getCommand());
		
		//return command for parent to execute
		return uParam.getCommandString();
	}

	public String getProcessId() {
		return "unicore_" + targetProcess.getProcessId();
	}
	
	public int getNodeNumber() {		
		return uParam.getVsiteNodes()*uParam.getVsiteProcessors();
	}

	public UniversalProcess getFinalProcess() {
		checkStarted();
		return targetProcess.getFinalProcess();
	}
}

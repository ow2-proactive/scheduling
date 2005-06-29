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
 * @author mleyton
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class UnicoreProcess extends AbstractExternalProcessDecorator{

	public UnicoreParameters uParam;
	
	//see internalBuildCommand(); for initialization
	private UnicoreProActiveClient uProClient; 
	
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
		
		//Debug
		//System.out.println(uParam);
		
		//commandToExecute is dismissed. Execution
		//is done directly from this process.
		uProClient.build();
		uProClient.saveJob();
		uProClient.submitJob();
	}
	
	protected String internalBuildCommand() {
		uParam.setScriptContent(targetProcess.getCommand());
		
		//Prompt the user for a keypassword if not
		//specified in the descriptor file
		if(uParam.getKeyPassword().length()<=0){
			UnicorePasswordGUI upGUI= new UnicorePasswordGUI();
			uParam.setKeyPassword(upGUI.getKeyPassword());
		}
		
		//Build a UnicoreProActive client with this parameters
		uProClient= new UnicoreProActiveClient(uParam);
	
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

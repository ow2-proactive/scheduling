package org.objectweb.proactive.ext.scilab;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;

/**
 * SciEngineInfo contains all methods to access at informations about a Scilab Engine
 * @author amangin
 *
 */
public class SciEngineInfo {
	private String idEngine;
	private SciEngineWorker sciEngine;
	private BooleanWrapper isActivate; //a future to test if the Scilab engine is activated
	
	public SciEngineInfo (String idEngine, SciEngineWorker sciEngine, BooleanWrapper isActivate){
		this.idEngine = idEngine;
		this.sciEngine = sciEngine;
		this.isActivate = isActivate;
	}
	
	public String getIdEngine() {
		return idEngine;
	}

	public SciEngineWorker getSciEngine() {
		return sciEngine;
	}
	
	public String getSciEngineUrl(){
		return ProActive.getActiveObjectNodeUrl(this.sciEngine);
	}

	public BooleanWrapper getIsActivate() {
		return isActivate;
	}

	public void setIsActivate(BooleanWrapper isActivate) {
		this.isActivate = isActivate;
	}
}

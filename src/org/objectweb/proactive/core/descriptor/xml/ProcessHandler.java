/* 
* ################################################################
* 
* ProActive: The Java(TM) library for Parallel, Distributed, 
*            Concurrent computing with Security and Mobility
* 
* Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
* Contact: proactive-support@inria.fr
* 
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or any later version.
*  
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
* 
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
* USA
*  
*  Initial developer(s):               The ProActive Team
*                        http://www.inria.fr/oasis/ProActive/contacts.html
*  Contributor(s): 
* 
* ################################################################
*/ 
package org.objectweb.proactive.core.descriptor.xml;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.process.ExternalProcessDecorator;
import org.objectweb.proactive.core.process.ExternalProcess;
import org.objectweb.proactive.core.process.globus.GlobusProcess;
import org.objectweb.proactive.core.process.lsf.LSFBSubProcess;
import org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator;
import org.objectweb.proactive.core.xml.handler.BasicUnmarshaller;
import org.objectweb.proactive.core.xml.handler.BasicUnmarshallerDecorator;
import org.objectweb.proactive.core.xml.handler.CollectionUnmarshaller;
import org.objectweb.proactive.core.xml.handler.PassiveCompositeUnmarshaller;
import org.objectweb.proactive.core.xml.handler.UnmarshallerHandler;
import org.objectweb.proactive.core.xml.io.Attributes;

/**
 * This class reads a process element from XML
 *
 * @author       Lionel Mestre
 * @version      1.0
 */
public class ProcessHandler extends AbstractUnmarshallerDecorator implements ProActiveDescriptorConstants {

  //
  //  ----- PRIVATE MEMBERS -----------------------------------------------------------------------------------
  //

  protected ProActiveDescriptor proActiveDescriptor;

  protected boolean isRef;
  protected ExternalProcess targetProcess;
  protected String refid;

  //
  //  ----- CONSTRUCTORS -----------------------------------------------------------------------------------
  //

 /**
  * Contructs a new intance of a Process
  */
  public ProcessHandler(ProActiveDescriptor proActiveDescriptor) {
    super();
    this.proActiveDescriptor = proActiveDescriptor;
    addHandler(ENVIRONMENT_TAG, new EnvironmentHandler());
    addHandler(BSUB_OPTIONS_TAG,new BsubOptionHandler());
    addHandler(GLOBUS_OPTIONS_TAG,new GlobusOptionHandler());
  }


  //
  //  ----- PUBLIC METHODS -----------------------------------------------------------------------------------
  //

  //
  // -- implements UnmarshallerHandler ------------------------------------------------------
  //

  public Object getResultObject() throws org.xml.sax.SAXException {
    if (refid != null) {
      String result = refid;
      refid = null;
      return result;
    } else {
      ExternalProcess result = targetProcess;
      targetProcess = null;
      return result;
    }
  }

  public void startContextElement(String name, Attributes attributes) throws org.xml.sax.SAXException {
    refid = attributes.getValue("refid");
    if (refid != null) return;
    String id = attributes.getValue("id");
    String className = attributes.getValue("class");
    //System.out.println("Found Process Element id="+id+" className="+className);
    if (! checkNonEmpty(className)) throw new org.xml.sax.SAXException("Process defined without specifying the class");
    try {
      if (id == null) {
        targetProcess = proActiveDescriptor.createProcess(className);
      } else {
        targetProcess = proActiveDescriptor.createProcess(id, className);
      }
    } catch (ProActiveException e) {
      //e.printStackTrace();
      throw new org.xml.sax.SAXException(e.getMessage());
    }
    String hostname = attributes.getValue("hostname");
    if (checkNonEmpty(hostname)) targetProcess.setHostname(hostname);
    String username = attributes.getValue("username");
    if (checkNonEmpty(username)) targetProcess.setHostname(username);
  }


  //
  // -- PROTECTED METHODS ------------------------------------------------------
  //

  protected void notifyEndActiveHandler(String name, UnmarshallerHandler activeHandler) throws org.xml.sax.SAXException {
    if (refid != null) return;
    if (name.equals(ENVIRONMENT_TAG)) {
      targetProcess.setEnvironment((String[]) activeHandler.getResultObject());
    } else if (name.equals(PROCESS_TAG) || name.equals(JVM_PROCESS_TAG)) {
      if (! (targetProcess instanceof ExternalProcessDecorator)) {
        throw new org.xml.sax.SAXException("found a Process defined inside a non composite process");
      }
      ExternalProcessDecorator cep = (ExternalProcessDecorator) targetProcess;
      Object result = activeHandler.getResultObject();
      if (result instanceof String) {
        // its an id of a process defined elsewhere
        proActiveDescriptor.registerProcess(cep, (String) result);
      } else if (result instanceof ExternalProcess) {
        // its a process
        cep.setTargetProcess((ExternalProcess) result);
      }
    }
  }


  protected UnmarshallerHandler getHandler(String elementName) {
    if (elementName.equals(PROCESS_TAG)) {
      return new ProcessHandler(proActiveDescriptor);
    } else if (elementName.equals(JVM_PROCESS_TAG)) {
      return new JVMProcessHandler(proActiveDescriptor);
    } else {
      return super.getHandler(elementName);
    }
  }



  //
  // -- INNER CLASSES ------------------------------------------------------
  //

  /**
   * This class receives environment events
   */
  protected class EnvironmentHandler extends BasicUnmarshaller {

    private java.util.ArrayList variables;

    public EnvironmentHandler() {
    }

    public void startContextElement(String name, Attributes attributes) throws org.xml.sax.SAXException {
      variables = new java.util.ArrayList();
    }

    public Object getResultObject() throws org.xml.sax.SAXException {
      if (variables == null) {
        isResultValid = false;
      } else {
        int n = variables.size();
        String[] result = new String[n];
        if (n > 0) {
          variables.toArray(result);
        }
        setResultObject(result);
        variables.clear();
        variables = null;
      }
      return super.getResultObject();
    }

    public void startElement(String name, Attributes attributes) throws org.xml.sax.SAXException {
      if (name.equals(VARIABLE_TAG)) {
        String vName = attributes.getValue("name");
        String vValue = attributes.getValue("value");
        if (checkNonEmpty(vName) && vValue != null) {
          System.out.println("Found environment variable name="+vName+" value="+vValue);
          variables.add(vName+"="+vValue);
        }
      }
    }

  } // end inner class EnvironmentHandler
  
  /**
   * This class receives options events
   */
  
  protected class BsubOptionHandler extends PassiveCompositeUnmarshaller{
  	
//  	private static final String HOSTLIST_ATTRIBUTE = "hostlist";
//  	private static final String PROCESSOR_ATRIBUTE = "processor";
  	//private LSFBSubProcess bSubProcess;
  	
  	
  	public BsubOptionHandler(){
  		
  		//this.bSubProcess = (LSFBSubProcess)targetProcess;
  		UnmarshallerHandler pathHandler = new PathHandler();
  		this.addHandler(HOST_LIST_TAG,new SingleValueUnmarshaller());
  		this.addHandler(PROCESSOR_TAG,new SingleValueUnmarshaller());
  		BasicUnmarshallerDecorator bch = new BasicUnmarshallerDecorator();
    	bch.addHandler(PATH_TAG, pathHandler);
    	this.addHandler(SCRIPT_PATH_TAG, bch);
  	}
  	
  	
  	public void startContextElement(String name, Attributes attributes) throws org.xml.sax.SAXException {
  	}
  	
  	protected void notifyEndActiveHandler(String name, UnmarshallerHandler activeHandler) throws org.xml.sax.SAXException {
  		// we know that it is a bsub process since we are
  		// in bsub option!!!
  		LSFBSubProcess bSubProcess = (LSFBSubProcess)targetProcess;
  		if(name.equals(HOST_LIST_TAG)){
  			bSubProcess.setHostList((String)activeHandler.getResultObject());
  		}else if (name.equals(PROCESSOR_TAG)){
  			bSubProcess.setProcessorNumber((String)activeHandler.getResultObject());
  		}else if (name.equals(SCRIPT_PATH_TAG)){
  			bSubProcess.setScriptLocation((String)activeHandler.getResultObject());
  		}
  	}	
  }// end inner class OptionHandler
  
  
  /**
   * This class receives globus options events
   */
  
  protected class GlobusOptionHandler extends PassiveCompositeUnmarshaller{
  	
  	public GlobusOptionHandler(){
  		this.addHandler(GRAM_PORT_TAG,new SingleValueUnmarshaller());
  		this.addHandler(GIS_PORT_TAG,new SingleValueUnmarshaller());
  		CollectionUnmarshaller cu = new CollectionUnmarshaller(String.class);
   		cu.addHandler(GLOBUS_HOST_TAG, new SingleValueUnmarshaller());
    	this.addHandler(GLOBUS_HOST_LIST_TAG, cu);
  	}
  	
  	public void startContextElement(String name, Attributes attributes) throws org.xml.sax.SAXException {
  	}
  	
  	protected void notifyEndActiveHandler(String name, UnmarshallerHandler activeHandler) throws org.xml.sax.SAXException {
  		// we know that it is a globus process since we are
  		// in globus option!!!
  		GlobusProcess globusProcess = (GlobusProcess)targetProcess;
  		if(name.equals(GRAM_PORT_TAG)){
  			globusProcess.setGramPort((String)activeHandler.getResultObject());
  		}else if (name.equals(GIS_PORT_TAG)){
  			globusProcess.setGISPort((String)activeHandler.getResultObject());
  		}else if (name.equals(GLOBUS_HOST_LIST_TAG)){
  			String[] globusHostList = (String[])activeHandler.getResultObject();
  			for (int i = 0; i < globusHostList.length; i++)
				{
					globusProcess.addGlobusHost(globusHostList[i]);
				}
  		}
  	}
  		
  }
  
  private class SingleValueUnmarshaller extends BasicUnmarshaller {
    	public void readValue(String value) throws org.xml.sax.SAXException {
      setResultObject(value);
    	} 	
  	} //end of inner class SingleValueUnmarshaller
  
}
/**
 * Created on 8 juil. 2002
 *
 * To change this generated comment edit the template variable "filecomment":
 * Window>Preferences>Java>Templates.
 */
package org.objectweb.proactive.core.process;

import java.io.Serializable;

import org.objectweb.proactive.core.process.JVMProcessImpl;
import org.objectweb.proactive.core.util.MessageLogger; 

/**
 * @author rquilici
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class JVMNodeProcess extends JVMProcessImpl implements Serializable{ 
    
  /**
   * Constructor for JVMNodeProcess.
   */
  public JVMNodeProcess() {
	this(new StandardOutputMessageLogger());
	setClassname("org.objectweb.proactive.core.runtime.StartRuntime"); 
  }

  /**
   * Constructor for JVMNodeProcess.
   * @param messageLogger
   */
  public JVMNodeProcess(MessageLogger messageLogger) {
    super(messageLogger);
  }

  /**
   * Constructor for JVMNodeProcess.
   * @param inputMessageLogger
   * @param errorMessageLogger
   */
  public JVMNodeProcess(MessageLogger inputMessageLogger, MessageLogger errorMessageLogger) {
    super(inputMessageLogger, errorMessageLogger);
  }

//  protected void handleProcess(java.io.BufferedReader in, java.io.BufferedWriter out, java.io.BufferedReader err) {
// String s = null;
//  try {
//////      //while ((s = in.readLine()) != null && (s.indexOf(NODEOK) == -1)) {
//     while ((s = in.readLine()) != null && (s.indexOf(RUNTIMEOK) == -1)){
//     	    //while ((s = in.readLine()) != null ) {
//       System.out.println("-----------------"+s);
//     }
//     } catch (Exception e) {
//     e.printStackTrace();
//     System.out.println("exception in handle process");
//      }
////   super.handleProcess(in,out,err);
//  }
}

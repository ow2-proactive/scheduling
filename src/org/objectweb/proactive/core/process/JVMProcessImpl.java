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
package org.objectweb.proactive.core.process;

import java.io.Serializable;

import org.objectweb.proactive.core.util.MessageLogger;

/**
 * <p>
 * The JVMProcess class is able to start localy any class of the ProActive library by 
 * creating a Java Virtual Machine.
 * </p><p>
 * For instance:
 * </p>
 * <pre>
 * .............
 * JVMProcessImpl process = new JVMProcessImpl(new StandardOutputMessageLogger());
 * process.setClassname("org.objectweb.proactive.StartNode");
 * process.setParameters("nodeName");
 * process.startProcess();
 * .............
 * </pre>
 * <p>
 * This piece of code launches the ProActive java class org.objectweb.proactive.StartNode
 * with nodeName as parameter.
 * </p>
 * @author  ProActive Team
 * @version 1.0,  2002/09/20
 * @since   ProActive 0.9.4
 */

public class JVMProcessImpl extends AbstractExternalProcess implements JVMProcess,Serializable {
  
  private static final String FILE_SEPARATOR = System.getProperty("file.separator");
  private final static String POLICY_FILE = "proactive.java.policy";
  private final static String POLICY_OPTION = " -Djava.security.policy=";

  public final static String DEFAULT_CLASSPATH = convertClasspathToAbsolutePath(System.getProperty("java.class.path"));
  public final static String DEFAULT_JAVAPATH = System.getProperty("java.home")+FILE_SEPARATOR+"bin"+FILE_SEPARATOR+"java";
  public final static String DEFAULT_POLICY_FILE = System.getProperty("user.dir")+FILE_SEPARATOR+POLICY_FILE;
  public final static String DEFAULT_CLASSNAME ="org.objectweb.proactive.StartNode";

  protected String classpath = DEFAULT_CLASSPATH;
  protected String javaPath = DEFAULT_JAVAPATH;
  protected String policyFile = DEFAULT_POLICY_FILE;
  protected String classname = DEFAULT_CLASSNAME;
  protected String parameters;
    
  
	
  //
  // -- CONSTRUCTORS -----------------------------------------------
  //
  /**
   * Creates a new JVMProcess
   * Used with XML Descriptor
   */
  public JVMProcessImpl() {
  }

	/**
   * Creates a new JVMProcess
   * @param messageLogger The logger that handles input and error stream of this process
   */
  public JVMProcessImpl(MessageLogger messageLogger) {
    super(messageLogger);
  }
  
  
  /**
   * Creates a new JVMProcess
   * @param inputMessageLogger The logger that handles input stream of this process
   * @param errorMessageLogger The logger that handles error stream of this process
   */
  public JVMProcessImpl(MessageLogger inputMessageLogger, MessageLogger errorMessageLogger) {
    super(inputMessageLogger, errorMessageLogger);
  }
  

  //
  // -- PUBLIC METHODS -----------------------------------------------
  //
    
  public static void main(String[] args) {
    try {
      JVMProcessImpl rsh = new JVMProcessImpl(new StandardOutputMessageLogger());
      rsh.setClassname("org.objectweb.proactive.StartNode");
      rsh.setParameters(args[0]);
      rsh.startProcess();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  //
  // -- implements JVMProcess -----------------------------------------------
  //

  public String getClasspath() {
    return classpath;
  }
  
  public void setClasspath(String classpath) {
    checkStarted();
    this.classpath = classpath;
  }
  

  public String getJavaPath() {
    return javaPath;
  }
  
  public void setJavaPath(String javaPath) {
    checkStarted();
    if (javaPath == null) throw new NullPointerException();
    this.javaPath = javaPath;
  }


  public String getPolicyFile() {
    return policyFile;
  }
  
  public void setPolicyFile(String policyFile) {
    checkStarted();
    this.policyFile = policyFile;
  }
  

  public String getClassname() {
    return classname;
  }
  
  public void setClassname(String classname) {
    checkStarted();
    this.classname = classname;
  }
  
  
  public String getParameters() {
    return parameters;
  }
  
  public void setParameters(String parameters) {
    checkStarted();
    this.parameters = parameters;
  }
  

  //
  // -- PROTECTED METHODS -----------------------------------------------
  //

  protected String buildCommand() {
    return buildJavaCommand(classname, parameters);
  }
  
  
  protected String buildJavaCommand(String classname, String parameters) {
    StringBuffer javaCommand = new StringBuffer();
    // append java command
    if (javaPath == null) {
      javaCommand.append("java");
    } else {
      javaCommand.append(javaPath);
    }
    // append classpath
    if ((classpath != null) && (classpath.length() > 0)) {
      javaCommand.append(" -cp ");
      javaCommand.append(classpath);
    }
    // append policy option
    if (policyFile != null) {
      javaCommand.append(POLICY_OPTION);
      javaCommand.append(policyFile);
    }
    // append classname
    javaCommand.append(" ");
    javaCommand.append(classname);
    if (parameters != null) {
      javaCommand.append(" ");
      javaCommand.append(parameters);
    }
    return javaCommand.toString();
  }


  //
  // -- PRIVATE METHODS -----------------------------------------------
  //

  private static String convertClasspathToAbsolutePath(String classpath) {
    StringBuffer absoluteClasspath = new StringBuffer();
    String pathSeparator = System.getProperty("path.separator");
    java.util.StringTokenizer st = new java.util.StringTokenizer(classpath, pathSeparator);
    while (st.hasMoreTokens()) {
      absoluteClasspath.append(new java.io.File(st.nextToken()).getAbsolutePath());
      absoluteClasspath.append(pathSeparator);
    }
    return absoluteClasspath.substring(0, absoluteClasspath.length()-1);
  }    

}

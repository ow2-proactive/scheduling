

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

package org.objectweb.proactive.core.process.globus;

import java.util.Iterator;
import java.util.List;

import org.globus.gram.GramException;
import org.globus.gram.GramJob;
import org.globus.gram.GramJobListener;
import org.globus.io.gass.server.GassServer;
import org.globus.io.gass.server.JobOutputListener;
import org.globus.io.gass.server.JobOutputStream;
import org.globus.rsl.Binding;
import org.globus.rsl.Bindings;
import org.globus.rsl.NameOpValue;
import org.globus.rsl.RSLParser;
import org.globus.rsl.RslNode;
import org.globus.rsl.Value;
import org.globus.rsl.VarRef;
import org.globus.security.GlobusProxy;
import org.globus.security.GlobusProxyException;
import org.objectweb.proactive.core.process.AbstractExternalProcessDecorator;
import org.objectweb.proactive.core.process.JVMProcess;
import org.objectweb.proactive.core.process.JVMProcessImpl;
import org.objectweb.proactive.core.util.UrlBuilder;


/**
 * The GlobusProcess class is able to start any class, of the ProActive library, 
 * on a remote host managed by globus.
 * 
 * This class is used in StartNode.java an in ic2d.
 * 
 * If you plan yo use it, we assume that, you are allowed to start a job on the remote host, 
 * this means:
 * 0) the file globus.xml is well configured;
 * 1) you have a globus certificate and you have install a version of cog > 0-9.13 => java 1.4
 * 2) the grid-mapfile of the remote host have an entry for you.
 * 3) you have an avaible globus-proxy by runing the grid-proxy-init command of the globus toolkit.
 *
 * Contact you're globus administrator for more details.
 *
 * Here is an example of GlobusProcess use :
 *
 * GlobusProcess gp=new GlobusProcess();
 * gp.startNodeWithGlobus("rmi://satura.inria.fr/GlobusRmiNode1");
 *
 * This example launch the class StartNode of the package org.objectweb.proactive with 
 * the parameter rmi://satura.inria.fr/GlobusRmiNode1
 *
 * Once you have launch a job, you can take a look at $HOME/proactive-tmp/stdOutGlobusProcess
 * to see what append.
 *
 * If you have a problem when you run a job; looking the $HOME/proactive-tmp/stdErrGlobusProcess file will certainly help you.
 * You can also look the log file corresponding to the job on the remote account 
 * (for example gram_job_mgr_10658.log).
 * If the problem persist you can contact :
 *   - the mailing list discuss@globus.org
 *   - diego.nieuwbourg@caramail.com
 */

public class GlobusProcess extends AbstractExternalProcessDecorator{

  //--------------- ATTRIBUTES
  
  private static final String FILE_SEPARATOR = System.getProperty("file.separator");
  private static final String DEFAULT_STDERR = System.getProperty("user.home")+FILE_SEPARATOR+"proActive-tmp";
  private static final String DEFAULT_STDIN = System.getProperty("user.home")+FILE_SEPARATOR+"proActive-tmp";
  private static final String DEFAULT_STDOUT = System.getProperty("user.home")+FILE_SEPARATOR+"proActive-tmp";
  private static final String DEFAULT_GRAMPORT = "2119";
  private static final String DEFAULT_GISPORT = "2135";
  //The job managed by the GlobusProcess
  private GramJob job;

  //The adviser for important globus host infos
  private GlobusHostAdviser globusHostAdviser;
  
	protected JVMProcessImpl jvmProcess;
  //Attributes used to launch the job
  private String java_home;
  private String classpath;  
  private String stderr = DEFAULT_STDERR;
  private String stdout = DEFAULT_STDOUT;
  private String stdin = DEFAULT_STDIN;
  private String gramPort = DEFAULT_GRAMPORT;
  private String gisPort = DEFAULT_GISPORT;

  //An identificator
  private String params;

  // Attribute used to get the output
  private GlobusProxy proxy;
  private static GassServer gassServer;
  private RslNode rslTree;
  private String jobOutput;  

  // Attribute used to shyncronise
  private boolean READY=false;

  //===========================================================
  // Constructor
  //===========================================================

  /**
   * This is the empty constructor, when you use it you will run you're job (process)
   * on the default remote host, unless you select an other remote host by using
   * getAllGlobusHost() and setRemoteHost(String remoteHost).
   */
  public GlobusProcess(){
  	super();
    setCompositionType(GIVE_COMMAND_AS_PARAMETER);
    this.hostname = null;
//    try {
//    	System.out.println("in constructor...");
//     // proxy = GlobusProxySerializable.getDefaultUserProxy();
//    } catch (GlobusProxyException e){
//      System.out.println("Cannot get the proxy:"+e.getMessage());	
//    }
    this.globusHostAdviser = new GlobusHostAdviser();
  }
  
  
  public GlobusProcess(JVMProcess process){
  	super(process,GIVE_COMMAND_AS_PARAMETER);
  	this.jvmProcess = (JVMProcessImpl)targetProcess;
  	this.hostname = null;
//    try {
//      //proxy = (GlobusProxySerializable) GlobusProxySerializable.getDefaultUserProxy();
//    } catch (GlobusProxyException e){
//      System.out.println("Cannot get the proxy:"+e.getMessage());	
//    }
    this.globusHostAdviser = new GlobusHostAdviser();
  }

  /**
   * using this constructor, allow you to directly run a class on a remote host.
   */
//  public GlobusProcess(String aProActivePackage, String className, String parameters, 
//			   String remoteHost){
//
//    System.out.println("En cours ...");
//    //globusHostAdviser = new GlobusHostAdviser();
//
//    if (globusHostAdviser.isAValidHostName(remoteHost)){
//      //...
//      startGlobusProcess(aProActivePackage, className, parameters, remoteHost);
//    }
//    else {
//      globusHostAdviser.displayAllValidHostName();
//    }
//
//  }

  //===========================================================
  // Accessor
  //===========================================================

  public String getId(){
      return params;
  }

  public String getJobOutput(){
      return jobOutput;
  }

  public GramJob getJob(){
      return job;
  }
  
  public String getGramPort(){
  	return this.gramPort;
  }
  
  public String getGISPort(){
  	return this.gisPort;
  }
  
  public GlobusHostAdviser getGlobusHostAdviser(){
  	return this.globusHostAdviser;
  }
  //===========================================================
  // Seteur
  //===========================================================

  // set Remote Host

  public void setId(String anId){
	this.params=anId;
	((JVMProcess)targetProcess).setParameters(anId);
  }

  public void setJobOutput(String output){
      if(jobOutput==null)
	  jobOutput=new String(output);
      else{
	  jobOutput = jobOutput + output;
      }
      System.out.println("GlobusProcess seting the output:");
      System.out.println("New output:"+output);
      System.out.println("Final output:"+jobOutput);
  }
  
  public void setGramPort(String gramPort){
  	this.gramPort = gramPort;
  }

  public void setGISPort(String gisPort){
  	this.gisPort = gisPort;
  }
  
  public void addGlobusHost(String host){
  	this.globusHostAdviser.addHost(host);
  }

    public void setReady(boolean b){
	READY=b;
    }

  //===========================================================
  // Redefine method from AbstracUnniversalProcess
  //===========================================================

  protected void internalStopProcess(){
    try {
      job.cancel();
    } catch (GramException e) {
      System.out.println("Error : "+ e.getMessage(e.getErrorCode()));
      return;
    } catch (GlobusProxyException e) {
      System.out.println("Erreur : "+ e.getMessage());
      return;
    } 
  }

  public void stopProcess() {
      try {
	  job.cancel();
      } catch (GramException e) {
	  System.out.println("Error : "+ e.getMessage(e.getErrorCode()));
	  return;
      } catch (GlobusProxyException e) {
	  System.out.println("Erreur : "+ e.getMessage());
	  return;
      } 
  }

  
  protected void internalStartProcess(String rslCommand) throws java.io.IOException{
      startGlobusProcess( rslCommand, this.hostname);
      //startGlobusProcessWithoutOutput(rslCommand,this.hostname);
  }

//  protected String buildCommand(){
//    //...
//    return null;
//  }
  
  protected String internalBuildCommand() {
   return buildRSLCommand();
  }
  
  protected String buildEnvironmentCommand(){
  	if (environment == null) return "";
  	StringBuffer sb = new StringBuffer();
  	String[] globusEnvironment = new String[environment.length];
  	for (int i=0; i<environment.length; i++) {
  		globusEnvironment[i] = environment[i].replace('=',' ');
  		sb.append("(");
  		sb.append(globusEnvironment[i]);
  		sb.append(")");
  	}
  	return sb.toString();
  }

  

  //===========================================================
  // Utility method
  //===========================================================


  //Start a node on a globus host
  public void startNodeWithGlobus(String parameters){

    String host = null;
    String protocol;
    //StringTokenizer st=new StringTokenizer(parameters,"/");
    //We get the host name
    //protocol=st.nextToken();		//token="rmi:"
    protocol = UrlBuilder.getProtocol(parameters);
    //System.out.println("*"+protocol);
    //host=st.nextToken();        //token=hostname
    try{
    host = UrlBuilder.getHostNameFromUrl(parameters);
    //System.out.println("*"+host);
    }catch(java.net.UnknownHostException e){
    	e.printStackTrace();
    }
    this.hostname = host;

   if (globusHostAdviser.isAValidHostName(host)){

     // set the id for this globusProcess
     setId(parameters);

     // We build the RSL command 
//     if (protocol.equals("rmi:")){
       // We launch the process
//       System.out.println("1");
//       System.out.println("*"+protocol);
//       System.out.println("*"+host);
       //startGlobusProcess("org.objectweb.proactive" ,"StartNode" , parameters, host);
       //String rslCommand=buildRSLCommand("org.objectweb.proactive" ,"StartNode" , parameters, host);
//       String rslCommand=buildRSLCommand((JVMProcess)targetProcess);
//       startGlobusProcess( rslCommand, host);
//     }
//     else {
//     	System.out.println("2");
//       System.out.println("*"+protocol);
//       System.out.println("*"+host);
     		//protocol="jini:"
       //String proActiveHome=globusHostAdviser.giveProActiveHome(host);
       //classpath=proActiveHome+"/classes:"+proActiveHome+"/lib/jini-ext.jar:"+proActiveHome+"/lib/reggie.jar";
       //classpath=((JVMProcess)targetProcess).getClasspath();
       //String rslCommand=buildRSLCommand("org.objectweb.proactive" ,"StartNode" , parameters, host);
				//String rslCommand=buildRSLCommand((JVMProcess)targetProcess);
       // We launch the rmid
       //startRMIDWithGlobus(host);  // an error appear in the stdOut but the rmid is laucnh ???

       // We launch the process
       //startGlobusProcess( rslCommand, host);
//     }
     try{
     startProcess();
     }catch(java.io.IOException e){
     	e.printStackTrace();
     }
    }
    else {	//Bad host name
      globusHostAdviser.displayAllValidHostName();
    }
  }

  public void startRMIDWithGlobus(String aHost){
//-----------------------for jini---------------------------------------
//    //String exec=globusHostAdviser.giveJavaPath(aHost)+"/bin/rmid";
//    String exec = ((JVMProcess)targetProcess).getJavaPath();
//
//    stderr=globusHostAdviser.giveStdout(aHost) + "/stdErrGlobusProcess";
//    stdout=globusHostAdviser.giveStdout(aHost) + "/stdOutGlobusProcess";
//
//    String proActiveHome=globusHostAdviser.giveProActiveHome(aHost);
//    classpath=proActiveHome+"/lib/reggie.jar";
//
//    String rslCommand= "&(executable=" + exec +")" +
//                       "(environment=(CLASSPATH " + classpath +"))"+
//                       "(stderr="+ stderr + ") "+
//		       "(stdout="+ stdout + ") ";
//
//    startGlobusProcessWithoutOutput(rslCommand, aHost);

  }

  /**
   * This method launch a class on a remote host (by default satura.inria.fr)
   * @param aPackage the ProActive package containing the class we're going to launch. 
   * @param classname the name of the class we're going to launch.
   * @param parameters the parameters use with the class we're going to launch.
   */
//  public void startGlobusProcess (String aPackage, String classname, String parameters, String aHost){
//
//    String rslCommand= buildRSLCommand(aPackage, classname, parameters,aHost);
//    String contact= aHost + ":" + this.gramPort;
//
//    job = new GramJob(rslCommand);
//
//    // This listener is used to notify the implementer when the status of a GramJob has changed.
//    job.addListener(new GramJobListener() {
//      public void statusChanged(GramJob job) {
//        // react to state change
//	// to do : switch on the status job ...
//        System.out.println("***** JOB STATUS CHANGE ***** ");
//        System.out.println("ID     : " + job.getID());
//        System.out.println("RSL    : " + job.getRSL());
//        System.out.println("STATUS : " + job.getStatusAsString());
//        //System.out.println("representation : " + job.toString());
//      }
//    });
//  	
//    // Submitting the job to the gatekeeper indentifie by the contact string
//    try {
//      job.request(contact);
//      System.out.println("job submited : "+ job.getIDAsString());
//    } catch (GramException e) {
//      System.out.println("Error : "+ e.getMessage(e.getErrorCode()));
//      return;
//    } catch (GlobusProxyException e) {
//      System.out.println("Erreur : "+ e.getMessage());
//      System.out.println("If the certificate has expired, run grid-proxy-init script");
//      return;
//    } 
//  }

  /**
   * This method launch a class on a remote host an display the output on the local host.
   * @param rslCommand to generate with buildRSLCommand
   */
  public void startGlobusProcess (String rslCommand,String aHost){

    MyJobListener jobListener = new MyJobListener();
    

//    String contact= aHost + ":" + globusHostAdviser.giveGramPort(aHost);
	String contact= aHost + ":" + this.gramPort;
    String finalRSL;

    JobOutputStream jos;
    try {
      proxy = GlobusProxy.getDefaultUserProxy();
    } catch (GlobusProxyException e){
      System.out.println("Cannot get the proxy:"+e.getMessage());	
    }

    try{
      rslTree= RSLParser.parse(rslCommand);
     
      gassServer  = new GassServer(proxy,0);
      rslOutputSubst(rslTree, gassServer.getURL());
      gassServer.setOptions(GassServer.STDOUT_ENABLE|GassServer.STDERR_ENABLE);
      gassServer.registerDefaultDeactivator();
      jos = new JobOutputStream(new MyJobOutputListener(jobListener,this));
      gassServer.registerJobOutputStream("out",jos);
      finalRSL=rslTree.toRSL(true);
    } catch(Exception e) {
      System.err.println("Unable to load user credentials");
      return;
    } 
  	
    job = new GramJob(proxy,finalRSL);

    // Submitting the job to the gatekeeper indentifie by the contact string
    try {
      gassServer.setOptions(GassServer.STDOUT_ENABLE|GassServer.STDERR_ENABLE);
      gassServer.registerDefaultDeactivator();
      jos = new JobOutputStream(new MyJobOutputListener(jobListener,this));
      gassServer.registerJobOutputStream("out",jos);
      job.addListener( new GramJobListener() {
	  public void statusChanged(GramJob job) {
	      // react to state change
	      // to do : switch on the status job ...
	      System.out.println("***** JOB STATUS CHANGE ***** ");
	      System.out.println("ID     : " + job.getID());
	      System.out.println("RSL    : " + job.getRSL());
	      System.out.println("STATUS : " + job.getStatusAsString());
	      //if (job.getStatusAsString()=="ACTIVE"){LAUNCHED=true;}
	      //System.out.println("representation : " + job.toString());
	  }
      });
      
      job.request(contact);
      System.out.println("job submited : "+ job.getIDAsString());
    } catch (GramException e) {
	System.out.println("Error : "+ e.getMessage(e.getErrorCode()));
	e.printStackTrace();
	return;
    } catch (GlobusProxyException e) {
	System.out.println("Erreur : "+ e.getMessage());
	return;
    } 
  }

   /**
   * This method launch a class on a remote host (by default satura.inria.fr)
   * @param rslCommand to generate with buildRSLCommand
   */
  public void startGlobusProcessWithoutOutput (String rslCommand,String aHost){

    String contact= aHost + ":" + this.gramPort;

    job = new GramJob(rslCommand);
 
    // This listener is used to notify the implementer when the status of a GramJob has changed.
    job.addListener(new GramJobListener() {
      public void statusChanged(GramJob job) {
        // react to state change
        // to do : switch on the status job ...
        System.out.println("***** JOB STATUS CHANGE ***** ");
        System.out.println("ID     : " + job.getID());
        System.out.println("RSL    : " + job.getRSL());
        System.out.println("STATUS : " + job.getStatusAsString());
        //if (job.getStatusAsString()=="ACTIVE"){LAUNCHED=true;}
        //System.out.println("representation : " + job.toString());
	if  (job.getStatus() == GramJob.STATUS_ACTIVE) {
	    READY=true;
	}
      }
    });
 
    // Submitting the job to the gatekeeper indentifie by the contact string
    try {
      job.request(contact);
      System.out.println("job submited : "+ job.getIDAsString());
    } catch (GramException e) {
      System.out.println("Error : "+ e.getMessage(e.getErrorCode()));
      return;
    } catch (GlobusProxyException e) {
      System.out.println("Erreur : "+ e.getMessage());
      return;
    }
  }

  public String getOutput(){
    return jobOutput;
  }


  public boolean isReady(){
    return READY;
  }

  //--------------- PROTECTED METHOD



  //--------------- PRIVATE METHOD
  
  private String buildRSLCommand (){
  	String rslCommand = "&(executable=" + ((JVMProcess)targetProcess).getJavaPath()+")" + 
  											"(arguments="+((JVMProcess)targetProcess).getClassname() + " " + ((JVMProcess)targetProcess).getParameters() +")"+
  											"(environment=(CLASSPATH "+((JVMProcess)targetProcess).getClasspath()+")"+buildEnvironmentCommand()+")";
		return rslCommand;
  }

  /**
   * This method build an RSL command in function of the environment and in function of 
   * the class you want to launch.  
   */
//  private String buildRSLCommand (String aPackage, String classname, String parameters,String aHost){
//
//    java_home=globusHostAdviser.giveJavaPath(aHost);
//    stderr=globusHostAdviser.giveStdout(aHost) + "/stdErrGlobusProcess";
//    stdout=globusHostAdviser.giveStdout(aHost) + "/stdOutGlobusProcess";
//
//    if (classpath==null){
//     System.out.println("2");
//     classpath=globusHostAdviser.giveProActiveHome(aHost)+"/classes";
//    }
//
//    String rslCommand= "&(executable=" + java_home +"/bin/java)" +
//		       "(arguments=" + aPackage + "." + classname + " " + parameters + ")" +
//                       "(environment=(CLASSPATH " + classpath +"))"+
//                       "(stderr="+ stderr + ") "+
//		       "(stdout="+ stdout + ") ";
//    return rslCommand;
//  }

  //===========================================================
  // Utility method and class used to get the ouptut of the job
  //===========================================================

  private static void rslOutputSubst(RslNode rslTree, String gassUrl) {
    if (rslTree.getOperator() == RslNode.MULTI) {
      List specs = rslTree.getSpecifications();
      Iterator iter = specs.iterator();
      RslNode node;
      while( iter.hasNext() ) {
        node = (RslNode)iter.next();
        rslOutputSubst(node, gassUrl);
      }
    } else {
      Binding bd = new Binding("GLOBUSRUN_GASS_URL", gassUrl);
      Bindings bind = rslTree.getBindings("rsl_substitution");
      if (bind == null) {
	bind = new Bindings("rsl_substitution");
	rslTree.put(bind);
      }
      bind.add(bd);
	    
      Value value = null;
      value = new VarRef("GLOBUSRUN_GASS_URL",
			  null,
		          new Value("/dev/stdout"));
	    
      rslTree.put(new NameOpValue("stdout", 
				  NameOpValue.EQ, 
				  value));

      value = new VarRef("GLOBUSRUN_GASS_URL",
			 null,
			new Value("/dev/stderr"));

      rslTree.put(new NameOpValue("stderr",
                                  NameOpValue.EQ,
				  value));
    }
  }


  static class MyJobOutputListener implements JobOutputListener {
    private MyJobListener jobListener;
    private GlobusProcess gp;


    public MyJobOutputListener(MyJobListener jl,GlobusProcess g) {
      jobListener = jl;
      gp=g;
    }
   
    public void outputClosed() {
      System.out.println("output closed");
      gassServer.unregisterJobOutputStream("out");
      (gp.getJob()).removeListener(jobListener);
      System.out.println("ok");
    }

    public void outputChanged(String output) {
      System.out.println("output changed:" + output);
      //if(haveChanged==false){
        jobListener.setOutput(output);
        gp.setJobOutput(output);
        gp.setReady(true);
	//}
    }
  }

  static class MyJobListener implements GramJobListener {
    private String vystup = null;
    private int error;

    public void setOutput(String l) { 
      vystup = l;      
    }

    public String getOutput() { 
      return vystup;
    }

    public void statusChanged(GramJob job) {
      System.out.println(" "+job.getIDAsString()+" "+job.getStatusAsString());
      if (job.getStatus() == GramJob.STATUS_DONE) { setError(0);} 
      else if (job.getStatus() == GramJob.STATUS_FAILED) {
        setError( (job.getError() == 0) ? 1 : job.getError() );
      }
      //else if  (job.getStatus() == GramJob.STATUS_ACTIVE) {
	  //  READY=true;
      //}
    }

    public int getError() {
      return error;
    }

    public void setError(int error) {
      this.error = error;
      synchronized(this) { notifyAll(); }
    }
  }

  //===========================================================
  // Main
  //===========================================================

  public static void main( String[] args ) {

     GlobusProcess gp=new GlobusProcess();
     // gp.startNodeWithGlobus("rmi://satura.inria.fr/RMINode2"); //ok
     // gp.startNodeWithGlobus("jini://satura.inria.fr/GlobusJININode1");  //ok
     // gp.startNodeWithGlobus("jini://satura.inria.fr/GlobusJININode3");  //ok
     // java GlobusProcess org.objectweb.proactive StartNode rmi://satura.inria.fr/JiniNode
     // gp.startGlobusProcess(pakage, classe, param);
  }
}


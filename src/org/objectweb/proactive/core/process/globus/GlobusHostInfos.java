
package org.objectweb.proactive.core.process.globus;

import org.apache.log4j.Logger;

public class GlobusHostInfos {
	
	static Logger logger = Logger.getLogger(GlobusHostInfos.class.getName());

  protected String hostname,
		   javahome,
		   classpath,
    		   proactivehome,
		   jiniproperties,
		   stdout,
   		   gramport,
		   gisport;

  //===========================================================
  // Constructor
  //===========================================================

  public GlobusHostInfos(){
  }

  public GlobusHostInfos(String hostName,
			 String javaHome,
			 String proActiveHome,
			 String stdOut,
			 String gramPort,
		 	 String gisPort ){
    hostname=hostName;
    javahome=javaHome;
    proactivehome=proActiveHome;
    stdout=stdOut;
    gramport=gramPort;
    gisport=gisPort;	
  }  

  //===========================================================
  // Setteur
  //===========================================================

  public void SetHostName(String hostName){
    hostname=hostName;
  }

  public void SetJavaHome(String javaHome){
    javahome=javaHome;
  }

  public void SetProActiveHome(String proActiveHome){
    proactivehome=proActiveHome;
  }

  public void SetStdOut(String stdOut){
    stdout=stdOut;
  }

  public void SetGramPort(String gramPort){
    gramport=gramPort;
  }

  public void SetGisPort(String gisPort){
    gisport=gisPort;
  }

  //===========================================================
  // Accessor
  //===========================================================


  public String GetHostName(){
    return hostname;
  }

  public String GetJavaHome(){
    return javahome;
  }

  public String GetProActiveHome(){
    return proactivehome;
  }

  public String GetStdOut(){
    return stdout;
  }

  public String GetGramPort(){
    return gramport;
  }

  public String GetGisPort(){
    return gisport;
  }

  //===========================================================
  // Utility method
  //===========================================================

  public void DisplayYourSelf(){

    logger.info("============= GLOBUS HOST INFOS =============");
    logger.info("Host Name:" + hostname);
    logger.info("Java Home:" + javahome);
    logger.info("ProActive Home:" + proactivehome);
    logger.info("Stdout:" + stdout);
    logger.info("GRAM port:" + gramport);
    logger.info("GIS port:" + gisport);
    logger.info("=============================================");

  }  

}


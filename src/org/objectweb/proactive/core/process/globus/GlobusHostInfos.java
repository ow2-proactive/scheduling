
package org.objectweb.proactive.core.process.globus;

public class GlobusHostInfos {

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

    System.out.println("============= GLOBUS HOST INFOS =============");
    System.out.println("Host Name:" + hostname);
    System.out.println("Java Home:" + javahome);
    System.out.println("ProActive Home:" + proactivehome);
    System.out.println("Stdout:" + stdout);
    System.out.println("GRAM port:" + gramport);
    System.out.println("GIS port:" + gisport);
    System.out.println("=============================================");

  }  

}


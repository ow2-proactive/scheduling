//Sujet: GlobusHostAdviser.java
//   De: Diego Nieuwbourg <Diego.Nieuwbourg@sophia.inria.fr>
//     A: nieuwbou@essi.fr
//   Date: Lun, 16 Septembre 2002, 15:37

package org.objectweb.proactive.core.process.globus;

import java.io.*;
import java.util.*;

/**
 * This class inform about globus host configuration
 */

public class GlobusHostAdviser implements java.io.Serializable{

//    protected static XMLConfigurator xmlConfig;
//    protected Dictionary globusHostsInfos;
			private java.util.ArrayList hostList;

    //===========================================================
    // Constructor
    //===========================================================

    public GlobusHostAdviser(){
    	this.hostList = new java.util.ArrayList();

//       xmlConfig = new XMLConfigurator();
//       globusHostsInfos=xmlConfig.getGlobusConfiguration();
	}
	
	
	//
	//----------------public methods------------------------------------------
	
	public void addHost(String hostName){
		hostList.add(hostName);
	}
	
	

    //===========================================================
    // Important Utility method
    //===========================================================
    
    // not an accesor so we put give instead of get
//    public String giveJavaPath(String aHost){
//
//      if (isAValidHostName(aHost)){
//        GlobusHostInfos ghiTemp = (GlobusHostInfos) globusHostsInfos.get(aHost);
//        return ghiTemp.GetJavaHome();
//      }
//      return null;
//
//    }


//    public String giveProActiveHome(String aHost){
//
//      if (isAValidHostName(aHost)){
//        GlobusHostInfos ghiTemp = (GlobusHostInfos) globusHostsInfos.get(aHost);
//        return ghiTemp.GetProActiveHome();
//      }
//      return null;      
//
//    }
//
//    public String giveStdout(String aHost){
//
//      if (isAValidHostName(aHost)){
//        GlobusHostInfos ghiTemp = (GlobusHostInfos) globusHostsInfos.get(aHost);
//        return ghiTemp.GetStdOut();
//      }
//      return null;            
//
//    }
//
//    public String giveGramPort(String aHost){
//
//      if (isAValidHostName(aHost)){
//        GlobusHostInfos ghiTemp = (GlobusHostInfos) globusHostsInfos.get(aHost);
//        return ghiTemp.GetGramPort();
//      }
//      return null;      
//      
//    }
//
//    public String giveGisPort(String aHost){
//
//      if (isAValidHostName(aHost)){
//        GlobusHostInfos ghiTemp = (GlobusHostInfos) globusHostsInfos.get(aHost);
//        return ghiTemp.GetGisPort();
//      }
//      return null;      
//      
//    }
//
//    public String giveAllValidHostName(){
//
//	StringBuffer result = new StringBuffer();
//	
//	Enumeration keys = globusHostsInfos.keys();
//	while(keys.hasMoreElements()){
//	    String key = (String) keys.nextElement();
//	    result.append(key);
//	    result.append("\n");
//	}
//
//	return new String(result);
//
//    }
//
//
//    //===========================================================
//    // Other Utility method
//    //===========================================================
//
//    //Display all the infos about the globus host contain in the dictionnary
//    public void DisplayAllGlobusHostsInfos(){
//
//      Enumeration keys = globusHostsInfos.keys();
//      while(keys.hasMoreElements()){
//	String key = (String) keys.nextElement();
//	GlobusHostInfos ghiTemp = (GlobusHostInfos) globusHostsInfos.get(key);
//	ghiTemp.DisplayYourSelf();
//      }
//
//    }
//
    //Test if a host name is valid
    public boolean isAValidHostName(String aHostName){
       if ((hostList.contains(aHostName))){
	 return true;
       }
       System.out.println("Error: The host name you use ("+aHostName+") is not valid");
       displayAllValidHostName();
       return false;
    }
//
//
    //Display all valid host name
    public void displayAllValidHostName(){

      System.out.println("Here is the list of valid host:");
      for (Iterator iter = hostList.iterator(); iter.hasNext();)
			{
				String hostName = (String) iter.next();
				System.out.println("Valid Globus Host "+hostName);
			}
//      Enumeration keys = hostList.keys();
//      while(keys.hasMoreElements()){
//	String key = (String) keys.nextElement();
//	System.out.println(key);
      }
      
      public String[] getGlobusHosts(){
      	String[] globusHosts = new String[hostList.size()];
      	int i = 0;
      	for (Iterator iter = hostList.iterator(); iter.hasNext();)
				{
					globusHosts[i] = (String)iter.next();
					i++;
				}
				return globusHosts;
      }
		
		
	//}
//
//
//    //===========================================================
//    // Main
//    //===========================================================
//
//    public static void main(String argv[]){
//
//      GlobusHostAdviser gha = new GlobusHostAdviser();
//      gha.DisplayAllGlobusHostsInfos();
//      String p=gha.giveJavaPath("satura.inria.fr");
//      System.out.println("Javapath:"+p);
//      /*if (gha.isAValidHostName("satura.inria.fr3")){
//	System.out.println("OK");
//      }
//      else {
//	System.out.println("...");
//      }*/
//    }

}

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
package org.objectweb.proactive.core.jini;

import net.jini.core.discovery.LookupLocator;
import net.jini.core.lookup.ServiceMatches;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.discovery.DiscoveryEvent;
import net.jini.discovery.DiscoveryListener;
import net.jini.discovery.LookupDiscovery;
import org.apache.log4j.Logger;
import org.objectweb.proactive.core.runtime.jini.JiniRuntime;

/**
  * <p>
  * The <code>ServiceLocatorHelper</code> is a utility class, that takes 
	*	care of creating or discovering the
  * Lookup Service when using JINI.
  * </p>
  *
  * @author  ProActive Team
  * @version 1.0,  2002/09/20
  * @since   ProActive 0.9.3
  *
  */


public class ServiceLocatorHelper implements DiscoveryListener {

protected static Logger logger = Logger.getLogger(ServiceLocatorHelper.class.getName());


  protected static int MAX_RETRY = 8;
  protected static long MAX_WAIT = 10000L;
  
  private static String policy = System.getProperty("user.dir") + System.getProperty("file.separator") + "proactive.java.policy";
  
  private static final String FILE_SEPARATOR = System.getProperty("file.separator");
  private static String DEFAULT_RMID_LOCATION = System.getProperty("java.home")+FILE_SEPARATOR+"bin"+FILE_SEPARATOR+"rmid";
  private static String DEFAULT_RMID_PARAMS = "-J-Djava.security.policy="+policy;

  protected static LookupLocator lookup = null;
  protected static ServiceRegistrar registrar = null;

  /**
   * settings of the service locator
   */
  protected boolean shouldCreateServiceLocator = true;
  protected boolean locatorChecked;
  protected static boolean multicastLocator = false;

  

  private static String host = null;

  static {
    try {
      host = java.net.InetAddress.getLocalHost().getHostName();
    } catch (java.net.UnknownHostException e) {
      logger.fatal("Lookup failed: " + e.getMessage());
      e.printStackTrace();
      System.exit(1);
    }
  }

  private static String grpName = "public";

  private static final String tmpDir = createTempDirectory(host);
  
  private static java.io.File jiniLockFile = null;
  
  private static final String jiniLockFileLocation = System.getProperty("user.dir")+System.getProperty("file.separator")+host+"jiniLockFile";

  //
  // -- Constructors -----------------------------------------------
  //

  public ServiceLocatorHelper() {
		if (logger.isDebugEnabled()) {
    	logger.debug("ServiceLocatorHelper() constructor");
		}
  }

  //
  // -- PUBLIC METHODS -----------------------------------------------
  //

  public boolean shouldCreateServiceLocator() {
    return shouldCreateServiceLocator;
  }

  public void setShouldCreateServiceLocator(boolean v) {
    shouldCreateServiceLocator = v;
  }

  /**
   * true if you want a multicast service Locator
   * false if you want a unicast service Locator
   */
  public void setMulticastLocator(boolean v) {
    ServiceLocatorHelper.multicastLocator = v;
  }

  /**
   * Initialise the service locator for this host
   */
  public synchronized void initializeServiceLocator() {
    if (!shouldCreateServiceLocator) return; // don't bother
    if (locatorChecked) return; // already done for this VM
    try {
    	getOrCreateServiceLocator();
    	//delete the lock file
      if(jiniLockFile != null){
        if(jiniLockFile.exists()) jiniLockFile.delete();
      }
    } catch(java.io.IOException e) {
    	if(jiniLockFile != null){
        if(jiniLockFile.exists()) jiniLockFile.delete();
      }
      e.printStackTrace();
      System.exit(1);
    }
    locatorChecked = true;
  }


  //
  // -- implements DiscoveryListener -----------------------------------------------
  // 

  /**
   * for multicast discover
   */
  public void discovered(DiscoveryEvent evt) {
			//if (logger.isDebugEnabled()) {
			//    //logger.debug(">> Start discover ...");
			//}
    ServiceRegistrar[] registrars = evt.getRegistrars();
//			if (logger.isDebugEnabled()) {
//			    logger.debug(">> > " + registrars.length + " registrars : ");
//			}
    for (int n = 0; n < registrars.length; n++) {
      ServiceLocatorHelper.registrar = registrars[n];
      //displayServices(); // just for test
    }
//if (logger.isDebugEnabled()) {
//    //logger.debug(">> Stop  discover...");
//}
  }

  /**
   * for multicast discover
   */
  public void discarded(DiscoveryEvent evt) {
		//if (logger.isDebugEnabled()) {
		//    //logger.debug(">> discarded ...");
		//}
  }



  //
  // -- PROTECTED METHODS -----------------------------------------------
  // 

  /**
   * Delete recursively all files and directory
   *@param dir The directory to clean
   */
  protected static void delDirectory(java.io.File dir) {
    java.io.File[] files = dir.listFiles();
    if (files != null) {
      for (int i = 0; i < files.length; i++) {
        delDirectory(files[i]);
      }
    }
    logger.info("deleting " + dir.getPath() + " ...");
    dir.delete();
    if (dir.exists()) {
      logger.warn("We cannot delete this file : " + dir.getPath());
      logger.warn("... You should delete it before running a new ServiceLocator ...");
    }
  }


  //
  // -- PRIVATE METHODS -----------------------------------------------
  // 


  /**
   * Display all services on this registrar
   *@param registrar The registrar to contact
   */
  private void displayServices() {
    try {
      // the code takes separate routes from here for client or service

      logger.info(">> found a service locator (registrar) : " + ServiceLocatorHelper.registrar);
      logger.info(">> >> ServiceID : " + ServiceLocatorHelper.registrar.getServiceID());

      logger.info(">> >> >> Groups : ");

      String[] groups = ServiceLocatorHelper.registrar.getGroups();
      for (int i = 0; i < groups.length; i++) {

        logger.info(">> >> >> >> " + i + ") " + groups[i]);

      }

      logger.info(">> >> >> Locator : " + ServiceLocatorHelper.registrar.getLocator());

  
      ServiceTemplate template = new ServiceTemplate(null, new Class[] { JiniRuntime.class }, null);
      ServiceMatches matches = ServiceLocatorHelper.registrar.lookup(template, Integer.MAX_VALUE);

      logger.info(">> >> >> " + matches.items.length + " required ");
      logger.info(">> >> >> " + matches.totalMatches + " founded ");

      for (int i = 0; i < matches.items.length; i++) {

        logger.info(">> >> >> >> Object (" + i + ") found : ");
        logger.info(">> >> >> >> >>        ID : " + matches.items[i].serviceID);
        logger.info(">> >> >> >> >>   Service : " + matches.items[i].service);
        logger.info(">> >> >> >> >> Attributs :");

        for (int j = 0; j < matches.items[i].attributeSets.length; j++) {

          logger.info(">> >> >> >> >> >> Attr : " + matches.items[i].attributeSets[j]);

        }

        logger.info("--------------------------------------------------------------------------------------");

      }
    } catch (java.rmi.RemoteException e) {
      e.printStackTrace();
    }
  }


  private static String createTempDirectory(String host) {
    try {
      java.io.File fTmp = java.io.File.createTempFile("proactive-", "-" + host);
      String tmpDirPath = fTmp.getAbsolutePath();
			//if (logger.isDebugEnabled()) {
			//      //logger.debug(">> TEMP directory = " + tmpDirPath);
			//}
      return tmpDirPath;
    } catch (Exception e) {
      logger.fatal("Cannot create the TEMP directory : " + e.toString());
      e.printStackTrace();
      System.exit(1);
      return null;
    }
  }

  /**
   * Try to get the Service Locator on the local host (unicast search)
   * Create it if it doesn't exist 
   */
  private void getOrCreateServiceLocator() throws java.io.IOException {
    if (System.getSecurityManager() == null) {
      System.setSecurityManager(new java.rmi.RMISecurityManager());
    }
    if (multicastLocator) {
      // For multicast
      LookupDiscovery discover = new LookupDiscovery(LookupDiscovery.ALL_GROUPS);
      discover.addDiscoveryListener(this);
      // stay around long enough to receive replies
      try {
        Thread.sleep(MAX_WAIT);
        if (ServiceLocatorHelper.registrar == null) createServiceLocator();
      } catch (InterruptedException e) {}

    } else {
      // For unicast on `host`
		
      logger.info("Lookup : jini://" + host);
      try {
        lookup = new LookupLocator("jini://" + host);
        logger.info("Lookup.getRegistrar() on " + host);
        ServiceLocatorHelper.registrar = lookup.getRegistrar();
      } catch (java.net.MalformedURLException e) {
        throw new java.io.IOException("Lookup failed: " + e.getMessage() );
      } catch (java.io.IOException e) {
        logger.error("Registrar search failed: " + e.getMessage() );
        if (MAX_RETRY-- > 0) {
        	//-----------wont work everywhere---------------------------
        	
        	String[] env = new String[2];
        	
        	env[0]=DEFAULT_RMID_LOCATION;
        	env[1]=DEFAULT_RMID_PARAMS;
        	Runtime.getRuntime().exec(env);
        	
          createServiceLocator();
          getOrCreateServiceLocator();
        } else {
        	//delete the lock file
      		if(jiniLockFile.exists()) jiniLockFile.delete();
          throw new java.io.IOException("\nCannot run a ServiceLocator : Have you launched the rmid deamon on " + host);
        }
      } catch (java.lang.ClassNotFoundException e) {
      	if(jiniLockFile.exists()) jiniLockFile.delete();
        throw new java.io.IOException("Registrar search failed: " + e.toString());
      }

      logger.info("Registrar found on " + host);
      
      // Just for test
      //displayServices();
    }
  }

  /**
   * Create a new Service Locator on the local host
   */
  private static void createServiceLocator() {
  	//this block is usefull to avoid many ServiceLocator to be created by different
  	//threads at the same time.If the file cannot be created, it is because another thread
  	//put a lock on it, in other word it is trying to create the serviceLocator
  	//so wait a bit for the service to be created 
  	if(!createLockFile()) {
  		try{
  			Thread.sleep(2000);
  		}catch(Exception e){
  				e.printStackTrace();
  			}
  		return;
  	}
  	logger.info("creating lock file");
    logger.info("No ServiceLocator founded ...  we launch a ServiceLocator on " + host);
    String reggieTmpDir = tmpDir + System.getProperty("file.separator") + "reggie_log";
    delDirectory(new java.io.File(tmpDir));
    java.io.File directory = new java.io.File(tmpDir);
    directory.mkdirs();

		if (logger.isDebugEnabled()) {
    	//logger.debug("We use the ClassServer : "+httpserver);
   	 logger.debug("We don't use a ClassServer for the service Locator (we use the CLASSPATH)");
		}

    //String[] args = { httpserver , policy , reggieTmpDir,};
//    String classpath = System.getProperty("java.class.path");
//    try{
//    java.io.File canonicalFile = new java.io.File(classpath);
//	    classpath = canonicalFile.getCanonicalPath();
//    }catch (Exception e){
//    	e.printStackTrace();
//    	
//    }

    String[] args = { "", policy, reggieTmpDir, };
    com.sun.jini.start.ServiceStarter.create(args, "com.sun.jini.reggie.CreateLookup", "com.sun.jini.reggie.RegistrarImpl", "lookup");
  }

	/**
	 * Method createLockFile.
	 * @param host
	 * @return String
	 */
	private static boolean createLockFile()
	{
	 jiniLockFile = new java.io.File(jiniLockFileLocation);
		//jiniLockFile.deleteOnExit();
		try{
		return jiniLockFile.createNewFile();
		}catch(java.io.IOException e){
			//an exception occured try anyway to create the service locator
			return true;
		}
	}



}

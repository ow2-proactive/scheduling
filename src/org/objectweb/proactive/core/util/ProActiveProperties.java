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
package org.objectweb.proactive.core.util;

import java.util.Enumeration;
import java.util.Properties;

public final class ProActiveProperties {

    public static final String PROACTIVE_DEFAULT_REQUESTRECEIVER = "proactive.requestreceiver";  
    public static final String PROACTIVE_DEFAULT_MIGRATIONMANAGER = "proactive.migrationmanager";
    public static final String PROACTIVE_DEFAULT_LOCATIONSERVER = "proactive.locationserver";
    /**
     * The rmi name of the location server
     */
  public static final String PROACTIVE_DEFAULT_LOCATIONSERVER_RMI = "proactive.locationserver.rmi";
  private static Properties defaultProperties;


  static {
    defaultProperties = new Properties();
    ProActiveProperties.loadDefaultProperties();
  }


  public static void loadDefaultProperties() {
    defaultProperties.setProperty(PROACTIVE_DEFAULT_REQUESTRECEIVER, "org.objectweb.proactive.core.body.request.RequestReceiverImpl"); 
    defaultProperties.setProperty(PROACTIVE_DEFAULT_MIGRATIONMANAGER, "org.objectweb.proactive.core.body.migration.MigrationManagerImpl");
    defaultProperties.setProperty(PROACTIVE_DEFAULT_LOCATIONSERVER, "org.objectweb.proactive.ext.locationserver.LocationServer");
    defaultProperties.setProperty(PROACTIVE_DEFAULT_LOCATIONSERVER_RMI, "//localhost/LocationServer");
    ProActiveProperties.addPropertiesToSystem(defaultProperties);

  }


  /**
   * Add a set of properties to the system properties
   * Does not overide any existing one
   *
   */
  protected static void addPropertiesToSystem(Properties p) {
    for (Enumeration e = p.propertyNames(); e.hasMoreElements();) {
      String s = (String)e.nextElement();
      //we don't override existing value
      if (System.getProperty(s) == null) {
        System.setProperty(s, p.getProperty(s));
      }
    }
  }


  public static String getRequestReceiverClass() {
    return System.getProperties().getProperty(ProActiveProperties.PROACTIVE_DEFAULT_REQUESTRECEIVER);
  }

  public static String getMigrationManagerClass() {
    return System.getProperties().getProperty(ProActiveProperties.PROACTIVE_DEFAULT_MIGRATIONMANAGER);
  }


  public static String getLocationServerClass() {
    return System.getProperties().getProperty(ProActiveProperties.PROACTIVE_DEFAULT_LOCATIONSERVER);
  }


  public static String getLocationServerRmi() {
    return System.getProperties().getProperty(ProActiveProperties.PROACTIVE_DEFAULT_LOCATIONSERVER_RMI);
  }
}

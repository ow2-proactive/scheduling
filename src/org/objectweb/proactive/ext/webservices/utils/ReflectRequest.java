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
package org.objectweb.proactive.ext.webservices.utils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;


/**
 * @author jbroccol
 */
public abstract class ReflectRequest {
	
	private static Logger logger = Logger.getLogger("XML_HTTP");
	
	/**
	 * init the hashmap, that contains all the methods of  ProActiveRuntimeImpl 
	 * @param theclass we want to get all the methods
	 * @return an HasMap containing methods and parameters
	 */
    protected static HashMap getHashMapReflect(Class theclass) {
        // init the hashmap, that contains all the methods of  ProActiveRuntimeImpl 
        // in 'Object' (value) and the name of funtions in key 
        // (Warning two functions can t have the same name (for now)) 
        Method[] allmethods = theclass.getMethods();
        int numberOfMethods = allmethods.length;
        HashMap hMapMethods = new HashMap(numberOfMethods);

        for (int i = 0; i < numberOfMethods; i++) {
            String methodname = allmethods[i].getName();

            if (hMapMethods.containsKey(methodname)) {
                Object obj = hMapMethods.get(methodname);

                if (!(obj instanceof ArrayList)) {
                    ArrayList array = new ArrayList();
                    array.add((Method) obj);
                    array.add(allmethods[i]);
                    hMapMethods.put(methodname, array);
                } else {
                    ((ArrayList) obj).add(allmethods[i]);
                    hMapMethods.put(methodname, (ArrayList) obj);
                }
            } else {
                hMapMethods.put(methodname, allmethods[i]);
            }
        }

        return hMapMethods;
    }
    
    /**
     * Get 
     * @param methodsearch The searched method
     * @param paramsearch The parameters of this method
     * @param hashobjet  ???
     * @return a Method representing the method in the ProActiveRuntime
     */
    protected Method getProActiveRuntimeMethod(String methodsearch, ArrayList paramsearch,Object hashobjet){
  	      	
    	Object mret =  hashobjet;
  	  
  	  if(mret instanceof ArrayList) {
  	  	
  	  	ArrayList allSameMethod = (ArrayList) ((ArrayList)mret).clone();
  	  	
  	  	int sameMethodSize = allSameMethod.size();
  	  	int paramsearchsize = paramsearch.size();
  	 	for(int i=sameMethodSize-1;i>=0;i--) {

  	  		if( ((Method)allSameMethod.get(i)).getParameterTypes().length != paramsearchsize) 
  	  			allSameMethod.remove(i);
  	 	}	

    		sameMethodSize = allSameMethod.size();
  	 	if( sameMethodSize == 1)
  	 		mret=allSameMethod.get(0);
  	  	else { 

  	  		Class [] paramtypes = null;
  	  		boolean isgood = true, ispossible = true;
  	  		for(int i=sameMethodSize-1;i>=0;i--)	{
  	  			paramtypes = ((Method)allSameMethod.get(i)).getParameterTypes();
  	  			
  	  			for(int j=0;j<paramsearchsize;j++) {
  	  				
  	  				Class classtest = paramsearch.get(j).getClass();
  	  				if( paramtypes[j] != classtest ){
  	  					isgood = false;
  	  					if( classtest.isAssignableFrom(paramtypes[j]) == false){
  	  						ispossible = false;
  	  						break;
  	  					}
  	  				}
  	  			}
  	  			if( isgood == true ){
  	  				mret=allSameMethod.get(i);
    					break;
  				}
  	  			else if( ispossible == false )
  	  				allSameMethod.remove(i);
  	  				
  	  			isgood = true;
  	  			ispossible = true;
  	  			}
  	  		
  	  		}		
  	  		
  	 	if( allSameMethod.size() == 1 )
  	 		mret=allSameMethod.get(0);
  	 	else {
	  				
			logger.error("----------------------------------------------------------------------------");
			logger.error("----- ERROR : two functions in ProActiveRuntimeImpl can t have the same name");
			logger.error("----- ERROR : and the same type of paramters (Extends Implements)");
			logger.error("----- search   : "+methodsearch+" nb param "+paramsearch.size());
			logger.error("----------------------------------------------------------------------------");
			
  	 	}
  	  }
  	  else if ( mret == null ) {
  	  	
		logger.error("----------------------------------------------------------------------------");
		logger.error("----- ERROR : no method (invoke) find ");
		logger.error("----- search   : "+methodsearch+" nb param "+paramsearch.size());
		logger.error("----------------------------------------------------------------------------");
  	  	
  	  }
  
  	  return (Method)mret;	
  }
  
  
 
    
    
    
    
    
    
    
}

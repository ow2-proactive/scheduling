/*
 * Created on Aug 6, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.objectweb.proactive.ext.webservices.utils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;


/**
 * @author jbroccol
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public abstract class ReflectRequest {
	
	private static Logger logger = Logger.getLogger("XML_HTTP");
	
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

/***
 * Fractal ADL Parser
 * Copyright (C) 2002-2004 France Telecom R&D
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Contact: Eric.Bruneton@rd.francetelecom.com
 *
 * Author: Eric Bruneton
 */
package org.objectweb.proactive.examples.pi;

import java.util.HashMap;
import java.util.List;

import org.objectweb.fractal.api.control.BindingController;

/**
 * This class contains code for managing the master component in the component version of the pi application.
 * @author ProActive team
 *
 */
public class PiBBPWrapper extends PiBBP implements MasterComputation, BindingController {
    
    HashMap nameToComputer = new HashMap(); // map between binding names and Components 
    PiCompMultiCast clientMultiCast;
    public PiBBPWrapper() {
    }


  
  public String[] listFc() {
      return new String[] { "multicastDispatcher" };
  }

   
    public Object lookupFc(final String cItf) {
    	if(cItf.compareTo("multicastDispatcher")==0)
    		return clientMultiCast;
    	return null;
    }

    public void bindFc(final String cItf, final Object sItf) {

        if (cItf.startsWith("multicastDispatcher")) {
           clientMultiCast=(PiCompMultiCast)sItf;
        }
    }
 
    public void unbindFc(final String cItf) {
    	if (cItf.startsWith("multicastDispatcher")) {
            clientMultiCast=null;
         }
    }
   
	public boolean computePi(List<Interval> params) {
			
		long timeAtBeginningOfComputation = System.currentTimeMillis();
		   
		/*Call on the client multicast interface. 
		 * Due to the dispatching policy of the client multicast interface, each item of the param list is sent to one "pi computer"*/   
		List<Result> results=clientMultiCast.compute(params);
		
		System.out.println("Intervals sent to the computers...\n");
		/*The different resluts are gathered to make the final result*/
        Result total = PiUtil.conquerPIList(results);

        
        
        long timeAtEndOfComputation = System.currentTimeMillis();

        //*************************************************************
        // * results
        // *************************************************************/
        System.out.println("\nComputation finished ...");
        System.out.println("Computed PI value is : " +
            total.getNumericalResult().toString());
        System.out.println("Time waiting for result : " +
            (timeAtEndOfComputation - timeAtBeginningOfComputation) + " ms");
        System.out.println("Cumulated time from all computers is : " +
            total.getComputationTime() + " ms");
        System.out.println("Ratio for " + results.size() +
            " processors is : " +
            (((double) total.getComputationTime() / ((double) (timeAtEndOfComputation -
            timeAtBeginningOfComputation))) * 100) + " %");
        
        System.out.println(total.getNumericalResult().toString());
		
		
		
		
		return true;
		
		
		
	}
}

/*
 * Created on Apr 7, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.objectweb.proactive.core.body.http;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.ext.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.ext.webservices.utils.ProActiveXMLUtils;

import java.io.IOException;


/**
 * @author vlegrand
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class HttpRequest implements HttpMessage {
    private Request request;
    private UniqueID IdBody;
 

    public HttpRequest(Request request, UniqueID idBody) {
        this.request = request;
        this.IdBody = idBody;
    }



    public Object  processMessage() {
    	if (this.request != null) {
    	try {
        	Body body = ProActiveXMLUtils.getBody(IdBody);

        	///////////// multyiple migrastion bug
        	for(int i=0;i<100;i++){
        		if(body == null){
        			body = ProActiveXMLUtils.getBody(IdBody);
        		}
        		else
        			break;
        			
        		if( (i%3) == 0)
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
        		
       	}	 
                body.receiveRequest(this.request);
          
        } catch (IOException e) {
         
            e.printStackTrace();
        } catch (RenegotiateSessionException e) {
       
            e.printStackTrace();
        }
    	}
        return null;
    }
}

/*
 * Created on Jul 15, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.objectweb.proactive.core.body.http;

import java.io.IOException;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.ext.webservices.utils.ProActiveXMLUtils;

/**
 * @author vlegrand
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class HttpReply implements HttpMessage{
	
	  private Reply reply;
	    private UniqueID  idBody;
	 

	    public HttpReply(Reply reply , UniqueID idBody) {
	        this.reply = reply;
	        this.idBody= idBody;
	    }



	    public Object  processMessage() {
	        try {
	        	Body body = ProActiveXMLUtils.getBody(idBody);
	            if (this.reply != null) 
	                body.receiveReply(this.reply);
	          
	        } catch (IOException e) {
	         
	            e.printStackTrace();
	        } 
	    
	    return null;
	    }
}



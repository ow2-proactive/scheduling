/*
 * Created on Jun 1, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.objectweb.proactive.ext.webservices.soap.test;

import java.rmi.RemoteException;




import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;





 
/** 
 * @author vlegrand
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class TestSoap {

public static void main (String [] args)  {

	String endpt = "http://noadcoco.inria.fr:8080/soap/servlet/rpcrouter";
	Service service = new Service();
	
	try {
		
		Call call = (Call)service.createCall();
	call.setTargetEndpointAddress(endpt);
	
	call.setOperationName(new QName("helloPA", "sayHello"));
	//call.addParameter("ttoto",org.apache.axis.encoding.XMLType.XSD_STRING,javax.xml.rpc.ParameterMode.IN);
	call.setProperty("namespace","helloPA");
	call.setSOAPActionURI("helloPA");
	System.out.println("call = " + call)	;
	String ret = (String)call.invoke(new Object []{});
	System.out.println("ret = " + ret)
;	} catch (ServiceException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (RemoteException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
}
}

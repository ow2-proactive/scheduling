package org.objectweb.proactive.examples.descriptor;

public class MiniDescrActive {
	public Message getComputerInfo(){
		try {
			return new Message(java.net.InetAddress.getLocalHost() + "");
		}
		catch(Exception e){
			return new Message("java.net.InetAddress.getLocalHost() IMPOSSIBLE");   
		}
	}
}

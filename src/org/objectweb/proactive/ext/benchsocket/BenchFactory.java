package org.objectweb.proactive.ext.benchsocket;

import java.util.ArrayList;
import java.util.Iterator;

public class BenchFactory {
	
	protected static ArrayList streamList = new ArrayList();
	
	protected void addStream(BenchStream s) {
		synchronized (streamList) {
			streamList.add(s);
		}
	}
	
	public static void dumpStreamIntermediateResults() {
		synchronized (streamList) {
			Iterator it = streamList.iterator();
			while (it.hasNext()) {
				((BenchStream) it.next()).dumpIntermediateResults();
			}
		}
	}

}

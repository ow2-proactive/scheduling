/*
 * Created on Jul 29, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.objectweb.proactive.ic2d.util;

import java.io.IOException;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeImpl;
import org.objectweb.proactive.core.runtime.http.HttpRuntimeAdapter;


/**
 * @author vlegrand
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class HttpHostNodeFinder implements HostNodeFinder {
	private IC2DMessageLogger logger;
	
	public HttpHostNodeFinder (IC2DMessageLogger logger) {
        this.logger = logger;
    }
	
	/* (non-Javadoc)
	 * @see org.objectweb.proactive.ic2d.util.HostNodeFinder#findNodes(java.lang.String)
	 */
	public Node[] findNodes(String host) throws IOException {
		Node [] nodes = null ;
		System.out.println("recherche des noeuds http sur : " + host);
		if (!host.startsWith("//"))
			host = "//" + host;
		HttpRuntimeAdapter adapter = new HttpRuntimeAdapter(host);
		try {
			String [] list = adapter.getNodesNames();
			
			System.out.println("-- > " + list.length);
			nodes  = new  Node [list.length];
			for (int i= 0 ; i< list.length ; i++) {
				String url = host + '/' + list[i];
				
				
                  nodes[i] = (new NodeImpl(adapter, url, "http",
                          adapter.getJobID(url)));
                  System.out.println(nodes[i].getNodeInformation().getName());
			}
		} catch (ProActiveException e) {
			e.printStackTrace();
		}
		
		return nodes;
	}
}

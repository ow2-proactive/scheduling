package org.objectweb.proactive.extra.infrastructuremanager.test.simple;

import java.util.Comparator;

import org.objectweb.proactive.extra.infrastructuremanager.dataresource.IMNode;

public class ComparatorIMNode implements Comparator {

	public int compare(Object arg0, Object arg1) {
		//System.out.println("compare");
		IMNode imnode1 = (IMNode) arg0;
		IMNode imnode2 = (IMNode) arg1;
		//System.out.println("compare : " + imnode1.getNodeName() + " with " + imnode2.getNodeName());
		if( imnode1.getPADName().equals(imnode2.getPADName())) {
			if( imnode1.getVNodeName().equals(imnode2.getVNodeName()) ) {
				if( imnode1.getHostName().equals(imnode2.getHostName()) ) {
					if( imnode1.getDescriptorVMName().equals(imnode2.getDescriptorVMName()) ) {
						return imnode1.getNodeName().compareTo(imnode2.getNodeName());
					}
					else {
						return imnode1.getDescriptorVMName().compareTo(imnode2.getDescriptorVMName());
					}
				}
				else {
					return imnode1.getHostName().compareTo(imnode2.getHostName());
				}
			}
			else {
				return imnode1.getVNodeName().compareTo(imnode2.getVNodeName());
			}
		}
		else {
			return imnode1.getPADName().compareTo(imnode2.getPADName());
		}
	}

}

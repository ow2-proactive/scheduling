/*
 * ################################################################
 * 
 * ProActive: The Java(TM) library for Parallel, Distributed, Concurrent
 * computing with Security and Mobility
 * 
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis Contact:
 * proactive-support@inria.fr
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Initial developer(s): The ProActive Team
 * http://www.inria.fr/oasis/ProActive/contacts.html Contributor(s):
 * 
 * ################################################################
 */
package org.objectweb.proactive.calcium;

import org.objectweb.proactive.calcium.Stream;
import org.objectweb.proactive.calcium.ResourceManager;
import org.objectweb.proactive.calcium.Skernel;
import org.objectweb.proactive.calcium.interfaces.Skeleton;
import org.objectweb.proactive.calcium.statistics.StatsGlobal;

/**
 * This class corresponds to the entry point of the skeleton framework.
 * 
 * In order to instantiate this class, a resource Manager must be provided.
 * This Manager must extend the AbstractManager class. The skeleton
 * kernel can be used with different Managers, for example: Monothreaded, Multihreaded
 * or Distributed (ProActive).
 * 
 * 
 * @author The ProActive Team (mleyton)
 *
 */
public class Calcium {

	private Facade facade;
	private Skernel skernel;
	private ResourceManager manager;

	
	public Calcium(ResourceManager manager){
		this.skernel=new Skernel();
		this.facade = new Facade();
		this.manager=manager;
	}
	
	/**
	 * This method is used to instantiate a new stream from the framework.
	 * The stream is then used to input T into the framework, and
	 * then retrieve the results (T) from the framework.
	 * 
	 * All the T inputed into this stream will be computed using the 
	 * skeleton strucutre specified as parameter.
	 * 
	 * @param <T> The type of the T this stream will work with.
	 * @param root This skeleton represents the structured code that will 
	 * be executed for each T inputted into the stream.
	 * @return A Stream that can input and output T from the framework.
	 */
	public <T> Stream<T> newStream(Skeleton<T> root){
		
		return new Stream<T>(facade, root);
	}

	public void boot() {

		skernel=manager.boot(skernel);
		facade.setSkernel(skernel);
	}

	public void shutdown() {
		manager.shutdown();
	}
	
	/**
	 * @return The current status of the global statistics.
	 */
	public StatsGlobal getStatsGlobal() {
		return skernel.getStatsGlobal();
	}
}

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
package org.objectweb.proactive.calcium.examples.blast;

import java.net.URL;

import org.apache.log4j.Logger;
import org.objectweb.proactive.calcium.exceptions.MuscleException;
import org.objectweb.proactive.calcium.exceptions.EnvironmentException;
import org.objectweb.proactive.calcium.interfaces.Execute;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;

public class ExecuteFormatDB extends AbstractExecuteCommand implements Execute<BlastParameters> {
	static Logger logger = ProActiveLogger.getLogger(Loggers.SKELETONS_APPLICATION);
	
	public BlastParameters execute(BlastParameters param) throws EnvironmentException {
		
		if(logger.isDebugEnabled()){
			logger.debug("Formating database file:"+param.getDatabaseFile().getAbsolutePath());
		}

		super.execProcess(param.getFormatDBString(), param.getWorkingDirectory());

		return param;
	}

	@Override
	public URL getProgramURL() throws EnvironmentException, MuscleException{

		String osName = System.getProperty("os.name");
		
		if(!osName.equals("Linux")){
			throw new EnvironmentException("Linux machines are required");
		}
		
		URL url=  Blast.class.getClass().getResource("/org/objectweb/proactive/calcium/examples/blast/bin/linux/formatdb");
		
		if(url==null){
			throw new MuscleException("Unable to find formatdb binary");
		}

		return url;
	}
}

/*
 * Created on Jul 13, 2004
 * author : Matthieu Morel
  */
package org.objectweb.proactive.core.component.adl;

import java.util.HashMap;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Factory;

/**
 * @author Matthieu Morel
 */
public class FactoryFactory {
	

		public final static String PROACTIVE_BACKEND =   "org.objectweb.proactive.core.component.adl.ProActiveBackend";

		  private FactoryFactory()
		  {
		  }

		  /**
		   * Returns a dream factory.
		   * 
		   * @see org.objectweb.fractal.adl.FactoryFactory#getFactory(java.lang.String,
		   *      java.lang.String, java.util.Map)
		   */
		  public static Factory getFactory() throws ADLException
		  {
		    return org.objectweb.fractal.adl.FactoryFactory.getFactory(PROACTIVE_BACKEND, new HashMap());
		  }
}

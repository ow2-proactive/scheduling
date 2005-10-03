package org.objectweb.proactive.core.process.glite;

import java.io.Serializable;
import java.text.ParseException;

import org.glite.wms.jdlj.JobAd;/* /lib/glite/glite-wms-jdlj.jar */
import org.glite.wms.jdlj.JobAdException;

/**
 * 
 * This class allows to use a serializable version of JobAd class.
 *
 */
public class GLiteJobAd extends JobAd implements Serializable{
	
	public GLiteJobAd () {
		super();
	}
	
	public GLiteJobAd(String ad) throws ParseException, JobAdException {
		super(ad);
	}
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}

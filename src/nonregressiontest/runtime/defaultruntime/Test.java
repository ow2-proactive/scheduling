package nonregressiontest.runtime.defaultruntime;

import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.RuntimeFactory;

import testsuite.test.FunctionalTest;

/**
 * @author rquilici
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class Test extends FunctionalTest
{
	
	ProActiveRuntime part;
	
	public Test(){
		super("defaultruntimecreation","Test default runtime creation");
	}

	/**
	 * @see testsuite.test.FunctionalTest#action()
	 */
	public void action() throws Exception
	{
		part = RuntimeFactory.getDefaultRuntime();
	}

	/**
	 * @see testsuite.test.AbstractTest#initTest()
	 */
	public void initTest() throws Exception
	{
	}

	/**
	 * @see testsuite.test.AbstractTest#endTest()
	 */
	public void endTest() throws Exception
	{
	}
	
	public boolean postConditions() throws Exception{
		return (part != null);
	}

}

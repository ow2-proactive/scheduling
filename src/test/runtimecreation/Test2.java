package test.runtimecreation;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.RuntimeFactory;

/**
 * @author rquilici
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class Test2
{

	public static void main(String[] args)
	{
		ProActiveRuntime PART;
		try
		{
			PART = RuntimeFactory.getRuntime("//palliata/PART","rmi");
			System.out.println("get ok");
			System.out.println(PART.getURL());
		}
		catch (ProActiveException e)
		{
		}
		
	}
}

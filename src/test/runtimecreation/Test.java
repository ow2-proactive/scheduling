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
public class Test
{

	public static void main(String[] args)
	{	
		try
		{
			ProActiveRuntime part = RuntimeFactory.getDefaultRuntime();	
		}
		catch (Exception e)
		{
		}
			
//			ProActiveRuntime toto;
//			try
//			{
//				toto = RuntimeFactory.getRuntime("//palliata/renderer");
//				part.register(toto,"renderer","toto","titi");
//			}
//			catch (ProActiveException e)
//			{
//			}
			
			
			
//			try
//			{
//				Thread.sleep(10000);
//				System.out.println(part.getProActiveRuntime("PART_renderer").getURL());
//				System.out.println(part.getProActiveRuntime("PART_dispatcher").getURL());
//			}
//			catch (Exception e)
//			{
//				e.printStackTrace();
//			}
					
	}
}

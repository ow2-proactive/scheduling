package nonregressiontest;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.SimpleLayout;

import testsuite.group.Group;
import testsuite.manager.FunctionalTestManager;
import testsuite.manager.ProActiveFuncTestManager;

/**
 * @author rquilici
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class MainManager extends ProActiveFuncTestManager
{

	/**
	 * Constructor for MainManager.
	 */
	public MainManager()
	{
		this("Main unit test manager","Manage all unit non-regression tests");
		logger.addAppender(new ConsoleAppender(new SimpleLayout()));
	}

	/**
	 * Constructor for MainManager.
	 * @param name
	 * @param description
	 */
	public MainManager(String name, String description)
	{
		super(name, description);
	}

	/**
	 * @see testsuite.manager.AbstractManager#initManager()
	 */
	public void initManager() throws Exception
	{
		Group testGroup = new Group("Unit test group","group of unit tests",new File("/net/home/rquilici/ProActive/classes/"),"nonregressiontest",null,false);
		add(testGroup);
	}

	/**
	 * @see testsuite.manager.AbstractManager#endManager()
	 */
	public void endManager() throws Exception
	{
	}
	
	public static void main(String[] args){
		MainManager manager = new MainManager();
		manager.execute(false);
 		manager.setVerbatim(true);
 		try
		{
			manager.toHTML(new File("/net/home/rquilici/test.html"));
		}
		catch (ParserConfigurationException e)
		{
			e.printStackTrace();
		}
		catch (TransformerException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
 		System.exit(0);
	}

}

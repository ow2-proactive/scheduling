package org.objectweb.proactive.examples.scheduler;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.objectweb.proactive.extra.scheduler.SchedulerUserAPI;

public class HelloRet {
	public static void main(String[] args)  {
		SchedulerUserAPI scheduler=null;
		try
		{

			String SNode;
			if(args.length==1)
				SNode=args[0];
			else SNode="//localhost/SCHEDULER_NODE";
			scheduler=SchedulerUserAPI.connectTo(SNode);
			
			InputStreamReader reader = new InputStreamReader (System.in);

		    // Wrap the reader with a buffered reader.
		    BufferedReader buf = new BufferedReader (reader);
		    String tID;
		    System.out.println("Please enter the task id to get it and 'stop' to ecit");
		    while(!(tID=buf.readLine()).equals("stop"))
		    {
		    	if(scheduler.isFinished(tID).booleanValue())
		    		System.out.println(scheduler.getResult(tID, System.getProperty("user.name")).getProActiveTaskExecutionResult().getObject());
		    	else System.out.println("Not finished or doesnt exist");
		    	
		    }
	
		}
		catch(Exception e)
		{
			System.out.println("Error:"+e.getMessage()+" will exit");
			System.exit(1);
		}

			
				
			System.exit(0);
		
	}
	

}

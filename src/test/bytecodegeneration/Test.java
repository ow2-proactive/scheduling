package test.bytecodegeneration;

import org.objectweb.proactive.core.mop.*;

public class Test
{
    public static void main (String[] args)
    {
	System.out.println ("Testing on-the-fly generation of stub classes in bytecode form");
	try
	    {
		A a = (A) MOP.newInstance ("test.bytecodegeneration.A", new Object[0], "org.objectweb.proactive.core.mop.ProxyOne", new Object[0]);	
		a.sayHello ();
		
	    }
	catch (Exception e)
	    {
		e.printStackTrace();
	    }
    }
}

package org.objectweb.proactive.ext.util;

import java.io.*;
import java.util.StringTokenizer;
import org.objectweb.proactive.core.mop.BytecodeStubBuilder;

public class StubGenerator
{
    public static void main (String[] args)
    {
	// Check number of arguments
	if (args.length <= 0)
	    {
		printUsageAndExit();
	    }

	// Name of the class
	String classname = args[0];

	// Deals with directory name
	String directoryName;
	if (args.length == 2)
	    {
		directoryName = args [1];
	    }
	else
	    {
		directoryName = ".";
	    }
	// If the directory name does not end with a file separator, add one
	if (!directoryName.endsWith (System.getProperty ("file.separator")))
	    {
		directoryName = directoryName + System.getProperty ("file.separator");		
	    }
	char sep = System.getProperty ("file.separator").toCharArray() [0];
	String fileName = directoryName + classname.replace ('.', sep) + ".class";

	try
	    {
		// Generates the bytecode for the class
		BytecodeStubBuilder bsb = new BytecodeStubBuilder(classname);
		byte[] data = bsb.create();

		// And writes it to a file
		new File (fileName.substring (0, fileName.lastIndexOf (sep))).mkdirs();
		//	String fileName = directoryName + System.getProperty ("file.separator") + 
		File f = new File (fileName);
		FileOutputStream fos = new FileOutputStream (f);
		fos.write(data);
		fos.flush();
		fos.close();
		System.out.println ("Wrote file "+fileName);
	    }
	catch (ClassNotFoundException e)
	    {
		System.err.println ("Cannot find class "+classname);	
	    }
	catch (Exception e)
	    {
		System.err.println ("Cannot write file "+fileName);
		System.err.println ("Reason is "+e);
	    }
    }

    public static void printUsageAndExit ()
    {
	System.out.println ("usage: pac <fully-qualified name of the class> [directory where to output stub .class file]");
	System.exit (0);
    }

}

package test.rsh;

import org.objectweb.proactive.core.process.rsh.RSHJVMProcess;

public class Test {
    

    public static void main (String[] args) {
	if (args.length<3) {
	    System.err.println("Usage: java " + Test.class.getName() + " <number> <hostname> <length> ");
	    System.exit(0);
	} // end of if (args.length<2)
	
	int max = Integer.parseInt(args[0]);
	RSHJVMProcess[] rsh = new RSHJVMProcess[max];
	for (int i=0;i<max;i++) {
	    // rsh[i]=new RSHJVMProcessImpl(new AbstractExternalProcess.StandardOutputMessageLogger());
	    rsh[i]=new RSHJVMProcess();
	    rsh[i].setHostname(args[1]);
	    rsh[i].setClassname(Speaker.class.getName());
	    rsh[i].setParameters((new Integer(i)).toString());
	    try {
		rsh[i].startProcess(); 
	    } catch (Exception e) {
		e.printStackTrace();
	    } // end of try-catch	 
	} // end of for (int i=0;i<max;i++)

	try {
	    Thread.sleep(Integer.parseInt(args[2]));
	} catch (Exception e) {
	    e.printStackTrace();
	} // end of try-catch
	System.out.println("Test: finishing");
	for (int i=0;i<max;i++) {
	    rsh[i].stopProcess();
	}
    } // end of main ()
}

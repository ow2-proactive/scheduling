package modelisation.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class NodeProcess {

    private static final String NODEOK = "Node successfully bound";
  //   protected String javaPath;
//     protected String className;
//     protected String classPath;
//     protected String nodeName;
//     protected String hostName;
//     protected String redirect;


    protected String commandLine;

    protected Process p;
    ThreadedReader r1, r2;
    // protected boolean running;
    protected BufferedReader d_error;
    // protected BufferedReader d_standard;
    protected static final String JAVA_NESSIE = "/u/dea_these/fhuet/solaris/j2re1_3_0_02/bin/java  -Xmx256m ";
    protected static final String CLASSPATH_NESSIE = "/u/dea_these/fhuet/java/classes";
    protected static final String JAVA_SATURA = " /u/satura/0/oasis/fhuet/linux/bin/jdk1.3.0_02/bin/java";
    protected static final String CLASSPATH_SATURA = "/u/satura/0/oasis/fhuet/java/classes";


    // public NodeProcess(String className, String javaPath, String classPath, String nodeName, String hostName, String file) {
// 	this.className = className;
// 	this.javaPath = javaPath;
// 	this.classPath = classPath;
// 	this.nodeName = nodeName;
// 	this.hostName = hostName;
// 	this.redirect=file;
//     }

    public NodeProcess(String commandLine)
    {
	this.commandLine=commandLine;	
    }

 //    public boolean initialise() {
// 	String s = null;
// 	try {
// 	    System.out.println("NodeProcess: hostname is " + hostName);
// 	    if (hostName.equals("polya")) {
// 		System.out.println("Starting on polya " + " rsh -n -l salouf " + hostName + " " + AutonomousBench.JAVA + " -classpath  " + classPath + className + " " + nodeName + " > " +this.redirect);

// 		p = Runtime.getRuntime().exec(" rsh -n -l salouf " + hostName + " " + AutonomousBench.JAVA + " -classpath  " + classPath + " " + className + " " + nodeName + " > " +this.redirect);
// 	    }
// 	    if (hostName.equals("satura")) {
// 		System.out.println("Starting on " + this.removePortNumber(hostName));
// 		p = Runtime.getRuntime().exec(" rsh -n " + this.removePortNumber(hostName) + " " + this.JAVA_SATURA + " -classpath  " + CLASSPATH_SATURA + " " + className + " " + nodeName);
// 	    }
// 	    if (hostName.indexOf("nessie.essi.fr") >= 0) {
// 		System.out.println("XXStarting on " + this.removePortNumber(hostName));
// 		p = Runtime.getRuntime().exec(" rsh -n " + this.removePortNumber(hostName) + " " + this.JAVA_NESSIE + " -classpath  " + CLASSPATH_NESSIE + " " + className + " " + nodeName);
// 	    }
// 	    if (hostName.equals("tuba")) {
// 		System.out.println("Starting on " + this.removePortNumber(hostName) + " (tuba)");
// 		System.out.println("command is " + " rsh -n " + this.removePortNumber(hostName) + " " + this.javaPath + " -classpath  " + classPath + " " + className + " " + nodeName + " > " +this.redirect);

// 		p = Runtime.getRuntime().exec(" rsh -n " + this.removePortNumber(hostName) + " " + this.javaPath + " -classpath  " + classPath + " " + className + " " + nodeName + " > " +this.redirect);
// 	    }

// 	    d_error = new BufferedReader(new InputStreamReader(p.getInputStream()));
// 	    while ((s = d_error.readLine()) != null && (s.indexOf(NODEOK) == -1)) {
// 		System.out.println(s);
// 	    }
// 	} catch (Exception e) {
// 	    e.printStackTrace();
// 	}
// 	if (s == null) {
// 	    System.err.println("NodeProcess: cannot read output from process");
// 	    return false;
// 	} else {
// 	    System.err.println("NodeProcess: output from process is " + s);
// 	    //ok, we read until the end, but maybe the process has exited
// 	    //check wether the process has terminated
// 	    try {
// 		p.exitValue();
// 		return false;
// 	    } catch (IllegalThreadStateException e) {
// 	    }//e.printStackTrace();}
	
// 	    return true;
// 	}
//     }


    /**
     * Removes the port number at the end of a hostname
     *
     */
    protected String removePortNumber(String s) {
	int deb = s.indexOf(":");
	if (deb > -1) {
	    //there is a port number specified
	    return s.substring(0, deb);
	} else
	    return s;
    }


    public boolean initialise() {
	String s = null;

	System.err.println("NodeProcess: using command " + commandLine);
	try {
	    p = Runtime.getRuntime().exec(commandLine);
 	    d_error = new BufferedReader(new InputStreamReader(p.getInputStream()));
 	    while ((s = d_error.readLine()) != null && (s.indexOf(NODEOK) == -1)) {
 		System.out.println(s);
 	    }
 	} catch (Exception e) {
 	    e.printStackTrace();
 	}
 	if (s == null) {
 	    System.err.println("NodeProcess: cannot read output from process");
 	    return false;
 	} else {
 	    System.err.println("NodeProcess: output from process is " + s);
 	    //ok, we read until the end, but maybe the process has exited
 	    //check wether the process has terminated
 	    try {
 		p.exitValue();
 		return false;
 	    } catch (IllegalThreadStateException e) {
 	    }//e.printStackTrace();}
	
 	    return true;
 	}
     }     


    public void destroy() {
	p.destroy();
	r1.setRunning(false);
	r2.setRunning(false);
	//	this.running = false; 
    }


    public void run() {
	System.out.println("NodeProcess Running");
	//	running = true;
	String s = null;
	r1 = new ThreadedReader(p.getInputStream());
	r2 = new ThreadedReader(p.getErrorStream());

	Thread t1 = new Thread(r1);
	Thread t2 = new Thread(r2);
	t1.start();
	t2.start();
	
	// 	try {
	//BufferedReader d_standard = new BufferedReader(new InputStreamReader(p.getInputStream()));
	// BufferedReader d_error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
       
	//  while (((s=d_standard.readLine()) != null)   && (running))
	// 		{
	// 		    System.out.println(s);
	// 		}    
	// 	} catch (Exception e) { e.printStackTrace();} 		
    }


  //   public static void main(String[] args) {
// 	if (args.length < 5) {
// 	    System.err.println("Usage:java modelisation.util.NodeProcess className javaPath classPath nodeName hostName");
// 	    System.exit(-1);
// 	}

// 	NodeProcess n = new NodeProcess(args[0], args[1], args[2], args[3], args[4],"test");
// 	n.initialise();
// 	n.run();

//     }
}

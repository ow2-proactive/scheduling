package modelisation.util;

import org.objectweb.proactive.core.process.rsh.RSHJVMProcess;
import org.objectweb.proactive.core.util.MessageLogger;
import java.io.BufferedReader;


public class RSHNodeProcessImpl extends RSHJVMProcess {

    protected static String NODEOK = "OK.";
    protected String errorMessage;

    public RSHNodeProcessImpl() {
	this(new StandardOutputMessageLogger());
	//this(new NullMessageLogger());
    }

    public RSHNodeProcessImpl(MessageLogger messageLogger){
	super(messageLogger);
    }

    protected void handleProcess(java.io.BufferedReader in, java.io.BufferedWriter out, java.io.BufferedReader err) {
	String s=null;
	try {
	    while ((s=in.readLine()) != null && (s.indexOf(NODEOK) == -1)) {
		System.out.println(s);
	    }

	    //  else {
	    //  		while ((s=in.readLine()) != null) {
	    //  		    System.out.println(s);
	    //  		}
	    //} 
	
	} catch (Exception e) {
	    e.printStackTrace();
	} 
	if (s == null) {
	    this.setError(err);
	    this.isFinished = true;
	}else {
	    super.handleProcess(in, out, err);
	} 
    }

    protected void setError (java.io.BufferedReader err ) {
	String s=null;
	StringBuffer st = new StringBuffer();
	try {
	    while ((s=err.readLine()) != null) {
		st.append(s);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
	errorMessage=st.toString();
    }

    public String getErrorMessage() {
	return errorMessage;
    }


    public String toString () {
	return this.buildCommand();
    }
}

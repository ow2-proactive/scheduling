package javasci;

public class Scilab {
	
    static {
	System.loadLibrary("javasci");
	initialize();
    }
    
    private static native void initialize();
    
    public static void init(){
	// call static bloc
    }
    
    public static void sendData(SciData data){
	if(data instanceof SciDoubleMatrix){
	    sendDoubleMatrix((SciDoubleMatrix) data);
	}
	if(data instanceof SciStringMatrix){
	    sendStringMatrix((SciStringMatrix) data);
	}
	else{
	    //NYI
	}
    }
        
    public static native void sendDoubleMatrix(SciDoubleMatrix matrix);
    public static native void receiveDoubleMatrix(SciDoubleMatrix matrix);
    public static native void sendStringMatrix(SciStringMatrix matrix);
    public static native void receiveStringMatrix(SciStringMatrix matrix);
    //public static native boolean loadScript(String filename);
    public static native SciData receiveDataByName(String id); 
    public static native boolean exec(String job);
   
    
	
}

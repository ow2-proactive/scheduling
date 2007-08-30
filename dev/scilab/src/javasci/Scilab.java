package javasci;

public class Scilab {

    /*******************************  Methods of the Official Scilab Javasci interface  ****************************************/

	
    public static native void Events();

    /**
    * do a Scilab event
    * See SCI/examples/callsci/callsciJava/others/ExempleEvent.java
    */
    public static native boolean HaveAGraph();

    /**
    * Detect if a Scilab graphic window is opened
    */
    public static native boolean Exec(String job);

    /**
    * Execute a command in Scilab
    */
    public static native boolean ExistVar(String VarName);

    /**
    * Detect if Variable name exists in Scilab
    */
    public static native int TypeVar(String VarName);

    /**
    * Get Scilab type  of a variable
    * -1 not exist
    * 1 : real or complex constant matrix.
    * 2 : polynomial matrix.
    * 4 : boolean matrix.
    * 5 : sparse matrix.
    * 6 : sparse boolean matrix.
    * 8 : matrix of integers stored on 1 2 or 4 bytes
    * 9 : matrix of graphic handles
    * 10 : matrix of character strings.
    * 11 : un-compiled function.
    * 13 : compiled function.
    * 14 : function library.
    * 15 : list.
    * 16 : typed list (tlist)
    * 17 : mlist
    * 128 : pointer
    */
    public static native int GetLastErrorCode();

    /**
    * Get Last Error Code
    * 0 no error
    */
    public static boolean ExecuteScilabScript(String scriptfilename)
    /**
    * Execute a scilab script .sce
    */
     {
        return Exec("exec(''" + scriptfilename + "'');");
    }

    public static native boolean Finish();
    
    /********************************** Modifications for ProActive ******************************/

    static {
        System.loadLibrary("javasci");
        initialize();
    }
    
    private static native void initialize();

    public static void init() {
        // call static bloc
    }
    /**
     * Send a matrix of any type of elements to Scilab
     * @param matrix matrix to send
     */
    public static void sendData(SciData data) {
        if (data instanceof SciDoubleMatrix) {
            sendDoubleMatrix((SciDoubleMatrix) data);
        }
        else if (data instanceof SciComplexMatrix) {
            sendComplexMatrix((SciComplexMatrix) data);
        }
        else if (data instanceof SciStringMatrix) {
            sendStringMatrix((SciStringMatrix) data);
        } else {
            //NYI
        }
    }

    /**
     * Send matrix of double elements to Scilab
     * @param matrix matrix to send
     */
    public static native void sendDoubleMatrix(SciDoubleMatrix matrix);
    /**
     * Receive matrix of double elements from Scilab
     * @param matrix matrix to receive the data
     */
    public static native void receiveDoubleMatrix(SciDoubleMatrix matrix);
    /**
     * Send matrix of complex elements to Scilab
     * @param matrix matrix to send
     */
    public static native void sendComplexMatrix(SciComplexMatrix matrix);
    /**
     * Receive matrix of complex elements from Scilab
     * @param matrix matrix to receive the data
     */
    public static native void receiveComplexMatrix(SciComplexMatrix matrix);
    /**
     * Send matrix of string elements to Scilab
     * @param matrix matrix to send
     */
    public static native void sendStringMatrix(SciStringMatrix matrix);
    /**
     * Receive matrix of string elements from Scilab
     * @param matrix matrix to receive the data
     */
    public static native void receiveStringMatrix(SciStringMatrix matrix);
    /**
     * Receive a matrix of unknown elements from Scilab
     * @param matrix matrix to receive the data
     */
    public static native SciData receiveDataByName(String id);

}

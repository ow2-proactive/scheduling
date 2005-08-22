package org.objectweb.proactive.examples.profractal;


/* fractResult.java
 *
 * Created on Semptember 6 2003, 11.56
 */

/**
 * @author Daniele Di Felice
 */
import java.io.*;


/**
 * This class represent the computation's results and it's needed by the
 * ProActive model, because te boolean[][] is final
 */
public class fractResult implements Serializable {
    private boolean[][] result;

    /** Empty constructor */
    public fractResult() {
    }

    /** Constructor
     * @param results: a bi-dimensional array of boolean values
     * @return an instance of the fractResult class
     */
    public fractResult(boolean[][] result) {
        this.result = result;
    }

    /** Sets the results array
     * @param results: a bi-dimensional array of boolean values
     * @return void
     */
    public void setResult(boolean[][] result) {
        this.result = result;
    }

    /** Gets the results matrix
     * @param void
     * @return results: the results matrix
     */
    public boolean[][] getResults() {
        return (this.result);
    }

    /** Gets the result at the row i and column j
     * @param void
     * @return result[i][j]: the result at the row i and column j
     */
    public boolean getResult(int i, int j) {
        return (result[i][j]);
    }
}

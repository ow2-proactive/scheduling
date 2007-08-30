package javasci;


/**
 * SciStringArray
 * @deprecated
 * @param job
 */
public class SciStringArray {
    private SciStringMatrix sciMatrix;

    public SciStringArray(String name, int nbRow, int nbCol) {
        sciMatrix = new SciStringMatrix(name, nbRow, nbCol);
    }

    public SciStringArray(String name, int nbRow, int nbCol, String[] matrix) {
        sciMatrix = new SciStringMatrix(name, nbRow, nbCol, matrix);
    }

    public SciStringArray(String name, SciStringArray obj) {
        sciMatrix = new SciStringMatrix(name, obj.getRow(), obj.getCol(),
                obj.getData());
    }

    /**
    * Job
    * @deprecated
    * @param job
    */
    public boolean Job(String job) {
        return Scilab.Exec(job);
    }

    /**
     * Send
     * @deprecated
     */
    public void Send() {
        Scilab.sendStringMatrix(sciMatrix);
    }

    /**
     * Get
     * @deprecated
     */
    public void Get() {
        Scilab.receiveStringMatrix(sciMatrix);
    }

    /**
     * getRow
     * @deprecated
     */
    public int getRow() {
        return sciMatrix.getNbRow();
    }

    /**
     * getCol
     * @deprecated
     */
    public int getCol() {
        return sciMatrix.getNbCol();
    }

    /**
     * getName
     * @deprecated
     */
    public String getName() {
        return sciMatrix.getName();
    }

    /**
     * getData
     * @deprecated
     */
    public String[] getData() {
        return sciMatrix.getData();
    }

    /**
     * disp
     * @deprecated
     */
    public void disp() {
        System.out.println("Matrix " + sciMatrix.getName() + "=");
        Scilab.Exec("disp(" + sciMatrix.getName() + ");");
    }

    /**
     * GetElement
     * @deprecated
     * @param iRow
     * @param iCol
     */
    public String GetElement(int iRow, int iCol) {
        return sciMatrix.getData()[(iRow * sciMatrix.getNbCol()) + iCol];
    }
}

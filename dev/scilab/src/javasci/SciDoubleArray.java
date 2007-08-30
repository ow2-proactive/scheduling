package javasci;

/**
 * SciDoubleArray
 * 
 * @deprecated for ProActive
 * @param job
 */
public class SciDoubleArray {
	private SciDoubleMatrix sciMatrix;

	public SciDoubleArray(String name, int nbRow, int nbCol) {
		sciMatrix = new SciDoubleMatrix(name, nbRow, nbCol);
	}

	public SciDoubleArray(String name, int nbRow, int nbCol, double[] matrix) {
		sciMatrix = new SciDoubleMatrix(name, nbRow, nbCol, matrix);
	}

	public SciDoubleArray(String name, SciDoubleArray obj) {
		sciMatrix = new SciDoubleMatrix(name, obj.getRow(), obj.getCol(), obj
				.getData());
	}

	/**
	 * Job
	 * 
	 * @deprecated
	 * @param job
	 */
	public boolean Job(String job) {
		return Scilab.Exec(job);
	}

	/**
	 * Send
	 * 
	 * @deprecated
	 */
	public void Send() {
		Scilab.sendDoubleMatrix(sciMatrix);
	}

	/**
	 * Get
	 * 
	 * @deprecated
	 */
	public void Get() {
		Scilab.receiveDoubleMatrix(sciMatrix);
	}

	/**
	 * getRow
	 * 
	 * @deprecated
	 */
	public int getRow() {
		return sciMatrix.getNbRow();
	}

	/**
	 * getCol
	 * 
	 * @deprecated
	 */
	public int getCol() {
		return sciMatrix.getNbCol();
	}

	/**
	 * getName
	 * 
	 * @deprecated
	 */
	public String getName() {
		return sciMatrix.getName();
	}

	/**
	 * getData
	 * 
	 * @deprecated
	 */
	public double[] getData() {
		return sciMatrix.getData();
	}

	/**
	 * disp
	 * 
	 * @deprecated
	 */
	public void disp() {
		System.out.println("Matrix " + sciMatrix.getName() + "=");
		Scilab.Exec("disp(" + sciMatrix.getName() + ");");
	}

	/**
	 * GetElement
	 * 
	 * @deprecated
	 * @param iRow
	 * @param iCol
	 */
	public double GetElement(int iRow, int iCol) {
		return sciMatrix.getData()[(iRow * sciMatrix.getNbCol()) + iCol];
	}
}

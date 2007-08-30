package javasci;

public class SciDoubleMatrix extends SciMatrix {
    private double[] matrix;

    public SciDoubleMatrix(String name, int nbRow, int nbCol) {
	super(name, nbRow, nbCol);
	this.matrix = new double[nbRow * nbCol];
    }

    public SciDoubleMatrix(String name, int nbRow, int nbCol, double[] matrix) {
	super(name, nbRow, nbCol);

	if (nbRow * nbCol != matrix.length) {
	    throw new BadDataArgumentException("");
	}
	this.matrix = matrix;
    }

    public double[] getData() {
	return this.matrix;
    }
    
    public String toString(){
	StringBuffer buffer = new StringBuffer();
	buffer.append(this.name + " = \n");
	for (int i = 0; i < nbRow; i++) {
	    buffer.append("| ");
	     for (int j = 0; j < nbCol; j++) {
		 buffer.append(matrix[i * nbCol + j] + " ");
	    }
	     buffer.append("|\n");
	}
	return buffer.toString();
    }
}

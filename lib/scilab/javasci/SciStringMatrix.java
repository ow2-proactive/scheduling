package javasci;

public class SciStringMatrix extends SciMatrix {
    protected String[] matrix;

    public SciStringMatrix(String name, int nbRow, int nbCol) {
	super(name, nbRow, nbCol);
	this.matrix = new String[nbRow * nbCol];
    }

    public SciStringMatrix(String name, int nbRow, int nbCol, String[] matrix) {
	super(name, nbRow, nbCol);

	if (nbRow * nbCol != matrix.length) {
	    throw new BadDataArgumentException("");
	}
	this.matrix = matrix;
    }

    public String[] getData() {
	return this.matrix;
    }

     public String toString(){
	String str = this.name + " = \n";
	for (int i = 0; i < nbRow; i++) {
	    str += "| ";
	     for (int j = 0; j < nbCol; j++) {
		str += matrix[i * nbCol + j] + " ";
	    }
	    str += "|\n";
	}
	return str;
    }
}

package javasci;

public class SciComplexMatrix extends SciMatrix {
    private double[] x,y;

    public SciComplexMatrix(String name, int nbRow, int nbCol) {
	super(name, nbRow, nbCol);
	this.x = new double[nbRow * nbCol];
	this.y = new double[nbRow * nbCol];
    }

    public SciComplexMatrix(String name, int nbRow, int nbCol, double[] real, double[] imag) {
	
    this(name, nbRow, nbCol);

	if ((nbRow * nbCol != real.length) || (nbRow * nbCol != imag.length)) {
	    throw new BadDataArgumentException("");
	}
	for (int i = 0; i < nbRow; i++) {
	     for (int j = 0; j < nbCol; j++) {
	    	 x[i*nbCol+j] = real[i*nbCol+j];
	    	 y[i*nbCol+j] = imag[i*nbCol+j];
	     }
	}
    }

    public double[] getRealElements() {
	return this.x;
    }
    
    public double[] getImagElements() {
    	return this.y;
        }
    
    public String toString(){
	StringBuffer buffer = new StringBuffer();
	buffer.append(this.name + " = \n");
	for (int i = 0; i < nbRow; i++) {
	    buffer.append("| ");
	     for (int j = 0; j < nbCol; j++) {
		 buffer.append("("+ x[i * nbCol + j]+","+ y[i * nbCol + j]+")" + " ");
	    }
	     buffer.append("|\n");
	}
	return buffer.toString();
    }
    
}

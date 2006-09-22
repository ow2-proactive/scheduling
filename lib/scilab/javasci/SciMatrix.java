package javasci;

public class SciMatrix extends SciData{
    protected int nbRow;
    protected int nbCol;

    public SciMatrix(String name, int nbRow, int nbCol){
	super(name);
	
	if (nbRow< 0 ||  nbCol< 0) {
	    throw new BadDataArgumentException("");
	}
	
	this.nbCol = nbCol;
	this.nbRow = nbRow;
    }

    public int getNbRow() {
	return this.nbRow;
    }

    public int getNbCol() {
	return this.nbCol;
    }
}

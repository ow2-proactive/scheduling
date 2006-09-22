package javasci;

 /**
  * SciDouble
  * @deprecated
  */

public class SciDouble {
    private SciDoubleMatrix sciMatrix;
    public SciDouble(String name) {
	sciMatrix = new SciDoubleMatrix(name, 1, 1);
    }

    public SciDouble(String name, double value){
	sciMatrix = new SciDoubleMatrix(name, 1, 1, new double[]{value});
    }
    
    public SciDouble(String name, SciDouble obj){
	sciMatrix = new SciDoubleMatrix(name, 1, 1, new double[]{obj.getData()});
    }
      
    /**
     * Job
     * @deprecated
     * @param job
     */
    public boolean Job(String job){
	return Scilab.exec(job);
    }

    /**
     * Send
     * @deprecated
     */
    
    public void Send(){
	Scilab.sendDoubleMatrix(sciMatrix);
    }
    
    /**
     * Get
     * @deprecated
     */
    public void Get(){
	Scilab.receiveDoubleMatrix(sciMatrix);
    }
    
    /**
     * getName
     * @deprecated
     */
    public String getName(){
	return  sciMatrix.getName();
    }

    /**
     * getData
     * @deprecated
     */
    public double getData(){
	return sciMatrix.getData()[0];
    }

    /**
     * disp
     * @deprecated
     */    
    public void disp(){
	 System.out.println("Double " + sciMatrix.getName() +"=");
	 Scilab.exec("disp(" + sciMatrix.getName() + ");");
    }


    
    

}

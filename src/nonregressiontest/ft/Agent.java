package nonregressiontest.ft;

import java.io.Serializable;

/**
 * @author cdelbe
 */
public class Agent implements Serializable {


    private Agent neighbour;
    private int counter;
    private int iter;
    private Collector launcher;
        
    public Agent() {}

    public void initCounter(int value){
        this.counter = value;
        this.iter = 0;
    }
    
    public void setNeighbour (Agent n){
        this.neighbour = n;
    }
    
    public void setLauncher (Collector l){
        this.launcher = l;
    }
    
    public ReInt doStuff (ReInt param){
        this.counter += param.getValue();
        return new ReInt(this.counter);
    }
    
    public ReInt getCounter(){
        return new ReInt(this.counter);
    }
    
    public void startComputation (int max) {
        
        iter++;
        ReInt a = this.neighbour.doStuff(new ReInt(this.counter));
        ReInt b = this.neighbour.doStuff(new ReInt(this.counter));
        ReInt c = this.neighbour.doStuff(new ReInt(this.counter));
        ReInt d = this.neighbour.doStuff(new ReInt(this.counter));
        this.counter+=a.getValue();
        this.counter+=b.getValue();
        this.counter+=c.getValue();
        this.counter+=d.getValue();
   
        if (iter<max){
            neighbour.startComputation(max);
            //if (iter%50 == 0) {System.out.println("Avance ... " + iter);}
        } else {
            //System.out.println("RESULTAT SUR AGENT= " + this.counter);
            this.launcher.finished(this.counter);
        }
        
    }
        
}

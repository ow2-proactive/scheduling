package nonregressiontest.ft;

import java.io.Serializable;

import org.objectweb.proactive.ProActive;


public class Collector implements Serializable {

    private int result=0;
    
    public Collector() {}
    
    public void go(Agent a, int max){
        a.startComputation(max);
    }
    
    public void finished(int res){
        this.result = res;
        //System.out.println("Collector : " + res);
    }

    
    public ReInt getResult(){
        if (this.result==0){
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return ((Collector)ProActive.getStubOnThis()).getResult();
        } else {
            return new ReInt(this.result);
        }
    }
    
}

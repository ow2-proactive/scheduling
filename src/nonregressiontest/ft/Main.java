package nonregressiontest.ft;

import java.io.Serializable;

public class Main implements Serializable{

    public static void main(String[] args){
        try {
            Test ftt = new Test();
            ftt.action();
            System.out.println("Test result : " + ftt.postConditions());
            System.exit(0);
        } catch (Exception e) {
            System.err.println("An exception occured during test :");
            e.printStackTrace();
        }
    }

}

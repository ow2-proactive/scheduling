package test.sleep;

import modelisation.statistics.ExponentialLaw;

public class Test {
    public Test() {}

    public static void main(String[] args) {
        long startTime=0;
        long endTime=0;
        double time;
       ExponentialLaw expo = new ExponentialLaw(10);
        while(true) {
            time=expo.next()*1000;
            startTime=System.currentTimeMillis();
            try {
                 Thread.sleep((long) time);
            }   catch (Exception e) {
                e.printStackTrace();
            }
            endTime=System.currentTimeMillis();
            System.out.println("Test.main waited " + (endTime-startTime)
            + " instead of " + time);
        }
    }


}

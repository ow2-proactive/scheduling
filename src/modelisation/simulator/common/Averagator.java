package modelisation.simulator.common;

import java.math.BigDecimal;


public class Averagator {
    protected double value;
    protected int number;
//    protected BigDecimal value2;

    public Averagator() {
       // this.value2 = new BigDecimal(0);
   //     this.value2.setScale(20);
    }

    public void add(double v) {
        this.value += v;
        //        System.out.println("***** " + new BigDecimal(v));
   //     this.value2 = this.value2.add(new BigDecimal(v));
        //        System.out.println("***** " + this.value2);
        this.number++;
    }

    public double average() {
        //       System.out.println(this.value2);
        //       System.out.println(this.value2.divide(new BigDecimal(number),BigDecimal.ROUND_HALF_EVEN ));
        // System.out.println(this.value2 + " " + this.number);
     //    this.value2 = this.value2.add(new BigDecimal(0.0000000001));
       // return this.value2.divide(new BigDecimal(number), 
         //                         BigDecimal.ROUND_HALF_EVEN).doubleValue();
              return this.value / this.number;
    }

    public int getCount() {
        return this.number;
    }

    public double getTotal() {
        return this.value;
    }

    public static void main(String[] args) {
        Averagator a = new Averagator();
        a.add(1);
        a.add(9);
        System.out.println(a.average());
    }
}
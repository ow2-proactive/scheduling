/*
 * Created on 4 mai 2003
 *
 * To change this generated comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package modelisation.simulator.mixed.mixedwithcalendar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import modelisation.simulator.common.ExpoAverageWithMemory;


/**
 * @author fhuet
 *
 * To change this generated comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class TtlGenerator {
    //  protected ArrayList times;
    protected static double NULL_RATE = 0.5;
    protected static final int POSITIVE_RATE = 2;
    protected static final int NORMAL_RATE = 1;
    protected static final int NEGATIVE_RATE = 0;
    protected static int INCREASING = 2;
    protected static int FIXED = 1;
    protected static int DECRASING = 0;
    protected static int LENGTH = 10;
    protected double initialTTL;
    protected double previousTtl;
    protected double currentTtl;
    protected int way;
    protected double factor;
    protected ExpoAverageWithMemory expoAveragatorTime;
    protected ExpoAverageWithMemory expoRate;

    //  protected MemoryArray timeMemory;
    protected ExpoAverageWithMemory timeHistory;
    protected ExpoAverageWithMemory expoDeriv2;
    protected int timeSpent;
    protected int timeCount;
    protected int normalCounter;
    protected static final int NORMAL_TRESHOLD = 20;
    protected static int MAX_TIME_SPENT = 20;

    public TtlGenerator() {
    }

    public TtlGenerator(double ttl) {
        this.initialTTL = ttl;
        this.currentTtl = this.initialTTL;
        this.way = 1;
        //    this.times = new ArrayList(TimeAnalyse.LENGTH);
        factor = 1.1;

        this.expoAveragatorTime = new ExpoAverageWithMemory(0.99, 10);
        this.expoRate = new ExpoAverageWithMemory(0.99, 10);
        //  this.timeMemory = new MemoryArray(10);
        this.timeHistory = new ExpoAverageWithMemory(0.95, 10);
        this.expoDeriv2 = new ExpoAverageWithMemory(0.99, 10);
        //  this.expoAveragatorTime99 = new ExpoAverageWithMemory(0.99, 4);
        //  this.expoAveragatorTime999 = new ExpoAverageWithMemory(0.999, 4);
    }

    public double getNewTtl() {
        return this.intelligentTtl();
    }

    protected double dumbTtl() {
        //       System.out.println("ExpoAverage995 = " +
        //           this.expoAveragatorTime995.getAverage());
        //    System.out.println("ExpoAverage99 = " +
        //         this.expoAveragatorTime99.getAverage());
        //     System.out.println("ExpoAverage999 = " +
        //         this.expoAveragatorTime999.getAverage());
        //	return this.currentTtl;
        if (this.timeSpent > MAX_TIME_SPENT) {
            //	System.out.println("max time reached " + this.timeSpent);
            //	System.out.println("                 " + this.currentTtl);
            if (factor > 1) {
                factor = 0.2;
            } else {
                factor = 5;
            }
            this.currentTtl = this.initialTTL * factor;
            this.timeSpent = 0;
        } else {
            timeSpent++;
        }

        // return this.initialTTL;
        return this.currentTtl;
    }

    /**
     *  Add the new time and calculate the new average using EWMA
     * returns the result of analyseConditions every 10 calls.
     * @param time
     * @return
     */
    public boolean newCommunicationTime(double time) {
        this.expoAveragatorTime.add(time);
        //     System.out.println("Time EWMA = " +
        //        this.expoAveragatorTime.getLastValue());
        this.timeCount++;
        //  if (this.timeCount > 10) {
        this.timeCount = 0;
        return analyseConditions();
        //   } else {
        //      return false;
        //  }
    }

    /**
     * we try to provide a more clever ttl
     * the idea is to wait until the time is stabilized, then perform a change
     * and see its consequences
     * @return
     */
    protected double intelligentTtl() {
        this.timeHistory.add(this.expoAveragatorTime.getAverage());

        // System.err.println("IT IS VERY NORMAL " + this.timeMemory);
        double rate = this.timeHistory.calculateRate();

        //   System.out.println("History = " + this.timeHistory.getAverage() + " " +
        //       this.expoAveragatorTime.getLastCounter());
        //    System.out.println("HistoryRate = " + rate + " " +
        //         this.expoAveragatorTime.getLastCounter() + "values " +
        //          this.timeHistory);
        //now we try to perform a change
        if (rate > 1) {
            this.currentTtl *= 1.1;
            //   System.err.println("TTL going up");
        } else { // if (rate<5) {
            //      System.err.println("TTL going down");
            this.currentTtl /= 1.1;
        }

        //  this.currentTtl = this.initialTTL;
        //      System.out.println("TTL = " + this.currentTtl + " " +
        //         this.expoAveragatorTime.getLastCounter());
        return this.initialTTL;
    }

    /**
     * Determinates if the time as reported by the source is stable
     * @return true if the time is stable
     */
    protected boolean analyseConditions() {
        this.expoRate.add(this.expoAveragatorTime.calculateRate());
        //      System.out.println("Rate = " + this.expoRate.getAverage() + " " +
        //        this.expoAveragatorTime.getLastCounter());
        int variation = this.currentVariation();
        switch (variation) {
        case NORMAL_RATE:
            //          System.out.println("normal " + normalCounter);
            if (normalCounter++ > NORMAL_TRESHOLD) {
                this.normalCounter = 0;
                return true;
            }
            break;
        case POSITIVE_RATE:
            //          System.out.println("positive");
            normalCounter = 0;
            break;
        case NEGATIVE_RATE:
            //            System.out.println("negative");
            normalCounter = 0;
            break;
        default:
            break;
        }
        return false;
    }

    protected int currentVariation() {
        double rate = this.expoRate.getAverage();

        //double rate = this.expoRate.calculateRate();
        if (rate > NULL_RATE) {
            // System.err.println("High positive rate detected " + rate);
            return POSITIVE_RATE;
        } else if (rate < -NULL_RATE) {
            //   System.err.println("High negative rate detected " + rate);
            return NEGATIVE_RATE;
        } else {
            this.expoDeriv2.add(this.expoRate.calculateRate());
            System.out.println("2deriv = " + this.expoDeriv2.getAverage() +
                " " + this.expoAveragatorTime.getLastCounter());
            //   System.err.println("Normal rate detected " + rate);
            return NORMAL_RATE;
        }
    }

    //    /**
    //     *
    //     * @param t
    //     * @return
    //     */
    //    protected double simpleVariance(double[] t) {
    //
    //        double average = (t.length * (t.length + 1)) / 2 / t.length;
    //        double tmp = 0;
    //        System.out.println("averageX = " + average);
    //        for (int i = 0; i < t.length; i++) {
    //            tmp += Math.pow((i + 1) - average, 2);
    //        }
    //        return tmp;
    //    }
    //
    //    protected double covariance(double[] t) {
    //
    //        double tmp = 0;
    //        double averageX = (t.length * (t.length + 1)) / 2 / t.length;
    //        double averageY = 0;
    //        for (int i = 0; i < t.length; i++) {
    //            averageY += t[i];
    //        }
    //        averageY = averageY / t.length;
    //        System.out.println("averageY = " + averageY);
    //        for (int i = 0; i < t.length; i++) {
    //            tmp += (((i + 1) - averageX) * (t[i] - averageY));
    //            // System.out.println(tmp);
    //        }
    //
    //        // 
    //        return tmp;
    //    }
    protected double calculateB(double[] t) {
        double b = 0;
        double sumX = (t.length * (t.length + 1)) / 2;
        double sumY = 0;
        double sumX2 = 0;
        double sumXY = 0;

        for (int i = 0; i < t.length; i++) {
            sumY += t[i];
        }

        for (int i = 1; i <= t.length; i++) {
            sumX2 += (i * i);
        }

        for (int i = 0; i < t.length; i++) {
            sumXY += ((i + 1) * t[i]);
        }

        return ((t.length * sumXY) - (sumX * sumY)) / ((t.length * sumX2) -
        Math.pow(sumX, 2));
    }

    public static void main(String[] arguments) {
        if (arguments.length < 1) {
            System.err.println("Usage: " + TtlGenerator.class.getName() +
                " <file>");
            System.exit(0);
        }

        TtlGenerator ttlG = new TtlGenerator(1000);

        File f = new File(arguments[0]);
        FileReader fr = null;
        BufferedReader br = null;
        try {
            fr = new FileReader(f);
            br = new BufferedReader(fr);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            while (br.ready()) {
                double d = Double.parseDouble(br.readLine());

                if (ttlG.newCommunicationTime(d)) {
                    //	this.currentTtl = this.ttlGenerator.getNewTtl();
                    System.out.println("Getting new value for ttl = " +
                        ttlG.getNewTtl());
                }
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        //        double[] tmp = { 10, 5, 1 };
        //        double pente;
        //        TimeAnalyse tg = new TimeAnalyse();
        //
        //        // v = tg.simpleVariance(tmp);
        //        //  cov = tg.covariance(tmp);
        //        //     System.out.println("variance = " + v);
        //        //      System.out.println("covariance = " + cov);
        //        //      System.out.println("Pente = " + (cov / v));
        //        pente = tg.calculateB(tmp);
        //        System.out.println("Pente2 = " + pente);
        //
        //        //        
        //        double averageX = (tmp.length * (tmp.length + 1)) / 2 / tmp.length;
        //        double averageY = 0;
        //        for (int i = 0; i < tmp.length; i++) {
        //            averageY += tmp[i];
        //        }
        //        averageY = averageY / tmp.length;
        //        System.out.println("a = " + (averageY - (pente * averageX)));
    }
}

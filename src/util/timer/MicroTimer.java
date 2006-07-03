package util.timer;

public class MicroTimer {

    static {
        System.loadLibrary("Util_timer_MicroTimer");
    }

    private long[] startTime;
    private long[] endTime;

    public native long[] currentTime();

    public MicroTimer() {
    }

    public void start() {
        this.startTime = currentTime();
        this.endTime = currentTime();
    }

    /**
     * Stop the timer and
     * returns the cumulated time
     */
    public void stop() {
        this.endTime = currentTime();
        //this.cumulatedTime = this.updateCumulatedTime(startTime, endTime);
        //return this.cumulatedTime;
    }

    //  /**
    //    * Resume the Microtimer
    //    * Doesn't reset the cumulatedTime value
    //    */
    //   public void resume() {
    //     this.startTime = currentTime();
    //   }

    /**
     * Return the newly computed cumulated time
     * as measured by this MicroTimer
     * in microseconds
     */
    public long getCumulatedTime() {
        long[] tmp = this.updateCumulatedTime(startTime, endTime); //this.stop();

        // this.resume();
        return (tmp[0] * 1000000) + tmp[1];
    }

    protected long[] updateCumulatedTime(long[] t1, long[] t2) {
        long[] tmp = new long[2]; // this.cumulatedTime;
        tmp[0] = t2[0] - t1[0];
        // System.out.println(" updateCumulatedTime: " + t1[0] + " " + t1[1]);
        //	System.out.println("  with updateCumulatedTime: " + t2[0] + " " + t2[1]);
        if ((t2[1] - t1[1]) < 0) {
            //one second has gone
            tmp[0]--;
            tmp[1] = (t2[1] + 1000000) - t1[1];

            //tmp[1]+=t1[1]-t2[1];
            //tmp[0]++;
        } else {
            tmp[1] += (t2[1] - t1[1]);
        }
        return tmp;
    }

    public static void main(String[] args) {
        MicroTimer timer = new MicroTimer();
        int i = 0;
        timer.start();
        while (i < 10) {
            // 	try {
            // 		    Thread.sleep(500);
            // 		} catch (Exception e) {e.printStackTrace();}
            System.out.println("Time is  " + timer.getCumulatedTime());
            i++;
        }
        timer.stop();
        System.out.println("Sleeping 1s");
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        i = 0;
        //    timer.resume();
        while (i < 10) {
            long startTime2 = System.currentTimeMillis();
            timer.start();
            try {
                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
            timer.stop();
            long endTimeMicro = timer.getCumulatedTime();
            long endTime2 = System.currentTimeMillis();
            System.out.println("Time is  " + endTimeMicro);
            System.out.println("Time in ms is  " + (endTime2 - startTime2));
            i++;
        }
    }
}

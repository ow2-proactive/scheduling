package modelisation.simulator.common;

public class Averagator {
    protected double value;
    protected int number;

    public void add(double v) {
        this.value += v;
        this.number++;
    }

    public double average() {
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
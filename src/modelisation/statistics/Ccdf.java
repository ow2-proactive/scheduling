package modelisation.statistics;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

//complementary cumulative distibution function
public class Ccdf {
    public Ccdf() {
    }

    public int countLines(String fileName) {

        int size = 0;
        try {

            FileInputStream file = new FileInputStream(fileName);
            BufferedReader in = new BufferedReader(new InputStreamReader(file));
            //tant qu'on peut lire
            while (in.ready()) {
                in.readLine();
                size++;
            }
            file.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return size;
    }

    public void analyse(String fileName, int size, double[] values) {

        int index = 0;
        int counter = 0;
      //  System.out.println("size " + size);
        try {

            FileInputStream file = new FileInputStream(fileName);
            BufferedReader in = new BufferedReader(new InputStreamReader(file));
            //tant qu'on peut lire
            while (in.ready()) {
                if (Double.parseDouble(in.readLine()) >= values[index]) {
                    System.out.println(
                            values[index] + " " + 
                            (1 - (double)counter / size)); // + " " + counter);
                    index++;
                }
                if (index == values.length) {
                    //we stop here
                    //System.out.println("stop");
                    return;
                }
                counter++;
                //      list.addElement(in.readLine());
            }
            file.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {

        String fileName = args[0];
      double[] vals = new double[47];
      for (int i = 0; i < vals.length; i++) {
      	
      	vals[i]=Math.pow(10,-1+i/10.0);
     // 	System.out.println(vals[i]);
		
	}
	//System.exit(0);
//        double[] vals = new double[args.length - 1];
//        for (int i = 1; i < args.length; i++) {
//            vals[i - 1] = new Double(args[i]).doubleValue();
//        }

        Ccdf cc = new Ccdf();
        int size = cc.countLines(args[0]);
        //System.arraycopy(args, 1, vals, 0, args.length-1);
        cc.analyse(args[0], size, vals);
    }
}
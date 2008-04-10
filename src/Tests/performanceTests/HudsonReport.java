package performanceTests;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class HudsonReport {

    static public void reportToHudson(Class<?> cl, double value) {

        try {
            FileWriter fw = new FileWriter(new File(cl.getCanonicalName()));
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("YVALUE=" + value);
            bw.newLine();
            bw.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

package migration.bench;

import org.objectweb.proactive.ProActive;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.Calendar;
import java.util.zip.InflaterInputStream;

public class LocalAnalyser extends Object {

  public static boolean COMPRESSION = false;


  public SearchResult searchInString(String s, String keyword) {
    return DataBase.searchInString(s, keyword);
  }


  public static void main(String args[]) {
    char result[] = {};

    byte zippedResult[] = {};
    byte unzippedResult[] = {};
    ByteArrayInputStream in = null;

    String resultString;
    SearchResult finalResult;

    long startTime;
    long executionTime = 0;

    if (args.length < 3) {
      System.out.println("Usage: java migration.bench.LocalAnalyser //hostname/converterName fileName keyWord");
      System.exit(-1);
    }
    try {
      //First we get the reference on the converter
      Converter converter = (Converter)ProActive.lookupActive("migration.bench.Converter", args[0]);
      Object temp;

      startTime = System.currentTimeMillis();

      if (COMPRESSION) {
        zippedResult = converter.getLocalFile(args[1]);
        in = new ByteArrayInputStream(zippedResult);

        ObjectInputStream objectIn = new ObjectInputStream(new InflaterInputStream(in));

        temp = objectIn.readObject();
        objectIn.close();
      } else {
        result = converter.getLocalFileWithoutCompression(args[1]);
      }
      // System.out.println("Size of the zipped result = " + zippedResult.length);
 
      //now we unzip it
      resultString = new String(result);

      finalResult = DataBase.searchInString(resultString, args[2]);

      executionTime = System.currentTimeMillis() - startTime;
      Calendar rightNow = Calendar.getInstance();
      System.out.println("Bench over at " + rightNow.getTime());
      System.out.println("Execution time is  " + executionTime);// " and found " + finalResult.size() + " occurences");
    } catch (Exception e) {
      e.printStackTrace();
    }

  }
}
 

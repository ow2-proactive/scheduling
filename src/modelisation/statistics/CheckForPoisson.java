/**
 * This class was originally written By 
 * Cesar Jalpa Villanueva as part of the Wagon
 * software
 * See http://www-sop.inria.fr/mistral
 */
package modelisation.statistics;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.Vector;

public class CheckForPoisson implements Serializable {

  //extends GenericDistribution {
  
  protected String fileName;


  public CheckForPoisson(String fileName) {
    this.fileName = fileName;
  }


  public double[] readFile() {
    String s;
    Vector tmp = new Vector();
    FileReader f_in = null;

    try {
      f_in = new FileReader(fileName);
    } catch (FileNotFoundException e) {
      System.out.println("File not Found");
    }
    // on ouvre un "lecteur" sur ce fichier
    BufferedReader _in = new BufferedReader(f_in);
	
    // on lit a partir de ce fichier
    // NB : a priori on ne sait pas combien de lignes on va lire !!
    try {
      // tant qu'il y a quelque chose a lire
      while (_in.ready()) {
        // on le lit
        s = _in.readLine();
        //put it in the vector
        tmp.addElement(s);

      }
    }// catch (IOException e) {} 
    catch (Exception e) {
    }

    try {
      _in.close();
    } catch (IOException e) {
      System.out.println("Error closing the file");
    }


    //now we convert the vector of Double 
    double[] tablo = new double[tmp.size()];
    for (int i = 0; i < tmp.size(); i++) {
      tablo[i] = Double.parseDouble((String)tmp.elementAt(i));
    }
    return tablo;

  }


  public double mean(double[] values) {
    double total = 0;
    for (int i = 0; i < values.length; i++) {
      total += values[i];
    }
    return total / values.length;
  }


  public double variance(double[] values) {
    double moyenne = this.mean(values);
    double[] tmp = new double[values.length];
    //we first build a new array
    for (int i = 0; i < values.length; i++) {
      tmp[i] = Math.pow(values[i] - moyenne, 2);
    }

    return this.mean(tmp);

  }


  public static void main(String args[]) {
    if (args.length < 1) {
      System.err.println("Usage: java statistics.CheckForPoisson <fileName>");
      System.exit(-1);
    }

    CheckForPoisson check = new CheckForPoisson(args[0]);

    double[] tablo = check.readFile();


    //	double tablo[] = new double[50000];
    //	double values2[] = new double[50000];
    //	double moyenne;

    // 	for (int i =0; i< 50000 ;i++)
    // 	{
    // 	   tablo[i] =  expo.rand();
    // 	}
	
    // 	moyenne = check.mean(tablo);
    // 	for (int i=0;i<50000;i++)
    // 	    {
    // 		values2[i] = Math.pow(tablo[i] - moyenne,2);
    // 	    }

    // 	double 	moyenne2=  check.mean(values2);


    // 	for (int i =0; i<tablo.length;i++)
    // 	    {
    // 		System.out.println(tablo[i]);
    // 	    }

    System.out.println("Mean = " + check.mean(tablo));
    double variance = check.variance(tablo);
    System.out.println("Variance = " + variance);
    System.out.println("Std = " + Math.sqrt(variance));
  



    // 	for (int i =0; i< max ;i++)
    // 	{
    // 	    values[i] =  expo.rand();
    // 	}

    // 	moyenne = ExponentialLaw.moyenne(values);
    // 	System.out.println(" Moyenne = " + moyenne);

	
    // 	for (int i=0;i<max;i++)
    // 	    {
    // 		values2[i] = Math.pow(values[i] - moyenne,2);
    // 	    }

    // 	moyenne2=  ExponentialLaw.moyenne(values2);
    // 	System.out.println(" Moyenne2Square = " + moyenne2);
    // 	System.out.println("Sqrt(moyenne2) = " + Math.sqrt(moyenne2));
    // 	//System.out.println(" MoyenneSquare = " + Math.pow(ExponentialLaw.moyenne(values2),2) );
    // 	//	System.out.println(" Variance = " + (totalSquare/max - Math.pow(total/max,2) ) );

  }
}

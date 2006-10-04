package org.objectweb.proactive.ext.scilab.test;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

import javasci.SciData;
import javasci.Scilab;

import org.objectweb.proactive.ext.scilab.util.SciMath;

class  SciTestSeqPi {
    
    public static void main(String[] args) throws Exception{
      
       if(args.length != 2){
    	   System.out.println("Invalid number of parameter : " + args.length);
    	   return;
       }
       	
       BufferedReader reader = new BufferedReader(new FileReader(args[0]));
       PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(args[1])));
		
       int precision; 
       String line;
       double startTime;
       double endTime;
       
       while((line = reader.readLine()) != null){
    	   
    	   if(line.trim().startsWith("#"))
				continue;
    	   
    	   if(line.trim().equals(""))
    		  break;
    	  
    	   precision = Integer.parseInt(line);
    	   startTime = System.currentTimeMillis();
    	   Scilab.exec(SciMath.formulaPi("pi", 0, precision));
    	   SciData sciPi = (SciData) Scilab.receiveDataByName("pi");
    	   endTime = System.currentTimeMillis();
    	   
    	   System.out.println(sciPi);
    	   writer.println(precision  + " " +(endTime - startTime));
       }
       
       reader.close();
       writer.close();
   }
}
  
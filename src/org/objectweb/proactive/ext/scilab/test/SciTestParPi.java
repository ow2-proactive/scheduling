package org.objectweb.proactive.ext.scilab.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

import org.objectweb.proactive.ext.scilab.monitor.ScilabService;
import org.objectweb.proactive.ext.scilab.util.FutureDoubleMatrix;
import org.objectweb.proactive.ext.scilab.util.GridMatrix;

public class SciTestParPi {
	
	public static void main(String[] args) throws Exception {
		ScilabService service = new  ScilabService();
		
		if(args.length !=5){
			System.out.println("Invalid number of parameter : " + args.length);
			return;
		}
		
		int nbEngine = Integer.parseInt(args[2]);
		service.deployEngine(args[0], args[1], nbEngine);
		
		BufferedReader reader = new BufferedReader(new FileReader(args[3]));
		PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(args[4])));
		
		int precision; 
		int nbBloc;
		String line;
		String arrayLine[];
		FutureDoubleMatrix piResult;
		
		double startTime;
		double endTime;
		double result;
		
		for(int i = 0; (line = reader.readLine()) != null; i++){
			if(line.trim().startsWith("#"))
				continue;
			
			
			if(line.trim().equals(""))
				break;
			
			arrayLine = line.split(" ");
			nbBloc = Integer.parseInt(arrayLine[0]);
			precision = Integer.parseInt(arrayLine[1]);
			
			startTime = System.currentTimeMillis();
			piResult = GridMatrix.calPi(service, "calPi" + i, precision, nbBloc);
			result = piResult.get()[0];
			service.removeAllEventListenerTask();
			endTime = System.currentTimeMillis();
			System.out.println("Pi = " + result);
			writer.println(nbEngine + " " + precision + " " + nbBloc + " " + (endTime - startTime));
			
		}
		
		reader.close();
		writer.close();
		service.exit();
		System.exit(0);
	}
	

}

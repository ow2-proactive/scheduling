package org.objectweb.proactive.ext.scilab.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

import javasci.SciData;
import javasci.Scilab;

import org.objectweb.proactive.ext.scilab.util.SciMath;

public class SciTestSeqMandel {

	public static void main(String[] args) throws Exception {
		
		if(args.length !=8){
			System.out.println("Invalid number of parameter : " + args.length);
			return;
		}
		
		BufferedReader reader = new BufferedReader(new FileReader(args[6]));
		PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(args[7])));
		
		int xres = Integer.parseInt(args[0]);  
		int yres = Integer.parseInt(args[1]); 
		double xmin = Double.parseDouble(args[2]) ;
		double xmax = Double.parseDouble(args[3]) ; 
		double ymin = Double.parseDouble(args[4]) ; 
		double ymax = Double.parseDouble(args[5]) ;
		int precision;
	
		String line;
		double startTime;
		double endTime;
		
		for(int i = 0; (line = reader.readLine()) != null; i++){
			
			if(line.trim().startsWith("#"))
				continue;
			
			if(line.trim().equals(""))
				break;
			
			precision = Integer.parseInt(line.trim());
			
			startTime = System.currentTimeMillis();
			Scilab.exec(SciMath.formulaMandelbrot("Fract", xres, yres, xmin, xmax, ymin, ymax, precision));
			SciData sciFract = (SciData) Scilab.receiveDataByName("Fract");
			endTime = System.currentTimeMillis();
			
			System.out.println(xres +" " + yres + " " + precision + " " + (endTime - startTime));
			writer.println(xres +" " + yres + " " + precision + " " + (endTime - startTime));
		}
		
		reader.close();
		writer.close();
	}

}

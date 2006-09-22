package org.objectweb.proactive.ext.scilab.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

import javasci.SciData;
import javasci.SciDoubleMatrix;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.ext.scilab.SciEngineWorker;
import org.objectweb.proactive.ext.scilab.SciResult;
import org.objectweb.proactive.ext.scilab.SciTask;

public class SciTest4 {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.out.println("Invalid number of parameter : " + args.length);
			return;
		}
		
		
		SciEngineWorker sciEngineWorker  =  (SciEngineWorker) ProActive.newActive(SciEngineWorker.class.getName(), null);
		
		BufferedReader reader = new BufferedReader(new FileReader(args[0]));
		PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(args[1])));

		double[] m1;
		double[] m2;

		String line;
		int nbRow;
		int nbCol;
		
		double startTime;
		double endTime;
		
		while ((line = reader.readLine()) != null) {
			if(line.trim().startsWith("#"))
				continue;
			
			if (line.trim().equals(""))
				break;

			nbRow = Integer.parseInt(line);
			nbCol = Integer.parseInt(line);

			m1 = new double[nbRow * nbCol];
			m2 = new double[nbRow * nbCol];
			for (int i = 0; i < nbRow * nbCol; i++) {
				m1[i] = (double) (Math.random() * 10);
				m2[i] = (double) (Math.random() * 10);
			}
			
			SciDoubleMatrix sciMatrix1 = new SciDoubleMatrix("M1", nbRow, nbCol, m1);
			SciDoubleMatrix sciMatrix2 = new SciDoubleMatrix("M2", nbRow, nbCol, m2);
			SciData sciMatrix3 = new SciData("M3");
			
			SciTask sciTask = new SciTask("mult");
			sciTask.addDataIn(sciMatrix1);
			sciTask.addDataIn(sciMatrix2);
			sciTask.addDataOut(sciMatrix3);
			sciTask.setJob(sciMatrix3.getName() + "=" + sciMatrix1.getName() + "*" + sciMatrix1.getName() + ";");
			
			startTime = System.currentTimeMillis();
			
			SciResult sciResult = sciEngineWorker.execute(sciTask);
			sciMatrix3 = (SciData) sciResult.getList().get(0);
			
			endTime = System.currentTimeMillis();
			
			//System.out.println(sciMatrix3);
	    	writer.println(nbRow  + " " + (endTime - startTime));
		}
		
		reader.close();
		writer.close();
		
		System.exit(0);
	}

}

package org.objectweb.proactive.ext.scilab.util;

public class SciMath {

	public static String formulaPi(String pi, int iBloc, int  sizeBloc){
		return 
		    pi + " = 0;" +
	        "j = " + iBloc * sizeBloc + ";" +  
		    "n = j + " + sizeBloc + ";" +
		    "for i = j:n, " + 
		    pi + " = " + pi + " + ((-1)**i/(2**(10*i))* (- (2**5/(4*i+1)) - (1/(4*i+3)) + (2**8/(10*i+1)) - (2**6/(10*i+3)) - (2**2/(10*i+5))  - (2**2/(10*i+7)) + (1/(10*i+9))));" +
		    "end;" +
		    pi + " = " + pi + "/(2**6);";	
	}

	public static String formulaMandelbrot(String name, int nbRow, int nbCol, double xmin, double xmax, double ymin, double ymax, int precision){
		return
		name + "("+ nbRow +","+ nbCol+ ") = -1; "+
		"xres = " + (xmax - xmin)/nbCol + "; "+ 
		"yres = " + (ymax - ymin)/nbRow + "; "+ 
		"a = " + xmin + "; "+
		"for i = 1:" + nbCol + ", " +
		    "a = a + xres; " +
		    "b = " + ymin + "; "+
			"for j = 1:" + nbRow + ", " +
				"b = b + yres; " + 
			    "x = 0; " +
				"y = 0; " + 
				"for k = 0:" + precision + ", " +
					"tmp = x; "+
					"x = (x**2) - (y**2) + a; "+
					"y = (2 * tmp * y) + b; " +
					"tmp = (x**2) + (y**2); "+
					"if tmp < 4 then " +
						name + "(j,i) = k; "+
					"end, "+
				"end; " +
			"end; " +
		"end; ";
	}
}

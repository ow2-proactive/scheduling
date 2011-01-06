package ScilabObjects;

import java.lang.reflect.Array;

public class ScilabJavaArray {

    public Object[] array;
    public int[] dims;
    public String name;

    private static int q = 0;
	
    public ScilabJavaArray() {
    }

    private ScilabJavaArray(String name, Object array, int[] dims) {
	this.name = name;
	this.array = (Object[]) array;
	this.dims = dims;
    }

    public static int newInstance(String className, int[] dims) throws ScilabJavaException {
	Class cl = ScilabJavaObject.arraySJO[ScilabClassLoader.loadJavaClass(className)].clazz;
	if (dims.length > 2) {
	    int len = dims.length;
	    int a = dims[0], b = dims[1];
	    System.arraycopy(dims, 2, dims, 0, len - 2);
	    dims[len - 2] = a;
	    dims[len - 1] = b;
	}
	return new ScilabJavaObject(new ScilabJavaArray(className, Array.newInstance(cl, dims), dims)).id;
    }

    public Object get(double[] tab) {
	Object obj = array;
	for (int i = 2; i < dims.length; i++) {
	    obj = Array.get(obj, (int) tab[i] - 1);
	}

	obj = Array.get(obj, (int) tab[0] - 1);
	if (dims.length > 1) {
	    obj = Array.get(obj, (int) tab[1] - 1);
	}

	return obj;
    }

    public void set(double[] tab, Object elem) {
	Object obj = array;
	for (int i = 2; i < dims.length; i++) {
	    obj = Array.get(obj, (int) tab[i] - 1);
	}
	
	if (dims.length == 1) {
	    Array.set(obj, (int) tab[0] - 1, elem);
	} else {
	    obj = Array.get(obj, (int) tab[0] - 1);
	    Array.set(obj, (int) tab[1] - 1, elem);
	}
    }

    public Object[] getArray() {
        return array;
    }


    public String[] toStrings() {
	int len = dims.length;
	String[] ret;
	String str = " " + name;
	for (int i = 0; i < len; i++) {
	    str += "[]";
	}
	str += ":\n";
	if (len == 1) {
	    ret = new String[1];
	    for (int i = 0; i < dims[0]; i++) {
		if (array[i] != null) {
		    str += "     [" + array[i] + "]";
		} else {
		    str += "     []";
		}
	    }
	    ret[0] = str;
	} else if (len == 2) {
	    ret = new String[1];
	    Object[][] arr = (Object[][])array;
	    for (int i = 0; i < dims[0]; i++) {
		for (int j = 0; j < dims[1]; j++) {
		    if (arr[i][j] != null) {
			str += "     [" + arr[i][j] + "]";
		    } else {
			str += "     []";
		    }
		}
		str += "\n";
	    }
	    ret[0] = str;
	} else {
	    int p = 1;
	    for (int i = 0; i < dims.length - 2; p *= dims[i++]);
	    ret = new String[p];
	    q = 0;
	    displayArray(ret, array, " (:,:,", 0);
	    ret[0] = str + ret[0];
	}
	return ret;
    }
	
    private void displayArray(String[] ret, Object[] arr, String ind, int n) {
	if (n < dims.length - 2) {
	    String s = (n < dims.length - 3) ? "," : "";
	    for (int i = 0; i < dims[n]; i++) {
		displayArray(ret, (Object[]) arr[i], ind + (i + 1) + s, n + 1);
	    }
	} else {
	    String str = ind + ") = \n\n";
	    Object[][] arra = (Object[][]) arr;
	    for (int k = 0; k < dims[dims.length - 2]; k++) {
		for (int l = 0; l < dims[dims.length - 1]; l++) {
		    if (arra[k][l] != null) {
			str += "     [" + arra[k][l] + "]";
		    } else {
			str += "     []";
		    }
		}
		str += "\n";
	    }
	    ret[q++] = str;	
	}
    }
}
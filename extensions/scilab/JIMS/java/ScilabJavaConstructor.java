package ScilabObjects;

import java.util.HashMap;
import java.util.Map;
import java.util.LinkedList;
import java.util.List;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.InvocationTargetException;

public class ScilabJavaConstructor {

    protected Map<Class[], Constructor> constructors = new HashMap<Class[], Constructor>();
    protected Class<?> cl;

    public ScilabJavaConstructor(Class cl) {
	this.cl = cl;
    }

    public Object invoke(int[] args) throws ScilabJavaException {
	int nbargs = args.length;
	Class[] cls = new Class[nbargs];
	Object[] argsO = new Object[nbargs];

	for (int i = 0; i < nbargs; i++) {
	    argsO[i] = ScilabJavaObject.arraySJO[args[i]].object;
	    cls[i] = ScilabJavaObject.arraySJO[args[i]].clazz;
	}
	
	try {
	    Constructor c = constructors.get(cls);
	    if (c != null) {
		return c.newInstance(argsO);
	    } else {
		c = cl.getConstructor(cls);
		constructors.put(cls, c);
		return c.newInstance(argsO);
	    }
	} catch (IllegalAccessException e) {
	    throw new ScilabJavaException("Illegal access to the constructor of class " + cl.getName() + ".");
	} catch (IllegalArgumentException e) {
	    throw new ScilabJavaException("Illegal argument in the constructor of class " + cl.getName() + " : \n" + e.getMessage());
	} catch (InstantiationException e) {
	    throw new ScilabJavaException("The class " + cl.getName() + "is abstract and cannot be instantiated.");
	} catch (ExceptionInInitializerError e) {
	    throw new ScilabJavaException("Initializer error with constructor of class " + cl.getName() + " :\n" + e.getMessage());
	} catch (InvocationTargetException e) {
	    throw new ScilabJavaException("An exception has been thrown in calling the constructor of class " + cl.getName() + " :\n" + e.getMessage());
	} catch (NoSuchMethodException e) {
        String argsString = "";
        for (Class acl : cls) {
          argsString += " " + acl.getName();
        }
	    throw new ScilabJavaException("No constructor in the class " + cl.getName() + " with arguments "+argsString);
	}
    }
}
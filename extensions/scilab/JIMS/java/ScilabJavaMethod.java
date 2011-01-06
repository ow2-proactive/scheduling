package ScilabObjects;

import java.util.HashMap;
import java.util.Map;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.InvocationTargetException;

public class ScilabJavaMethod {

    protected Map<Class[], Method> methods = new HashMap<Class[], Method>();
    protected Class<?> clazz;
    protected String name;

    //We keep in memory the map between class and accessible methods
    private static Map<Class, Map<String, ScilabJavaMethod>> methsInClass = new HashMap<Class, Map<String, ScilabJavaMethod>>();

    public ScilabJavaMethod(Class clazz, String name) {
        this.clazz = clazz;
        this.name = name;
    }

    public Object invoke(Object obj, Class[] returnType, int[] args) throws ScilabJavaException {
        int nbargs = args.length;
        Class[] cl = new Class[nbargs];
        Object[] argsO = new Object[nbargs];

        for (int i = 0; i < nbargs; i++) {
            argsO[i] = ScilabJavaObject.arraySJO[args[i]].object;
            cl[i] = ScilabJavaObject.arraySJO[args[i]].clazz;
        }

        return call(obj, returnType, argsO, cl);
    }

    protected Method findMethod(Class[] argsClass,Object[] args) {

        Method[] all = clazz.getMethods();
        for (Method meth : all) {
             if (meth.getName().equals(name)) {
                 Class[] types = meth.getParameterTypes();
                 if (types.length == argsClass.length) {
                     boolean ok=true;
                     for (int i=0; i < types.length; i++) {
                         if (argsClass[i].equals(Void.class) || (args[i] == null) || argsClass[i].getName().equals("void")) {

                         } else if (types[i].isAssignableFrom(argsClass[i])) {

                         } else {
                             ok = false;
                             break;
                         }
                     }
                     if (ok) {
                         return meth;
                     }
                 }
             }
        }
        return null;
    }

    protected Object call(Object obj, Class[] returnType, Object[] args, Class[] argsClass) throws ScilabJavaException {
        try {
            Method meth = methods.get(argsClass);
            if (meth != null) {
                returnType[0] = meth.getReturnType();
                if (Modifier.isStatic(meth.getModifiers())) {
                    return meth.invoke(null, args);
                }
                return meth.invoke(obj, args);
            }

            meth = findMethod(argsClass,args);
            if (meth == null) {
                meth = clazz.getMethod(name, argsClass);
            }
            methods.put(argsClass, meth);
            returnType[0] = meth.getReturnType();

            if (Modifier.isStatic(meth.getModifiers())) {
                return meth.invoke(null, args);
            }
            return meth.invoke(obj, args);
        } catch (IllegalAccessException e) {
            throw new ScilabJavaException("Illegal access to the method " + name + ".");
        } catch (IllegalArgumentException e) {
            throw new ScilabJavaException("Illegal argument in the method " + name + " : \n" + e.getMessage());
        } catch (NullPointerException e) {
            throw new ScilabJavaException("The method " + name + " is called on a null object.");
        } catch (ExceptionInInitializerError e) {
            throw new ScilabJavaException("Initializer error with method " + name + " :\n" + e.getMessage());
        } catch (InvocationTargetException e) {
            throw new ScilabJavaException("An exception has been thrown in calling the method " + name + " :\n" + e.getCause().toString());
        } catch (NoSuchMethodException e) {
            /* In scilab it could be boring to write str.substring(int32(1),int32(5))
              so we search if a double is an integer and we convert it into an int
              and retry to invoke the method */
            boolean modified = false;
            for (int i = 0; i < args.length; i++) {
                if (argsClass[i] == double.class && ((Double) args[i]).intValue() == ((Double) args[i]).doubleValue()) {
                    argsClass[i] = int.class;
                    args[i] = ((Double) args[i]).intValue();
                    modified = true;
                }
            }
            if (modified) {
                return call(obj, returnType, args, argsClass);
            }

            String argsString = "";
            for (Class acl : argsClass) {
                argsString += " " + acl.getName();
            }
            throw new ScilabJavaException("No method " + name + " in the class " + clazz.getName() + " with arguments : " + argsString);
        }
    }

    public static Map<String, ScilabJavaMethod> getMethods(Class clazz) {
        Map<String, ScilabJavaMethod> hm = methsInClass.get(clazz);

        if (hm != null) {
            return hm;
        }

        hm = new HashMap<String, ScilabJavaMethod>();
        Method[] meth = clazz.getMethods();

        for (int i = 0; i < meth.length; i++) {
            int modif = meth[i].getModifiers();
            //  if (Modifier.isPublic(modif) && !Modifier.isAbstract(modif)) {
            if (Modifier.isPublic(modif)) {
                String name = meth[i].getName();
                if (!hm.containsKey(name)) {
                    hm.put(name, new ScilabJavaMethod(clazz, name));
                }
            }
        }

        methsInClass.put(clazz, hm);

        return hm;
    }
}
package ScilabObjects;

import java.util.HashMap;
import java.util.Map;

public class ScilabClassLoader extends ClassLoader {

    protected static Map<String, Integer> clazz = new HashMap<String, Integer>();
    protected static ScilabClassLoader scl = new ScilabClassLoader();

    private ScilabClassLoader() {
        super(ScilabClassLoader.class.getClassLoader());
    }

    public static int loadJavaClass(String name) throws ScilabJavaException {
        Integer id = clazz.get(name);
        if (id != null) {
            return id;
        } else {
            try {
                ScilabJavaClass sjc = new ScilabJavaClass(Class.forName(name, false, scl));
               //ScilabJavaClass sjc = new ScilabJavaClass(scl.loadClass(name, true));
                clazz.put(name, sjc.id);
                return sjc.id;
            } catch (ClassNotFoundException e) {
                throw new ScilabJavaException("Cannot find the class " + name + ". Check the name or if the classpath contains it.");
            }
        }
    }
}
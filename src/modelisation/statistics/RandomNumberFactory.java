package modelisation.statistics;

import java.lang.reflect.Constructor;

public class RandomNumberFactory {

    protected static String getFactoryName(String variableName) {
        return System.getProperties().getProperty(variableName + ".law");
    }

    protected static RandomNumberGenerator _getDefaultGenerator() {
        return new ExponentialLaw();
    }

    public static RandomNumberGenerator getGenerator(String variableName) {
        RandomNumberGenerator tmp = null;
        String className = getFactoryName(variableName);
        try {
            Class[] argsClass = new Class[] {String.class};
            Object[] args = new Object[] {variableName};
            Class factoryClass = Class.forName(className);
            Constructor constructor = factoryClass.getConstructor(argsClass);
            tmp = (RandomNumberGenerator) constructor.newInstance(args);
            System.out.println("RandomNumberFactory: generator for " +
                               variableName + " is  " + tmp);
            return tmp;
        } catch (Exception e) {
        }
        tmp = _getDefaultGenerator();
        System.out.println("RandomNumberFactory: default generator for " +
                           variableName + " is  " + tmp);
        return tmp;
    }

    public static void main(String[] args) {
        RandomNumberGenerator gen = RandomNumberFactory.getGenerator("lambda");
        System.out.println(gen);
        gen.initialize(Double.parseDouble(args[0]));
        for (int i = 0; i < 1000; i++) {
            System.out.println(gen.next());
        }
    }
}

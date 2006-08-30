package org.objectweb.proactive.core.component.gen;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.List;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.component.exceptions.InterfaceGenerationFailedException;
import org.objectweb.proactive.core.component.type.ProActiveInterfaceType;
import org.objectweb.proactive.core.util.ClassDataCache;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;

public class GatherInterfaceGenerator {
    
    protected static final transient ClassPool pool = ClassPool.getDefault();
    private static Logger gatherLogger = ProActiveLogger.getLogger(Loggers.COMPONENTS_GATHERCAST);
    
    public static Class generateInterface(ProActiveInterfaceType itfType) throws InterfaceGenerationFailedException {
        Class generated = null;
        String gatherProxyItfName = Utils.getGatherProxyItfClassName(itfType);
        try {
            
//          try to fetch the class from the default class loader
            generated = Thread.currentThread().getContextClassLoader().loadClass(gatherProxyItfName);
        } catch (ClassNotFoundException cnfe) {
            byte[] bytecode = generateInterfaceByteCode(gatherProxyItfName);

            try {
            // convert the bytes into a Class
            generated = Utils.defineClass(gatherProxyItfName, bytecode);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        return generated;
    }
    
    

    
    static byte[] generateInterfaceByteCode(String gatherProxyItfName) {
        if (ClassDataCache.instance().getClassData(gatherProxyItfName) != null) {
            return ClassDataCache.instance().getClassData(gatherProxyItfName);
        }
        try {
            Class serverItfClass = Class.forName(Utils.getInterfaceSignatureFromGathercastProxyClassName(gatherProxyItfName));
            CtClass repGatherItfClass = pool.makeInterface(gatherProxyItfName);
        Method[] serverItfMethods = serverItfClass.getMethods();
//        CtMethod[] resultingServerItfMethods = new CtMethod[serverItfMethods.length];
        CtMethod[] repServerItfMethods = new CtMethod[serverItfMethods.length];

        for (int i = 0; i < serverItfMethods.length; i++) {
            // get parameterizing types for return type and parameters
            CtClass repReturnType = null;
            java.lang.reflect.Type returnType = serverItfMethods[i].getGenericReturnType();
            CtClass[] repParameterTypes = new CtClass[serverItfMethods[i].getParameterTypes().length];
            // return type
            if (Void.TYPE == returnType) {
                repReturnType = CtClass.voidType;
            } else {
                if (!(returnType instanceof ParameterizedType)) {
                    throw new InterfaceGenerationFailedException("gather method " + serverItfMethods[i].toGenericString() + " in gather interface of signature " + serverItfClass.getName() + " must return a parameterized list or void");
                }
                
                if (!(List.class.isAssignableFrom((Class)((ParameterizedType)returnType).getRawType()))) {
                    throw new InterfaceGenerationFailedException("gather method " + serverItfMethods[i].toGenericString() + " in gather interface " + serverItfClass.getName() + " must return a parameterized list or void");
                }
                java.lang.reflect.Type[] actualTypeArguments = ((ParameterizedType)returnType).getActualTypeArguments();
                if (actualTypeArguments.length != 1) {
                    throw new InterfaceGenerationFailedException("gather method " + serverItfMethods[i].toGenericString() + " in gather interface " + serverItfClass.getName() + " must return a parameterized type with one parameter");
                }
                repReturnType = pool.get(((Class)(((ParameterizedType)returnType).getActualTypeArguments()[0])).getName());
            }
            // parameters types
            java.lang.reflect.Type[] paramTypes = serverItfMethods[i].getGenericParameterTypes();
            for (int j = 0; j < paramTypes.length; j++) {
                java.lang.reflect.Type paramType = paramTypes[j];
                if (!(paramType instanceof ParameterizedType)) {
                    throw new InterfaceGenerationFailedException("gather method " + serverItfMethods[i].toGenericString() + " in gather interface " + serverItfClass.getName() + " must have type-parameterized parameter types");
                }
                java.lang.reflect.Type[] actualTypeArguments = ((ParameterizedType)paramType).getActualTypeArguments();
                if (actualTypeArguments.length != 1) {
                    throw new InterfaceGenerationFailedException("gather method " + serverItfMethods[i].toGenericString() + " in gather interface " + serverItfClass.getName() + " must have type-parameterized parameters with only one parameterizing element");
                }
                repParameterTypes[j] = pool.get(((Class)actualTypeArguments[0]).getName());
            }
            
            // exceptions
            Class[] exceptions = serverItfMethods[i].getExceptionTypes();
            CtClass[] repExceptions = new CtClass[exceptions.length];
            for (int j = 0; j < exceptions.length; j++) {
                repExceptions[j] = pool.get(exceptions[j].getName());
            }
            
            
            repServerItfMethods[i] = CtNewMethod.abstractMethod(repReturnType, serverItfMethods[i].getName(), repParameterTypes, repExceptions, repGatherItfClass);
            
            repGatherItfClass.addMethod(repServerItfMethods[i]);
//            System.out.println("[GATHER_ITF_GEN] added method " + repServerItfMethods[i] + " matching method " + serverItfMethods[i]);
            
        }

//        repGatherItfClass.writeFile("generated/");
//        System.out.println("[JAVASSIST] generated gather interface : " +
//        		gatherProxyItfName);
        
        byte[] bytecode = repGatherItfClass.toBytecode();
        ClassDataCache.instance().addClassData(gatherProxyItfName,
        		repGatherItfClass.toBytecode());
        
//            System.out.println("added " + gatherProxyItfName + " to cache");
//            System.out.println("cache is now " + ClassDataCache.instance().toString());
        



//        
        return bytecode; 
        } catch (Exception e) {
            e.printStackTrace();
            gatherLogger.error("cannot generate gather proxy interface class " + gatherProxyItfName );
            return null;
        }
        
    }

}

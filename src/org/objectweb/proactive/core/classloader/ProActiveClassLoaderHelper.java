package org.objectweb.proactive.core.classloader;

import org.apache.log4j.Logger;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.component.asmgen.MetaObjectInterfaceClassGenerator;
import org.objectweb.proactive.core.component.asmgen.RepresentativeInterfaceClassGenerator;
import org.objectweb.proactive.core.mop.ASMBytecodeStubBuilder;
import org.objectweb.proactive.core.mop.BytecodeStubBuilder;
import org.objectweb.proactive.core.mop.MOPClassLoader;
import org.objectweb.proactive.core.mop.Utils;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.util.ClassDataCache;
import org.objectweb.proactive.core.util.Loggers;

import java.util.Hashtable;


/**
 * <p>Instances of this class are created and used from the ProActiveClassLoader by reflection,
 * in order to work in a dedicated namespace.</p>
 *
 * <p>This class provides a method for getting the bytecode of a given class, from the cache, from other
 * runtimes, or by generating it.</p>
 *
 *
 * @author Matthieu Morel
 *
 */
public class ProActiveClassLoaderHelper {
    private static Logger logger = Logger.getLogger(Loggers.CLASSLOADER);
    private Hashtable localClassDataCache = new Hashtable();
    private ClassDataCache classCache;

    public ProActiveClassLoaderHelper() {
        classCache = ClassDataCache.instance();
    }

    /**
     * Looks for the bytecode of the given class in different places :
     * 1. cache
     * 2. runtime parents
     * 3. tries to generate it (stub, component interface representative, or component interface metaobject)
     */
    public byte[] getClassData(String className) throws ClassNotFoundException {
        byte[] class_data = null;

        // 1. look in class cache
        debug("looking for " + className + "  in class data cache");
        class_data = classCache.getClassData(className);
        if (class_data != null) {
            debug("found " + className + " in class data cache");
            return class_data;
        }

        // 2. look in runtime parents
        try {
            debug("looking for " + className + " in parent runtimes");

            class_data = ProActiveRuntimeImpl.getProActiveRuntime()
                                             .getClassDataFromParentRuntime(className);
            if (class_data != null) {
                debug("found " + className + " in ancestor runtime");
                return class_data;
            }
        } catch (Exception e) {
            e.printStackTrace();
            // continue
        }

        // 3. standard proactive stub?
        if (Utils.isStubClassName(className)) {
            // do not use directly MOP methods
            logger.info("Generating class : " + className);
            //    e.printStackTrace();
            String classname = Utils.convertStubClassNameToClassName(className);

            //ASM is now the default bytecode manipulator
            if (MOPClassLoader.BYTE_CODE_MANIPULATOR.equals("ASM")) {
                ASMBytecodeStubBuilder bsb = new ASMBytecodeStubBuilder(classname);
                class_data = bsb.create();
            } else if (MOPClassLoader.BYTE_CODE_MANIPULATOR.equals("BCEL")) {
                BytecodeStubBuilder bsb = new BytecodeStubBuilder(classname);
                class_data = bsb.create();
            } else {
                // that shouldn't happen, unless someone manually sets the BYTE_CODE_MANIPULATOR static variable
                System.err.println(
                    "byteCodeManipulator argument is optionnal. If specified, it can only be set to BCEL.");
                System.err.println(
                    "Any other setting will result in the use of ASM, the default bytecode manipulator framework");
            }
            if (class_data != null) {
                classCache.addClassData(className, class_data);
                return class_data;
            }
        }

        if (class_data != null) {
            return class_data;
        }

        // 4. component representative?
        class_data = RepresentativeInterfaceClassGenerator.getClassData(className);

        if (class_data != null) {
            classCache.addClassData(className, class_data);
            return class_data;
        }

        // 5. component metaobject interface?
        class_data = MetaObjectInterfaceClassGenerator.getClassData(className);
        if (class_data != null) {
            classCache.addClassData(className, class_data);
            return class_data;
        }

        throw new ClassNotFoundException(className);
    }

    private void debug(String message) {
        if (logger.isDebugEnabled()) {
            try {
                logger.debug(ProActiveRuntimeImpl.getProActiveRuntime().getURL() +
                    " --> " + message);
            } catch (ProActiveException e) {
                logger.debug("[unresolved runtime url] -- > " + message);
            }
        }
    }
}

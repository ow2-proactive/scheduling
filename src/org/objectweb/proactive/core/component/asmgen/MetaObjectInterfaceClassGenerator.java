package org.objectweb.proactive.core.component.asmgen;

import org.apache.log4j.Logger;

import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Type;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.type.InterfaceType;

import org.objectweb.proactive.core.component.ProActiveInterface;
import org.objectweb.proactive.core.component.exceptions.InterfaceGenerationFailedException;

import java.io.Serializable;

import java.lang.reflect.Method;

import java.util.Hashtable;
import java.util.Vector;


/**
 * Creates Interface implementations for the functional interfaces of the
 * component metaobject.
 *<br>
 * The functional calls are delegated to the "impl" field, whose value is set during
 * binding operations.
 *<br>
 * - In case of a primitive component, the impl field will be the reified object to
 * which the body is attached.<br>
 * - In case of a composite component, the impl field will be a component
 * representative.<br>
 * - For a parallel component, the impl field will be a group of component representatives.<br>
 *
 *  @author Matthieu Morel
 *
 */
public class MetaObjectInterfaceClassGenerator
    extends AbstractInterfaceClassGenerator {
    protected static Logger logger = Logger.getLogger(MetaObjectInterfaceClassGenerator.class.getName());
    protected static final String IMPL_FIELD_NAME = "impl"; //delegatee
    private static MetaObjectInterfaceClassGenerator instance;

    // generatedClassesCache that contains all the generated classes according to their name
    private static Hashtable generatedClassesCache = new Hashtable();

    // Those fields contain information about the class
    // for which we are building a wrapper
    protected Class cl;
    protected String className;
    protected String packageName;

    // this boolean for deciding of a possible indirection for the functionnal calls
    protected boolean isPrimitive = false;

    public MetaObjectInterfaceClassGenerator() {
        // Obtains the object that represents the type we want to create
        // a wrapper class for. This call may fail with a ClassNotFoundException
        // if the class corresponding to this type cannot be found.
        this.cl = ProActiveInterface.class;

        // Keep this info at hand for performance purpose
        this.className = cl.getName();

        //generatedClassesCache = new Hashtable();
    }

    public static MetaObjectInterfaceClassGenerator instance() {
        if (instance == null) {
            return new MetaObjectInterfaceClassGenerator();
        } else {
            return instance;
        }
    }

    /**
     * retreives the bytecode associated to the generated class of the given name
     */
    public static byte[] getClassData(String classname) {
        return (byte[]) getGeneratedClassesCache().get(classname);
    }

    /**
             * Returns the generatedClassesCache.
             * @return Hashtable
             */
    public static Hashtable getGeneratedClassesCache() {
        return generatedClassesCache;
    }

    public ProActiveInterface generateInterface(final String fcInterfaceName,
        Component owner, InterfaceType interfaceType, boolean isInternal,
        boolean isPrimitive) throws InterfaceGenerationFailedException {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("generating metaobject interface reference");
            }

            //isPrimitive = ((ProActiveComponent) owner).getHierarchicalType().equals(ComponentParameters.PRIMITIVE);
            interfacesToImplement = new Vector();

            // add functional interface
            interfacesToImplement.add(Class.forName(
                    interfaceType.getFcItfSignature()));

            // add Serializable interface
            interfacesToImplement.addElement(Serializable.class);

            this.stubClassFullName = org.objectweb.proactive.core.component.asmgen.Utils.getMetaObjectClassName(fcInterfaceName,
                    interfaceType.getFcItfSignature());

            Class generated_class;

            // check whether class has already been generated
            try {
                generated_class = loadClass(stubClassFullName);
            } catch (ClassNotFoundException cnfe) {
                byte[] bytes;
                setInfos();
                bytes = create();
                getGeneratedClassesCache().put(stubClassFullName, bytes);
                if (logger.isDebugEnabled()) {
                    logger.debug("added " + stubClassFullName + " to cache");
                    logger.debug("generated classes cache is : " +
                        getGeneratedClassesCache().toString());
                }

                //                // Next few lines for debugging only
                //                			try {
                //                				//java.io.File file = new java.io.File(System.getProperty("user.home") + "/ProActive/generated/" + stubClassFullName + ".class");
                //                				java.io.File file = new java.io.File("generated/" + stubClassFullName + ".class");
                //                
                //                				java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
                //                				fos.write(bytes);
                //                				fos.close();
                //                			} catch (Exception e) {
                //                				// e.printStackTrace();
                //                				logger.info("if you want a dump of the generated classes, you need to create a /generated folder at the root of you command");
                //                			}
                // convert the bytes into a Class
                generated_class = defineClass(stubClassFullName, bytes);
            }

            ProActiveInterface reference = (ProActiveInterface) generated_class.newInstance();
            reference.setName(fcInterfaceName);
            reference.setOwner(owner);
            reference.setType(interfaceType);
            reference.setIsInternal(isInternal);

            return reference;
        } catch (ClassNotFoundException e) {
            throw new InterfaceGenerationFailedException("cannot find interface signature class",
                e);
        } catch (IllegalAccessException e) {
            throw new InterfaceGenerationFailedException("constructor not accessible",
                e);
        } catch (InstantiationException e) {
            throw new InterfaceGenerationFailedException("constructor belongs to an abstract class?",
                e);
        }
    }

    protected void createGetAndSetFcItfImplMethods() {
        // Do the getFcItfImpl method first
        CodeVisitor cv = this.classGenerator.visitMethod(ACC_PUBLIC,
                "getFcItfImpl", "()" + OBJECT_TYPE, null, null);

        // Now, fills in the instruction list
        cv.visitVarInsn(ALOAD, 0);
        cv.visitFieldInsn(GETFIELD, this.stubClassFullName.replace('.', '/'),
            IMPL_FIELD_NAME, OBJECT_TYPE);
        cv.visitInsn(ARETURN);

        // Needed stack size
        // Needed locals
        cv.visitMaxs(0, 0);

        // Now, do the setProxy method
        cv = this.classGenerator.visitMethod(ACC_PUBLIC, "setFcItfImpl",
                "(" + OBJECT_TYPE + ")V", null, null);

        // Now, fills in the instruction list
        cv.visitVarInsn(ALOAD, 0);
        cv.visitVarInsn(ALOAD, 1);

        // add a cast if there is only one interface?
        cv.visitFieldInsn(PUTFIELD, this.stubClassFullName.replace('.', '/'),
            IMPL_FIELD_NAME, OBJECT_TYPE);
        cv.visitInsn(RETURN);

        // Needed stack size
        // Needed locals
        cv.visitMaxs(0, 0);

        return;
    }

    protected CodeVisitor createMethod(int methodIndex, Method m) {
        String itf = Type.getInternalName(m.getDeclaringClass());
        String method_name = m.getName();
        String method_descriptor = Type.getMethodDescriptor(m);
        CodeVisitor cv = createMethodGenerator(m);

        // step 1.
        // Pushes on the stack the reference to the impl object
        cv.visitVarInsn(ALOAD, 0);
        cv.visitFieldInsn(GETFIELD, this.stubClassFullName.replace('.', '/'),
            IMPL_FIELD_NAME, OBJECT_TYPE);

        // add a cast if there is more than 1 interface implemented
        if (interfacesToImplement.size() > 1) {
            cv.visitTypeInsn(CHECKCAST, itf);
        }

        // Step 2.	
        Class[] paramTypes = m.getParameterTypes();
        int offset = 1;
        for (int i = 0; i < paramTypes.length; ++i) {
            cv.visitVarInsn(ILOAD + getOpcodeOffset(paramTypes[i]), offset);
            offset += getSize(paramTypes[i]);
        }

        // Step 3. invoke method m
        cv.visitMethodInsn(INVOKEINTERFACE, itf, method_name, method_descriptor);

        // Step 4. return the result
        createReturnCode(cv, m.getReturnType());

        cv.visitMaxs(0, 0); // max stack and max locals automatically computed

        return cv;
    }

    protected void createFields() {
        // Creates the field that points to the delegatee
        this.classGenerator.visitField(ACC_PROTECTED, IMPL_FIELD_NAME,
            OBJECT_TYPE, null, null);
    }

    protected void createStaticVariables() {
        // no static variables
    }

    protected void createStaticInitializer() throws ClassNotFoundException {
    }

    /**
     * @see org.objectweb.proactive.core.body.component.asmgen.AbstractInterfaceClassGenerator#createDefaultMethods()
     */
    protected void createDefaultMethods() {
        createGetAndSetFcItfImplMethods();
    }
}

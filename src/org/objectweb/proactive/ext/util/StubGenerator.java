package org.objectweb.proactive.ext.util;

import java.io.File;
import java.io.FileOutputStream;

import org.objectweb.proactive.core.mop.ASMBytecodeStubBuilder;
import org.objectweb.proactive.core.mop.BytecodeStubBuilder;
import org.objectweb.proactive.core.mop.MOPClassLoader;

public class StubGenerator {
	public static void main(String[] args) {
		// This is the file into which we are about to write the bytecode for the stub
		String fileName = null;

		// Check number of arguments
		if (args.length <= 0) {
			printUsageAndExit();
		}

		// Name of the class
		String className = args[0];
		String stubClassName;

		try {
			// Generates the bytecode for the class

			//ASM is now the default bytecode manipulator
			byte[] data;
			if (MOPClassLoader.BYTE_CODE_MANIPULATOR.equals("ASM")) {
				ASMBytecodeStubBuilder bsb = new ASMBytecodeStubBuilder(className);
				data = bsb.create();
				stubClassName = bsb.getStubClassFullName();
			} else if (MOPClassLoader.BYTE_CODE_MANIPULATOR.equals("BCEL")) {
				BytecodeStubBuilder bsb = new BytecodeStubBuilder(className);
				data = bsb.create();
				stubClassName = bsb.getStubClassFullName();
			} else {
				// that shouldn't happen, unless someone manually sets the BYTE_CODE_MANIPULATOR static variable
				System.err.println(
					"byteCodeManipulator argument is optionnal. If specified, it can only be set to BCEL.");
				System.err.println(
					"Any other setting will result in the use of ASM, the default bytecode manipulator framework");
				stubClassName = null;
				data = null;
			}
			// Deals with directory name
			String directoryName;
			if (args.length == 2) {
				directoryName = args[1];
			} else {
				directoryName = ".";
			}
			// If the directory name does not end with a file separator, add one
			if (!directoryName.endsWith(System.getProperty("file.separator"))) {
				directoryName = directoryName + System.getProperty("file.separator");
			}
			char sep = System.getProperty("file.separator").toCharArray()[0];
			fileName = directoryName + stubClassName.replace('.', sep) + ".class";

			// And writes it to a file
			new File(fileName.substring(0, fileName.lastIndexOf(sep))).mkdirs();
			//	String fileName = directoryName + System.getProperty ("file.separator") + 
			File f = new File(fileName);
			FileOutputStream fos = new FileOutputStream(f);
			fos.write(data);
			fos.flush();
			fos.close();
			System.out.println("Wrote file " + fileName);
		} catch (ClassNotFoundException e) {
			System.err.println("Cannot find class " + className);
		} catch (Exception e) {
			System.err.println("Cannot write file " + fileName);
			System.err.println("Reason is " + e);
		}
	}

	public static void printUsageAndExit() {
		System.out.println(
			"usage: pac <fully-qualified name of the class> [directory where to output stub .class file]");
		System.exit(0);
	}

}

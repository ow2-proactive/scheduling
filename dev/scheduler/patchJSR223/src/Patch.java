/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Patching class for the ProActive Scheduler.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class Patch {

	private static String[] jarFiles = new String[]{"js.jar","script-api.jar","script-js.jar"};
	private static String schedulerLib = "plugins/org.ow2.proactive.scheduler.lib_1.0.0.jar";
	private static String manifestPath = "META-INF";
	private static String manifestName = "MANIFEST.MF";
	private static String manifestFile = manifestPath+"/"+manifestName;

	private static boolean fileNotExists(String jarPath){
		for (String s : jarFiles){
			if (!new File(jarPath,s).exists()){
				return true;
			}
		}
		return false;
	}

	private static boolean addEntry(File source, ZipOutputStream destination) {
		FileInputStream in = null;
		try {
			in = new FileInputStream(source);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				destination.write(buf, 0, len);
			}
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		finally {
			try {
				destination.closeEntry();
				in.close();
			}
			catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
	}

	/**
	 * Read the given file in the given zip file and return an input stream on the read file.
	 *
	 * @param zipFile The compressed file where to read the file.
	 * @param fileName the file to be read
	 * @return an input stream on the read file
	 * @throws IOException if an exception occurred during reading
	 */
	public static InputStream unzipAndReadFile(String zipFile, String fileName) throws IOException {
		ZipInputStream jarSrc = new ZipInputStream(new FileInputStream(zipFile));
		ZipEntry ze;
		ByteArrayOutputStream f = new ByteArrayOutputStream();
		while ((ze=jarSrc.getNextEntry()) != null){
			if (ze.getName().contains(fileName)){
				byte[] buf = new byte[1024];
				int len;
				while ((len = jarSrc.read(buf)) > 0) {
					f.write(buf,0, len);
				}
			}
		}
		f.close();
		jarSrc.closeEntry();
		jarSrc.close();
		return new ByteArrayInputStream(f.toByteArray());
	}

	private static void fillManifest(PrintWriter manifest) {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(unzipAndReadFile(schedulerLib,manifestFile)));
			boolean first = true;
			String line;
			while((line=br.readLine())!=null){
				if (line.contains("Export-Package:")){
					for (String s : jarFiles){
						//add script libs to manifest
						manifest.print(",lib/"+s);
					}
					manifest.println();
				} else if (line.equals("")){
					//when finished, add additional content
					manifest.println(MANIFEST_ADDITIONAL_CONTENT);
					break;
				} else if (!first){
					manifest.println();
				} else {
					first = false;
				}
				manifest.print(line);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public static Result patch(String jarPath) {
		return patch(jarPath,false);
	}

	/**
	 * Patch the given Scheduler RCP jar with scripts jars.
	 *
	 * @param jarsPath the path where to find the script jars
	 * @param force true if the jar must be patched even if the JVM version is more than 1.5
	 * @return The result of patching
	 */
	public static Result patch(String jarsPath, boolean force) {
		try {
			//check ./Scheduler
			if (!(new File("Scheduler").exists()) && !(new File("Scheduler.exe").exists()) && !(new File("Scheduler.app").exists()) || !(new File(schedulerLib).exists())){
				return new Result(false,"This patch must be executed\n in the ProActive Scheduler directory.");
			}
			//check files in jarPath
			if (!new File(jarsPath).canRead() || fileNotExists(jarsPath)){
				return new Result(false,"Required JSR-223 jar files (script-api.jar, script-js.jar and js.jar)\nare missing in '"+jarsPath+"'.");
			}
			//check java 1.5
			if (!force && !(System.getProperty("java.version").split("[.]")[1].equals("5"))){
				return new Result(false,"This script is applicable only for Java 1.5 version.\nCurrent version is '"+System.getProperty("java.version")+"'. Patch anyway ?",true);
			}
			//patch Scheduler jar
			new File(manifestPath).mkdir();
			new File(manifestFile).createNewFile();
			System.out.print("Creating "+manifestName+" file...");
			PrintWriter manifest = null;
			try {
				manifest = new PrintWriter(manifestFile);
				fillManifest(manifest);
			} catch(Exception e) {
				e.printStackTrace();
				return new Result(false,"Enable to write the '"+manifestFile+"' file !");
			}
			finally{
				manifest.close();
				System.out.println("   OK !");
			}
			//copying jars
			System.out.println("Creating JAR...");
			ZipOutputStream jarDest = new ZipOutputStream(new FileOutputStream("tmp.jar"));
			ZipInputStream jarSrc = new ZipInputStream(new FileInputStream(schedulerLib));
			ZipEntry ze = null;
			boolean alreadyPatch = false;
			while ((ze=jarSrc.getNextEntry()) != null){
				if (ze.getName().matches(".*"+jarFiles[0]+".*")){
					alreadyPatch = true;
					break;
				}
				if (!ze.getName().matches(".*"+manifestName+".*")){
					jarDest.putNextEntry(new ZipEntry(ze.getName()));
					byte[] buf = new byte[1024];
					int len;
					while ((len = jarSrc.read(buf,0,1024)) > 0) {
						jarDest.write(buf, 0, len);
					}
					jarSrc.closeEntry();
					jarDest.closeEntry();
				}
			}
			jarSrc.close();
			if (alreadyPatch){
				jarDest.close();
				new File("tmp.jar").delete();
				new File(manifestFile).delete();
				new File(manifestPath).delete();
				System.out.println("Already patched ! aborting...");
				return new Result(false,"The application seems to be already patched !");
			}
			System.out.println("Updating JAR...");
			for (String s : jarFiles){
				try {
					File f = new File(jarsPath,s);
					jarDest.putNextEntry(new ZipEntry("lib/"+s));
					System.out.println("\tAdding "+s+" to jar..."+(addEntry(f, jarDest)?"   OK !":"   FAILED !"));
					jarDest.closeEntry();
				}
				catch (Exception e) {
					e.printStackTrace();
					return new Result(false,"An error occured while creating the jar !");
				}
			}
			//update manifest
			System.out.println("Updating manifest file in "+schedulerLib+"...");
			try {
				File f = new File(manifestFile);
				jarDest.putNextEntry(new ZipEntry(manifestFile));
				System.out.println("\tAdding "+manifestName+" to jar : "+(addEntry(f, jarDest)?"   OK !":"   FAILED !"));
				jarDest.closeEntry();
			}
			catch (Exception e) {
				e.printStackTrace();
				return new Result(false,"An error occured while adding "+manifestName+" jar file !");
			}
			jarDest.close();
			System.out.println("JAR completed successfully !");
			//move tmp.jar file
			System.out.print("Moving temp files...");
			new File(schedulerLib).delete();
			new File("tmp.jar").renameTo(new File(schedulerLib));
			System.out.println("   OK !");
			//remove temporary files
			System.out.print("Removing temp files...");
			new File(manifestFile).delete();
			new File(manifestPath).delete();
			System.out.println("   OK !");
			//patched !!
			System.out.println("Application has been successfully patched !");
			return new Result(true,"Application has been successfully patched !");
		} catch (IOException e){
			e.printStackTrace();
			return new Result(false,"An error occured while reading files on hard drive !");
		}
	}

	//manifest content.
	private static String MANIFEST_ADDITIONAL_CONTENT =
		 ",org.mozilla.classfile,"+
		 "org.mozilla.javascript,"+
		 "org.mozilla.javascript.continuations,"+
		 "org.mozilla.javascript.debug,"+
		 "org.mozilla.javascript.jdk11,"+
		 "org.mozilla.javascript.jdk13,"+
		 "org.mozilla.javascript.optimizer,"+
		 "org.mozilla.javascript.regexp,"+
		 "org.mozilla.javascript.resources,"+
		 "org.mozilla.javascript.serialize,"+
		 "org.mozilla.javascript.tools,"+
		 "org.mozilla.javascript.tools.debugger,"+
		 "org.mozilla.javascript.tools.debugger.downloaded,"+
		 "org.mozilla.javascript.tools.idswitch,"+
		 "org.mozilla.javascript.tools.jsc,"+
		 "org.mozilla.javascript.tools.resources,"+
		 "org.mozilla.javascript.tools.shell,"+
		 "org.mozilla.javascript.xml,"+
		 "org.mozilla.javascript.xmlimpl,"+
		 "javax.script,"+
		 "com.sun.script.javascript,"+
		 "com.sun.script.util";
}

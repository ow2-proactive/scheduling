package util;

public class AddCopyright {
  private static String copyright = 
    "/* "+"\n"+
    " * ################################################################"+"\n"+
    " * "+"\n"+
    " * ProActive: The Java(TM) library for Parallel, Distributed, "+"\n"+
    " *            Concurrent computing with Security and Mobility"+"\n"+
    " * "+"\n"+
    " * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis"+"\n"+
    " * Contact: proactive-support@inria.fr"+"\n"+
    " * "+"\n"+
    " * This library is free software; you can redistribute it and/or"+"\n"+
    " * modify it under the terms of the GNU Lesser General Public"+"\n"+
    " * License as published by the Free Software Foundation; either"+"\n"+
    " * version 2.1 of the License, or any later version."+"\n"+
    " *  "+"\n"+
    " * This library is distributed in the hope that it will be useful,"+"\n"+
    " * but WITHOUT ANY WARRANTY; without even the implied warranty of"+"\n"+
    " * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU"+"\n"+
    " * Lesser General Public License for more details."+"\n"+
    " * "+"\n"+
    " * You should have received a copy of the GNU Lesser General Public"+"\n"+
    " * License along with this library; if not, write to the Free Software"+"\n"+
    " * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307"+"\n"+
    " * USA"+"\n"+
    " *  "+"\n"+
    " *  Initial developer(s):               The ProActive Team"+"\n"+
    " *                        http://www.inria.fr/oasis/ProActive/contacts.html"+"\n"+
    " *  Contributor(s): "+"\n"+
    " * "+"\n"+
    " * ################################################################"+"\n"+
    " */ "+"\n";

  public static void main(String[] arg) throws java.io.IOException {
    java.io.File sourceDir = new java.io.File("D:\\cygwin\\home\\lmestre\\ProActive\\src\\org\\objectweb\\proactive");
    addCopyrightToDir(sourceDir);
  }
  
  private static void addCopyrightToFile(java.io.File file) throws java.io.IOException {
    String name = file.getName();
    System.out.println("Processing "+file);
    if (! name.endsWith(".java")) return;
    byte[] b = getBytesFromInputStream(new java.io.FileInputStream(file));
    String program = new String(b);
    int packageStart = program.indexOf("package");
    if (packageStart == -1) return;
    String uncopyrightedProgram = program.substring(packageStart);
    String copyrightedProgram = copyright+uncopyrightedProgram;
    b = copyrightedProgram.getBytes();
    file.delete();
    java.io.OutputStream out = new java.io.BufferedOutputStream(new java.io.FileOutputStream(file));
    out.write(b, 0, b.length);
    out.flush();
    out.close();
  }
  
  private static void addCopyrightToDir(java.io.File file) throws java.io.IOException {
    java.io.File[] listFiles = file.listFiles();
    if (listFiles == null) return;
    for (int i=0; i<listFiles.length; i++) {
      java.io.File fileItem = listFiles[i];
      if (fileItem.isDirectory()) {
        addCopyrightToDir(fileItem);
      } else {
        addCopyrightToFile(fileItem);
      }
    }
  }


  /**
   * Returns an array of bytes containing the bytecodes for
   * the class represented by the InputStream
   * @param in the inputstream of the class file
   * @return the bytecodes for the class
   * @exception java.io.IOException if the class cannot be read
   */
  private static byte[] getBytesFromInputStream(java.io.InputStream in)  throws java.io.IOException {
    java.io.DataInputStream din = new java.io.DataInputStream(in);
    byte[] bytecodes = new byte[in.available()];
    try {
      din.readFully(bytecodes);
    } finally {
      if (din != null) din.close();
    }
    return bytecodes;
  }
}
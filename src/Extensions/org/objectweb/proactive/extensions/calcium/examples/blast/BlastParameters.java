/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.extensions.calcium.examples.blast;

import java.io.File;
import java.util.ArrayList;


public class BlastParameters {
    private boolean isRootParameter;
    private File queryFile; //query file
    private File databaseFile; //database file

    //blastall params
    private String program; //-p blastn|blastp|blastx

    //FormatDB
    private boolean isNucleotide; //-p F Input is a nucleotide, not a protein.
    private String createIndex; //-o Parse SeqID and create indexes. If the source database is in FASTA format, the database identifiers in the FASTA definition line must follow the conventions of the FASTA Defline Format.
    private int divideDBInto; //Number of parts the database should be divided into
    private long maxDBSize; //maximum database size to accept
    private File workingDirectory;

    public BlastParameters(File queryFile, File databaseFile,
        boolean isNucleotide, long maxDBSize) {
        this.isRootParameter = false;

        this.queryFile = queryFile;
        this.databaseFile = databaseFile;
        this.isNucleotide = isNucleotide;

        this.program = "blastn";
        this.workingDirectory = queryFile.getParentFile();

        this.divideDBInto = 2;
        this.maxDBSize = maxDBSize;
    }

    public String getBlastParemeterString() {
        StringBuffer sb = new StringBuffer();

        sb.append("-p ").append(program).append(" ");
        sb.append("-d ").append(databaseFile.getAbsolutePath()).append(" ");
        ;
        sb.append("-i ").append(queryFile.getAbsoluteFile()).append(" ");
        ;
        sb.append("-o ").append(getOutPutFile().getAbsolutePath()).append(" ");
        ;

        return sb.toString();
    }

    public String getFormatQueryString() {
        return getFormatParemeterString(queryFile);
    }

    public String getFormatDBString() {
        return getFormatParemeterString(databaseFile);
    }

    private String getFormatParemeterString(File inputFile) {
        StringBuffer sb = new StringBuffer();

        sb.append("-i ").append(inputFile.getAbsoluteFile()).append(" ");
        if (isNucleotide) {
            sb.append("-p F ");
        }
        sb.append("-o T ");

        return sb.toString();
    }

    private ArrayList<String> getParameterArray(String cmd) {
        ArrayList<String> a = new ArrayList<String>();
        String[] args = cmd.split(" ");

        for (String arg : args) {
            a.add(arg);
        }

        return a;
    }

    /**
     * @return Returns the databaseFile.
     */
    public File getDatabaseFile() {
        return databaseFile;
    }

    /**
     * @return Returns the maxDBSize.
     */
    public long getMaxDBSize() {
        return maxDBSize;
    }

    /**
     * @return Returns the divideDBInto.
     */
    public int getDivideDBInto() {
        return divideDBInto;
    }

    /**
     * @return Returns the queryFile.
     */
    public File getQueryFile() {
        return queryFile;
    }

    /**
     * @return Returns the isNucleotide.
     */
    public boolean isNucleotide() {
        return isNucleotide;
    }

    /**
     * @return Returns the workingDirectory.
     */
    public File getWorkingDirectory() {
        return workingDirectory;
    }

    /**
     * @return Returns the outPutFile.
     */
    public File getOutPutFile() {
        return new File(workingDirectory +
            System.getProperty("file.separator") + "/" +
            databaseFile.getName() + "-" + queryFile.getName() + ".blast");
    }

    public void setRootParameter(boolean value) {
        this.isRootParameter = value;
    }

    /**
     * @return Returns the isRootParameter.
     */
    protected boolean isRootParameter() {
        return isRootParameter;
    }
}

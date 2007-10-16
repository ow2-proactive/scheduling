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
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.examples.mydiary;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;


/**
 *  This classe represents the implementation of the diary interface
 *
 */
public class DiaryImpl implements Diary, Serializable {
    protected String diaryName;
    protected ArrayList<String> entries;

    public DiaryImpl() {
    }
    ;
    public DiaryImpl(String name) {
        diaryName = name;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.examples.mydiary.Diary#addEntry(java.lang.String)
     */
    public void addEntry(String entry) {
        entries.add(entry);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.examples.mydiary.Diary#getNumberOfEntries()
     */
    public int getNumberOfEntries() {
        return entries.size();
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.examples.mydiary.Diary#getEntry(int)
     */
    public String getEntry(int entryNumber) {
        return entries.get(entryNumber);
    }

    public static Diary load(String fileName) {
        FileInputStream fis;
        try {
            fis = new FileInputStream(fileName);

            ObjectInputStream ois = new ObjectInputStream(fis);
            Diary diary = (Diary) ois.readObject();
            ois.close();
            return diary;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void save(String fileName) {
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(fileName);

            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(this);
            oos.flush();
            oos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

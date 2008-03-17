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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 */
package org.objectweb.proactive.ic2d.chronolog.data.store;

import java.io.IOException;

import org.objectweb.proactive.ic2d.chronolog.data.model.AbstractTypeModel;
import org.objectweb.proactive.ic2d.chronolog.data.model.NumberBasedTypeModel;
import org.rrd4j.ConsolFun;
import org.rrd4j.DsType;
import org.rrd4j.core.FetchRequest;
import org.rrd4j.core.RrdDb;
import org.rrd4j.core.RrdDef;
import org.rrd4j.core.Sample;


/**
 * An AbstractDataStore implementation based on Rrd4j.
 * <p>
 * This data store implementation store only numerical values.
 * 
 * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>.
 */
public final class Rrd4jDataStore extends AbstractDataStore {
    /** The default amount of steps for the first archive */
    public static final int DEFAULT_ARR1_STEPS = 1;
    /** The default amount of records for the first archive */
    public static final int DEFAULT_ARR1_RECORDS = 150;
    /** The default amount of steps for the second archive */
    public static final int DEFAULT_ARR2_STEPS = 5;
    /** The default amount of records for the second archive */
    public static final int DEFAULT_ARR2_RECORDS = 180;

    /** The rrd data base */
    protected RrdDb rrdDb;
    /** The current rrd sample */
    protected Sample sample;
    /** The length of the time interval in seconds */
    protected int maxIntervalLengthInSecs;
    /** The init time */
    protected long initTimeInSecs;

    /**
     * Builds a new instance of the data store.
     * 
     * @param dataStoreName
     *            The name of the data store.
     */
    public Rrd4jDataStore(final String dataStoreName) {
        super(dataStoreName);
    }

    /**
     * Can be called more than one time if and only if close has been called !
     */
    public boolean init() {
        // Frist check that the store is not empty
        boolean noNumberTypes = true;
        for (final AbstractTypeModel provider : super.modelElements) {
            if (provider instanceof NumberBasedTypeModel) {
                noNumberTypes = false;
                break;
            }
        }
        if (noNumberTypes)
            return false;
        this.initTimeInSecs = System.currentTimeMillis() / 1000;
        final RrdDef rrdDef = new RrdDef("./" + this.dataStoreName, initTimeInSecs, super.stepInSeconds);
        // Set DataSources
        for (final AbstractTypeModel provider : super.modelElements) {
            if (provider instanceof NumberBasedTypeModel) {
                rrdDef.addDatasource(provider.getDataProvider().getName(), DsType.GAUGE, 600, 0, Double.NaN);
            }
        }
        // rrdDef.setStartTime(this.initTimeInSecs);
        // Add two archives
        // First archive of the last (150 * step) seconds i.e if step=4s we'll
        // have 10 mins of completely detailed data
        rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, DEFAULT_ARR1_STEPS, DEFAULT_ARR1_RECORDS);
        // Second archive of the last (180 * (average on five consecutive
        // steps)) seconds i.e if steps=4s we'll have 5*4s=20s and
        // 20s*180=3600s=1h of data
        rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, DEFAULT_ARR2_STEPS, DEFAULT_ARR2_RECORDS);
        this.maxIntervalLengthInSecs = (super.stepInSeconds * DEFAULT_ARR2_STEPS) * DEFAULT_ARR2_RECORDS;

        try {
            // Create the data base and the sample
            this.rrdDb = new RrdDb(rrdDef);
            this.sample = rrdDb.createSample();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void provideValuesFromElements() {
        int i = 0;
        for (final AbstractTypeModel elementModel : super.modelElements) {
            if (elementModel instanceof NumberBasedTypeModel) {
                this.sample.setValue(i++, ((NumberBasedTypeModel) elementModel).getProvidedValue()
                        .doubleValue());
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.ic2d.chronolog.data.store.AbstractDataStore#putValueByIndex(int,
     *      double)
     */
    @Override
    public void putValueByIndex(int index, double value) {
        sample.setValue(index, value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.ic2d.chronolog.data.store.AbstractDataStore#setValueByName(java.lang.String,
     *      double)
     */
    @Override
    public void setValueByName(String dataProviderName, double value) {
        sample.setValue(dataProviderName, value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.ic2d.chronolog.data.store.AbstractDataStore#store()
     */
    @Override
    public void store() {
        try {
            this.sample.setTime(System.currentTimeMillis() / 1000);
            this.sample.update();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.ic2d.chronolog.data.store.AbstractDataStore#close()
     */
    @Override
    public void close() {
        try {
            // Cancel the runnable collector
            this.runnableDataCollector.cancel();
            // Close the database
            if (this.rrdDb != null) {
                this.rrdDb.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.ic2d.chronolog.data.store.AbstractDataStore#dump()
     */
    @Override
    public void dump() {
        try {
            FetchRequest fr = this.rrdDb.createFetchRequest(ConsolFun.AVERAGE,
                    this.rrdDb.getLastUpdateTime() - (this.rrdDb.getRrdDef().getStep() * 30), this.rrdDb
                            .getLastUpdateTime());
            System.out.println(" --> " + fr.fetchData().dump());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.ic2d.chronolog.data.store.AbstractDataStore#getLeftBoundTime()
     */
    @Override
    public long getLeftBoundTime() {
        try {
            final long currDiff = this.rrdDb.getLastUpdateTime() - this.initTimeInSecs;
            return this.rrdDb.getLastUpdateTime() -
                (currDiff < this.maxIntervalLengthInSecs ? currDiff + (this.rrdDb.getRrdDef().getStep() * 30)
                        : this.maxIntervalLengthInSecs);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.ic2d.chronolog.data.store.AbstractDataStore#getRightBoundTime()
     */
    @Override
    public long getRightBoundTime() {
        try {
            return this.rrdDb.getLastUpdateTime();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.ic2d.chronolog.data.store.AbstractDataStore#isClosed()
     */
    @Override
    public boolean isClosed() {
        try {
            return this.rrdDb.isClosed();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}

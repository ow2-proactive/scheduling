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
import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.ic2d.chronolog.data.model.AbstractTypeModel;
import org.objectweb.proactive.ic2d.chronolog.data.model.GroupedNumberBasedTypeModel;
import org.objectweb.proactive.ic2d.chronolog.data.model.NumberBasedTypeModel;
import org.objectweb.proactive.ic2d.chronolog.data.provider.IDataProvider;
import org.rrd4j.ConsolFun;
import org.rrd4j.DsType;
import org.rrd4j.core.FetchRequest;
import org.rrd4j.core.RrdDb;
import org.rrd4j.core.RrdDef;
import org.rrd4j.core.Sample;


/**
 * An IDataStore implementation based on Rrd4j.
 * <p>
 * This data store implementation store only numerical values.
 * 
 * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>.
 */
public final class Rrd4jDataStore implements IDataStore {

    /** The default amount of steps for the first archive */
    public static final int DEFAULT_ARR1_STEPS = 1;
    /** The default amount of records for the first archive */
    public static final int DEFAULT_ARR1_RECORDS = 150;
    /** The default amount of steps for the second archive */
    public static final int DEFAULT_ARR2_STEPS = 5;
    /** The default amount of records for the second archive */
    public static final int DEFAULT_ARR2_RECORDS = 180;

    /** The length of the time interval in seconds */
    protected int maxIntervalLengthInSecs;
    /** The init time */
    protected long initTimeInSecs;
    /**
     * The name of this data store
     */
    protected String dataStoreName;
    /** The rrd data base */
    protected RrdDb rrdDb;
    /** The current rrd sample */
    protected Sample sample;
    /**
     * Stored models
     */
    protected List<AbstractTypeModel<?>> storedModels;

    /**
     * Builds a new instance of the data store.
     * 
     * @param dataStoreName
     *            The name of the data store.
     */
    public Rrd4jDataStore(final String dataStoreName) {
        this.dataStoreName = dataStoreName;
    }

    /**
     * Can be called more than one time if and only if close has been called !
     * !! modelsToStore MUST NOT BE EMPTY OR NULL
     */
    public boolean init(final List<AbstractTypeModel<?>> storedModels, final int stepInSeconds) {
        this.storedModels = storedModels;
        // Collect a list of unique providers
        final List<IDataProvider> providers = new ArrayList<IDataProvider>();
        for (final AbstractTypeModel<?> model : storedModels) {
            if (model instanceof NumberBasedTypeModel) {
                final IDataProvider provider = model.getDataProvider();
                if (!providers.contains(provider)) {
                    providers.add(provider);
                }
            } else if (model instanceof GroupedNumberBasedTypeModel) {
                final GroupedNumberBasedTypeModel gn = (GroupedNumberBasedTypeModel) model;
                for (final IDataProvider provider : gn.getValuesProviders()) {
                    if (!providers.contains(provider)) {
                        providers.add(provider);
                    }
                }
            }
        }
        // Perform all rrd4j related initialization        
        this.initTimeInSecs = System.currentTimeMillis() / 1000;
        final RrdDef rrdDef = new RrdDef("./" + this.dataStoreName, initTimeInSecs, stepInSeconds);
        // Add corresponding data sources
        for (final IDataProvider provider : providers) {
            rrdDef.addDatasource(provider.getName(), DsType.GAUGE, 600, 0, Double.NaN);
        }
        // Add two archives
        // First archive of the last (150 * step) seconds i.e if step=4s we'll
        // have 10 mins of completely detailed data
        rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, DEFAULT_ARR1_STEPS, DEFAULT_ARR1_RECORDS);
        // Second archive of the last (180 * (average on five consecutive
        // steps)) seconds i.e if steps=4s we'll have 5*4s=20s and
        // 20s*180=3600s=1h of data
        rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, DEFAULT_ARR2_STEPS, DEFAULT_ARR2_RECORDS);
        this.maxIntervalLengthInSecs = (stepInSeconds * DEFAULT_ARR2_STEPS) * DEFAULT_ARR2_RECORDS;

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

    /**
     * Sets all values
     */
    public void setValues() {
        for (final AbstractTypeModel<?> model : this.storedModels) {
            if (model.getClass() == NumberBasedTypeModel.class) {
                NumberBasedTypeModel nModel = (NumberBasedTypeModel) model;
                final String name = model.getDataProvider().getName();
                this.setValueByName(name, nModel.getCachedProvidedValue().doubleValue());
            } else if (model.getClass() == GroupedNumberBasedTypeModel.class) {
                final GroupedNumberBasedTypeModel gnModel = (GroupedNumberBasedTypeModel) model;
                final String[] names = gnModel.getValuesProvidersNames();
                final Double[] values = gnModel.getCachedProvidedValue();
                int i = 0;
                for (final String name : names) {
                    this.setValueByName(name, values[i++]);
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.ic2d.chronolog.data.store.IDataStore#setValueByName(java.lang.String,
     *      double)
     */
    public void setValueByName(String dataProviderName, double value) {
        sample.setValue(dataProviderName, value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.ic2d.chronolog.data.store.IDataStore#store()
     */
    public void update() {
        try {
            this.setValues();
            this.sample.setTime(System.currentTimeMillis() / 1000);
            this.sample.update();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.ic2d.chronolog.data.store.IDataStore#close()
     */
    public void close() {
        try {
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
     * @see org.objectweb.proactive.ic2d.chronolog.data.store.IDataStore#dump()
     */
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
     * @see org.objectweb.proactive.ic2d.chronolog.data.store.IDataStore#getLeftBoundTime()
     */
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
     * @see org.objectweb.proactive.ic2d.chronolog.data.store.IDataStore#getRightBoundTime()
     */
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
     * @see org.objectweb.proactive.ic2d.chronolog.data.store.IDataStore#isClosed()
     */
    public boolean isClosed() {
        try {
            return this.rrdDb.isClosed();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public String getDataStoreName() {
        return this.dataStoreName;
    }

}

package org.objectweb.proactive.extensions.scilab;

import java.io.Serializable;

import javasci.SciData;

import ptolemy.data.Token;


public class AbstractData implements Serializable {

    /**
         *
         */
    private static final long serialVersionUID = 3613670513261702795L;
    private SciData sciData;
    private String matName;
    private Token matData;
    private enum data_type {SCILAB_DATA,
        MATLAB_DATA;
    }
    ;
    private data_type type;

    public AbstractData(SciData data) {
        this.sciData = data;
        this.type = data_type.SCILAB_DATA;
    }

    public AbstractData(String name, Token data) {
        this.matName = name;
        this.matData = data;
        this.type = data_type.MATLAB_DATA;
    }

    public String getName() {
        switch (type) {
        case SCILAB_DATA:
            return sciData.getName();
        case MATLAB_DATA:
            return matName;
        }

        return null;
    }

    public Object getData() {
        switch (type) {
        case SCILAB_DATA:
            return sciData;
        case MATLAB_DATA:
            return matData;
        }

        return null;
    }

    public String toString() {
        switch (type) {
        case SCILAB_DATA:
            return sciData.toString();
        case MATLAB_DATA:
            return matData.toString();
        }

        return null;
    }
}

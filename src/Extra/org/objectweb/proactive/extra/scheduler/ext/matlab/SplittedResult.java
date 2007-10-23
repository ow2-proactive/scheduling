package org.objectweb.proactive.extra.scheduler.ext.matlab;

import java.io.Serializable;

import ptolemy.data.ArrayToken;
import ptolemy.data.Token;


public class SplittedResult implements Serializable {

    /**
         *
         */
    private static final long serialVersionUID = -2621827641513499574L;
    private ArrayToken token;

    public SplittedResult(ArrayToken token) {
        this.token = token;
    }

    public Token getResult(int index) {
        return token.getElement(index);
    }
}

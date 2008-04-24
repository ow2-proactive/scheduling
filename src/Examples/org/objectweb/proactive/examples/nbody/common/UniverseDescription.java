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
package org.objectweb.proactive.examples.nbody.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.xml.sax.Attributes;


/**
 * $LastChangedDate: 2006-05-14 13:21:43 +0200 (Sun, 14 May 2006) $
 * $LastChangedRevision: 31 $
 *
 * Describes what is a universe.
 * This class doesn't have any methods (except accessors and modifiers)
 *
 * @author The ProActive Team
 *
 */
public class UniverseDescription implements Serializable {
    // Fields
    private Vector<PlanetDescription> planets;
    private double depth;
    private double g;
    private double height;
    private double width;
    protected static final Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);

    /**
     * Creates a new empty UniverseDescription
     */
    public UniverseDescription() {
        planets = new Vector<PlanetDescription>();
    }

    // Accessors

    /**
     * Accessor
     * @return depth
     */
    public double getDepth() {
        return depth;
    }

    /**
     * Accessor
     * @return gravitationnal constant
     */
    public double getG() {
        return g;
    }

    /**
     * Accessor
     * @return height
     */
    public double getHeight() {
        return height;
    }

    /**
     * Accessor
     * @return all planets
     */
    public PlanetDescription[] getPlanetsDescriptions() {
        PlanetDescription[] result = new PlanetDescription[planets.size()];
        Iterator<PlanetDescription> i = planets.iterator();
        int j = 0;
        while (i.hasNext()) {
            result[j++] = i.next();
        }
        return result;
    }

    /**
     * Accessor
     * @return width
     */
    public double getWidth() {
        return width;
    }

    /**
     * Accessor
     * @return <ul><li>true if initialized</li>
     * <li>false otherwise</li></ul>
     */
    public boolean isInitialised() {
        return planets != null && planets.size() > 0;
    }

    // Modifiers

    /**
     * Modifier
     * @param depth new depth value
     */
    public void setDepth(double depth) {
        this.depth = depth;
    }

    /**
     * Modifier
     * @param depth new depth value
     */
    public void setDepth(String depth) {
        try {
            setDepth(Double.parseDouble(depth));
        } catch (NumberFormatException e) {
            logger.warn(depth + "is not a correct depth value");
        }
    }

    /**
     * Modifier
     * @param g new gravitational constant value
     */
    public void setG(double g) {
        this.g = g;
    }

    /**
     * Modifier
     * @param g new gravitational constant value
     */
    public void setG(String g) {
        try {
            setG(Double.parseDouble(g));
        } catch (NumberFormatException e) {
            logger.warn(g + "is not a correct g value");
        }
    }

    /**
     * Modifier
     * @param height new height value
     */
    public void setHeight(double height) {
        this.height = height;
    }

    /**
     * Modifier
     * @param height new height value
     */
    public void setHeight(String height) {
        try {
            setHeight(Double.parseDouble(height));
        } catch (NumberFormatException e) {
            logger.warn(height + "is not a correct height value");
        }
    }

    /**
     * Modifier
     * @param width new width tvalue
     */
    public void setWidth(double width) {
        this.width = width;
    }

    /**
     * Modifier
     * @param width new width tvalue
     */
    public void setWidth(String width) {
        try {
            setWidth(Double.parseDouble(width));
        } catch (NumberFormatException e) {
            logger.warn(width + "is not a correct width value");
        }
    }

    /**
     * Modifier
     * @param atts parameters of new planet to add
     */
    public void addPlanet(Attributes atts) {
        addPlanet(new PlanetDescription(this, atts));
    }

    /**
     * Modifier
     * @param description description of new planet to add
     */
    public void addPlanet(PlanetDescription description) {
        if (planets == null) {
            planets = new Vector<PlanetDescription>();
        }
        planets.add(description);
    }

    /**
     * Initializes with XML attributes
     * @param xmlAttributes initial values
     */
    public void set(Attributes xmlAttributes) {
        planets = new Vector<PlanetDescription>();

        for (int i = 0; i < xmlAttributes.getLength(); i++) {
            String name = xmlAttributes.getQName(i).toLowerCase();
            String value = xmlAttributes.getValue(i);

            if (name.compareTo("depth") == 0) {
                setDepth(value);
            } else if (name.compareTo("g") == 0) {
                setG(value);
            } else if (name.compareTo("height") == 0) {
                setHeight(value);
            } else if (name.compareTo("width") == 0) {
                setWidth(value);
            }
        }
    }

    /**
     * Sets the number of planets to given parameters.
     * It may remove planets if there are too many, or add some (with
     * random value) if not enough.
     * @param numberOfPlanets new number of planets
     */
    public void setNumberOfPlanets(int numberOfPlanets) {
        if (planets == null) {
            planets = new Vector<PlanetDescription>();
        }
        int size = planets.size();

        if (size > numberOfPlanets) {
            logger.info("Too many planets (" + size + "), removing " + (size - numberOfPlanets));
            planets = new Vector<PlanetDescription>(new ArrayList<PlanetDescription>(planets).subList(0,
                    numberOfPlanets));
        } else if (size < numberOfPlanets) {
            logger.info("Not enough planets (" + size + "), adding " + (numberOfPlanets - size));
            for (int i = size; i < numberOfPlanets; i++)
                planets.add(new PlanetDescription(this));
        }
    }

    /**
     * Sets the number of planets to given parameters.
     * It may remove planets if there are too many, or add some (with
     * random value) if not enough.
     * @param numberOfPlanets new number of planets
     */
    public void setNumberOfPlanets(String numberOfPlanets) {
        try {
            setNumberOfPlanets(Integer.parseInt(numberOfPlanets));
        } catch (NumberFormatException e) {
            logger.warn(numberOfPlanets + "is not a correct number of planets value");
        }
    }
}

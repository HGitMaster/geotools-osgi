/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.data.gpx.temporal;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * This is extension of JTS Coordinate with a 4., temporal ordinate.
 * This new ordinate is treated much like the 3., which could be used
 * for elevation.
 * 
 * The 4. ordinate is type long, interpreted as milliseconds. Not using java.util.Date
 * because it's not immutable.
 * 
 * @author Peter Bolla
 *
 */
public class TemporalCoordinate extends Coordinate {

    /**
     * 
     */
    private static final long serialVersionUID = 4219067320121212806L;

    /**
     * The value of the 4. ordinate, -1 meaning unset.
     */
    public double time = -1;
    
    public TemporalCoordinate(double x, double y, double z, double time) {
        super(x, y, z);
        this.time = time;
    }
    
    public TemporalCoordinate() {
        super();
        time = -1;
    }

    public TemporalCoordinate(Coordinate c) {
        super(c);
        if(c instanceof TemporalCoordinate)
            time = ((TemporalCoordinate)c).time;
        else
            time = -1;
    }

    public TemporalCoordinate(double x, double y, double z) {
        super(x, y, z);
        time = -1;
    }

    public TemporalCoordinate(double x, double y) {
        super(x, y);
        time = -1;
    }

    /**
     *  Returns <code>true</code> if <code>other</code> has the same values for x,
     *  y , z and time.
     *
     *@param  other  a <code>TemporalCoordinate</code> with which to do the 4D comparison.
     *@return        <code>true</code> if <code>other</code> is a <code>TemporalCoordinate</code>
     *      with the same values for x, y, z and time.
     */
    public boolean equals4D(TemporalCoordinate other) {
        if(!equals3D(other))
            return false;
        
        if(other.time != this.time)
            return false;
        
        return true;
    }
    
    /**
     *  Returns a <code>String</code> of the form <I>(x,y,z,time)</I> .
     *
     *@return    a <code>String</code> of the form <I>(x,y,z,time)</I>
     */
    public String toString() {
      return "(" + x + ", " + y + ", " + z + ", " + time + ")";
    }

    public Object clone() {
        return (TemporalCoordinate) super.clone();
    }
}

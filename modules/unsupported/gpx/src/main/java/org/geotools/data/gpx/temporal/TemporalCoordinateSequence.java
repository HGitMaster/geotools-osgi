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
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.impl.PackedCoordinateSequence;

/**
 * This is a PackedCoordinateSequence implementation, which stores 4D
 * coordinates, 3 spatial and 1 temporal.
 * 
 * The base of the class is the JTS PackedCoordinateSequence.Double
 * 
 * @author Peter Bolla
 * 
 */
public class TemporalCoordinateSequence extends PackedCoordinateSequence.Double {

    /**
     * Builds a new packed coordinate sequence
     * 
     * @param coords
     * @param dimension
     */
    public TemporalCoordinateSequence(double[] coords) {
        super(coords, 4);
    }

    /**
     * Builds a new packed coordinate sequence out of a coordinate array
     * 
     * @param coordinates
     */
    public TemporalCoordinateSequence(Coordinate[] coordinates) {
        super(coordinates==null ? 0 : coordinates.length, 4);
        
        if (coordinates == null)
            coordinates = new TemporalCoordinate[0];

        for (int i = 0; i < coordinates.length; i++) {
            setOrdinate(i, 0, coordinates[i].x);
            setOrdinate(i, 1, coordinates[i].y);
            setOrdinate(i, 2, coordinates[i].z);
            if(coordinates[i] instanceof TemporalCoordinate)
                setOrdinate(i, 3, ((TemporalCoordinate)coordinates[i]).time);
            else
                setOrdinate(i, 3, java.lang.Double.NaN);
        }

    }

    /**
     * Builds a new empty packed coordinate sequence of a given size.
     * 
     * @param coordinates
     */
    public TemporalCoordinateSequence(int size) {
        super(size, 4);
    }

    /**
     * @see com.vividsolutions.jts.geom.CoordinateSequence#getCoordinate(int)
     */
    public Coordinate getCoordinateInternal(int i) {
        double x = getOrdinate(i, 0);
        double y = getOrdinate(i, 1);
        double z = getOrdinate(i, 2);
        double t = getOrdinate(i, 3);
        return new TemporalCoordinate(x, y, z, t);
    }

    public Envelope expandEnvelope(Envelope env) {
        for (int i = 0; i < size(); i ++ ) {
            env.expandToInclude(getOrdinate(i, 0), getOrdinate(i, 1));
        }
        return env;
    }
}

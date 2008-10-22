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
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFactory;

public class TemporalCoordinateSequenceFactory implements CoordinateSequenceFactory {

    public CoordinateSequence create(Coordinate[] coordinates) {
        return new TemporalCoordinateSequence(coordinates);
    }

    public CoordinateSequence create(CoordinateSequence coordSeq) {
        return new TemporalCoordinateSequence(coordSeq.toCoordinateArray());
    }

    public CoordinateSequence create(int size, int dimension) {
        return new TemporalCoordinateSequence(size);
    }

}

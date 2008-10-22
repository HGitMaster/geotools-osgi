/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2003-2008, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2003-2004, Julian J. Ray, All Rights Reserved
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
package org.geotools.data.tiger;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Hashtable;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateArrays;
import com.vividsolutions.jts.geom.CoordinateList;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;


/**
 * <p>
 * Title: GeoTools2 Development
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 *
 * @author Julian J. Ray
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/tiger/src/main/java/org/geotools/data/tiger/TigerGeometryAdapter.java $
 * @version 1.0
 */
public class TigerGeometryAdapter {
    // Used to construct JTS geometry objects

    /** DOCUMENT ME! */
    private final double SCALE_FACTOR = 1000000.0;

    /** DOCUMENT ME! */
    private GeometryFactory geometryFactory;

    /** DOCUMENT ME! */
    private BufferedReader rt2Reader;

    /** DOCUMENT ME! */
    private int currentRow; // We perform forward-only processing on the records

    /** DOCUMENT ME! */
    private Hashtable type2s;

    // Used to test for NULL geometries read from the RT2 file

    /** DOCUMENT ME! */
    private Coordinate nullCoord = new Coordinate(0.0, 0.0);

    /**
     * TigerGeometryAdapter
     *
     * @param rt2Reader BufferedReader
     */
    public TigerGeometryAdapter(BufferedReader rt2Reader) {
        // cache the reference to the reader.
        this.rt2Reader = rt2Reader;

        // Initialize the storage
        try {
            type2s = new Hashtable();
            currentRow = -1;
            loadType2Recs();
        } catch (IOException e) {
            // Swallowed !
        }

        // We cache a JTS geometry factory for expediency
        geometryFactory = new GeometryFactory();
    }

    /**
     * deSerialize
     *
     * @param featureId String
     * @param line String
     *
     * @return Geometry
     */
    public Geometry deSerialize(String featureId, String line) {
        CoordinateList list = new CoordinateList();

        // Extract the shape points from the type 1 record
        double x1 = Double.parseDouble(line.substring(190, 200));
        double y1 = Double.parseDouble(line.substring(200, 209));
        double xn = Double.parseDouble(line.substring(209, 219));
        double yn = Double.parseDouble(line.substring(219, 228));

        // Add the first element
        list.add(new Coordinate(x1 / SCALE_FACTOR, y1 / SCALE_FACTOR));

        // Look for Type2 records
        String buff = null;
        int i = 1;
        Type2Record rec = new Type2Record();
        rec.mTlid = Integer.parseInt(featureId);

        while (true) {
            rec.Ordinal = i;
            buff = (String) type2s.get(rec.getKey());

            if (buff != null) {
                getCoordinates(buff, list);
                i++;
            } else {
                break;
            }
        }

        // Add the last element
        list.add(new Coordinate(xn / SCALE_FACTOR, yn / SCALE_FACTOR));

        // return the geometry
        return geometryFactory.createLineString(list.toCoordinateArray());
    }

    /**
     * getCoordinates
     *
     * @param buff String
     * @param list CoordinateList
     */
    private void getCoordinates(String buff, CoordinateList list) {
        Coordinate c;
        int startPos = 18;
        int increment = 19;

        for (int i = 0; i < 10; i++) {
            // Extract the shape points from the type 2 record
            double x = Double.parseDouble(buff.substring(startPos, startPos + 10).trim());
            double y = Double.parseDouble(buff.substring(startPos + 10, startPos + 19).trim());
            c = new Coordinate(x / SCALE_FACTOR, y / SCALE_FACTOR);

            // If the coords are 0.0, 0.0 then we have completed processing this feature
            if (nullCoord.equals2D(c)) {
                break;
            } else {
                list.add(c);
            }

            startPos += increment;
        }
    }

    /**
     * Constructs and returns a JTS LineString geometry.
     *
     * @param elems double[]
     *
     * @return LineString
     */
    private LineString createLineString(double[] elems) {
        CoordinateList list = new CoordinateList();

        // Check to see if the elems list is long enough
        if ((elems.length != 0) && (elems.length < 4)) {
            return null;
        }

        // Watch for incorrectly encoded string from the server
        if ((elems.length % 2) != 0) {
            return null;
        }

        for (int i = 0; i < elems.length;) {
            list.add(new Coordinate(elems[i], elems[i + 1]));
            i += 2;
        }

        return geometryFactory.createLineString(CoordinateArrays.toCoordinateArray(list));
    }

    /**
     * loadType2Recs - read the type twos and sort them on non-deacresing tlid and ordinal
     *
     * @throws IOException
     */
    private void loadType2Recs() throws IOException {
        String buffer;

        while (true) {
            buffer = rt2Reader.readLine();

            if (buffer == null) {
                break;
            }

            Type2Record rec = new Type2Record();
            rec.mTlid = Integer.parseInt(buffer.substring(5, 15).trim());
            rec.Ordinal = Integer.parseInt(buffer.substring(15, 18).trim());

            // Add it to the cache
            type2s.put(rec.getKey(), buffer);
        }
    }

    /**
     * <p>
     * Title: GeoTools2 Development
     * </p>
     * 
     * <p>
     * Description:
     * </p>
     * 
     * <p>
     * Copyright: Copyright (c) 2003
     * </p>
     * 
     * <p>
     * Company:
     * </p>
     *
     * @author Julian J. Ray
     * @version 1.0
     */
    private class Type2Record {
        //~ Instance fields --------------------------------------------------------------------------------------------

        /** DOCUMENT ME! */
        int mTlid;

        /** DOCUMENT ME! */
        int Ordinal;

        //~ Methods ----------------------------------------------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public Integer getKey() {
            return new Integer((mTlid * 100) + Ordinal);
        }
    }
}

/*
 *    GeoLBS - OpenSource LocationReference Based Servces toolkit
 *    Copyright (C) 2003-2004, Julian J. Ray, All Rights Reserved
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
 *
 */

package org.geotools.data.geomedia.attributeio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Hashtable;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateArrays;
import com.vividsolutions.jts.geom.CoordinateList;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;


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
 * Converts geometry objects between JTS geometry types and GeoMedia GDO serlaized geometry blobs. The following tables
 * describe the mappings:
 * 
 * <P></p>
 * 
 * <P></p>
 *
 * @author Julian J. Ray
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/geomedia/src/main/java/org/geotools/data/geomedia/attributeio/GeoMediaGeometryAdapter.java $
 * @version 1.0
 *
 * @todo Add interior polygons to Polygon constructor
 * @todo Add support multi-polygon types
 * @todo Add support for GeoMedia Arcs
 */
public class GeoMediaGeometryAdapter {
    // These are the Windows-based GUIDs used to identify each GeoMedia geometry type. Not used but here for reference!

    /** DOCUMENT ME! */
    private static String[] mGdoGuidStrings = {
        "0FD2FFC0-8CBC-11CF-ABDE-08003601B769", // Point Geometry
        "0FD2FFC8-8CBC-11CF-ABDE-08003601B769", // Oriented Point Geometry
        "0FD2FFC9-8CBC-11CF-ABDE-08003601B769", // Text Point Geometry
        "0FD2FFC1-8CBC-11CF-ABDE-08003601B769", // Line Geometry
        "0FD2FFC2-8CBC-11CF-ABDE-08003601B769", // Polyline Geometry
        "0FD2FFC3-8CBC-11CF-ABDE-08003601B769", // Polygon Geometry
        "0FD2FFC7-8CBC-11CF-ABDE-08003601B769", // Rectangle Geometry
        "0FD2FFC5-8CBC-11CF-ABDE-08003601B769", // Boundary Geometry
        "0FD2FFC6-8CBC-11CF-ABDE-08003601B769", // Geometry Collection (Hetereogeneous)
        "0FD2FFCB-8CBC-11CF-ABDE-08003601B769", // Composite Polyline Geometry
        "0FD2FFCC-8CBC-11CF-ABDE-08003601B769", // Composite Polygon Geometry
        "0FD2FFCA-8CBC-11CF-ABDE-08003601B769" // Arc Geometry
    };

    // This is the format of non-significant component of the GDO GUID headers represented
    // as signed ints. These are written as unsigned bytes to the output stream

    /** DOCUMENT ME! */
    private static int[] mGdoGuidByteArray = { 210, 15, 188, 140, 207, 17, 171, 222, 8, 0, 54, 1, 183, 105 };

    // Used to construct JTS geometry objects

    /** DOCUMENT ME! */
    GeometryFactory mGeometryFactory;

    /** DOCUMENT ME! */
    private Hashtable mTypeMapping;

    /**
     * Creates a new GeoMediaGeometryAdapter object.
     */
    public GeoMediaGeometryAdapter() {
        // We cache a JTS geometry factory for expediency
        mGeometryFactory = new GeometryFactory();

        // Set up the geometry type mappings
        mTypeMapping = new Hashtable();
        mTypeMapping.put(new Integer(65472), Point.class); // One-to-One correspondence
        mTypeMapping.put(new Integer(65480), Point.class); // removes orientation component
        mTypeMapping.put(new Integer(65481), Point.class); // Will not show the actual text but will indicate anchor points
        mTypeMapping.put(new Integer(65473), LineString.class); // Converts simple lines to line strings
        mTypeMapping.put(new Integer(65474), LineString.class); // One-to-One correspondence
        mTypeMapping.put(new Integer(65475), Polygon.class); // One-to-One correspondence
        mTypeMapping.put(new Integer(65479), Polygon.class); // Interpret rectangles as simple polygons
        mTypeMapping.put(new Integer(65477), Polygon.class); // Interpret boundary polygons as simple polygons with no islands
        mTypeMapping.put(new Integer(65478), GeometryCollection.class); // Heterogeneous collection
        mTypeMapping.put(new Integer(65484), MultiLineString.class); // One-to-One correspondence
        mTypeMapping.put(new Integer(65483), MultiPolygon.class); // Todo: Fix this

        //mTypeMapping.put(new Integer(65482), null);                   // No equivalent for arcs: TODO look for a stroking algorithm to piecewise it
    }

    /**
     * Converts GeoMedia blobs to JTS geometry types. Performs endian-conversion on data contained in the blob.
     *
     * @param input GeoMedia geometry blob read from geomedia spatial database.
     *
     * @return JTS Geometry
     *
     * @throws IOException
     * @throws GeoMediaGeometryTypeNotKnownException
     * @throws GeoMediaUnsupportedGeometryTypeException
     */
    public Geometry deSerialize(byte[] input)
        throws IOException, GeoMediaGeometryTypeNotKnownException, GeoMediaUnsupportedGeometryTypeException {
        Geometry geom = null;

        if (input == null) {
            return geom;
        }

        // 40 bytes is the minimum size for a point geometry
        if (input.length < 40) {
            return geom;
        }

        ByteBuffer byteBuffer = ByteBuffer.allocate(input.length);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.put(input);
        byteBuffer.position(0);

        // Extract the 16 byte GUID on the front end
        // First 2 bytes (short) is the index into the type map as an unsigned short
        int uidNum = (int) (byteBuffer.getShort() & 0xffff);

        // Skip the next 14 bytes
        int[] vals = new int[14];

        for (int i = 0; i < 14; i++) {
            vals[i] = byteBuffer.get();
        }

        Class geomType = (Class) mTypeMapping.get(new Integer(uidNum));

        if (geomType == null) {
            throw new GeoMediaGeometryTypeNotKnownException();
        }

        // Delegate to the appropriate de-serializer. Throw exceptions if we come across a geometry we have not yet supported
        if (geomType == Point.class) {
            geom = createPointGeometry(byteBuffer);
        } else if (geomType == LineString.class) {
            geom = createLineStringGeometry(uidNum, byteBuffer);
        } else if (geomType == MultiLineString.class) {
            geom = createMultiLineStringGeometry(uidNum, byteBuffer);
        } else if (geomType == Polygon.class) {
            geom = createPolygonGeometry(uidNum, byteBuffer);
        } else if (geomType == GeometryCollection.class) {
            geom = createGeometryCollectionGeometry(byteBuffer);
        } else if (geomType == MultiPolygon.class) { // TODO: Support this type
            throw new GeoMediaUnsupportedGeometryTypeException();
        }

        return geom;
    }

    /**
     * DOCUMENT ME!
     *
     * @param input DOCUMENT ME!
     * @param binaryWriter DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    private void writeGUID(Geometry input, ByteBuffer binaryWriter)
        throws IOException {
        int guidFlag = 0;

        if (input instanceof Point) {
            guidFlag = 65472;
        } else if (input instanceof LineString) {
            guidFlag = 65474;
        } else if (input instanceof Polygon) {
            guidFlag = 65475;
        }

        // Notice that the short has to be unsigned
        binaryWriter.putShort((short) (guidFlag & 0xffff));

        for (int i = 0; i < mGdoGuidByteArray.length; i++) {
            binaryWriter.put((byte) mGdoGuidByteArray[i]);
        }
    }

    /**
     * Converts a JTS geometry to a GeoMedia geometry blob which can be stored in a geomedia spatial database.
     *
     * @param input JTS Geometry
     *
     * @return byte[] GeoMedia blob format
     *
     * @throws IOException
     * @throws GeoMediaUnsupportedGeometryTypeException
     *
     * @todo Figure out how to write the GDO_BOUNDS_XHI etc. bounding box for SQL Server data stores
     */
    public byte[] serialize(Geometry input) throws IOException, GeoMediaUnsupportedGeometryTypeException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(65535);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.position(0);

        if (input instanceof Point) {
            Point geom = (Point) input;
            Coordinate c = geom.getCoordinate();

            writeGUID(input, byteBuffer);
            byteBuffer.putDouble(c.x);
            byteBuffer.putDouble(c.y);
            byteBuffer.putDouble((double) 0.0);
        } else if (input instanceof LineString) {
            LineString geom = (LineString) input;
            Coordinate[] coords = geom.getCoordinates();

            writeGUID(input, byteBuffer);
            byteBuffer.putInt(coords.length);

            for (int i = 0; i < coords.length; i++) {
                byteBuffer.putDouble(coords[i].x);
                byteBuffer.putDouble(coords[i].y);
                byteBuffer.putDouble((double) 0.0);
            }
        } else if (input instanceof MultiLineString) {
            MultiLineString geom = (MultiLineString) input;
            int numGeoms = geom.getNumGeometries();

            writeGUID(input, byteBuffer);
            byteBuffer.putInt(numGeoms);

            for (int i = 0; i < numGeoms; i++) {
                // Use recursion to serialize all the sub-geometries
                byte[] b = serialize(geom.getGeometryN(i));
                byteBuffer.putInt(b.length);

                for (int j = 0; j < b.length; j++) {
                    byteBuffer.put(b[j]);
                }
            }
        } else if (input instanceof Polygon) {
            Polygon geom = (Polygon) input;
            Coordinate[] coords = geom.getExteriorRing().getCoordinates();

            writeGUID(input, byteBuffer);
            byteBuffer.putInt(coords.length);

            for (int i = 0; i < coords.length; i++) {
                byteBuffer.putDouble(coords[i].x);
                byteBuffer.putDouble(coords[i].y);
                byteBuffer.putDouble((double) 0.0);
            }
        } else {
            // Choke if we can't handle the geometry type yet...
            throw new GeoMediaUnsupportedGeometryTypeException();
        }

        return byteBuffer.array();
    }

    /**
     * Constructs and returns a JTS Point geometry
     *
     * @param x double
     * @param y double
     *
     * @return Point
     */
    private Point createPointGeometry(double x, double y) {
        return mGeometryFactory.createPoint(new Coordinate(x, y));
    }

    /**
     * Constructs and returns a JTS Point geometry by reading from the byte stream.
     *
     * @param reader LEDataInputStream reading from byte array containing a GeoMedia blob.
     *
     * @return Point
     *
     * @throws IOException
     */
    private Point createPointGeometry(ByteBuffer reader)
        throws IOException {
        return createPointGeometry(reader.getDouble(), reader.getDouble());
    }

    /**
     * Constructs and returns a JTS LineString geometry.
     *
     * @param elems double[]
     *
     * @return LineString
     */
    private LineString createLineStringGeometry(double[] elems) {
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

        return mGeometryFactory.createLineString(CoordinateArrays.toCoordinateArray(list));
    }

    /**
     * Constructs and returns a JTS LineString geometry from a GDO blob.
     *
     * @param guid int GeoMedia GUID flag.
     * @param reader LEDataInputStream
     *
     * @return LineString
     *
     * @throws IOException
     */
    private LineString createLineStringGeometry(int guid, ByteBuffer reader)
        throws IOException {
        double[] a = null;

        if (guid == 65473) { // GDO Line Geometry
            a = new double[4];
            a[0] = reader.getDouble(); // x1
            a[1] = reader.getDouble(); // y1
            reader.getDouble(); // z1;
            a[2] = reader.getDouble(); // x2
            a[3] = reader.getDouble(); // y2
        } else { // GDO Polyline Geometry

            int numOrdinates = reader.getInt();
            a = new double[numOrdinates * 2];

            for (int i = 0; i < numOrdinates; i++) {
                a[2 * i] = reader.getDouble(); // xn
                a[(2 * i) + 1] = reader.getDouble(); // yn
                reader.getDouble(); // zn
            }
        }

        return createLineStringGeometry(a);
    }

    /**
     * DOCUMENT ME!
     *
     * @param lineStrings DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private MultiLineString createMultiLineStringGeometry(ArrayList lineStrings) {
        LineString[] array = new LineString[lineStrings.size()];

        for (int i = 0; i < lineStrings.size(); i++) {
            array[i] = (LineString) lineStrings.get(i);
        }

        return mGeometryFactory.createMultiLineString(array);
    }

    /**
     * DOCUMENT ME!
     *
     * @param guid DOCUMENT ME!
     * @param reader DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws GeoMediaUnsupportedGeometryTypeException DOCUMENT ME!
     * @throws GeoMediaGeometryTypeNotKnownException DOCUMENT ME!
     */
    private MultiLineString createMultiLineStringGeometry(int guid, ByteBuffer reader)
        throws IOException, GeoMediaUnsupportedGeometryTypeException, GeoMediaGeometryTypeNotKnownException {
        // get the number of items in the collection
        int numItems = reader.getInt();

        // This is to hold the geometries from the collection
        ArrayList array = new ArrayList();

        for (int i = 0; i < numItems; i++) {
            // Read the size of the next blob
            int elemSize = reader.getInt();

            // Recursively create a geometry from this blob
            byte[] elem = new byte[elemSize];

            for (int j = 0; j < elemSize; j++) {
                elem[j] = reader.get();
            }

            Geometry g = deSerialize(elem);
            array.add(g);
        }

        // Now we need to append these items together
        return createMultiLineStringGeometry(array);
    }

    /**
     * DOCUMENT ME!
     *
     * @param elems DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private Polygon createPolygonGeometry(double[] elems) {
        CoordinateList list = new CoordinateList();

        // Check to see if the elems list is long enough
        if ((elems.length != 0) && (elems.length <= 6)) {
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

        LinearRing ring = mGeometryFactory.createLinearRing(CoordinateArrays.toCoordinateArray(list));

        return mGeometryFactory.createPolygon(ring, null);
    }

    /**
     * DOCUMENT ME!
     *
     * @param guid DOCUMENT ME!
     * @param reader DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    private Polygon createPolygonGeometry(int guid, ByteBuffer reader)
        throws IOException {
        double[] a = null;

        if (guid == 65475) { // Polygon Geometry

            int numOrdinates = reader.getInt();
            a = new double[numOrdinates * 2];

            for (int i = 0; i < numOrdinates; i++) {
                a[2 * i] = reader.getDouble();
                a[(2 * i) + 1] = reader.getDouble();
                reader.getDouble();
            }
        } else if (guid == 65479) { // Rectangle geomety

            // x, y, z, width, height
            double x = reader.getDouble();
            double y = reader.getDouble();
            double z = reader.getDouble();
            double w = reader.getDouble();
            double h = reader.getDouble();

            a = new double[8];
            a[0] = x;
            a[1] = y;
            a[2] = x + w;
            a[3] = y;
            a[4] = x + w;
            a[5] = y + h;
            a[6] = x;
            a[7] = y + h;
        }

        return createPolygonGeometry(a);
    }

    /**
     * DOCUMENT ME!
     *
     * @param reader DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws GeoMediaUnsupportedGeometryTypeException DOCUMENT ME!
     * @throws GeoMediaGeometryTypeNotKnownException DOCUMENT ME!
     */
    private GeometryCollection createGeometryCollectionGeometry(ByteBuffer reader)
        throws IOException, GeoMediaUnsupportedGeometryTypeException, GeoMediaGeometryTypeNotKnownException {
        // get the number of items in the collection
        int numItems = reader.getInt();

        // This is to hold the geometries from the collection
        ArrayList array = new ArrayList();

        for (int i = 0; i < numItems; i++) {
            // Read the size of the next blob
            int elemSize = reader.getInt();

            // Recursively create a geometry from this blob
            byte[] elem = new byte[elemSize];

            for (int j = 0; j < elemSize; j++) {
                elem[j] = reader.get();
            }

            Geometry g = deSerialize(elem);

            if (g != null) {
                array.add(g);
            }
        }

        // Now we need to append these items together
        return createGeometryCollectionGeometry(array);
    }

    /**
     * DOCUMENT ME!
     *
     * @param geoms DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private GeometryCollection createGeometryCollectionGeometry(ArrayList geoms) {
        Geometry[] array = new Geometry[geoms.size()];

        for (int i = 0; i < geoms.size(); i++) {
            array[i] = (Geometry) geoms.get(i);
        }

        return mGeometryFactory.createGeometryCollection(array);
    }
}

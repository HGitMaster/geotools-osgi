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
package org.geotools.data.shapefile.shp;

import java.nio.ByteBuffer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPoint;

/**
 * 
 * @author aaime
 * @author Ian Schneider
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/plugin/shapefile/src/main/java/org/geotools/data/shapefile/shp/MultiPointHandler.java $
 * 
 */
public class MultiPointHandler implements ShapeHandler {
    final ShapeType shapeType;
    GeometryFactory geometryFactory = new GeometryFactory();

    /** Creates new MultiPointHandler */
    public MultiPointHandler() {
        shapeType = ShapeType.POINT;
    }

    public MultiPointHandler(ShapeType type) throws ShapefileException {
        if ((type != ShapeType.MULTIPOINT) && (type != ShapeType.MULTIPOINTM)
                && (type != ShapeType.MULTIPOINTZ)) {
            throw new ShapefileException(
                    "Multipointhandler constructor - expected type to be 8, 18, or 28");
        }

        shapeType = type;
    }

    /**
     * Returns the shapefile shape type value for a point
     * 
     * @return int Shapefile.POINT
     */
    public ShapeType getShapeType() {
        return shapeType;
    }

    /**
     * Calcuates the record length of this object.
     * 
     * @return int The length of the record that this shapepoint will take up in
     *         a shapefile
     */
    public int getLength(Object geometry) {
        MultiPoint mp = (MultiPoint) geometry;

        int length;

        if (shapeType == ShapeType.MULTIPOINT) {
            // two doubles per coord (16 * numgeoms) + 40 for header
            length = (mp.getNumGeometries() * 16) + 40;
        } else if (shapeType == ShapeType.MULTIPOINTM) {
            // add the additional MMin, MMax for 16, then 8 per measure
            length = (mp.getNumGeometries() * 16) + 40 + 16
                    + (8 * mp.getNumGeometries());
        } else if (shapeType == ShapeType.MULTIPOINTZ) {
            // add the additional ZMin,ZMax, plus 8 per Z
            length = (mp.getNumGeometries() * 16) + 40 + 16
                    + (8 * mp.getNumGeometries()) + 16
                    + (8 * mp.getNumGeometries());
        } else {
            throw new IllegalStateException("Expected ShapeType of Arc, got "
                    + shapeType);
        }

        return length;
    }

    private Object createNull() {
        Coordinate[] c = null;
        return geometryFactory.createMultiPoint(c);
    }

    public Object read(ByteBuffer buffer, ShapeType type) {
        if (type == ShapeType.NULL) {
            return createNull();
        }

        // read bounding box (not needed)
        buffer.position(buffer.position() + 4 * 8);

        int numpoints = buffer.getInt();
        Coordinate[] coords = new Coordinate[numpoints];

        for (int t = 0; t < numpoints; t++) {
            double x = buffer.getDouble();
            double y = buffer.getDouble();
            coords[t] = new Coordinate(x, y);
        }

        if (shapeType == ShapeType.MULTIPOINTZ) {
            buffer.position(buffer.position() + 2 * 8);

            for (int t = 0; t < numpoints; t++) {
                coords[t].z = buffer.getDouble(); // z
            }
        }

        return geometryFactory.createMultiPoint(coords);
    }

    public void write(ByteBuffer buffer, Object geometry) {
        MultiPoint mp = (MultiPoint) geometry;

        Envelope box = mp.getEnvelopeInternal();
        buffer.putDouble(box.getMinX());
        buffer.putDouble(box.getMinY());
        buffer.putDouble(box.getMaxX());
        buffer.putDouble(box.getMaxY());

        buffer.putInt(mp.getNumGeometries());

        for (int t = 0, tt = mp.getNumGeometries(); t < tt; t++) {
            Coordinate c = (mp.getGeometryN(t)).getCoordinate();
            buffer.putDouble(c.x);
            buffer.putDouble(c.y);
        }

        if (shapeType == ShapeType.MULTIPOINTZ) {
            double[] zExtreame = JTSUtilities.zMinMax(mp.getCoordinates());

            if (Double.isNaN(zExtreame[0])) {
                buffer.putDouble(0.0);
                buffer.putDouble(0.0);
            } else {
                buffer.putDouble(zExtreame[0]);
                buffer.putDouble(zExtreame[1]);
            }

            for (int t = 0; t < mp.getNumGeometries(); t++) {
                Coordinate c = (mp.getGeometryN(t)).getCoordinate();
                double z = c.z;

                if (Double.isNaN(z)) {
                    buffer.putDouble(0.0);
                } else {
                    buffer.putDouble(z);
                }
            }
        }

        if (shapeType == ShapeType.MULTIPOINTM
                || shapeType == ShapeType.MULTIPOINTZ) {
            buffer.putDouble(-10E40);
            buffer.putDouble(-10E40);

            for (int t = 0; t < mp.getNumGeometries(); t++) {
                buffer.putDouble(-10E40);
            }
        }
    }

}

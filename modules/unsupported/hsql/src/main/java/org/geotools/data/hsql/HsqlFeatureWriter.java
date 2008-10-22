/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.data.hsql;

import java.io.IOException;

import org.geotools.data.FeatureReader;
import org.geotools.data.jdbc.JDBCTextFeatureWriter;
import org.geotools.data.jdbc.QueryData;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTWriter;


/**
 * Feature writer handling specific geometric function from HSQL
 *
 * This essentially adds the DB in a box wrappers around various HSQL 
 * queries make them work.
 *
 * @author Amr Alam, Refractions Research
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/hsql/src/main/java/org/geotools/data/hsql/HsqlFeatureWriter.java $
 */
public class HsqlFeatureWriter extends JDBCTextFeatureWriter {
    private static WKTWriter geometryWriter = new WKTWriter();

    /**
     * Creates a new instance of HsqlFeatureWriter
     * 
     * @param fReader
     * @param queryData
     * @throws IOException
     */
    public HsqlFeatureWriter(FeatureReader fReader, QueryData queryData)
        throws IOException {
        super(fReader, queryData);
    }
    
    /**
     * @see org.geotools.data.jdbc.JDBCTextFeatureWriter#getGeometryInsertText(com.vividsolutions.jts.geom.Geometry,
     *      int)
     */
    protected String getGeometryInsertText(Geometry geom, int srid) {
        if (geom == null) {
            return "NULL";
        }

        String geoText = geometryWriter.write(geom);
        String sql = null;

        //HSQL doesn't support spatial types, and we're using 'DB in a box'
        //which only has the 'geometry' type
        sql = "GeomFromWKT";
//        if (GeometryCollection.class.isAssignableFrom(geom.getClass())) {
//            if (MultiPoint.class.isAssignableFrom(geom.getClass())) {
//                sql = "MultiPointFromText";
//            } else if (MultiLineString.class.isAssignableFrom(geom.getClass())) {
//                sql = "MultiLineStringFromText";
//            } else if (MultiPolygon.class.isAssignableFrom(geom.getClass())) {
//                sql = "MultiPolygonFromText";
//            } else {
//                sql = "GeometryCollectionFromText";
//            }
//        } else {
//            if (Point.class.isAssignableFrom(geom.getClass())) {
//                sql = "PointFromText";
//            } else if (LineString.class.isAssignableFrom(geom.getClass())) {
//                sql = "LineStringFromText";
//            } else if (Polygon.class.isAssignableFrom(geom.getClass())) {
//                sql = "PolygonFromText";
//            } else {
//                sql = "GeometryFromText";
//            }
//        }

        sql += ("('" + geoText + "')");

        return sql;
    }
}

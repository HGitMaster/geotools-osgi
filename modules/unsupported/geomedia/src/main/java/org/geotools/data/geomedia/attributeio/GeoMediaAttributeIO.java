/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2003-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.data.geomedia.attributeio;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.DataSourceException;
import org.geotools.data.jdbc.attributeio.AttributeIO;

import com.vividsolutions.jts.geom.Geometry;


/**
 * AttributeIO implementation that read and writes geomedia columns
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/geomedia/src/main/java/org/geotools/data/geomedia/attributeio/GeoMediaAttributeIO.java $
 */
public class GeoMediaAttributeIO implements AttributeIO {
    private static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger(
            "org.geotools.data.geomedia");

    // geometry adpater
    private GeoMediaGeometryAdapter geometryAdapter = null;

    /**
     * Creates a new instance of the GeomediaAttributeIO for a specific QueryData instance
     *
     * @throws DataSourceException
     */
    public GeoMediaAttributeIO() throws DataSourceException {

        geometryAdapter = new GeoMediaGeometryAdapter();
    }

    /**
     * @see org.geotools.data.jdbc.attributeio.AttributeIO#read(java.sql.ResultSet,
     *      int)
     */
    public Object read(ResultSet rs, int position) throws IOException {
        try {
            return geometryAdapter.deSerialize((byte[]) rs.getObject(position));
        } catch (SQLException e) {
            String msg = "SQL Exception reading geometry column";
            LOGGER.log(Level.SEVERE, msg, e);
            throw new DataSourceException(msg, e);
        } catch (GeoMediaGeometryTypeNotKnownException e) {
            String msg = "Geometry Conversion type error";
            LOGGER.log(Level.SEVERE, msg, e);
            throw new DataSourceException(msg, e);
        } catch (GeoMediaUnsupportedGeometryTypeException e) {
            String msg = "Geometry Conversion type error";
            LOGGER.log(Level.SEVERE, msg, e);
            throw new DataSourceException(msg, e);
        }
    }

    /**
     * @see org.geotools.data.jdbc.attributeio.AttributeIO#write(java.sql.ResultSet,
     *      int, java.lang.Object)
     */
    public void write(ResultSet rs, int position, Object value)
        throws IOException {
        try {
            if(value == null) {
                rs.updateNull(position);
            } else {
                byte[]  blob = geometryAdapter.serialize((Geometry) value);
                rs.updateObject(position, (Object) blob);
            }
        } catch (SQLException sqlException) {
            String msg = "SQL Exception writing geometry column";
            LOGGER.log(Level.SEVERE, msg, sqlException);
            throw new DataSourceException(msg, sqlException);
        } catch (GeoMediaUnsupportedGeometryTypeException e) {
            String msg = "Geometry Conversion type error";
            LOGGER.log(Level.SEVERE, msg, e);
            throw new DataSourceException(msg, e);
        }
    }

    /**
     * @see org.geotools.data.jdbc.attributeio.AttributeIO#write(java.sql.PreparedStatement, int, java.lang.Object)
     */
    public void write(PreparedStatement ps, int position, Object value) throws IOException {
        try {
            if(value == null) {
                ps.setNull(position, Types.OTHER);
            } else {
                byte[]  blob = geometryAdapter.serialize((Geometry) value);
                ps.setObject(position, (Object) blob);
            }
        } catch (SQLException sqlException) {
            String msg = "SQL Exception writing geometry column";
            LOGGER.log(Level.SEVERE, msg, sqlException);
            throw new DataSourceException(msg, sqlException);
        } catch (GeoMediaUnsupportedGeometryTypeException e) {
            String msg = "Geometry Conversion type error";
            LOGGER.log(Level.SEVERE, msg, e);
            throw new DataSourceException(msg, e);
        }
        
    }
}
